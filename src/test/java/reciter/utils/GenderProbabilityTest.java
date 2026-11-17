package reciter.utils;


import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.TypeToken;

import reciter.database.dynamodb.model.Gender;
import reciter.engine.EngineParameters;
import reciter.model.article.ReCiterArticle;
import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;

@ExtendWith(MockitoExtension.class)
public class GenderProbabilityTest {
	
	private static final Logger log = LoggerFactory.getLogger(GenderProbabilityTest.class);
	
	private static List<Gender> genders;
	
	@BeforeAll
	public static void setUpBeforeClass() throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/files/Gender.json"));
		Type listType = new TypeToken<List<Gender>>() {}.getType();
		genders = GenericJsonDeserializer.getGson().fromJson(reader, listType);
	}

	@Test
	public final void getGenderIdentityProbabilityTest() {
		EngineParameters.setGenders(GenderProbabilityTest.genders);
		
		Identity identity = new Identity();
		identity.setPrimaryName(new AuthorName("Paul", "J", "Albert"));
		List<AuthorName> alternateName = new ArrayList<AuthorName>();
		alternateName.add(new AuthorName("Paul James", "Andrew", "Albert"));
		alternateName.add(new AuthorName("J", "Andrew-Juan", "Albert"));
		alternateName.add(new AuthorName("J", "Rew Juan", "Albert"));
		identity.setAlternateNames(alternateName);
		
		GenderProbability.getGenderIdentityProbability(identity);
		assertNotNull(identity.getGender());
	}
	
	
	@Test
	public final void getGenderArticleProbabilityTest() {
		EngineParameters.setGenders(GenderProbabilityTest.genders);
		ReCiterArticle reCiterArticle = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/reciter/utils/user.json"));
			reCiterArticle = GenericJsonDeserializer.getGson().fromJson(reader, ReCiterArticle.class);
		} catch (IOException e) {
			log.error("Cannot parse Json", e);
		}
		
		if (reCiterArticle != null) {
			assertNotNull(GenderProbability.getGenderArticleProbability(reCiterArticle),"Article has gender probability");
		}
	}

}
