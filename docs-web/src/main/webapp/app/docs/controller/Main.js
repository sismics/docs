'use strict';

/**
 * Main controller.
 */
App.controller('Main', function($scope, $rootScope, $state, User) {
  User.userInfo().then(function(data) {
    if (data.anonymous) {
      $state.transitionTo('login');
    } else {
      $state.transitionTo('document.default');
    }
  });
});