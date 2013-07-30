'use strict';

/**
 * Navigation controller.
 */
App.controller('Navigation', function($scope, User) {
  $scope.userInfo = User.userInfo();
});