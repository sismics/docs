'use strict';

/**
 * Settings user edition page controller.
 */
angular.module('docs').controller('SettingsUserEdit', function($scope, $dialog, $state, $stateParams, Restangular, $translate) {
  /**
   * Returns true if in edit mode (false in add mode).
   */
  $scope.isEdit = function () {
    return $stateParams.username;
  };
  
  /**
   * In edit mode, load the current user.
   */
  if ($scope.isEdit()) {
    Restangular.one('user', $stateParams.username).get().then(function (data) {
      data.storage_quota /= 1000000;
      $scope.user = data;
    });
  } else {
    $scope.user = {}; // Very important otherwise ng-if in template will make a new scope variable
  }

  /**
   * Update the current user.
   */
  $scope.edit = function () {
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
    
    promise.then(function () {
      $scope.loadUsers();
      $state.go('settings.user');
    }, function (e) {
      if (e.data.type === 'AlreadyExistingUsername') {
        var title = $translate.instant('settings.user.edit.edit_user_failed_title');
        var msg = $translate.instant('settings.user.edit.edit_user_failed_message');
        var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
        $dialog.messageBox(title, msg, btns);
      }
    });
  };

  /**
   * Delete the current user.
   */
  $scope.remove = function () {
    var title = $translate.instant('settings.user.edit.delete_user_title');
    var msg = $translate.instant('settings.user.edit.delete_user_message');
    var btns = [
      { result:'cancel', label: $translate.instant('cancel') },
      { result:'ok', label: $translate.instant('ok'), cssClass: 'btn-primary' }
    ];

    $dialog.messageBox(title, msg, btns, function (result) {
      if (result === 'ok') {
        Restangular.one('user', $stateParams.username).remove().then(function () {
          $scope.loadUsers();
          $state.go('settings.user');
        }, function() {
          $state.go('settings.user');
        });
      }
    });
  };

  $scope.passwordReset = function () {
      Restangular.one('user').post('password_lost', {
          username: $stateParams.username
      }).then(function () {
          var title = $translate.instant('settings.user.edit.password_lost_sent_title');
          var msg = $translate.instant('settings.user.edit.password_lost_sent_message', { username: $stateParams.username });
          var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
          $dialog.messageBox(title, msg, btns);
      });
  };
});