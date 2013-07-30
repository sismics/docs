'use strict';

/**
 * Navigation controller.
 */
App.controller('Navigation', function($scope, $rootScope, User) {
  $rootScope.userInfo = User.userInfo();
});