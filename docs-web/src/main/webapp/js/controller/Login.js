'use strict';

/**
 * Login controller.
 */
App.controller('Login', function($scope, $state, $dialog, User) {
  $scope.login = function() {
    User.login($scope.user).then(function() {
      $state.transitionTo('document.default');
    }, function() {
      var title = 'Login failed';
      var msg = 'Username or password invalid';
      var btns = [{result:'ok', label: 'OK', cssClass: 'btn-primary'}];

      $dialog.messageBox(title, msg, btns).open();
    });
  };
});