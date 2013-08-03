'use strict';

/**
 * Settings account controller.
 */
App.controller('SettingsAccount', function($scope, Restangular) {
  $scope.editUserAlert = false;
  
  // Alerts
  $scope.alerts = [];
  
  /**
   * Close an alert.
   */
  $scope.closeAlert = function(index) {
    $scope.alerts.splice(index, 1);
  };
  
  /**
   * Edit user.
   */
  $scope.editUser = function() {
    Restangular.one('user').post('', $scope.user).then(function() {
      $scope.user = {};
      $scope.alerts.push({ type: 'success', msg: 'Account successfully updated' });
    });
  };
});