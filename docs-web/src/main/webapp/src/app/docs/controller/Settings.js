'use strict';

/**
 * Settings controller.
 */
angular.module('docs').controller('Settings', function($scope, Restangular) {
  // Flag if the user is admin
  $scope.isAdmin = $scope.userInfo.base_functions.indexOf('ADMIN') != -1;
});