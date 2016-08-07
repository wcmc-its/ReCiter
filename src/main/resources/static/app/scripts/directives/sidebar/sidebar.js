'use strict';

/**
 * @ngdoc directive
 * @name izzyposWebApp.directive:adminPosHeader
 * @description
 * # adminPosHeader
 */

var sbAdminApp = angular.module('sbAdminApp')
  .directive('sidebar',['$location',function() {
    return {
      templateUrl:'scripts/directives/sidebar/sidebar.html',
      restrict: 'E',
      replace: true,
      scope: {
      },
      controller:function($scope){
        $scope.selectedMenu = 'dashboard';
        $scope.collapseVar = 0;
        $scope.multiCollapseVar = 0;
        
        $scope.check = function(x){
          
          if(x==$scope.collapseVar)
            $scope.collapseVar = 0;
          else
            $scope.collapseVar = x;
        };
        
        $scope.multiCheck = function(y){
          
          if(y==$scope.multiCollapseVar)
            $scope.multiCollapseVar = 0;
          else
            $scope.multiCollapseVar = y;
        };
      }
    }
  }]);

/**
 * Read a file uploaded by user.
 */
sbAdminApp.directive('onReadFile', function ($parse) {
	return {
		restrict: 'A',
		scope: false,
		link: function(scope, element, attrs) {
			var fn = $parse(attrs.onReadFile);

			element.on('change', function(onChangeEvent) {
				var reader = new FileReader();

				reader.onload = function(onLoadEvent) {
					scope.$apply(function() {
						fn(scope, {$fileContent:onLoadEvent.target.result});
					});
				};

				reader.readAsText((onChangeEvent.srcElement || onChangeEvent.target).files[0]);
			});
		}
	};
});

sbAdminApp.controller("DataImport", function ($scope, $http) {
	$scope.showContent = function($fileContent) {
		// split by new line and filter all falsy (false, null, undefined, 0, NaN or an empty string) elements.
		$scope.content = $fileContent;
	};
//	$scope.cwid = '';
//	$scope.submit = function () {
//		console.log($scope.content);
//		$http.get('http://localhost:8080/reciter/pubmedarticle/by/cwids?cwids=' + $scope.content);
//	};
	$scope.user = {
            cwid: "test",
            lastName: "lastName",
            firstName: "test"
        };
	$scope.submit = function () {
		$http.post('http://localhost:8080/reciter/data_import/rc_identity', JSON.parse($scope.content))
			.success(function (data, status, headers) {
				console.log("added new identity.");
			});
		console.log($scope.content);
		var test = '' + $scope.content;
		console.log(JSON.parse($scope.content));
	};
});

sbAdminApp.controller("reciterNew", function ($scope, $http) {
//	var first = {
//		firstName: "Paul",
//		middleName: "J",
//		lastName: "Albert"
//	};
//	
//	var second = {
//		firstName: "Paul",
//		middleName: "J",
//		lastName: "Albert"
//	};
//	
//	$scope.user = {
//		cwid: "test",
//		authorName: {
//			firstName: "Paul",
//			middleName: "J",
//			lastName: "Albert"
//		},
//		emails: ["test1", "test2"],
//		departments: ["d1", "d2"],
//		yearOfTerminalDegree: 1992,
//		institutions: ["i1", "i2"],
//		knownRelationships: [first, second],
//		knownPmids: [1, 2, 3]
//	};
	$scope.submit = function () {
		var emails = $scope.email.split('\n');
		var departments = $scope.department.split('\n');
		var institutions = $scope.institutions.split('\n');
		var knownRelationships = $scope.knownRelationships.split('\n');
		var knownRelationshipsArray = [];
		for (var i = 0; i < knownRelationships.length; i++) {
			var nameParts = knownRelationships[i].split(',');
			var author = {firstName: nameParts[0], middleName: nameParts[1], lastName: nameParts[2]};
			knownRelationshipsArray.push(author);
		}
		var knownPmids = $scope.knownPmids.split('\n');
		var knownPmidsArray = [];
		for (var i = 0; i < knownPmids.length; i++) {
			knownPmidsArray.push(Number(knownPmids[i]));
		}
		$scope.user = {
				cwid: $scope.cwid,
				authorName: {
					firstName: $scope.firstName,
					middleName: $scope.middleName,
					lastName: $scope.lastName
				},
				emails: emails,
				departments: departments,
				yearOfTerminalDegree: $scope.yearOfTerminalDegree,
				institutions: institutions,
				knownRelationships: knownRelationshipsArray,
				knownPmids: knownPmids
		}
		console.log($scope.user);
		$http.post('http://localhost:8080/reciter/newidentity/', $scope.user)
		.success(function (data, status, headers) {
			console.log("added new identity.");
		});
	};
});

sbAdminApp.controller("reciterExisting", function ($scope, $http) {
	$scope.test = "test";
	$scope.submit = function () {
		$http.get('http://localhost:8080/reciter/analysis/by/cwid?cwid=' +  $scope.targetAuthor.originalObject.cwid).
		success(function(data) {
			$scope.data = data;
			console.log($scope.data);
		});
	};
});
