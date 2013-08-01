'use strict';

/**
 * Document default controller.
 */
App.controller('DocumentDefault', function($scope, $state, Restangular) {
  // Load app data
  $scope.app = Restangular.one('app').get();
});