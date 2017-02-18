package reciter.engine;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StrategyParameters {

	@Value("${strategy.email}")
	private boolean isEmail;
	
	@Value("${strategy.department}")
	private boolean isDepartment;
	
	@Value("${strategy.known.relationship}")
	private boolean isKnownRelationship;
	
	@Value("${strategy.affiliation}")
	private boolean isAffiliation;
	
	@Value("${strategy.scopus.common.affiliation}")
	private boolean isScopusCommonAffiliation;
	
	@Value("${strategy.coauthor}")
	private boolean isCoauthor;
	
	@Value("${strategy.journal}")
	private boolean isJournal;
	
	@Value("${strategy.citizenship}")
	private boolean isCitizenship;
	
	@Value("${strategy.education}")
	private boolean isEducation;
	
	@Value("${strategy.grant}")
	private boolean isGrant;
	
	@Value("${strategy.citation}")
	private boolean isCitation;
	
	@Value("${strategy.cocitation}")
	private boolean isCoCitation;
	
	@Value("${strategy.article.size}")
	private boolean isArticleSize;
	
	@Value("${strategy.bachelors.year.discrepancy}")
	private boolean isBachelorsYearDiscrepancy;
	
	@Value("${strategy.doctoral.year.discrepancy}")
	private boolean isDoctoralYearDiscrepancy;
	
	@Value("${strategy.remove.by.name}")
	private boolean isRemoveByName;
	
	@Value("${strategy.cluster.size}")
	private boolean isClusterSize;
	
	@Value("${strategy.mesh.major}")
	private boolean isMeshMajor;
	
	@Value("${use.gold.standard.evidence}")
	private boolean useGoldStandardEvidence;
	
	@Value("${use.rejected.evidence}")
	private boolean useRejectedEvidence;

	public boolean isEmail() {
		return isEmail;
	}

	public void setEmail(boolean isEmail) {
		this.isEmail = isEmail;
	}

	public boolean isDepartment() {
		return isDepartment;
	}

	public void setDepartment(boolean isDepartment) {
		this.isDepartment = isDepartment;
	}

	public boolean isKnownRelationship() {
		return isKnownRelationship;
	}

	public void setKnownRelationship(boolean isKnownRelationship) {
		this.isKnownRelationship = isKnownRelationship;
	}

	public boolean isAffiliation() {
		return isAffiliation;
	}

	public void setAffiliation(boolean isAffiliation) {
		this.isAffiliation = isAffiliation;
	}

	public boolean isScopusCommonAffiliation() {
		return isScopusCommonAffiliation;
	}

	public void setScopusCommonAffiliation(boolean isScopusCommonAffiliation) {
		this.isScopusCommonAffiliation = isScopusCommonAffiliation;
	}

	public boolean isCoauthor() {
		return isCoauthor;
	}

	public void setCoauthor(boolean isCoauthor) {
		this.isCoauthor = isCoauthor;
	}

	public boolean isJournal() {
		return isJournal;
	}

	public void setJournal(boolean isJournal) {
		this.isJournal = isJournal;
	}

	public boolean isCitizenship() {
		return isCitizenship;
	}

	public void setCitizenship(boolean isCitizenship) {
		this.isCitizenship = isCitizenship;
	}

	public boolean isEducation() {
		return isEducation;
	}

	public void setEducation(boolean isEducation) {
		this.isEducation = isEducation;
	}

	public boolean isGrant() {
		return isGrant;
	}

	public void setGrant(boolean isGrant) {
		this.isGrant = isGrant;
	}

	public boolean isCitation() {
		return isCitation;
	}

	public void setCitation(boolean isCitation) {
		this.isCitation = isCitation;
	}

	public boolean isCoCitation() {
		return isCoCitation;
	}

	public void setCoCitation(boolean isCoCitation) {
		this.isCoCitation = isCoCitation;
	}

	public boolean isArticleSize() {
		return isArticleSize;
	}

	public void setArticleSize(boolean isArticleSize) {
		this.isArticleSize = isArticleSize;
	}

	public boolean isBachelorsYearDiscrepancy() {
		return isBachelorsYearDiscrepancy;
	}

	public void setBachelorsYearDiscrepancy(boolean isBachelorsYearDiscrepancy) {
		this.isBachelorsYearDiscrepancy = isBachelorsYearDiscrepancy;
	}

	public boolean isDoctoralYearDiscrepancy() {
		return isDoctoralYearDiscrepancy;
	}

	public void setDoctoralYearDiscrepancy(boolean isDoctoralYearDiscrepancy) {
		this.isDoctoralYearDiscrepancy = isDoctoralYearDiscrepancy;
	}

	public boolean isRemoveByName() {
		return isRemoveByName;
	}

	public void setRemoveByName(boolean isRemoveByName) {
		this.isRemoveByName = isRemoveByName;
	}

	public boolean isClusterSize() {
		return isClusterSize;
	}

	public void setClusterSize(boolean isClusterSize) {
		this.isClusterSize = isClusterSize;
	}

	public boolean isMeshMajor() {
		return isMeshMajor;
	}

	public void setMeshMajor(boolean isMeshMajor) {
		this.isMeshMajor = isMeshMajor;
	}

	public boolean isUseGoldStandardEvidence() {
		return useGoldStandardEvidence;
	}

	public void setUseGoldStandardEvidence(boolean useGoldStandardEvidence) {
		this.useGoldStandardEvidence = useGoldStandardEvidence;
	}
	
	public boolean isUseRejectedEvidence() {
		return useRejectedEvidence;
	}

	public void setUseRejectedEvidence(boolean useRejectedEvidence) {
		this.useRejectedEvidence = useRejectedEvidence;
	}
}
