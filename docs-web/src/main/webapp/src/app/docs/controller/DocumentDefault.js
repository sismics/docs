'use strict';

/**
 * Document default controller.
 */
angular.module('docs').controller('DocumentDefault', function($scope, $state, Restangular) {
  // Load app data
  Restangular.one('app').get().then(function(data) {
    $scope.app = data;
  });
});