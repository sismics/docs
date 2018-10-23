'use strict';

/**
 * Settings modal disable TOTP controller.
 */
angular.module('docs').controller('SettingsSecurityModalDisableTotp', function ($scope, $uibModalInstance) {
  $scope.password = '';
  $scope.close = function(password) {
    $uibModalInstance.close(password);
  }
});