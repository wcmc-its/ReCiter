package test.reciter.model.author;

import static org.junit.Assert.assertEquals;
import main.reciter.model.author.AuthorName;

import org.junit.Test;

public class NameTest {

	@Test
	public void testConstructor() {
		AuthorName name = new AuthorName("jane", "mary", "smith");
		assertEquals("Jane", name.getFirstName());
		assertEquals("Mary", name.getMiddleName());
		assertEquals("Smith", name.getLastName());
		assertEquals("J", name.getFirstInitial());
		assertEquals("M", name.getMiddleInitial());
	}

	@Test
	public void testNullConstructor() {
		AuthorName name = new AuthorName(null, null, null);
		assertEquals(null, name.getFirstName());
		assertEquals(null, name.getMiddleName());
		assertEquals(null, name.getLastName());
		assertEquals(null, name.getFirstInitial());
		assertEquals(null, name.getMiddleInitial());
	}
	
	@Test
	public void testEquality() {
		AuthorName name = new AuthorName("Jane", "Mary", "Smith");
		AuthorName copy = new AuthorName("Jane", "Mary", "Smith");
		assertEquals(true, name.equals(copy));
	}
	
	@Test
	public void testDeFormatLucene() {
		AuthorName name = new AuthorName("Jane", "Mary", "Smith");
		AuthorName copy = name.deFormatLucene(name.getLuceneIndexableFormat());
		assertEquals(true, name.equals(copy));
	}
	
	// Testing with "target" and intials = 1

//	@Test
//	public void testNameVariant() {
//		AuthorName name = new AuthorName("Jane", "Mary", "Smith");
//		List<AuthorName> variants = name.variants("target", 1);
//		assertEquals(5, variants.size());
//		assertEquals(true, variants.contains(new AuthorName("Jane", "Mary", "Smith")));
//		assertEquals(true, variants.contains(new AuthorName("J", "Mary", "Smith")));
//		assertEquals(true, variants.contains(new AuthorName("J", "M", "Smith")));
//		assertEquals(true, variants.contains(new AuthorName("Jane", "", "Smith")));
//		assertEquals(true, variants.contains(new AuthorName("Jane", "M", "Smith")));
//	}
//	
//	@Test
//	public void testNameVariantMissingMiddleName() {
//		AuthorName name = new AuthorName("Jane", "", "Smith");
//		List<AuthorName> variants = name.variants("target", 1);
//		assertEquals(1, variants.size());
//		assertEquals(true, variants.contains(new AuthorName("Jane", "", "Smith")));
//	}
//	
//	@Test
//	public void testNameVariantMissingFirstAndMiddleName() {
//		AuthorName name = new AuthorName("", "", "Smith");
//		List<AuthorName> variants = name.variants("target", 1);
//		assertEquals(1, variants.size());
//		assertEquals(true, variants.contains(new AuthorName("", "", "Smith")));
//	}
//	
//	@Test
//	public void testNameVariantMissingAll() {
//		AuthorName name = new AuthorName("", "", "");
//		List<AuthorName> variants = name.variants("target", 1);
//		assertEquals(1, variants.size());
//		assertEquals(true, variants.contains(new AuthorName("", "", "")));
//	}
//	
//	@Test
//	public void testNameVariantFirstAndMiddleInitialsPresent() {
//		AuthorName name = new AuthorName("J", "M", "Smith");
//		List<AuthorName> variants = name.variants("target", 1);
//		assertEquals(1, variants.size());
//		assertEquals(true, variants.contains(new AuthorName("J", "M", "Smith")));
//	}
//	
//	@Test
//	public void testNameVariantMiddleInitialPresent() {
//		AuthorName name = new AuthorName("Jane", "M", "Smith");
//		List<AuthorName> variants = name.variants("target", 1);
//		assertEquals(3, variants.size());
//		assertEquals(true, variants.contains(new AuthorName("Jane", "M", "Smith")));
//		assertEquals(true, variants.contains(new AuthorName("J", "M", "Smith")));
//		assertEquals(true, variants.contains(new AuthorName("Jane", "", "Smith")));
//	}
//	// end testing with "target" and initials = 1
//	
//	// testing with "target" and initials = 2
//	@Test
//	public void testNameVariant2() {
//		AuthorName name = new AuthorName("Jane", "Mary", "Smith");
//		List<AuthorName> variants = name.variants("target", 2);
//		assertEquals(4, variants.size());
//		assertEquals(true, variants.contains(new AuthorName("Jane", "Mary", "Smith")));
//		assertEquals(true, variants.contains(new AuthorName("J", "Mary", "Smith")));
//		assertEquals(true, variants.contains(new AuthorName("Jane", "", "Smith")));
//		assertEquals(true, variants.contains(new AuthorName("Jane", "M", "Smith")));
//	}
//	
//	@Test
//	public void testNameVariantMissingMiddleName2() {
//		AuthorName name = new AuthorName("Jane", "", "Smith");
//		List<AuthorName> variants = name.variants("target", 2);
//		assertEquals(1, variants.size());
//		assertEquals(true, variants.contains(new AuthorName("Jane", "", "Smith")));
//	}
//	
//	@Test
//	public void testNameVariantMissingFirstAndMiddleName2() {
//		AuthorName name = new AuthorName("", "", "Smith");
//		List<AuthorName> variants = name.variants("target", 2);
//		assertEquals(1, variants.size());
//		assertEquals(true, variants.contains(new AuthorName("", "", "Smith")));
//	}
//	
//	@Test
//	public void testNameVariantMissingAll2() {
//		AuthorName name = new AuthorName("", "", "");
//		List<AuthorName> variants = name.variants("target", 2);
//		assertEquals(1, variants.size());
//		assertEquals(true, variants.contains(new AuthorName("", "", "")));
//	}
//	
//	@Test
//	public void testNameVariantFirstAndMiddleInitialsPresent2() {
//		AuthorName name = new AuthorName("J", "M", "Smith");
//		List<AuthorName> variants = name.variants("target", 2);
//		assertEquals(1, variants.size());
//		assertEquals(true, variants.contains(new AuthorName("", "", "")));
//	}
//	
//	@Test
//	public void testNameVariantMiddleInitialPresent2() {
//		AuthorName name = new AuthorName("Jane", "M", "Smith");
//		List<AuthorName> variants = name.variants("target", 2);
//		assertEquals(2, variants.size());
//		assertEquals(true, variants.contains(new AuthorName("Jane", "M", "Smith")));
//		assertEquals(true, variants.contains(new AuthorName("Jane", "", "Smith")));
//	}
	
}
