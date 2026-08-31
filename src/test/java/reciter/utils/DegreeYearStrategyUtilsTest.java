package reciter.utils;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations="classpath:application.properties")
public class DegreeYearStrategyUtilsTest {

	private static final Logger log = LoggerFactory.getLogger(DegreeYearStrategyUtilsTest.class);
	
    @Value("${strategy.discrepancyDegreeYear.degreeYearDiscrepancyScore}")
    private String degreeYearDiscrepancyScore;

    @Test
    public final void testGetDegreeYearDiscrepancyScoreMap() {
        DegreeYearStrategyUtils degreeYearStrategyUtils = new DegreeYearStrategyUtils();
        Map<Double, Double> degreeYearDiscrepancyScoreMap = degreeYearStrategyUtils.getDegreeYearDiscrepancyScoreMap(this.degreeYearDiscrepancyScore);
        log.info(degreeYearDiscrepancyScoreMap.size() + "Size");
		assertEquals(200, degreeYearDiscrepancyScoreMap.size(), "Map size matches");
    }
    
}
