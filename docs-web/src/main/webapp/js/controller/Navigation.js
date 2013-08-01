'use strict';

/**
 * Navigation controller.
 */
App.controller('Navigation', function($scope, $state, $rootScope, User, Restangular) {
  $rootScope.userInfo = User.userInfo();
  
  /**
   * User logout.
   */
  $scope.logout = function($event) {
    User.logout().then(function() {
      $rootScope.userInfo = User.userInfo(true);
      $state.transitionTo('main');
    });
    $event.preventDefault();
  };
});