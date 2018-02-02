'use strict';

/**
 * Document view workflow controller.
 */
angular.module('docs').controller('DocumentViewWorkflow', function ($scope, $stateParams, Restangular) {
  $scope.loadRoutes = function () {
    Restangular.one('route').get({
      documentId: $stateParams.id
    }).then(function(data) {
      $scope.routes = data.routes;
    });
  };

  // Load route models
  Restangular.one('routemodel').get().then(function(data) {
    $scope.routemodels = data.routemodels;
  });

  // Start the selected workflow
  $scope.startWorkflow = function () {
    Restangular.one('route').post('start', {
      routeModelId: $scope.routemodel,
      documentId: $stateParams.id
    }).then(function (data) {
      $scope.document.route_step = data.route_step;
      $scope.loadRoutes();
    });
  };

  // Load routes
  $scope.loadRoutes();
});