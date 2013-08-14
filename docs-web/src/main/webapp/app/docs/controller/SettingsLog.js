'use strict';

/**
 * Settings logs controller.
 */
App.controller('SettingsLog', function($scope, Restangular) {
  Restangular.one('app/log').get({
    limit: 100
  }).then(function(data) {
    $scope.logs = data.logs;
  });
});