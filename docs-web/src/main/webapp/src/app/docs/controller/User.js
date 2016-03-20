'use strict';

/**
 * User controller.
 */
angular.module('docs').controller('User', function(Restangular, $scope, $state) {
  // Load users
  Restangular.one('user/list').get({
    sort_column: 1,
    asc: true
  }).then(function(data) {
    $scope.users = data.users;
  });

  // Open a user
  $scope.openUser = function(user) {
    $state.go('user.profile', { username: user.username });
  };
});