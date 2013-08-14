'use strict';

/**
 * Share controller.
 */
App.controller('Share', function($scope, $state, $stateParams, Restangular) {
  // Load document
  Restangular.one('document', $stateParams.documentId).get({ share: $stateParams.shareId })
      .then(function (data) {
        $scope.document = data;
      }, function (response) {
        if (response.data.status == 403) {
          // TODO Sharing no more valid
        }
      });

  // Load files
  Restangular.one('file').getList('list', { id: $stateParams.documentId, share: $stateParams.shareId })
      .then(function (data) {
        $scope.files = data.files;
      });
});