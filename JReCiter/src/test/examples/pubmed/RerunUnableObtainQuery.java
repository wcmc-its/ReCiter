package test.examples.pubmed;

/**
 * Main method to retry the xml fetch after the NCBI server responds with an xml that has the following contents:
 * 
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE eEfetchResult PUBLIC "-//NLM//DTD efetch 20131226//EN" "http://eutils.ncbi.nlm.nih.gov/eutils/dtd/20131226/efetch.dtd">
<eFetchResult>
	<ERROR>Unable to obtain query #1</ERROR>
</eFetchResult>
 * @author jil3004
 *
 */
public class RerunUnableObtainQuery {

	public static void main(String[] args) {
		
	}
}
