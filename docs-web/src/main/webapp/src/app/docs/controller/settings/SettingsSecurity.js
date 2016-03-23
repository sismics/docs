'use strict';

/**
 * Settings security controller.
 */
angular.module('docs').controller('SettingsSecurity', function($scope, User, $dialog, $modal, Restangular) {
  User.userInfo().then(function(data) {
    $scope.user = data;
  });

  /**
   * Enable TOTP.
   */
  $scope.enableTotp = function() {
    var title = 'Enable two-factor authentication';
    var msg = 'Make sure you have a TOTP-compatible application on your phone ready to add a new account';
    var btns = [{result:'cancel', label: 'Cancel'}, {result:'ok', label: 'OK', cssClass: 'btn-primary'}];

    $dialog.messageBox(title, msg, btns, function(result) {
      if (result == 'ok') {
        Restangular.one('user/enable_totp').post().then(function(data) {
          $scope.secret = data.secret;
          User.userInfo(true).then(function(data) {
            $scope.user = data;
          })
        });
      }
    });
  };

  /**
   * Disable TOTP.
   */
  $scope.disableTotp = function() {
    $modal.open({
      templateUrl: 'partial/docs/settings.security.disabletotp.html',
      controller: 'SettingsSecurityModalDisableTotp'
    }).result.then(function (password) {
      if (password == null) {
        return;
      }

      // Disable TOTP
      Restangular.one('user/disable_totp').post('', {
        password: password
      }).then(function() {
        User.userInfo(true).then(function(data) {
          $scope.user = data;
        })
      });
    });
  };
});