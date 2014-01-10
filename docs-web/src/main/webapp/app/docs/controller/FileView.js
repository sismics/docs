'use strict';

/**
 * File view controller.
 */
App.controller('FileView', function($modal, $state, $stateParams) {
  var modal = $modal.open({
    windowClass: 'modal modal-fileview',
    templateUrl: 'partial/docs/file.view.html',
    controller: function($scope, $state, $stateParams, Restangular, dialog) {
      // Load files
      Restangular.one('file').getList('list', { id: $stateParams.id }).then(function(data) {
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
              $state.transitionTo('document.view.file', { id: $stateParams.id, fileId: next.id });
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
              $state.transitionTo('document.view.file', { id: $stateParams.id, fileId: previous.id });
            }
          }
        });
      };
      
      /**
       * Open the file in a new window.
       */
      $scope.openFile = function() {
        window.open('api/file/' + $stateParams.fileId + '/data');
      };

      /**
       * Close the file preview.
       */
      $scope.closeFile = function () {
        dialog.close();
      };

      // Close the modal when the user exits this state
      var off = $scope.$on('$stateChangeStart', function(event, toState){
        if (dialog.isOpen()) {
          dialog.close(toState.name == 'document.view.file' ? {} : null);
        }
        off();
      });
    }
  });

  // Returns to document view on file close
  modal.result.then(function(result) {
    if (result == null) {
      $state.transitionTo('document.view', { id: $stateParams.id });
    }
  });
});