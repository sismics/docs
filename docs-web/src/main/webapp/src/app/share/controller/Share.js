'use strict';

/**
 * Share controller.
 */
angular.module('share').controller('Share', function($scope, $state, $stateParams, Restangular) {
  // Load document
  Restangular.one('document', $stateParams.documentId).get({ share: $stateParams.shareId })
      .then(function (data) {
        $scope.document = data;
      }, function (response) {
        if (response.status == 403) {
          $state.transitionTo('403');
        }
      });

  // Load files
  Restangular.one('file').getList('list', { id: $stateParams.documentId, share: $stateParams.shareId })
      .then(function (data) {
        $scope.files = data.files;
      });

  /**
   * Navigate to the selected file.
   */
  $scope.openFile = function (file) {
    $state.transitionTo('share.file', { documentId: $stateParams.documentId, shareId: $stateParams.shareId, fileId: file.id })
  };
});