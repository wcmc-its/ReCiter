package reciter.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import reciter.model.author.AuthorName;

public class AuthorNameUtilsTest {

	@Test
	public void testIsFullNameMatch() {
		AuthorName authorName = new AuthorName("John", "C", "Smith");
		AuthorName otherAuthorName = new AuthorName("John", "C", "Smith");
		boolean actual = AuthorNameUtils.isFullNameMatch(authorName, otherAuthorName);
		assertEquals(true, actual);
	}
	
	

}
