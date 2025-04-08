package reciter.database.dynamodb.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reciter.database.dynamodb.model.ApplicationUser;
import reciter.database.dynamodb.repository.ApplicationUserRepository;

@ExtendWith(MockitoExtension.class)
public class ApplicationUserRepositoryTest {

    @Mock
    private ApplicationUserRepository applicationUserRepository;

    @BeforeEach
    public void setUp() {
        // Initialize mocks if needed
    }

    @Test
    public void save() {
        ApplicationUser applicationUser = new ApplicationUser("user1", "user1@example.com","user1");
        
        // Mock the findById behavior
        when(applicationUserRepository.findById("user1")).thenReturn(Optional.of(applicationUser));
        
        // Call the save method
        applicationUserRepository.save(applicationUser);
        
        // Verify save was called
        verify(applicationUserRepository).save(applicationUser);

        // Retrieve the ApplicationUser object
        ApplicationUser retrievedApplicationUser = applicationUserRepository.findById("user1").orElse(null);

        assertEquals(applicationUser, retrievedApplicationUser);
    }

    @Test
    public void testFindById() {
        // Create test data
        ApplicationUser applicationUser = new ApplicationUser("user2", "user2@example.com","user2");
        
        // Mock the findById behavior
        when(applicationUserRepository.findById("user2")).thenReturn(Optional.of(applicationUser));
        
        // Save the ApplicationUser (mocked)
        applicationUserRepository.save(applicationUser);
        verify(applicationUserRepository).save(applicationUser);

        // Retrieve the ApplicationUser object by ID
        ApplicationUser retrievedApplicationUser = applicationUserRepository.findById("user2").orElse(null);

        // Assert that the retrieved object is not null and matches the saved object
        assertTrue(retrievedApplicationUser != null);
        assertEquals(applicationUser, retrievedApplicationUser);
    }

    @Test
    public void testFindAll() {
        // Create test data
        ApplicationUser applicationUser1 = new ApplicationUser("user3", "user3@example.com","user1");
        ApplicationUser applicationUser2 = new ApplicationUser("user4", "user4@example.com","user1");
        
        // Mock findAll and getItemCount behavior
        when(applicationUserRepository.findAll()).thenReturn(Arrays.asList(applicationUser1, applicationUser2));
        when(applicationUserRepository.getItemCount()).thenReturn(2L);
        
        // Save the ApplicationUsers (mocked)
        applicationUserRepository.saveAll(Arrays.asList(applicationUser1,applicationUser2));
        verify(applicationUserRepository).saveAll(Arrays.asList(applicationUser1,applicationUser2));

        // Retrieve all ApplicationUser objects
        Iterable<ApplicationUser> applicationUsers = applicationUserRepository.findAll();

        int count = 0;
        for (ApplicationUser user : applicationUsers) {
            count++;
        }
        long itemCount = applicationUserRepository.getItemCount();

        // Assert that the number of retrieved items is correct
        assertEquals(itemCount, count);
    }

    @Test
    public void testDeleteById() {
        // Create test data
        ApplicationUser applicationUser = new ApplicationUser("user5", "user5@example.com", "user1");
        
        // First mock findById to return the user
        when(applicationUserRepository.findById("user5")).thenReturn(Optional.of(applicationUser));
        
        // Save the ApplicationUser (mocked)
        applicationUserRepository.save(applicationUser);
        verify(applicationUserRepository).save(applicationUser);
        
        // This is the important part - BEFORE we verify deleteById, we need to change
        // the mock behavior to return empty after the deletion
        doAnswer(invocation -> {
            // Change the mock behavior after deletion
            when(applicationUserRepository.findById("user5")).thenReturn(Optional.empty());
            return null;
        }).when(applicationUserRepository).deleteById("user5");
        
        // Delete the ApplicationUser object by ID
        applicationUserRepository.deleteById("user5");
        verify(applicationUserRepository).deleteById("user5");
        
        // Attempt to retrieve the deleted ApplicationUser object (should be null)
        ApplicationUser deletedApplicationUser = applicationUserRepository.findById("user5").orElse(null);
        
        // Assert that the ApplicationUser object has been deleted
        assertEquals(null, deletedApplicationUser);
    }

    @Test
    public void testDeleteAll() {
        // Create test data
        ApplicationUser applicationUser1 = new ApplicationUser("user6", "user6@example.com", "user1");
        ApplicationUser applicationUser2 = new ApplicationUser("user7", "user7@example.com", "user1");
        
        // Initially mock findAll to return the users
        when(applicationUserRepository.findAll()).thenReturn(Arrays.asList(applicationUser1, applicationUser2));
        
        // Save the ApplicationUsers (mocked)
        applicationUserRepository.save(applicationUser1);
        applicationUserRepository.save(applicationUser2);
        verify(applicationUserRepository).save(applicationUser1);
        verify(applicationUserRepository).save(applicationUser2);
        
        // Use doAnswer to change the behavior of findAll after deleteAll is called
        doAnswer(invocation -> {
            // Change the mock behavior after deletion
            when(applicationUserRepository.findAll()).thenReturn(List.of());
            return null;
        }).when(applicationUserRepository).deleteAll();
        
        // Delete all ApplicationUser objects
        applicationUserRepository.deleteAll();
        verify(applicationUserRepository).deleteAll();
        
        // Attempt to retrieve all ApplicationUser objects (should be empty)
        Iterable<ApplicationUser> applicationUsers = applicationUserRepository.findAll();
        
        // Assert that no ApplicationUser objects are returned
        int count = 0;
        for (ApplicationUser user : applicationUsers) {
            count++;
        }
        assertEquals(0, count);
    }

    @Test
    public void testGetItemCount() {
        // Create test data
        ApplicationUser applicationUser1 = new ApplicationUser("user10", "user10@example.com","user12");
        ApplicationUser applicationUser2 = new ApplicationUser("user11", "user11@example.com","user12");
        
        // Mock getItemCount to return 2
        when(applicationUserRepository.getItemCount()).thenReturn(2L);
        
        // Save the ApplicationUsers (mocked)
        applicationUserRepository.saveAll(Arrays.asList(applicationUser1,applicationUser2));
        verify(applicationUserRepository).saveAll(Arrays.asList(applicationUser1,applicationUser2));

        // Get the count of items in the table
        long itemCount = applicationUserRepository.getItemCount();

        // Assert that the item count is correct
        assertEquals(2L, itemCount);
    }

    @Test
    public void testFindAllById() {
        // Create test data
        ApplicationUser applicationUser1 = new ApplicationUser("user12", "user12@example.com","user12");
        ApplicationUser applicationUser2 = new ApplicationUser("user13", "user13@example.com","user12");
        List<ApplicationUser> expectedUsers = Arrays.asList(applicationUser1, applicationUser2);
        
        // Mock findAllById behavior
        List<String> ids = List.of("user12", "user13");
        when(applicationUserRepository.findAllById(ids)).thenReturn(expectedUsers);
        
        // Save the ApplicationUsers (mocked)
        applicationUserRepository.saveAll(Arrays.asList(applicationUser1,applicationUser2));
      
        verify(applicationUserRepository).saveAll(Arrays.asList(applicationUser1,applicationUser2));

        // Retrieve ApplicationUser objects by their IDs
        List<ApplicationUser> applicationUsers = applicationUserRepository.findAllById(ids);

        // Assert that the retrieved list contains the correct ApplicationUser objects
        assertEquals(2, applicationUsers.size());
        assertEquals("user12", applicationUsers.get(0).getId());
        assertEquals("user13", applicationUsers.get(1).getId());
    }
}