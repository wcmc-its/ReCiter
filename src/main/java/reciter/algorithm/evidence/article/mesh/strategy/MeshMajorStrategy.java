package reciter.algorithm.evidence.article.mesh.strategy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import database.dao.MeshRawCount;
import database.dao.impl.MeshRawCountImpl;
import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleMeshHeading;
import reciter.model.article.ReCiterCitationYNEnum;
import reciter.model.article.ReCiterMeshHeadingQualifierName;
import reciter.model.author.TargetAuthor;

/**
 * https://github.com/wcmc-its/ReCiter/issues/131
 * 
 * @author Jie
 *
 */
public class MeshMajorStrategy extends AbstractTargetAuthorStrategy {

	private MeshRawCount meshRawCount;
	private Set<String> generatedMeshMajors;
	private Map<String, Long> meshFrequency;
	private final double threshold = 0.4;

	private static Map<String, Long> meshRawCountCache = new HashMap<String, Long>();

	private final static Logger slf4jLogger = LoggerFactory.getLogger(MeshMajorStrategy.class);

	public MeshMajorStrategy(List<ReCiterArticle> selectedReCiterArticles) {
		meshRawCount = new MeshRawCountImpl();
		meshFrequency = buildMeshFrequency(selectedReCiterArticles);
		generatedMeshMajors = buildGeneratedMesh(meshFrequency, threshold);
	}

	/**
	 * <p>
	 * Identify candidate records (currently in the negative pile) that have one or MeSH majors from the list we've 
	 * generated.
	 * <p>
	 * Discard where a name match is not possible. For example, Aaron J. Marcus (the person) could not have authored 
	 * a paper by Marcus, AF.
	 * <p>
	 * Assign any remaining articles to the positive pile. Assume that the list of MeSH major terms that we generated 
	 * step 1 is constant or fixed. i.e., Once a record is assigned to the positive pile, its list of MeSH major terms 
	 * is NOT added to the list of MeSH major terms that we generated in step 1.
	 * 
	 * @param reCiterArticle
	 * @param targetAuthor
	 * @return
	 */
	public double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {

		boolean isAuthorNameMatch = matchAuthorName(reCiterArticle, targetAuthor);

		if (isAuthorNameMatch) {
			List<ReCiterArticleMeshHeading> meshHeadings = reCiterArticle.getMeshHeadings();
			for (ReCiterArticleMeshHeading meshHeading : meshHeadings) {
				String descriptorName = meshHeading.getDescriptorName().getDescriptorName();
				if (generatedMeshMajors.contains(descriptorName)) {
					slf4jLogger.info("Moved reCiterArticle=[" + reCiterArticle.getArticleId() + "] to 'yes` pile using "
							+ "mesh=[" + descriptorName + "] gold standard=[" + reCiterArticle.getGoldStandard() + "]");
					return 1;
				}
			}
		} else {
			slf4jLogger.info("reCiterArticle=[" + reCiterArticle.getArticleId() + "] author name doesn't match.");
		}

		return 0;
	}

	/**
	 * Constructs a map of mesh majors to its frequency of occurrence in list of ReCiterArticles.
	 * @param reCiterArticles
	 * @return
	 */
	private Map<String, Long> buildMeshFrequency(List<ReCiterArticle> reCiterArticles) {
		Map<String, Long> map = new HashMap<String, Long>();
		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			List<ReCiterArticleMeshHeading> meshHeadings = reCiterArticle.getMeshHeadings();
			for (ReCiterArticleMeshHeading meshHeading : meshHeadings) {
				if (isMeshMajor(meshHeading)) { // check if it's a mesh heading.
					String descriptorName = meshHeading.getDescriptorName().getDescriptorName();
					if (!map.containsKey(descriptorName)) {
						map.put(descriptorName, 1L);
					} else {
						long currentFrequency = map.get(descriptorName);
						currentFrequency++;
						map.put(descriptorName, currentFrequency);
					}
				}
			}
		}
		return map;
	}

	/**
	 * <p>
	 * Lookup the total count of the term in all of Medline using the wcmc_mesh_raw_count table. 
	 * (If there is no such term in the table, ignore it.) For example, the count of "blood platelets" 
	 * in that table is 85,807.
	 * 
	 * <p>
	 * Divide the latter by the former and multiply by 10,000. For example, (47 / 85,807) x 10,000 = 5.477408603.
	 * 
	 * @param meshFrequency
	 * @param threshold
	 * @return
	 */
	private Set<String> buildGeneratedMesh(Map<String, Long> meshFrequency, double threshold) {
		Set<String> generatedMeshMajors = new HashSet<String>();
		for (Map.Entry<String, Long> entry : meshFrequency.entrySet()) {
			String mesh = entry.getKey();
			long meshCountInTargetAuthorArticles = entry.getValue();

			if (!meshRawCountCache.containsKey(mesh)) {
				long rawCountFromPubmed = meshRawCount.getCount(mesh);
				meshRawCountCache.put(mesh, rawCountFromPubmed);
			}

			long rawCountFromPubmed = meshRawCount.getCount(mesh);
			if (rawCountFromPubmed != 0) {
				double score = meshCountInTargetAuthorArticles * 10000.0 / (rawCountFromPubmed) ;
				slf4jLogger.info("mesh count=[" + meshCountInTargetAuthorArticles + "], raw=[" + rawCountFromPubmed + "], mesh=[" + mesh + "], score=[" + score + "]");
				if (score > threshold) {
					generatedMeshMajors.add(mesh);
				}
			}
		}
		slf4jLogger.info("generated mesh majors = " + generatedMeshMajors);
		slf4jLogger.info("meshRawCountCache = " + meshRawCountCache);
		return generatedMeshMajors;
	}

	/**
	 * <p>
	 * MeSH major parsing
	 * <p>
	 * Easy: no subheading (e.g., for 23919362, “Contracts" is MeSH major)
	 * <p>
	 * Single subheading (e.g., for 21740463, "Cold Temperature” is MeSH major)
	 * <p>
	 * Harder: Multiple subheadings modifying the same MeSH term. (e.g., for 6358887, "Aspirin" is mesh major even 
	 * though there are cases where aspirin is not listed as MeSH major.)
	 * @param meshHeading
	 * @return
	 */
	private boolean isMeshMajor(ReCiterArticleMeshHeading meshHeading) {
		// Easy case:
		if (meshHeading.getDescriptorName().getMajorTopicYN() == ReCiterCitationYNEnum.Y)
			return true;

		// Single subheading or Multiple subheading:
		for (ReCiterMeshHeadingQualifierName qualifierName : meshHeading.getQualifierNameList()) {
			if (qualifierName.getMajorTopicYN() == ReCiterCitationYNEnum.Y) {
				return true;
			}
		}

		return false;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor) {
		// TODO Auto-generated method stub
		return 0;
	}
}
