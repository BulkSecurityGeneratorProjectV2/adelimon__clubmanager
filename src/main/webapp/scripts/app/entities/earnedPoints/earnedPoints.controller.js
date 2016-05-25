'use strict';

angular.module('clubmanagerApp')
    .controller('EarnedPointsController', function ($scope, $state, EarnedPoints, ParseLinks) {

        $scope.earnedPointss = [];
        $scope.predicate = 'id';
        $scope.reverse = true;
        $scope.page = 0;
        // yes, use the equal to true here because this guy can be undefined otherwise and Javascript is
        // stupid in that case.  I hate this and it looks like discount programming, but at least I
        // did it on purpose.
        $scope.verifiedView = ($state.$current.data.viewOnlyVerified === true);
        $scope.loadAll = function() {
            var queryProps = {page: $scope.page, size: 20, sort: [$scope.predicate + ',' + ($scope.reverse ? 'asc' : 'desc'), 'id']};
            // if we only want to see verified points, then add that to the query properties.
            if ($scope.verifiedView) {
                queryProps.id = "unverified";
            }
            EarnedPoints.query(queryProps, function(result, headers) {
                $scope.links = ParseLinks.parse(headers('link'));
                for (var i = 0; i < result.length; i++) {
                    $scope.earnedPointss.push(result[i]);
                }
            });
        };
        $scope.reset = function() {
            $scope.page = 0;
            $scope.earnedPointss = [];
            $scope.loadAll();
        };
        $scope.loadPage = function(page) {
            $scope.page = page;
            $scope.loadAll();
        };
        $scope.loadAll();


        $scope.refresh = function () {
            $scope.reset();
            $scope.clear();
        };

        $scope.clear = function () {
            $scope.earnedPoints = {
                date: null,
                description: null,
                pointValue: null,
                verified: false,
                id: null
            };
        };
    });
