'use strict';

/**
 * Settings workflow edition page controller.
 */
angular.module('docs').controller('SettingsWorkflowEdit', function($scope, $dialog, $state, $stateParams, Restangular, $translate) {
  /**
   * Returns true if in edit mode (false in add mode).
   */
  $scope.isEdit = function () {
    return $stateParams.id;
  };
  
  /**
   * In edit mode, load the current workflow.
   */
  if ($scope.isEdit()) {
    Restangular.one('routemodel', $stateParams.id).get().then(function (data) {
      $scope.workflow = data;
    });
  }

  /**
   * Update the current workflow.
   */
  $scope.edit = function () {
    var promise = null;
    var workflow = angular.copy($scope.workflow);

    if ($scope.isEdit()) {
      promise = Restangular
        .one('routemodel', $stateParams.id)
        .post('', workflow);
    } else {
      promise = Restangular
        .one('routemodel')
        .put(workflow);
    }
    
    promise.then(function () {
      $scope.loadWorkflows();
      $state.go('settings.workflow');
    });
  };

  /**
   * Delete the current workflow.
   */
  $scope.remove = function () {
    var title = $translate.instant('settings.workflow.edit.delete_workflow_title');
    var msg = $translate.instant('settings.workflow.edit.delete_workflow_message');
    var btns = [
      { result:'cancel', label: $translate.instant('cancel') },
      { result:'ok', label: $translate.instant('ok'), cssClass: 'btn-primary' }
    ];

    $dialog.messageBox(title, msg, btns, function (result) {
      if (result === 'ok') {
        Restangular.one('routemodel', $stateParams.id).remove().then(function () {
          $scope.loadWorkflows();
          $state.go('settings.workflow');
        }, function() {
          $state.go('settings.workflow');
        });
      }
    });
  };
});