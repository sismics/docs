'use strict';

/**
 * Document view workflow controller.
 */
angular.module('docs').controller('DocumentViewWorkflow', function ($scope, $stateParams, Restangular, $translate, $dialog) {
  /**
   * Load routes.
   */
  $scope.loadRoutes = function () {
    Restangular.one('route').get({
      documentId: $stateParams.id
    }).then(function(data) {
      $scope.routes = data.routes;
    });
  };

  /**
   * Start the selected workflow
   */
  $scope.startWorkflow = function () {
    Restangular.one('route').post('start', {
      routeModelId: $scope.routemodel,
      documentId: $stateParams.id
    }).then(function (data) {
      $scope.document.route_step = data.route_step;
      $scope.loadRoutes();
    });
  };

  /**
   * Cancel the current workflow.
   */
  $scope.cancelWorkflow = function () {
    var title = $translate.instant('document.view.workflow.cancel_workflow_title');
    var msg = $translate.instant('document.view.workflow.cancel_workflow_message');
    var btns = [
      {result: 'cancel', label: $translate.instant('cancel')},
      {result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}
    ];

    $dialog.messageBox(title, msg, btns, function (result) {
      if (result === 'ok') {
        Restangular.one('route').remove({
          documentId: $stateParams.id
        }).then(function () {
          delete $scope.document.route_step;
          $scope.loadRoutes();
        });
      }
    });
  };

  // Load route models
  Restangular.one('routemodel').get().then(function(data) {
    $scope.routemodels = data.routemodels;
  });

  // Load routes
  $scope.loadRoutes();
});