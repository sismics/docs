'use strict';

/**
 * Document view activity controller.
 */
angular.module('docs').controller('DocumentViewActivity', function ($scope, $stateParams, Restangular) {
  // Load audit log data from server
  Restangular.one('auditlog').get({
    document: $stateParams.id
  }).then(function(data) {
    $scope.logs = data.logs;
  });
});