'use strict';

/**
 * Document modal share controller.
 */
angular.module('docs').controller('DocumentModalShare', function ($scope, $uibModalInstance) {
  $scope.name = '';
  $scope.close = function(name) {
    $uibModalInstance.close(name);
  }
});