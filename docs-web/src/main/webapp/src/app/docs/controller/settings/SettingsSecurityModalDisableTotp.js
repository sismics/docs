'use strict';

/**
 * Settings modal disable TOTP controller.
 */
angular.module('docs').controller('SettingsSecurityModalDisableTotp', function ($scope, $modalInstance) {
  $scope.password = '';
  $scope.close = function(password) {
    $modalInstance.close(password);
  }
});