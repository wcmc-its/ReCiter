package test.reciter.lucene;

import static org.junit.Assert.assertEquals;

import java.io.File;

import main.reciter.lucene.DocumentIndexReader;

import org.junit.Test;

public class DocumentIndexReaderTest {

	/**
	 * Test whether the isIndexed(String cwid) function works by creating
	 * a test directory "kukafka" in "data/lucene_index". Deletes this
	 * directory afterward.
	 */
	@Test
	public void testIsIndexed() {
		File file = new File("data/lucene_index/kukafka");
		if (!file.exists()) {
			file.mkdir();
		}
		DocumentIndexReader documentIndexReader = new DocumentIndexReader();
		assertEquals(true, documentIndexReader.isIndexed("kukafka"));
		file.delete();
	}
}
