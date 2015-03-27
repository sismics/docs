'use strict';

/**
 * File view controller.
 */
angular.module('docs').controller('FileView', function($modal, $state, $stateParams) {
  var modal = $modal.open({
    windowClass: 'modal modal-fileview',
    templateUrl: 'partial/docs/file.view.html',
    controller: 'FileModalView'
  });

  // Returns to document view on file close
  modal.closed = false;
  modal.result.then(function() {
    modal.closed = true;
  }, function() {
    modal.closed = true;
    if ($stateParams.id) {
      $state.transitionTo('document.view', { id: $stateParams.id });
    } else {
      $state.transitionTo('document.default');
    }
  });
});