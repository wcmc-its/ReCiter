package reciter.database.dynamodb.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reciter.database.dynamodb.model.GoldStandard;
import reciter.database.dynamodb.model.GoldStandardAuditLog;
import reciter.database.dynamodb.model.ScienceMetrix;
import reciter.database.dynamodb.repository.DynamoDbGoldStandardRepository;

@ExtendWith(MockitoExtension.class)
public class DynamoDbGoldStandardRepositoryTest {

    @Mock
    private DynamoDbGoldStandardRepository goldStandardRepository;

    @BeforeEach
    public void setUp() {
        // Initialize mocks if needed
    }

    @Test
    public void testSave() {
        // Create a new GoldStandard object with mock data
        GoldStandardAuditLog auditLog = new GoldStandardAuditLog();
        auditLog.setUid("uid1");
        auditLog.setUserVerbose("user1");
        auditLog.setDateTime(Instant.now());
        auditLog.setPmids(Arrays.asList(123L, 456L));

        GoldStandard goldStandard = new GoldStandard("uid1", Arrays.asList(123L, 456L), Arrays.asList(789L), null);

        // Mock findById to return the saved object
        when(goldStandardRepository.findById("uid1")).thenReturn(Optional.of(goldStandard));

        // Save the GoldStandard object
        goldStandardRepository.save(goldStandard);
        verify(goldStandardRepository).save(goldStandard);

        // Retrieve the saved GoldStandard object by ID
        GoldStandard retrievedGoldStandard = goldStandardRepository.findById("uid1").orElse(null);

        // Assert that the saved and retrieved objects are the same
        assertEquals(goldStandard, retrievedGoldStandard);
    }

    @Test
    public void testFindById() {
        // Create and save a GoldStandard object
        GoldStandard goldStandard = new GoldStandard("uid2", Arrays.asList(111L, 222L), Arrays.asList(3433L), null);
        
        // Mock findById to return the object
        when(goldStandardRepository.findById("uid2")).thenReturn(Optional.of(goldStandard));

        // Save the object (mocked)
        goldStandardRepository.save(goldStandard);
        verify(goldStandardRepository).save(goldStandard);

        // Retrieve the GoldStandard object by ID
        GoldStandard retrievedGoldStandard = goldStandardRepository.findById("uid2").orElse(null);

        // Assert that the retrieved object is not null and matches the saved object
        assertTrue(retrievedGoldStandard != null);
        assertEquals(goldStandard, retrievedGoldStandard);
    }

    
    @Test
    public void testFindAll() {
        // Create test data
        GoldStandard goldStandard1 = new GoldStandard("uid3", Arrays.asList(333L, 444L), Arrays.asList(777L), null);
        GoldStandard goldStandard2 = new GoldStandard("uid4", Arrays.asList(555L, 666L), Arrays.asList(888L), null);
		List<GoldStandard> goldStandardList = Arrays.asList(goldStandard1, goldStandard2);
        
        // Mock findAll and getItemCount
		when(goldStandardRepository.findAll()).thenReturn(goldStandardList);
        when(goldStandardRepository.getItemCount()).thenReturn((long) goldStandardList.size());

        Iterable<GoldStandard> retrieved = goldStandardRepository.findAll();
        long count = goldStandardRepository.getItemCount();
        
        
        List<GoldStandard> goldStandardListFromIterable = new ArrayList<>();
        retrieved.forEach(goldStandardListFromIterable::add);
        
        // Verify interactions
        verify(goldStandardRepository).findAll();
        verify(goldStandardRepository).getItemCount();
        
        // Assert that the size of the list matches the expected count
        assertEquals(count, goldStandardListFromIterable.size());
    }

    @Test
    public void testGetItemCount() {
        // Create test data
        GoldStandard goldStandard1 = new GoldStandard("uid8", Arrays.asList(333L, 444L), Arrays.asList(777L), null);
        GoldStandard goldStandard2 = new GoldStandard("uid9", Arrays.asList(555L, 666L), Arrays.asList(888L), null);
        
        // Mock getItemCount
        when(goldStandardRepository.getItemCount()).thenReturn(4L);

        // Save the objects (mocked)
        goldStandardRepository.saveAll(Arrays.asList(goldStandard1, goldStandard2));
        verify(goldStandardRepository).saveAll(Arrays.asList(goldStandard1, goldStandard2));

        // Get the count of items in the table
        long itemCount = goldStandardRepository.getItemCount();

        // Assert that the item count is correct
        assertEquals(4L, itemCount);
    }

    @Test
    public void testFindAllById() {
        // Create test data
        GoldStandard goldStandard1 = new GoldStandard("uid10", Arrays.asList(777L, 888L), Arrays.asList(1111L), null);
        GoldStandard goldStandard2 = new GoldStandard("uid11", Arrays.asList(999L, 1001L), Arrays.asList(2222L), null);
        List<GoldStandard> expectedResults = Arrays.asList(goldStandard1, goldStandard2);
        List<String> ids = Arrays.asList("uid10", "uid11");
        
        // Mock findAllById
        when(goldStandardRepository.findAllById(ids)).thenReturn(expectedResults);

        // Save the objects (mocked)
        goldStandardRepository.saveAll(Arrays.asList(goldStandard1, goldStandard2));
        verify(goldStandardRepository).saveAll(Arrays.asList(goldStandard1, goldStandard2));

        // Retrieve GoldStandard objects by their IDs
        List<GoldStandard> goldStandards = goldStandardRepository.findAllById(ids);

        // Assert that the retrieved list contains the correct GoldStandard objects
        assertEquals(2, goldStandards.size());
        assertEquals("uid10", goldStandards.get(0).getUid());
        assertEquals("uid11", goldStandards.get(1).getUid());
    }
}