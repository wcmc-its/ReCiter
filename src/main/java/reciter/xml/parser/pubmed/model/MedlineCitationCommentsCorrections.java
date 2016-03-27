package reciter.xml.parser.pubmed.model;

public class MedlineCitationCommentsCorrections {

	private String refType;
	private String refSource;
	private String pmidVersion;
	private String pmid;
	public String getRefType() {
		return refType;
	}
	public void setRefType(String refType) {
		this.refType = refType;
	}
	public String getRefSource() {
		return refSource;
	}
	public void setRefSource(String refSource) {
		this.refSource = refSource;
	}
	public String getPmidVersion() {
		return pmidVersion;
	}
	public void setPmidVersion(String pmidVersion) {
		this.pmidVersion = pmidVersion;
	}
	public String getPmid() {
		return pmid;
	}
	public void setPmid(String pmid) {
		this.pmid = pmid;
	}
}
