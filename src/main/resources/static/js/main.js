/**
 * Main AngularJS Web Application
 */
var app = angular.module('reciterWebApp', ['ngRoute'])
.constant('ENDPOINT_URI', 'http://localhost:8080/api/')
.service('ItemsModel', function ($http, ENDPOINT_URI) {
});

/**
 * Configure the Routes
 */
app.config(['$routeProvider', function ($routeProvider) {
	$routeProvider
	// Home
	.when("/", {templateUrl: "partials/reciter_home.html", controller: "PageCtrl"})
	// Pages
	.when("/about", {templateUrl: "partials/about.html", controller: "PageCtrl"})
	.when("/faq", {templateUrl: "partials/faq.html", controller: "PageCtrl"})
	.when("/pricing", {templateUrl: "partials/pricing.html", controller: "PageCtrl"})
	.when("/services", {templateUrl: "partials/services.html", controller: "PageCtrl"})
	.when("/contact", {templateUrl: "partials/contact.html", controller: "PageCtrl"})
	// Blog
	.when("/blog", {templateUrl: "partials/blog.html", controller: "BlogCtrl"})
	.when("/blog/post", {templateUrl: "partials/blog_item.html", controller: "BlogCtrl"})
	// else 404
	.otherwise("/404", {templateUrl: "partials/404.html", controller: "PageCtrl"});
}]);

/**
 * Controls the Blog
 */
app.controller('BlogCtrl', function (/* $scope, $location, $http */) {
	console.log("Blog Controller reporting for duty.");
});

/**
 * Controls all other Pages
 */
app.controller('PageCtrl', function (/* $scope, $location, $http */) {
	console.log("Page Controller reporting for duty.");

	// Activates the Carousel
	$('.carousel').carousel({
		interval: 5000
	});

	// Activates Tooltips for Social Links
	$('.tooltip-social').tooltip({
		selector: "a[data-toggle=tooltip]"
	})
});

app.controller("submitCwid", function ($scope, $http) {
	$scope.cwid = '';
	$scope.submit = function () {
		$http.get('http://localhost:8080/reciter/targetauthor/by/cwid?cwid=' + $scope.cwid).
		success(function(data) {
			$scope.data = data;
		});
	};
});

app.controller("retrieveCwid", function ($scope, $http) {
	$scope.submit = function () {
		$http.get('http://localhost:8080/reciter/analysis/by/cwid?cwid=' + $scope.cwid).
		success(function(data) {
			$scope.precision = data.precision;
			$scope.recall = data.recall;
		});
	};
});

app.controller('MainCtrl', function ($scope) {
	$scope.showContent = function($fileContent) {
		// split by new line and filter all falsy (false, null, undefined, 0, NaN or an empty string) elements.
		$scope.content = $fileContent.split(/\r|\n/).filter(Boolean);
	};
});

/**
 * Read a file.
 */
app.directive('onReadFile', function ($parse) {
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

/**
 * Retrieve articles for a given cwid.
 */
app.controller("retrieveArticles", function ($scope, $http) {
	$scope.cwid = '';
	$scope.submit = function () {
		console.log($scope.content);
		$http.get('http://localhost:8080/reciter/pubmedarticle/by/cwids?cwids=' + $scope.content);
	};
});