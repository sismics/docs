'use strict';

/**
 * File view controller.
 */
angular.module('docs').controller('FileView', function($uibModal, $state, $stateParams, $timeout) {
  var modal = $uibModal.open({
    windowClass: 'modal modal-fileview',
    templateUrl: 'partial/docs/file.view.html',
    controller: 'FileModalView',
    size: 'lg'
  });

  // Returns to document view on file close
  modal.closed = false;
  modal.result.then(function() {
    modal.closed = true;
  }, function() {
    modal.closed = true;
    $timeout(function () {
      // After all router transitions are passed,
      // if we are still on the file route, go back to the document
      if ($state.current.name === 'document.view.content.file' || $state.current.name === 'document.default.file') {
        $state.go('^', {id: $stateParams.id});
      }
    });
  });
});