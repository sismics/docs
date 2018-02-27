'use strict';

/**
 * Settings inbox page controller.
 */
angular.module('docs').controller('SettingsInbox', function($scope, $rootScope, Restangular) {
  // Get the inbox configuration
  Restangular.one('app/config_inbox').get().then(function (data) {
    $scope.inbox = data;
  });

  // Get the tags
  Restangular.one('tag/list').get().then(function(data) {
    $scope.tags = data.tags;
  });

  // Save the inbox configuration
  $scope.editInboxConfig = function () {
    return Restangular.one('app').post('config_inbox', $scope.inbox);
  };

  $scope.testInboxConfig = function () {
    $scope.testLoading = true;
    $scope.testResult = undefined;
    $scope.editInboxConfig().then(function () {
      Restangular.one('app').post('test_inbox').then(function (data) {
        $scope.testResult = data;
        $scope.testLoading = false;
      });
    });
  };
});