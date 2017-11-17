'use strict';

/**
 * Login modal password lost controller.
 */
angular.module('docs').controller('LoginModalPasswordLost', function ($scope, $uibModalInstance) {
  $scope.email = '';
  $scope.close = function(name) {
    $uibModalInstance.close(name);
  }
});