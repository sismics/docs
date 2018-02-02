'use strict';

/**
 * Settings workflow edition page controller.
 */
angular.module('docs').controller('SettingsWorkflowEdit', function($scope, $dialog, $state, $stateParams, Restangular, $translate, $q) {
  /**
   * UI sortable options.
   */
  $scope.sortableOptions = {
    forceHelperSize: true,
    forcePlaceholderSize: true,
    tolerance: 'pointer',
    handle: '.handle'
  };

  /**
   * Auto-complete on ACL target.
   */
  $scope.getTargetAclTypeahead = function($viewValue) {
    var deferred = $q.defer();
    Restangular.one('acl/target/search')
      .get({
        search: $viewValue
      }).then(function(data) {
      var output = [];

      // Add the type to use later
      output.push.apply(output,  _.map(data.users, function(user) {
        user.type = 'USER';
        return user;
      }));
      output.push.apply(output, _.map(data.groups, function(group) {
        group.type = 'GROUP';
        return group;
      }));

      // Send the data to the typeahead directive
      deferred.resolve(output, true);
    });
    return deferred.promise;
  };

  /**
   * Add a workflow step.
   */
  $scope.addStep = function () {
    $scope.workflow.steps.push({
      type: 'VALIDATE'
    });
  };

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
      $scope.workflow.steps = JSON.parse(data.steps);
    });
  } else {
    $scope.workflow = {
      steps: []
    }
  }

  /**
   * Update the current workflow.
   */
  $scope.edit = function () {
    var promise = null;
    var workflow = angular.copy($scope.workflow);
    workflow.steps = JSON.stringify(workflow.steps);

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

  /**
   * Remove a route step.
   */
  $scope.removeStep = function (step) {
    $scope.workflow.steps.splice($scope.workflow.steps.indexOf(step), 1);
  };
});