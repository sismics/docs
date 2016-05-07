'use strict';

/**
 * Tag edit controller.
 */
angular.module('docs').controller('TagEdit', function($scope, $stateParams, Restangular) {
  Restangular.one('tag', $stateParams.id).get().then(function(data) {
    $scope.tag = data;
  })
});