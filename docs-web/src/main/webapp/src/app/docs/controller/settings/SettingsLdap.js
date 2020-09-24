'use strict';

/**
 * Settings LDAP page controller.
 */
angular.module('docs').controller('SettingsLdap', function($scope, Restangular, $translate, $timeout) {
  $scope.ldap = {
    enabled: false
  };

  // Get the LDAP configuration
  Restangular.one('app/config_ldap').get().then(function (data) {
    $scope.ldap = data;
    if ($scope.ldap.default_storage) {
      $scope.ldap.default_storage /= 1000000;
    }
  });

  // Edit SMTP config
  $scope.saveResult = undefined;
  $scope.save = function () {
    var ldap = angular.copy($scope.ldap);
    if (ldap.default_storage) {
      ldap.default_storage *= 1000000;
    }
    Restangular.one('app').post('config_ldap', ldap).then(function () {
      $scope.saveResult = $translate.instant('settings.ldap.saved');
      $timeout(function() {
        $scope.saveResult = undefined;
      }, 5000);
    });
  };
});