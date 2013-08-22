'use strict';

/**
 * Navigation controller.
 */
App.controller('Navigation', function($scope, $http, $state, $rootScope, User, Restangular) {
  $rootScope.userInfo = User.userInfo();

  // Last time when the errors logs was checked
  $scope.lastLogCheck = new Date().getTime();

  // Number of errors logs
  $scope.errorNumber = 0;

  // Check repeatedly if there is a new error log
  setInterval(function() {
    $scope.$apply(function() {
      Restangular.one('app/log').get({
        limit: 100,
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
  }, 10000);

  /**
   * Navigate to error logs.
   */
  $scope.openLogs = function() {
    $scope.errorNumber = 0;
    $state.transitionTo('settings.log');
  };

  /**
   * User logout.
   */
  $scope.logout = function($event) {
    User.logout().then(function() {
      $rootScope.userInfo = User.userInfo(true);
      $state.transitionTo('main');
    });
    $event.preventDefault();
  };

  /**
   * Returns true if at least an asynchronous request is in progress.
   */
  $scope.isLoading = function() {
    return $http.pendingRequests.length > 0;
  };
});