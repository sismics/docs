'use strict';

/**
 * Login controller.
 */
angular.module('docs').controller('Login', function($scope, $rootScope, $state, $dialog, User) {
  $scope.login = function() {
    User.login($scope.user).then(function() {
      $rootScope.userInfo = User.userInfo(true);
      $state.transitionTo('document.default');
    }, function() {
      var title = 'Login failed';
      var msg = 'Username or password invalid';
      var btns = [{result:'ok', label: 'OK', cssClass: 'btn-primary'}];

      $dialog.messageBox(title, msg, btns);
    });
  };
});