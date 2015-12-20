'use strict';

/**
 * Document modal PDF controller.
 */
angular.module('docs').controller('DocumentModalPdf', function ($scope, $window, $stateParams, $modalInstance) {
  $scope.export = {
    metadata: false,
    comments: false,
    fitimagetopage: false
  };

  // Export to PDF
  $scope.exportPdf = function() {
    $window.open('../api/document/' + $stateParams.id
        + '/pdf?metadata=' + $scope.export.metadata
        + '&comments=' + $scope.export.comments
        + '&fitimagetopage=' + $scope.export.fitimagetopage);

    $modalInstance.close();
  };

  // Close the modal
  $scope.close = function () {
    $modalInstance.close();
  }
});