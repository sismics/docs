'use strict';

/**
 * Settings session controller.
 */
angular.module('docs').controller('SettingsSession', function($scope, Restangular) {
  /**
   * Load sessions.
   */
  $scope.loadSession = function() {
    Restangular.one('user/session').get().then(function(data) {
      $scope.sessions = data.sessions;
    });
  };
  
  /**
   * Clear all active sessions.
   */
  $scope.deleteSession = function() {
    Restangular.one('user/session').remove().then(function() {
      $scope.loadSession();
    })
  };
  
  $scope.loadSession();
});