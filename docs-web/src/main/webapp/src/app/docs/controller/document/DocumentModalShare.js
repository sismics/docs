'use strict';

/**
 * Document modal share controller.
 */
angular.module('docs').controller('DocumentModalShare', function ($scope, $modalInstance) {
  $scope.name = '';
  $scope.close = function(name) {
    $modalInstance.close(name);
  }
});