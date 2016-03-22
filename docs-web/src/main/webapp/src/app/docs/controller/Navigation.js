'use strict';

/**
 * Navigation controller.
 */
angular.module('docs').controller('Navigation', function($scope, $http, $state, $rootScope, User, Restangular) {
  User.userInfo().then(function(data) {
    $rootScope.userInfo = data;
  });

  // Last time when the errors logs was checked
  $scope.lastLogCheck = new Date().getTime();

  // Number of errors logs
  $scope.errorNumber = 0;

  // Check repeatedly if there is a new error log
  setInterval(function() {
    $scope.$apply(function() {
      Restangular.one('app/log').get({
        // Error count will be wrong if there is more than 10 errors in 60 seconds
        limit: 10,
        level: 'ERROR'
      }).then(function(data) {
          // Add new errors
          $scope.errorNumber += _.reduce(data.logs, function(number, log) {
            if (log.date > $scope.lastLogCheck) {
              return ++number; // It's a new error
            }
            return number; // Not a new error
          }, 0);

          // Update last check timestamp
          $scope.lastLogCheck = new Date().getTime();
        });
    })
  }, 60000);

  /**
   * Navigate to error logs.
   */
  $scope.openLogs = function() {
    $scope.errorNumber = 0;
    $state.go('settings.log');
  };

  /**
   * User logout.
   */
  $scope.logout = function($event) {
    User.logout().then(function() {
      User.userInfo(true).then(function(data) {
        $rootScope.userInfo = data;
      });
      $state.go('main');
    });
    $event.preventDefault();
  };
});