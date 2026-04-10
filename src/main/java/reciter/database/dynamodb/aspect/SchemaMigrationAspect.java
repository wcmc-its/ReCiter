package reciter.database.dynamodb.aspect;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import reciter.database.dynamodb.model.VersionedItem;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Aspect
@Component
public class SchemaMigrationAspect {

	private static final Logger log = LoggerFactory.getLogger(SchemaMigrationAspect.class);
	private final MigrationRegistry registry;
	@Autowired
    private DynamoDBMapper dynamoDBMapper;

	public SchemaMigrationAspect(MigrationRegistry registry) {
        this.registry = registry;
    }

    @Around("@annotation(migrateSchema)")
    public Object checkAndMigrate(ProceedingJoinPoint joinPoint, MigrateSchema migrateSchema) throws Throwable {
    	// 1. Execute the service method
        Object result = joinPoint.proceed();
        if (result == null) return null;

        int target = registry.getTargetVersion(migrateSchema.tableName());

        if (result instanceof VersionedItem) {
            handleMigration((VersionedItem) result, target);
        } else if (result instanceof Collection) {
            for (Object obj : (Collection<?>) result) {
                if (obj instanceof VersionedItem) {
                    handleMigration((VersionedItem) obj, target);
                }
            }
        }
        
        return result;
    }
    @Around("execution(* org.springframework.data.repository.CrudRepository.save(..))")
    public Object ensureDefaultsOnSave(ProceedingJoinPoint joinPoint) throws Throwable {
        Object item = joinPoint.getArgs()[0];
        
        if (item instanceof VersionedItem) {
            
        	String tableName = getTableNameFromEntity(item);
        	int target = registry.getTargetVersion(tableName); // Get from properties
            VersionedItem vItem = (VersionedItem) item;
            // Re-run populateDefaults right before the Repo sends it to DynamoDB
            // This ensures any "" set by the Controller are turned back into " "
            // Force the version and defaults one last time
            if (vItem.getSchemaVersion() == 0 || vItem.getSchemaVersion() < target) {
                vItem.setSchemaVersion(target);
                populateDefaults(vItem); // Ensure " " are set
            }
        }
        
        return joinPoint.proceed(new Object[]{item});
    }
    private void handleMigration(VersionedItem item, int target) {
        int current = item.getSchemaVersion();
        
        // Target is from your application.properties
        if (current == 0 || current < target ) {
            item.setSchemaVersion(target);
            
            // 1. Recursively fill all fields incld 
            populateDefaults(item);

            // 2. Save using the safest behavior for V1
            DynamoDBMapperConfig config = DynamoDBMapperConfig.builder()
                    .withSaveBehavior(SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
                    .build();

            dynamoDBMapper.save(item, config);
            log.info("DEBUG: Migrated " + item.getClass().getSimpleName() + " to version " + target);
        }
    }

    private void populateDefaults(Object obj) {
        if (obj == null) return;
        
        for (Field field : obj.getClass().getDeclaredFields()) {
        	
        	String fieldName = field.getName();
            try {
                field.setAccessible(true);
                if (field.get(obj) == null) {
                    Class<?> type = field.getType();
                    
                    if (type.equals(String.class)) {
                        field.set(obj, " ");
                    } else if (type.equals(Integer.class) || type.equals(int.class)) {
                        field.set(obj, 0);
                    } else if (type.equals(Long.class) || type.equals(long.class)) {
                        field.set(obj, 0L);
                    } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
                        field.set(obj, false);
                    } else if (type.equals(Date.class)) {
                        field.set(obj, new Date());
                    } else if (type.equals(java.util.List.class)) {
                        field.set(obj, new ArrayList<>());
                    } else if (type.equals(java.util.Map.class)) {
                        field.set(obj, new HashMap<>());
                    } 
                    // Handle @DynamoDBDocument / Nested Objects
                    else if (!type.isPrimitive() && !type.getName().startsWith("java.")) {
                    	try
                    	{
	                        Object nested = type.getDeclaredConstructor().newInstance();
	                        field.set(obj, nested);
	                        populateDefaults(nested); // RECURSION
                    	}catch (NoSuchMethodException e) {
                            log.error("MIGRATION ERROR: Field '{}' in class '{}' missing default constructor.", 
                                    fieldName, obj.getClass().getSimpleName());
                      }
                    }
                    else {
                        // This catches types we haven't defined (e.g., custom Enums or Arrays)
                        log.warn("MIGRATION WARNING: No default rule for field '{}' (Type: {}) in class '{}'. Field remains null.", 
                                 fieldName, type.getSimpleName(), obj.getClass().getSimpleName());
                    }
                  }
            } catch (Exception e) {
            	log.error("MIGRATION CRITICAL: Unexpected error accessing field '{}' in class '{}'.", 
                        fieldName, obj.getClass().getSimpleName(), e);
            }
        }
    }
    private String getTableNameFromEntity(Object item) {
        DynamoDBTable annotation = item.getClass().getAnnotation(DynamoDBTable.class);
        if (annotation != null) {
            return annotation.tableName();
        }
        // Fallback if the annotation is missing (unlikely for a DynamoDB entity)
        return item.getClass().getSimpleName().toLowerCase();
    }
}
