package reciter.junit.testcases;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({ 
	
	// include which all test cases to be Tested for a given CWID
	
	RcgoldstandardJunitTest.class, 
	SpecialCharactersJunitTest.class, 
	StemmerImplementationJuitTest.class, 
	
	
        })
public class TestController {
	
	//CWID for which the tests are to be Done 
	
	public static String cwid_junit = "aas2004";
	
}