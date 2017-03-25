use reciter;

/* Viewing data */

/* Total number of documents in this identity collection */
db.identity.count();

/* View all available data for a given UID */
db.identity.find({"_id": "yok2006"});

/* Find pubmed article with PMID 18341570 */
db.pubmedarticle.find({"_id": 18341570});

/* Find the PubMed search strategies for uid 'amb9023' */
db.esearchresult.find({"uid": "yok2006"});

/* Find all identities */
db.identity.find();

/* Find identity whose uid is 'rgcryst' */
db.identity.find({"_id": "rgcryst"});

/* Working with the gold standard */

/* Get a list of all UIDs in the gold standard */

db.goldstandard.find();

/* Get a list of all PMIDs in the gold standard for a given UID */
db.goldstandard.find({"_id" : "dwf2001"});



/** Updating gold standard for rak2007 */
db.goldstandard.update({"_id": "tdn2001"}, {$addToSet: {"knownPmids": {$each: [
NumberLong(9814529),
NumberLong(9814530)
]}}});

/* Add pmid 26976629 to gold standard for 'cnathan' */
db.goldstandard.update({"_id": "mlg2007"}, {$addToSet: {"knownPmids": NumberLong(26898884)}});

/* Remove from gold standard for 'cnathan' an erroneous array [26976629] */
db.goldstandard.update({"_id": "cnathan"}, {$pull: {"knownPmids": [26976629]}});

/* Remove from gold standard for 'cnathan' an erroneous pmid NumberLong(26976629) */
db.goldstandard.update({"_id": "cnathan"}, {$pull: {"knownPmids": NumberLong(26976629)}});

/* Find if PMID exist in this uid */
db.goldstandard.find({"_id": "rgcryst", "knownPmids": {$in: [NumberLong(26927796)]}});

/* Add a new gold standard */
db.goldstandard.insertOne(
	{
		"_id": "yok2006",
		"knownPmids" : [
        	NumberLong(21249174),
            NumberLong(23561054),
NumberLong(20165523),
NumberLong(26084472)
        ]
	}
);

/* Working with analysis results */

/* Find average precision and recall in analysis */
db.analysis.aggregate([
{
	$group: {
		_id:null, precision: {$avg:"$analysis.precision"}, recall: {$avg:"$analysis.recall"}
	}
}
]);

/* Display the results of the analysis for a given UID */
db.analysis.find({"_id": "rbaergen"});

/* Find all uids whose precision is less than 0.1 */
db.analysis.find({"analysis.precision": {"$lt" : 0.1}});

/* Find all uids whose recall is less than 0.1 */
db.analysis.find({"analysis.recall": {"$lt" : 0.1}});

/* Additional query examples */

/* Delete the esearch result where the uid is 'rbaergen' */
db.esearchresult.deleteMany({"uid": "tdn2001"});

db.meshterm.find();

db.analysis.count();

db.recitercluster.find();

db.recitercluster.find({"_id": "ccole"});

db.recitercluster.find({"$and" : [
  {"reCiterClusters.articleCluster.meshMajorStrategyScore": {"$gt" : 0.0}}, 
  {"_id" : "aas2004"}
 ]
});
