package reciter;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.s3.AmazonS3;

import reciter.consumer.service.CognitoAuthService;
import reciter.database.dynamodb.DynamoDbConfig;
import reciter.security.CognitoClientRegistry;
import reciter.service.GenderService;
import reciter.service.NameFrequencyService;
import reciter.service.ScienceMetrixDepartmentCategoryService;
import reciter.service.ScienceMetrixService;
import reciter.service.dynamo.DynamoDbInstitutionAfidService;
import reciter.service.dynamo.DynamoDbMeshTermService;

/**
 * Stage 0 context-load smoke test (#634 dependency modernization).
 *
 * <p>Boots the full Spring application context under the dedicated {@code test}
 * profile (see {@code application-test.properties}), which neutralizes the
 * property-controlled live-AWS startup blockers. The {@code @MockBean} overrides
 * below stand in for the beans that build live AWS SDK clients or run data loads
 * at startup:
 * <ul>
 *   <li>{@code AmazonDynamoDB} / {@code AmazonS3} — would otherwise hit DynamoDB/S3/STS.</li>
 *   <li>{@code CognitoAuthService} / {@code CognitoClientRegistry} — build live Cognito clients.</li>
 *   <li>The six services invoked by {@code Application}'s {@code ApplicationReadyEvent}
 *       listeners — mocked so the startup data-load is a no-op (Mockito returns empty
 *       lists for list-returning methods by default).</li>
 * </ul>
 *
 * <p>This is the per-stage gate for #634: every framework/wiring change (Stages 1-3)
 * must keep this green, proving the context still wires end to end.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
public class ApplicationContextSmokeTest {

    @MockBean
    private AmazonDynamoDB amazonDynamoDB;

    @MockBean
    private AmazonS3 amazonS3;

    @MockBean
    private CognitoAuthService cognitoAuthService;

    @MockBean
    private CognitoClientRegistry cognitoClientRegistry;

    // Services invoked by Application's ApplicationReadyEvent listeners. Mocking them
    // makes populateStaticEngineParameters() a no-op at startup without needing AWS.
    @MockBean
    private ScienceMetrixService scienceMetrixService;

    @MockBean
    private ScienceMetrixDepartmentCategoryService scienceMetrixDepartmentCategoryService;

    @MockBean
    private DynamoDbMeshTermService dynamoDbMeshTermService;

    @MockBean
    private GenderService genderService;

    @MockBean
    private NameFrequencyService nameFrequencyService;

    @MockBean
    private DynamoDbInstitutionAfidService dynamoDbInstitutionAfidService;

    @Autowired
    private ApplicationContext context;

    @Test
    public void contextLoads() {
        assertNotNull("Spring application context should load", context);
        // A concrete bean from the DynamoDB wiring path proves the persistence config
        // wired (the surface Stage 1 will rework when spring-data-dynamodb is removed).
        assertNotNull("DynamoDbConfig should be wired", context.getBean(DynamoDbConfig.class));
    }
}
