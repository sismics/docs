'use strict';

/**
 * Settings logs controller.
 */
angular.module('docs').controller('SettingsLog', function($scope, Restangular) {
  Restangular.one('app/log').get({
    limit: 100
  }).then(function(data) {
    $scope.logs = data.logs;
  });
});