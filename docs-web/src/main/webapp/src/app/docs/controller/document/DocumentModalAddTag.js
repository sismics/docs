'use strict';

/**
 * Document modal add tag controller.
 */
angular.module('docs').controller('DocumentModalAddTag', function ($scope, $uibModalInstance) {
  $scope.tag = { name: '', color: '#3a87ad' };
  $scope.close = function(tag) {
    $uibModalInstance.close(tag);
  }
});