'use strict';

/**
 * Settings monitoring controller.
 */
angular.module('docs').controller('SettingsMonitoring', function($scope, Restangular) {
  Restangular.one('app').get().then(function(data) {
    $scope.app = data;
  });

  Restangular.one('app/log').get({
    limit: 100
  }).then(function(data) {
    $scope.logs = data.logs;
  });

  $scope.reindexingStarted = false;
  $scope.startReindexing = function() {
    Restangular.one('app').post('batch/reindex').then(function () {
      $scope.reindexingStarted = true;
    });
  };
});