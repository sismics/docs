'use strict';

/**
 * Main controller.
 */
App.controller('Main', function($scope, $state, User) {
  User.userInfo(true).then(function(data) {
    if (data.anonymous) {
      $state.transitionTo('login');
    } else {
      $state.transitionTo('document.default');
    }
  });
});