package reciter.database.mongo.model;

public class Feature {

	private long pmid;
	private int medCornellEdu;
	private int mailMedCornellEdu;
	private int weillCornellEdu;
	private int nypOrg;
	private int departmentMatch;
	private int numKnownCoinvestigators;
	private int weillCornellAffiliation;
	private int containsWeillCornellFromScopus;
	private int isGoldStandard;
	
	public long getPmid() {
		return pmid;
	}
	public void setPmid(long pmid) {
		this.pmid = pmid;
	}
	public int getMedCornellEdu() {
		return medCornellEdu;
	}
	public void setMedCornellEdu(int medCornellEdu) {
		this.medCornellEdu = medCornellEdu;
	}
	public int getMailMedCornellEdu() {
		return mailMedCornellEdu;
	}
	public void setMailMedCornellEdu(int mailMedCornellEdu) {
		this.mailMedCornellEdu = mailMedCornellEdu;
	}
	public int getWeillCornellEdu() {
		return weillCornellEdu;
	}
	public void setWeillCornellEdu(int weillCornellEdu) {
		this.weillCornellEdu = weillCornellEdu;
	}
	public int getNypOrg() {
		return nypOrg;
	}
	public void setNypOrg(int nypOrg) {
		this.nypOrg = nypOrg;
	}
	public int getDepartmentMatch() {
		return departmentMatch;
	}
	public void setDepartmentMatch(int departmentMatch) {
		this.departmentMatch = departmentMatch;
	}
	public int getNumKnownCoinvestigators() {
		return numKnownCoinvestigators;
	}
	public void setNumKnownCoinvestigators(int numKnownCoinvestigators) {
		this.numKnownCoinvestigators = numKnownCoinvestigators;
	}
	public int getWeillCornellAffiliation() {
		return weillCornellAffiliation;
	}
	public void setWeillCornellAffiliation(int weillCornellAffiliation) {
		this.weillCornellAffiliation = weillCornellAffiliation;
	}
	public int getContainsWeillCornellFromScopus() {
		return containsWeillCornellFromScopus;
	}
	public void setContainsWeillCornellFromScopus(int containsWeillCornellFromScopus) {
		this.containsWeillCornellFromScopus = containsWeillCornellFromScopus;
	}
	public int getIsGoldStandard() {
		return isGoldStandard;
	}
	public void setIsGoldStandard(int isGoldStandard) {
		this.isGoldStandard = isGoldStandard;
	}
	
	


}
