package reciter.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReCiterControllerTest {

    @Test
    public final void testReCiterFeatureGeneratorGroupFilters() {
        List<String> identityInstitutions = Arrays.asList("Weill Cornell Medical College",
                "New York-Presbyterian Hospital", "Hamad Medical Corporation");
        List<String> filterInstitution = Arrays.asList("New York-Presbyterian Hospital", "MSKCC");
        List<String> identityOrgUnits = Arrays.asList("Pediatrics", "General Internal Medicine (Medicine)",
                "Doctor of Medicine");
        List<String> filterOrgUnits = Arrays.asList("Pediatrics");
        List<String> identityPersonTypes = Arrays.asList("academic", "affiliate-nyp-epic", "affiliate");
        List<String> filterPersonTypes = Arrays.asList("affiliate-nyp-epic");

        // Valid filters match
        assertFalse(Collections.disjoint(identityInstitutions, filterInstitution));
        assertFalse(Collections.disjoint(identityOrgUnits, filterOrgUnits));
        assertFalse(Collections.disjoint(identityPersonTypes, filterPersonTypes));

        if(identityPersonTypes != null
                &&
                !identityPersonTypes.isEmpty() && !Collections.disjoint(identityPersonTypes, filterPersonTypes)
                &&
                identityInstitutions != null
                &&
                !identityInstitutions.isEmpty()
                &&
                !Collections.disjoint(identityInstitutions, filterInstitution)
                &&
                identityOrgUnits != null
                &&
                !identityOrgUnits.isEmpty()
                &&
                !Collections.disjoint(identityOrgUnits, filterOrgUnits)) {
            System.out.println("Filter for both orgunits and personType");
        }

        // No match filter
        identityOrgUnits = Arrays.asList("Pediatrics", "General Internal Medicine (Medicine)", "Doctor of Medicine");
        filterOrgUnits = Arrays.asList("Neurology");
        assertTrue(Collections.disjoint(identityOrgUnits, filterOrgUnits));
    }
}
