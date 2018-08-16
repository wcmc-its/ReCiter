var fs = require('fs');
var parse = require('csv-parse');
var async = require('async');
const AWS = require('aws-sdk');
const dynamodbDocClient = new AWS.DynamoDB({ convertEmptyValues: true, region: "us-east-1" });

var csv_filename = "./SciMetrix.1.csv";

rs = fs.createReadStream(csv_filename);
parser = parse({
    columns : true,
    delimiter : ','
}, function(err, data) {
    var split_arrays = [], size = 25;

    while (data.length > 0) {

        //split_arrays.push(data.splice(0, size));
        let cur25 = data.splice(0, size)
        let item_data = []

        for (var i = cur25.length - 1; i >= 0; i--) {
          const this_item = {
            "PutRequest" : {
              "Item": {
                // your column names here will vary, but you'll need do define the type
                "smsid": {
                  "N": cur25[i].smsid
                },
                "publicationName": {
                "S": cur25[i].publicationName
                 },
                 "issn": {
                "S": cur25[i].issn
                },
                "issncut": {
                "S": cur25[i].issncut
                },
                "eissn": {
                "S": cur25[i].eissn
                },
                "scienceMetrixDomain": {
                "S": cur25[i].scienceMetrixDomain
                },
                "scienceMetrixField": {
                "S": cur25[i].scienceMetrixField
                },
                "scienceMetrixSubfield": {
                "S": cur25[i].scienceMetrixSubfield
                },
                "scienceMatrixSubfieldId": {
                "S": cur25[i].scienceMatrixSubfieldId
                }
                }
              }
          };
          const santitizedItem = removeEmptyStringElements(this_item)
          console.log(santitizedItem);
          item_data.push(santitizedItem)
        }
        split_arrays.push(item_data);
    }
    data_imported = false;
    chunk_no = 1;
    async.each(split_arrays, (item_data, callback) => {
        const params = {
            RequestItems: {
                "ScienceMetrix" : item_data
            }
        }
        dynamodbDocClient.batchWriteItem(params, function(err, res, cap) {
            if (err === null) {
                console.log('Success chunk #' + chunk_no);
                data_imported = true;
            } else {
                console.log(err);
                console.log('Fail chunk #' + chunk_no);
                data_imported = false;
            }
            chunk_no++;
            callback();
        });

    }, () => {
        // run after loops
        console.log('all data imported....');

    });

});
rs.pipe(parser);

function removeEmptyStringElements(obj) {
      for (var prop in obj) {
        //console.log(obj);

        if (typeof obj[prop] === 'object') {// dive deeper in
          removeEmptyStringElements(obj[prop]);
        } else if(obj[prop] === '') {// delete elements that are empty strings
          //delete obj[prop];
          //this is because dynamodb does not accept empty strings had to add a space
          obj[prop] = ' ';
        }
      }
      return obj;
    }
