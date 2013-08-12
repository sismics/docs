'use strict';

/**
 * Settings controller.
 */
App.controller('Settings', function($scope, Restangular) {
  // Flag if the user is admin
  $scope.userInfo.then(function (data) {
    $scope.isAdmin = data.base_functions.indexOf('ADMIN') != -1;
  });
});