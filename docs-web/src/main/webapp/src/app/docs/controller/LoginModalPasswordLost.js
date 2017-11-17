'use strict';

/**
 * Login modal password lost controller.
 */
angular.module('docs').controller('LoginModalPasswordLost', function ($scope, $uibModalInstance) {
  $scope.username = '';
  $scope.close = function(username) {
    $uibModalInstance.close(username);
  }
});