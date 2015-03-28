//package test.reciter.lucene;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.net.MalformedURLException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Set;
//
//import javax.json.Json;
//import javax.json.JsonArrayBuilder;
//import javax.json.JsonBuilderFactory;
//import javax.json.JsonObject;
//import javax.json.JsonObjectBuilder;
//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.parsers.SAXParser;
//import javax.xml.parsers.SAXParserFactory;
//
//import main.database.dao.ArticleDao;
//import main.reciter.algorithm.cluster.ReCiterClusterer;
//import main.reciter.algorithm.cluster.model.ReCiterCluster;
//import main.reciter.lucene.DocumentIndexWriter;
//import main.reciter.lucene.DocumentTerm;
//import main.reciter.lucene.DocumentVector;
//import main.reciter.lucene.DocumentVectorType;
//import main.reciter.lucene.DocumentVectorGenerator;
//import main.reciter.lucene.DocumentTranslator;
//import main.reciter.lucene.docsimilarity.WeightedCosineSimilarity;
//import main.reciter.model.article.ReCiterArticle;
//import main.reciter.model.article.ReCiterArticleCoAuthors;
//import main.reciter.model.author.AuthorAffiliation;
//import main.reciter.model.author.AuthorName;
//import main.reciter.model.author.ReCiterAuthor;
//import main.xml.pubmed.PubmedEFetchHandler;
//import main.xml.pubmed.PubmedXmlFetcher;
//import main.xml.pubmed.model.PubmedArticle;
//import main.xml.scopus.ScopusAffiliationHandler;
//import main.xml.scopus.ScopusXmlQuery;
//import main.xml.scopus.model.ScopusEntry;
//import main.xml.translator.ArticleTranslator;
//
//import org.apache.lucene.document.Document;
//import org.apache.lucene.index.DirectoryReader;
//import org.apache.lucene.index.IndexReader;
//import org.apache.lucene.index.Terms;
//import org.apache.lucene.index.TermsEnum;
//import org.apache.lucene.store.FSDirectory;
//import org.apache.lucene.util.BytesRef;
//import org.xml.sax.SAXException;
//
//public class ReCiterClusteringExample {
//
//	private final String XML_DIR = "xml_cwid/"; // directory containing xml contents for each cwid.
//	private final String XML_INDEX = "xml_cwid_index/"; // directory containing Lucene indexed files for each cwid.
//	private Map<String, List<ReCiterArticle>> pmidToArticleMap;
//	private double totalPrecision = 0;
//	private double totalRecall = 0;
//	private static double cwidCounter = 0;
//	private boolean useSeed = false;
//	private boolean debug = true;
//	private static double maxAvgPrecision = 0;
//	private static double maxAvgRecall = 0;
//
//	private List<Double> highPrecisionList = new ArrayList<Double>();
//
//	public ReCiterClusteringExample() {
//		pmidToArticleMap = new HashMap<String, List<ReCiterArticle>>();
//	}
//
//	/**
//	 * Fetches XML for a list of users and stores them into a directory.
//	 * @throws IOException
//	 * @throws SAXException
//	 * @throws ParserConfigurationException
//	 */
//	public void fetchXml() {
//		PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
//		for (int i = 0; i < TestData.cwid.length; i++) {
//			pubmedXmlFetcher.getPubmedArticle(TestData.lastName[i], TestData.firstInitial[i], TestData.cwid[i]);
//		}
//	}
//
//	/**
//	 * Get the list of xml files.
//	 * @param dirPath
//	 * @return
//	 */
//	public List<String> getXmlFileNameList(String dirPath) {
//		List<String> list = new ArrayList<String>();
//		File dir = new File(dirPath);
//		File[] listOfFiles = dir.listFiles();
//		for (File file : listOfFiles) {
//			list.add(file.getName());
//		}
//		return list;
//	}
//
//	public void storeAffiliationInfo(List<ReCiterArticle> reCiterArticleList) throws MalformedURLException, IOException, SAXException, ParserConfigurationException {
//		List<String> pmidList = new ArrayList<String>();
//		for (ReCiterArticle reCiterArticle : reCiterArticleList) {
//			pmidList.add(Integer.toString(reCiterArticle.getArticleID()));
//			if (pmidList.size() % 100 == 0) {
//				ScopusXmlQuery scopusQuery = new ScopusXmlQuery();
//				ScopusAffiliationHandler handler = scopusQuery.executeQuery(pmidList);
//				List<ScopusEntry> listAffiliation = handler.getScopusEntryList();
//				ArticleDao.storeScopusAffiliation(listAffiliation);
//				pmidList.clear();
//			}
//		}
//	}
//
//	/**
//	 * Index the xml files using Lucene.
//	 * @param files
//	 * @throws ParserConfigurationException
//	 * @throws SAXException
//	 * @throws IOException
//	 */
//	public void indexXml(List<String> files) throws ParserConfigurationException, SAXException, IOException {
//
//		Map<Integer, String> pmidToAffiliationMap = ArticleDao.getScopusAffiliation();
//
//		for (String file : files) {
//			String cwid = file.replace(".xml", "");
//
//			if (cwid.equals("bsg2001")) {
//				if (debug) {
//					System.out.println("Indexing: " + file);
//				}
//				// Parse xml file.
//				SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
//				PubmedEFetchHandler pubmedXmlHandler = new PubmedEFetchHandler();
//				saxParser.parse(XML_DIR + file, pubmedXmlHandler);
//				List<PubmedArticle> articles = pubmedXmlHandler.getPubmedArticles();
//
//				// Stores ReCiterArticle.
//				List<ReCiterArticle> reCiterArticleList = new ArrayList<ReCiterArticle>();
//
//				// Translates PubmedArticle to ReCiterArticle
//				for (PubmedArticle pubmedArticle : articles) {
//					reCiterArticleList.add(ArticleTranslator.translate(pubmedArticle));
//				}
//
//				// Add affiliation information from Scopus (by adding a fake author with empty name).
//				for (ReCiterArticle article : reCiterArticleList) {
//					if (pmidToAffiliationMap.get(article.getArticleID()) != null) {
//						String affiliation = pmidToAffiliationMap.get(article.getArticleID());
//						article.getArticleCoAuthors().addCoAuthor(new ReCiterAuthor(
//								new AuthorName(Integer.toString(article.getArticleID()), 
//										Integer.toString(article.getArticleID()), 
//										Integer.toString(article.getArticleID())), new AuthorAffiliation(affiliation)));
//					}
//				}
//
//				pmidToArticleMap.put(cwid, reCiterArticleList);
//
//				// Translates ReCiterArticle to Lucene Document
//				List<Document> luceneDocumentList = new ArrayList<Document>();
//
//				for (ReCiterArticle reCiterArticle : reCiterArticleList) {
//					luceneDocumentList.add(DocumentTranslator.translate(reCiterArticle));
//				}
//
//				// Writes to index.
//				DocumentIndexWriter docIndexWriter = new DocumentIndexWriter(XML_INDEX + cwid);
//				for (Document doc : luceneDocumentList) {
//					docIndexWriter.index(doc);
//				}
//
//				// Index target person.
//				ReCiterArticle targetPerson = new ReCiterArticle(-1);
//				targetPerson.setArticleCoAuthors(new ReCiterArticleCoAuthors());
//
//				// Get index so that FirstName and LastName can be retrieved.
//				int index = -1;
//				for (int i = 0; i < TestData.cwid.length; i++) {
//					if (cwid.equals(TestData.cwid[i])) {
//						index = i;
//						break;
//					}
//				}
//
//				System.out.println("Person name: " + TestData.firstName[index] + ", " + TestData.lastName[index]);
//				targetPerson.getArticleCoAuthors().addCoAuthor(new ReCiterAuthor(new AuthorName(TestData.firstName[index], null, TestData.lastName[index]), 
//						new AuthorAffiliation("Department of Pathology New Jersey Medical School Newark")));
//
//				luceneDocumentList.add(DocumentTranslator.translate(targetPerson));
//
//				// Close Lucene IndexWriter.
//				docIndexWriter.indexClose();
//			}
//		}
//	}
//
//	public void runClustering(int index) throws IOException {
//
//		String cwid = TestData.cwid[index];
//		String firstName = TestData.firstName[index];
//		String lastName = TestData.lastName[index];
//
//		if (debug) {
//			printStar();
//			System.out.println(lastName + ", " + firstName + " (cwid=" + cwid + ")");
//			printStar();
//		}
//
//		IndexReader indexReader = DirectoryReader.open(FSDirectory.open(new File(XML_INDEX + TestData.cwid[index])));
//
//		if (debug) {
//			System.out.println("Number of XML documents retrieved from PubMed: " + indexReader.maxDoc());
//		}
//
//		// Initialize all Document Terms.
//		DocumentTerm documentTerms = new DocumentTerm();
//		//		documentTerms.initIDFMap(indexReader, DocumentVectorEnum.AFFILIATIONS);
//
//		documentTerms.initAllTerms(indexReader, DocumentVectorType.AFFILIATION);
//		documentTerms.initAllTerms(indexReader, DocumentVectorType.ARTICLE_TITLE);
//		documentTerms.initAllTerms(indexReader, DocumentVectorType.JOURNAL_TITLE);
//		documentTerms.initAllTerms(indexReader, DocumentVectorType.KEYWORD);
//
//		if (debug) {
//			System.out.println("IDF map: " + documentTerms.getAffiliationIDFMap());
//		}
//
//		DocumentVectorGenerator docVectorGenerator = new DocumentVectorGenerator();
//
//		List<ReCiterDocument> reCiterDocumentList = new ArrayList<ReCiterDocument>();
//
//		// Read the index file (article) into a ReCiterDocument.
//		for (int i = 0; i < indexReader.maxDoc(); i++) {
//			
//			// Get PMID of current article.
//			Terms idVector = indexReader.getTermVector(i, DocumentVectorType.PMID.name());
//			String pmid = null;
//			TermsEnum termsEnum = null;
//			if (idVector != null) {
//				termsEnum = idVector.iterator(termsEnum);
//				BytesRef text = null;
//				while ((text = termsEnum.next()) != null) {
//					pmid = text.utf8ToString();
//				}
//			}
//
//			// Create a new ReCiterDocument.
//			ReCiterDocument reCiterDocument = new ReCiterDocument(Integer.parseInt(pmid));
//
//			DocumentVector[] docVectorArray = docVectorGenerator.getDocumentVectors(indexReader, i, documentTerms);
//			DocumentVector docV0 = docVectorArray[0];
//			DocumentVector docV1 = docVectorArray[1];
//			DocumentVector docV2 = docVectorArray[2];
//			DocumentVector docV3 = docVectorArray[3];
//
//			Map<DocumentVectorType, DocumentVector> map = new HashMap<DocumentVectorType, DocumentVector>();
//			map.put(DocumentVectorType.AFFILIATION, docV0);
//			map.put(DocumentVectorType.ARTICLE_TITLE, docV1);
//			map.put(DocumentVectorType.JOURNAL_TITLE, docV2);
//			map.put(DocumentVectorType.KEYWORD, docV3);
//
//			// Add the DocumentVector (sparse vectors containing feature data) to this ReCiterDocument.
//			reCiterDocument.setDocumentVectors(map);
//			reCiterDocumentList.add(reCiterDocument);
//		}
//
//		List<ReCiterArticle> reCiterArticleList = pmidToArticleMap.get(cwid);
//
//		// Set ReCiterDocument to each ReCiterArticle by matching pmid.
//		// TODO code inefficiency here.
//		for (ReCiterDocument reCiterDocument : reCiterDocumentList) {
//			for (ReCiterArticle reCiterArticle : reCiterArticleList) {
//				if (reCiterArticle.getArticleID() == reCiterDocument.getPmid()) {
//					reCiterArticle.setDocumentVectors(reCiterDocument);
//				}
//			}
//		}
//
//		List<ReCiterArticle> articlesOnly = new ArrayList<ReCiterArticle>();
//
//		// Only using true articles (the last one contains the information relevant to the targetPerson).
//		for (int i = 0; i < reCiterArticleList.size() - 1; i++) {
//			articlesOnly.add(reCiterArticleList.get(i));
//		}
//
//		indexReader.close();
//
//		ArticleDao articleDao = new ArticleDao();
//		Set<Integer> pmidSet = articleDao.getPmidList(TestData.cwid[index]);
//
//		ReCiterAuthor targetAuthor = new ReCiterAuthor(new AuthorName(firstName, "", lastName), new AuthorAffiliation("Department of Pathology New Jersey Medical School Newark"));
//
//		double currAvg = -1;
//		double maxSimT = -1;
//		double maxPrecision = -1;
//		double maxRecall = -1;
//		for (double simT = 0.1; simT < 1.0; simT += 0.05) {
//			simT = 0.7;
//			ReCiterClusterer reCiterClusterer = new ReCiterClusterer();
//
//			Collections.sort(articlesOnly);
//
//			reCiterClusterer.similarityThreshold = simT;
//			reCiterClusterer.cluster(articlesOnly, targetAuthor);
//
//			if (debug) {
//				System.out.println(reCiterClusterer);
//			}	
//
//			ReCiterArticle targetPersonArticle = reCiterArticleList.get(reCiterArticleList.size() - 1); // TargetPerson.
//
//			if (debug) {
//				System.out.println("Target person title: " + targetPersonArticle.getArticleTitle().getTitle());
//				System.out.println("Target person journal title: " + targetPersonArticle.getJournal().getJournalTitle());
//				System.out.println("Target person keywords: " + targetPersonArticle.getArticleKeywords().getConcatForm());
//				System.out.println("Target person authors: " + targetPersonArticle.getArticleCoAuthors().getAffiliationConcatForm());
//			}
//
//			int assign = reCiterClusterer.assignTargetToCluster(targetPersonArticle, targetAuthor); // assign to clusters
//
//			double highestPrecision = -1;
//			double hPrecision = 0;
//			double hRecall = 0;
//			for (Entry<Integer, ReCiterCluster> reCiterCluster : reCiterClusterer.getFinalCluster().entrySet()) {
//				assign = reCiterCluster.getKey();
//				if (debug) {
//					System.out.println("Assigned cluster id: " + assign + "\n");
//				}
//
//				if (assign != -1) {
//					// analysis
//					int tp = 0;
//					int size = reCiterClusterer.getFinalCluster().get(assign).getArticleCluster().size();
//					for (ReCiterArticle article : reCiterClusterer.getFinalCluster().get(assign).getArticleCluster()) {
//						int pmid = article.getArticleID();
//						if (pmidSet.contains(pmid)) {
//							tp += 1;
//						}
//					}
//
//					// Calculating precision and recall.
//					double precision = tp * 1.0 / size;
//
//					totalPrecision += precision;
//					int sizeOfPub = pmidSet.size();
//
//					int correctSizeOfPub = 0;
//					for (int pmid : pmidSet) {
//						for (ReCiterArticle article : articlesOnly) {
//							if (article.getArticleID() == pmid) {
//
//								if (debug) {
//									System.out.println("Correct: " + pmid);
//								}
//								correctSizeOfPub ++;
//							}
//						}
//					}
//					double recall = tp * 1.0 / correctSizeOfPub;
//
//					//					System.out.println("Precision: " + precision + " recall: " + recall);
//
//					if ((precision + recall) / 2 > highestPrecision) {
//						highestPrecision = (precision + recall) / 2;
//						hPrecision = precision;
//						hRecall = recall;
//					}
//					totalRecall += recall;
//
//					if (debug) {
//						System.out.println("Gold standard (number of pubs=" + sizeOfPub + "): " + pmidSet + "\n");
//					}
//
//					if (debug) {
//						System.out.println("Precision is : " + precision);
//						System.out.println("Recall is : " + recall);
//					}
//
//					if (debug) {
//						System.out.println("\nFrom the cluster assignment, the following articles (true positives) are correctly assigned to cluster:");
//					}
//
//					if (debug) {
//						for (ReCiterArticle article : reCiterClusterer.getFinalCluster().get(assign).getArticleCluster()) {
//							int pmid = article.getArticleID();
//							if (pmidSet.contains(pmid)) {
//								System.out.print(pmid + ", ");
//							}
//						}
//						System.out.println("\n");
//						System.out.println("These following articles (false positives) are incorrectly assigned to the cluster: ");
//						for (ReCiterArticle article : reCiterClusterer.getFinalCluster().get(assign).getArticleCluster()) {
//							int pmid = article.getArticleID();
//							if (!pmidSet.contains(pmid)) {
//								System.out.print(pmid + ", ");
//							}
//						}
//
//						System.out.println("\n");
//
//						System.out.println("These articles (false negatives) were assigned to another cluster:" );
//						for (Entry<Integer, ReCiterCluster> r : reCiterClusterer.getFinalCluster().entrySet()) {
//							ReCiterCluster c = r.getValue();
//							int key = r.getKey();
//							if (key != assign) {
//								for (ReCiterArticle article : c.getArticleCluster()) {
//									if (pmidSet.contains(article.getArticleID())) {
//										System.out.println(key + ": " + article.getArticleID());
//									}	
//								}
//							}
//						}
//
//						System.out.println("\n");
//						System.out.println("These articles which are supposed to be in " + cwid + " publication list were not retrieved when querying PubMed: ");
//						Set<Integer> pmidRetrieved = new HashSet<Integer>();
//
//						// Pmids of retrieved articles from PubMed.
//						for (ReCiterArticle article : articlesOnly) {
//							pmidRetrieved.add(article.getArticleID());
//						}
//
//						for (int pmid : pmidSet) {
//							if (!pmidRetrieved.contains(pmid)) {
//								System.out.println(pmid);
//							}
//						}
//					}
//				} else {
//					if (debug) {
//						System.out.println("Not assigned to any cluster.");
//					}
//				}
//			}
//
//			if ( (hPrecision + hRecall) / 2 > currAvg) {
//				currAvg = (hPrecision + hRecall) / 2;
//				maxSimT = simT;
//				maxPrecision = hPrecision;
//				maxRecall = hRecall;
//			}
//			//			System.out.println(simT + ", " + hPrecision + ", " + hRecall);
//
//			if (debug) {
//				// Json Constructor
//				System.out.println("Printing Json");
//				JsonBuilderFactory factory = Json.createBuilderFactory(null); // Json factory
//				JsonObjectBuilder builder = factory.createObjectBuilder(); // Outer json builder.
//
//				JsonArrayBuilder nodesArrayBuilder = factory.createArrayBuilder(); // "nodes" json builder.
//
//				Map<Integer, List<Integer>> jsonIndexCenterTojsonIndexCluster = new HashMap<Integer, List<Integer>>();
//
//				int currentIndex = 0;
//
//				for (Entry<Integer, ReCiterCluster> r : reCiterClusterer.getFinalCluster().entrySet()) {
//
//					int clusterId = r.getKey();
//					ReCiterCluster reCiterCluster = r.getValue();
//
//					List<Integer> jsonIndexList= new ArrayList<Integer>(); // list of json cluster indices.
//					int tempCenterIndex = currentIndex; // current center index.
//					jsonIndexCenterTojsonIndexCluster.put(currentIndex, jsonIndexList); // out current index into map.
//
//					JsonObjectBuilder articleObjectBuilderCenter = factory.createObjectBuilder(); // article json builder.
//
//					// color code assigned cluster id.
//					if (assign == clusterId) {
//						articleObjectBuilderCenter
//						.add("pmid", "Cluster Center " + clusterId)
//						.add("article_title", "N/A")
//						.add("journal_title", "N/A")
//						.add("authors", factory.createArrayBuilder()
//								.add(factory.createObjectBuilder()
//										.add("first_name", "N/A")
//										.add("last_name", "N/A")
//										.add("affiliation", "N/A")))
//										.add("keywords", factory.createArrayBuilder()
//												.add(factory.createObjectBuilder()
//														.add("keyword", "N/A")))
//														.add("group", 1);
//					} else {
//						articleObjectBuilderCenter
//						.add("pmid", "Cluster Center " + clusterId)
//						.add("article_title", "N/A")
//						.add("journal_title", "N/A")
//						.add("authors", factory.createArrayBuilder()
//								.add(factory.createObjectBuilder()
//										.add("first_name", "N/A")
//										.add("last_name", "N/A")
//										.add("affiliation", "N/A")))
//										.add("keywords", factory.createArrayBuilder()
//												.add(factory.createObjectBuilder()
//														.add("keyword", "N/A")))
//														.add("group", 2);
//					}
//
//					currentIndex += 1; // increment json index.
//					nodesArrayBuilder.add(articleObjectBuilderCenter);
//
//					for (ReCiterArticle article : reCiterCluster.getArticleCluster()) {
//
//						JsonObjectBuilder articleObjectBuilder = factory.createObjectBuilder();
//						articleObjectBuilder
//						.add("pmid", article.getArticleID())
//						.add("article_title", article.getArticleTitle().getTitle())
//						.add("journal_title", article.getJournal().getJournalTitle());
//
//						for (ReCiterAuthor author : article.getArticleCoAuthors().getCoAuthors()) {
//							articleObjectBuilder
//							.add("authors", factory.createArrayBuilder()
//									.add(factory.createObjectBuilder()
//											.add("first_name", author.getAuthorName().getFirstName())
//											.add("last_name", author.getAuthorName().getLastName())
//											.add("affiliation", article.getArticleCoAuthors().getAffiliationConcatForm())));
//						}
//
//						articleObjectBuilder
//						.add("keywords", factory.createArrayBuilder()
//								.add(factory.createObjectBuilder()
//										.add("keyword", article.getArticleKeywords().getConcatForm())));
//
//						if (pmidSet.contains(article.getArticleID()) && assign == clusterId) {
//							articleObjectBuilder.add("group", 3); // True positive
//						} else if (!pmidSet.contains(article.getArticleID()) && assign == clusterId) {
//							articleObjectBuilder.add("group", 4); // False positive.
//						} else if (pmidSet.contains(article.getArticleID()) && assign != clusterId) {
//							articleObjectBuilder.add("group", 5); // False negative.
//						} else {
//							articleObjectBuilder.add("group", 6); // True negative.
//						}
//
//
//						nodesArrayBuilder.add(articleObjectBuilder);
//
//						jsonIndexCenterTojsonIndexCluster.get(tempCenterIndex).add(currentIndex); // adding current index.
//						currentIndex += 1; // increment json index.
//					}
//
//				}
//				builder
//				.add("nodes", nodesArrayBuilder);
//
//				// Building links.
//				JsonArrayBuilder linksArrayBuilder = factory.createArrayBuilder();
//				for (Entry<Integer, List<Integer>> entry : jsonIndexCenterTojsonIndexCluster.entrySet()) {
//					int centerIndex = entry.getKey();
//					List<Integer> clusterIndices = entry.getValue();
//					for (int clusterIndex : clusterIndices) {
//						JsonObjectBuilder linkObjectBuilder = factory.createObjectBuilder(); // article json builder.
//						linkObjectBuilder
//						.add("source", clusterIndex)
//						.add("target", centerIndex)
//						.add("value", 10);
//
//						linksArrayBuilder.add(linkObjectBuilder);
//					}
//				}
//
//				/**
//				 *  factory.createArrayBuilder()
//						.add(factory.createObjectBuilder()
//								.add("source", 1)
//								.add("target", 0)
//								.add("value", 10)));
//				 */
//
//				builder
//				.add("links", linksArrayBuilder);
//
//				JsonObject value = builder.build();
//
//				writeJson(value.toString(), cwid);
//				System.out.println("End print Json");
//			}
//			break;
//		}
//		maxAvgPrecision += maxPrecision;
//		maxAvgRecall += maxRecall;
//		System.out.println(cwid + " Highest SimT: " + maxSimT + " precision: " + maxPrecision + " recall: " + maxRecall);
//	}
//
//	public void writeJson(String jsonString, String cwid) {
//		try {
//			File file = new File("xml_cwid_json/" + cwid + ".json");
//
//			// if file doesnt exists, then create it
//			if (!file.exists()) {
//				file.createNewFile();
//			}
//
//			FileWriter fw = new FileWriter(file.getAbsoluteFile());
//			BufferedWriter bw = new BufferedWriter(fw);
//			bw.write(jsonString);
//			bw.close();
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//
//	public void printStar() {
//		if (debug) {
//			System.out.println("************************************************************");
//		}
//	}
//
//	public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
//		long startTime = System.currentTimeMillis();
//		ReCiterClusteringExample example = new ReCiterClusteringExample();
//
//		if (example.debug) {
//			WeightedCosineSimilarity weightedCosineSimilarity = new WeightedCosineSimilarity();
//
//			System.out.println("Current Program Settings: ");
//			System.out.println();
//
//			System.out.println("Article to Article Similarity:");
//			System.out.println("Similarity measure used: weighted cosine similarity");
//			System.out.println("Similarity constants: ");
//			System.out.println("Affiliation weight: " + weightedCosineSimilarity.getAFFIL_WEIGHT());
//			System.out.println("Article Title weight: " + weightedCosineSimilarity.getTITLE_WEIGHT());
//			System.out.println("Journal Title weight: " + weightedCosineSimilarity.getJOURN_WEIGHT());
//			System.out.println("Keyword weight: " + weightedCosineSimilarity.getKEYWD_WEIGHT());
//			System.out.println();
//
//			//			System.out.println("Article to ReCiterCluster similarity: ");
//			//			System.out.println("Similarity Threshold: " + ReCiterClusterer.similarityThreshold);
//			//
//			//			System.out.println("Target Person to Final Cluster similarity: ");
//			//			System.out.println("Similarity Threshold: " + ReCiterClusterer.targetAuthorSimilarityThreshold);
//		}
//
//		example.indexXml(example.getXmlFileNameList(example.XML_DIR));
//
//		List<String> cwidList = example.getXmlFileNameList(example.XML_DIR);
//
//		// ArticleDao can be passed as parameter.
//		// 
//		example.totalPrecision = 0;
//		cwidCounter = 0;
//		example.totalRecall = 0;
//		for (int i = 0; i < TestData.cwid.length; i++) {
//			if ("bsg2001".equals(TestData.cwid[i])) {
//				if (cwidList.contains(TestData.cwid[i] + ".xml")) {
//					cwidCounter += 1;
//					example.runClustering(i);
//				}
//			}
//			//			double avgPrecision = example.totalPrecision / example.cwidCounter;
//			//			double avgRecall = example.totalRecall / example.cwidCounter;
//			//			System.out.println(t + ", Precision=" + + avgPrecision + ", Recall=" + avgRecall);
//			//			for (double p : example.highPrecisionList) {
//			//				System.out.println("Highest Precision: " + p);
//			//			}
//		}
//		System.out.println("Total Cwids: " + cwidCounter);
//		System.out.println("Total Average Precision: " + maxAvgPrecision / cwidCounter);
//		System.out.println("Total Average Recall: " + maxAvgRecall / cwidCounter);
//		long endTime   = System.currentTimeMillis();
//		long totalTime = (endTime - startTime) / (1000 * 60); // in minutes
//		System.out.println("Total Time: " + totalTime + " minutes");
//	}
//}
