'use strict';

/**
 * Modal password lost controller.
 */
angular.module('docs').controller('ModalPasswordLost', function ($scope, $uibModalInstance) {
  $scope.username = '';
  $scope.close = function(username) {
    $uibModalInstance.close(username);
  }
});