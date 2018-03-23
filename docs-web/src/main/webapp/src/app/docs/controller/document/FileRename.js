'use strict';

/**
 * File rename controller.
 */
angular.module('docs').controller('FileRename', function ($scope, file, $uibModalInstance) {
  $scope.file = file;
  $scope.close = function(file) {
    $uibModalInstance.close(file);
  }
});