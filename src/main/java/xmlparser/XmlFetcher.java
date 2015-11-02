package xmlparser;

public interface XmlFetcher {
	void fetch(String lastName, String firstName, String middleNamne, String cwid);
	void saveXml(String url, String directoryLocation, String directoryName, String fileName);
}
