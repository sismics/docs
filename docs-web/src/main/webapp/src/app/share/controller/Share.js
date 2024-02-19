'use strict';

/**
 * Share controller.
 */
angular.module('share').controller('Share', function($scope, $state, $stateParams, Restangular, $uibModal) {
  $scope.displayMode = _.isUndefined(localStorage.fileDisplayMode) ? 'grid' : localStorage.fileDisplayMode;

  /**
   * Watch for display mode change.
   */
  $scope.$watch('displayMode', function (next) {
    localStorage.fileDisplayMode = next;
  });

  // Load document
  Restangular.one('document', $stateParams.documentId).get({ share: $stateParams.shareId })
      .then(function (data) {
        $scope.document = data;
      }, function (response) {
        if (response.status === 403) {
          $state.go('403');
        }
      });

  // Load files
  Restangular.one('file/list').get({ id: $stateParams.documentId, share: $stateParams.shareId })
      .then(function (data) {
        $scope.files = data.files;
      });

  // Load comments from server
  Restangular.one('comment', $stateParams.documentId).get({ share: $stateParams.shareId }).then(function(data) {
        $scope.comments = data.comments;
      }, function(response) {
        $scope.commentsError = response;
      });

  /**
   * Navigate to the selected file.
   */
  $scope.openFile = function (file) {
    $state.go('share.file', { documentId: $stateParams.documentId, shareId: $stateParams.shareId, fileId: file.id })
  };

  /**
   * Export the current document to PDF.
   */
  $scope.exportPdf = function() {
    $uibModal.open({
      templateUrl: 'partial/share/share.pdf.html',
      controller: 'ShareModalPdf'
    });

    return false;
  };
});