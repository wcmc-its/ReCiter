function Hello($scope, $http) {
    $http.get('http://localhost/reciter/targetauthor/by/cwid').
        success(function(data) {
            $scope.greeting = data;
        });
}