'use strict';

/**
 * Modal feedback controller.
 */
angular.module('docs').controller('ModalFeedback', function ($scope, $uibModalInstance) {
  $scope.content = '';
  $scope.close = function(content) {
    $uibModalInstance.close(content);
  }
});