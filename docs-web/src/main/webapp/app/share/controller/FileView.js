'use strict';

/**
 * File view controller.
 */
App.controller('FileView', function($dialog, $state, $stateParams) {
  var dialog = $dialog.dialog({
    keyboard: true,
    dialogClass: 'modal modal-fileview',
    templateUrl: 'partial/share/file.view.html',
    controller: function($scope, $state, $stateParams, Restangular, dialog) {
      // Load files
      Restangular.one('file').getList('list', { id: $stateParams.documentId, share: $stateParams.shareId }).then(function(data) {
        $scope.files = data.files;
        
        // Search current file
        _.each($scope.files, function(value) {
          if (value.id == $stateParams.fileId) {
            $scope.file = value;
          }
        });
      });
      
      /**
       * Navigate to the next file.
       */
      $scope.nextFile = function() {
        _.each($scope.files, function(value, key) {
          if (value.id == $stateParams.fileId) {
            var next = $scope.files[key + 1];
            if (next) {
              dialog.close({});
              $state.transitionTo('share.file', { documentId: $stateParams.documentId, shareId: $stateParams.shareId, fileId: next.id });
            }
          }
        });
      };
      
      /**
       * Navigate to the previous file.
       */
      $scope.previousFile = function() {
        _.each($scope.files, function(value, key) {
          if (value.id == $stateParams.fileId) {
            var previous = $scope.files[key - 1];
            if (previous) {
              dialog.close({});
              $state.transitionTo('share.file', { documentId: $stateParams.documentId, shareId: $stateParams.shareId,  fileId: previous.id });
            }
          }
        });
      };
      
      /**
       * Open the file in a new window.
       */
      $scope.openFile = function() {
        window.open('api/file/' + $stateParams.fileId + '/data?share=' + $stateParams.shareId);
      };

      /**
       * Close the file preview.
       */
      $scope.closeFile = function () {
        dialog.close();
      };

      // Close the dialog when the user exits this state
      var off = $scope.$on('$stateChangeStart', function(event, toState){
        if (dialog.isOpen()) {
          dialog.close(toState.name == 'share.file' ? {} : null);
        }
        off();
      });
    }
  });

  // Returns to share view on file close
  dialog.open().then(function(result) {
    if (result == null) {
      $state.transitionTo('share', { documentId: $stateParams.documentId, shareId: $stateParams.shareId });
    }
  });
});