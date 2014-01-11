'use strict';

/**
 * Settings controller.
 */
App.controller('Settings', function($scope, Restangular) {
  // Flag if the user is admin
  $scope.isAdmin = $scope.userInfo.base_functions.indexOf('ADMIN') != -1;
});