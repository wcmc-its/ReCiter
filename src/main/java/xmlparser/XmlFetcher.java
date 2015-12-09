package xmlparser;

public interface XmlFetcher {
	void fetch(String query, String fileName);
	void saveXml(String url, String directoryLocation, String directoryName, String fileName);
}
