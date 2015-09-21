'use strict';

/**
 * File view controller.
 */
angular.module('share').controller('FileView', function($modal, $state, $stateParams) {
  var modal = $modal.open({
    windowClass: 'modal modal-fileview',
    templateUrl: 'partial/share/file.view.html',
    controller: 'FileModalView'
  });

  // Returns to share view on file close
  modal.closed = false;
  modal.result.then(function() {
    modal.closed = true;
  },function(result) {
    modal.closed = true;
    $state.go('share', { documentId: $stateParams.documentId, shareId: $stateParams.shareId });
  });
});