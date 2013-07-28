'use strict';

/**
 * Document view controller.
 */
App.controller('DocumentView', function($rootScope, $scope, $state, $stateParams, $dialog, Restangular) {
  // Load data from server
  $scope.document = Restangular.one('document', $stateParams.id).get();
  
  /**
   * Load files from server.
   */
  $scope.loadFiles = function() {
    Restangular.one('file').getList('list', { id: $stateParams.id }).then(function(data) {
      $rootScope.files = data.files;
    });
  };
  $scope.loadFiles();
  
  /**
   * Navigate to the selected file.
   */
  $scope.openFile = function(file) {
    $state.transitionTo('document.view.file', { id: $stateParams.id, fileId: file.id })
  };
  
  /**
   * Delete a file.
   */
  $scope.deleteFile = function(file) {
    var title = 'Delete file';
    var msg = 'Do you really want to delete this file?';
    var btns = [{result:'cancel', label: 'Cancel'}, {result:'ok', label: 'OK', cssClass: 'btn-primary'}];

    $dialog.messageBox(title, msg, btns)
    .open()
    .then(function(result) {
      if (result == 'ok') {
        Restangular.one('file', file.id).remove().then(function() {
          $scope.loadFiles();
        });
      }
    });
  }
});