'use strict';

/**
 * Document modal PDF controller.
 */
angular.module('share').controller('ShareModalPdf', function ($scope, $window, $stateParams, $uibModalInstance) {
  $scope.export = {
    metadata: false,
    comments: false,
    fitimagetopage: true,
    margin: 10
  };

  // Export to PDF
  $scope.exportPdf = function() {
    $window.open('../api/document/' + $stateParams.documentId
        + '/pdf?metadata=' + $scope.export.metadata
        + '&comments=' + $scope.export.comments
        + '&fitimagetopage=' + $scope.export.fitimagetopage
        + '&margin=' + $scope.export.margin
        + '&share=' + $stateParams.shareId);

    $uibModalInstance.close();
  };

  // Close the modal
  $scope.close = function () {
    $uibModalInstance.close();
  }
});