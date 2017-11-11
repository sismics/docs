'use strict';

/**
 * File view controller.
 */
angular.module('docs').controller('FileView', function($uibModal, $state, $stateParams) {
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
    $state.go('^', { id: $stateParams.id });
  });
});