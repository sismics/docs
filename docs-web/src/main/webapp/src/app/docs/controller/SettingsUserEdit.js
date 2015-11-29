'use strict';

/**
 * Settings user edition page controller.
 */
angular.module('docs').controller('SettingsUserEdit', function($scope, $dialog, $state, $stateParams, Restangular) {
  /**
   * Returns true if in edit mode (false in add mode).
   */
  $scope.isEdit = function() {
    return $stateParams.username;
  };
  
  /**
   * In edit mode, load the current user.
   */
  if ($scope.isEdit()) {
    Restangular.one('user', $stateParams.username).get().then(function(data) {
      data.storage_quota /= 1000000;
      $scope.user = data;
    });
  }

  /**
   * Update the current user.
   */
  $scope.edit = function() {
    var promise = null;
    var user = angular.copy($scope.user);
    user.storage_quota *= 1000000;
    
    if ($scope.isEdit()) {
      promise = Restangular
      .one('user', $stateParams.username)
      .post('', user);
    } else {
      promise = Restangular
      .one('user')
      .put(user);
    }
    
    promise.then(function() {
      $scope.loadUsers();
      $state.go('settings.user');
    });
  };

  /**
   * Delete the current user.
   */
  $scope.remove = function () {
    var title = 'Delete user';
    var msg = 'Do you really want to delete this user? All associated documents, files and tags will be deleted';
    var btns = [{result:'cancel', label: 'Cancel'}, {result:'ok', label: 'OK', cssClass: 'btn-primary'}];

    $dialog.messageBox(title, msg, btns, function(result) {
      if (result == 'ok') {
        Restangular.one('user', $stateParams.username).remove().then(function() {
          $scope.loadUsers();
          $state.go('settings.user');
        }, function () {
          $state.go('settings.user');
        });
      }
    });
  };

});