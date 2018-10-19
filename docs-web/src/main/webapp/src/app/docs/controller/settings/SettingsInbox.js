'use strict';

/**
 * Settings inbox page controller.
 */
angular.module('docs').controller('SettingsInbox', function($scope, $rootScope, Restangular, $translate, $timeout) {
  // Get the inbox configuration
  Restangular.one('app/config_inbox').get().then(function (data) {
    $scope.inbox = data;
  });

  // Get the tags
  Restangular.one('tag/list').get().then(function(data) {
    $scope.tags = data.tags;
  });

  // Save the inbox configuration
  $scope.saveResult = undefined;
  $scope.editInboxConfig = function () {
    return Restangular.one('app').post('config_inbox', $scope.inbox).then(function () {
      $scope.saveResult = $translate.instant('settings.inbox.saved');
      $timeout(function() {
        $scope.saveResult = undefined;
      }, 5000);
    });
  };

  $scope.testInboxConfig = function () {
    $scope.testLoading = true;
    $scope.testResult = undefined;
    $scope.editInboxConfig().then(function () {
      Restangular.one('app').post('test_inbox').then(function (data) {
        $scope.testResult = data;
        $scope.testLoading = false;
        $timeout(function() {
          $scope.testResult = undefined;
        }, 5000);
      });
    });
  };
});