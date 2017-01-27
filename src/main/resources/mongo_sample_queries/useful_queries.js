use reciter;

/* find average precision and recall in analysis */
db.analysis.aggregate([
{
	$group: {
		_id:null, precision: {$avg:"$analysis.precision"}, recall: {$avg:"$analysis.recall"}
	}
}
]);

db.meshterm.find();
/* List all uids */
db.analysis.find({"_id": "rbaergen"});

db.analysis.count();

/* Find all uids whose precision is less than 0.1 */
db.analysis.find({"analysis.precision": {"$lt" : 0.1}});

/* Find all uids whose recall is less than 0.1 */
db.analysis.find({"analysis.recall": {"$lt" : 0.1}});

/* Find all gold standard */
db.goldstandard.find();

/* Find all gold standard for a given uid */
db.goldstandard.find({"_id" : "rak2007"});

/* Add pmid 26976629 to gold standard for 'cnathan' */
db.goldstandard.update({"_id": "mlg2007"}, {$addToSet: {"knownPmids": NumberLong(26898884)}});

/* Remove from gold standard for 'cnathan' an erroneous array [26976629] */
db.goldstandard.update({"_id": "cnathan"}, {$pull: {"knownPmids": [26976629]}});

/* Remove from gold standard for 'cnathan' an erroneous pmid NumberLong(26976629) */
db.goldstandard.update({"_id": "cnathan"}, {$pull: {"knownPmids": NumberLong(26976629)}});

/* Find if PMID exist in this uid */
db.goldstandard.find({"_id": "rgcryst", "knownPmids": {$in: [NumberLong(26927796)]}});

/* Total number of documents in this identity collection */
db.identity.count();

/* Find all identities */
db.identity.find();

/* Find identity whose uid is 'rgcryst' */
db.identity.find({"_id": "rgcryst"});

/* Find pubmed article with PMID 18341570 */
db.pubmedarticle.find({"_id": 18341570});

/* Find the PubMed search strategies for uid 'amb9023' */
db.esearchresult.find({"uid": "wcb2001"});

/* Delete the esearch result where the uid is 'rbaergen' */
db.esearchresult.deleteMany({"uid": "rbaergen"});

/** Updating gold standard for rak2007 */
db.goldstandard.update({"_id": "rgcryst"}, {$addToSet: {"knownPmids": {$each: [
NumberLong(26771416),
NumberLong(27007171),
NumberLong(26161876),
NumberLong(26822727),
NumberLong(26927796),
NumberLong(26490036),
NumberLong(26674646),
NumberLong(26541521),
NumberLong(24191907),
NumberLong(24548017),
NumberLong(24649839),
NumberLong(25144894),
NumberLong(25238276),
NumberLong(25758611),
NumberLong(25270115),
NumberLong(26026937),
NumberLong(26116571),
NumberLong(26728717)
]}}});

db.recitercluster.find();

db.recitercluster.find({"_id": "rgcryst"});

db.recitercluster.find({"$and" : [
  {"reCiterClusters.articleCluster.meshMajorStrategyScore": {"$gt" : 0.0}}, 
  {"_id" : "aas2004"}
 ]
});
