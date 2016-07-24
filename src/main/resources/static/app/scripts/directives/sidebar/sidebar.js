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
