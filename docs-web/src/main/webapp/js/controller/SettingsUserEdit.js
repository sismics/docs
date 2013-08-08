'use strict';

/**
 * Settings user edition page controller.
 */
App.controller('SettingsUserEdit', function($scope, $stateParams, Restangular) {
  $scope.user = Restangular.one('user', $stateParams.username).get();
});