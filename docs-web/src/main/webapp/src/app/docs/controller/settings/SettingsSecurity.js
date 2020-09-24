'use strict';

/**
 * Settings security controller.
 */
angular.module('docs').controller('SettingsSecurity', function($scope, User, $dialog, $uibModal, Restangular, $translate) {
  User.userInfo().then(function(data) {
    $scope.user = data;
  });

  /**
   * Enable TOTP.
   */
  $scope.enableTotp = function () {
    var title = $translate.instant('settings.security.enable_totp');
    var msg = $translate.instant('settings.security.enable_totp_message');
    var btns = [
      { result:'cancel', label: $translate.instant('cancel') },
      { result:'ok', label: $translate.instant('ok'), cssClass: 'btn-primary' }
    ];

    $dialog.messageBox(title, msg, btns, function(result) {
      if (result === 'ok') {
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
  $scope.disableTotp = function () {
    $uibModal.open({
      templateUrl: 'partial/docs/settings.security.disabletotp.html',
      controller: 'SettingsSecurityModalDisableTotp'
    }).result.then(function (password) {
      if (password === null) {
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

  /**
   * Test TOTP.
   */
  $scope.testValidationCodeSuccess = null;
  $scope.testTotp = function (code) {
    Restangular.one('user/test_totp').post('', {
      code: code
    }).then(function() {
      $scope.testValidationCodeSuccess = true;
    }, function () {
      $scope.testValidationCodeSuccess = false;
    });
  };
});