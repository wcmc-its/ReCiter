package test.examples.pubmed;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ReadErrorFileTest {

	public static void main(String[] args) throws IOException {
		int n = 3;
		String lineN = Files.lines(Paths.get("C:\\Users\\jil3004\\xml\\aad2004\\aad2004_0.xml"))
		                    .skip(n)
		                    .findFirst()
		                    .get()
		                    .trim();
		System.out.println("<ERROR>Unable to obtain query #1</ERROR>".equals(lineN));
	}
}
