'use strict';

/**
 * Document default controller.
 */
App.controller('DocumentDefault', function($scope, $state, Restangular) {
  // Load app data
  Restangular.one('app').get().then(function(data) {
    $scope.app = data;
  });
});