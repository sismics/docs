'use strict';

/**
 * Group profile controller.
 */
angular.module('docs').controller('GroupProfile', function($stateParams, Restangular, $scope) {
  // Load user
  Restangular.one('group', $stateParams.name).get().then(function(data) {
    $scope.group = data;
  });
});