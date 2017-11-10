'use strict';

/**
 * Document modal PDF controller.
 */
angular.module('docs').controller('DocumentModalPdf', function ($scope, $window, $stateParams, $uibModalInstance) {
  $scope.export = {
    metadata: false,
    comments: false,
    fitimagetopage: true,
    margin: 10
  };

  // Export to PDF
  $scope.exportPdf = function() {
    $window.open('../api/document/' + $stateParams.id
        + '/pdf?metadata=' + $scope.export.metadata
        + '&comments=' + $scope.export.comments
        + '&fitimagetopage=' + $scope.export.fitimagetopage
        + '&margin=' + $scope.export.margin);

    $uibModalInstance.close();
  };

  // Close the modal
  $scope.close = function () {
    $uibModalInstance.close();
  }
});