'use strict';

/**
 * File view controller.
 */
angular.module('share').controller('FileView', function($uibModal, $state, $stateParams) {
  var modal = $uibModal.open({
    windowClass: 'modal modal-fileview',
    templateUrl: 'partial/share/file.view.html',
    controller: 'FileModalView',
    size: 'lg'
  });

  // Returns to share view on file close
  modal.closed = false;
  modal.result.then(function() {
    modal.closed = true;
  },function() {
    modal.closed = true;
    $state.go('share', { documentId: $stateParams.documentId, shareId: $stateParams.shareId });
  });
});