'use strict';

/**
 * File view controller.
 */
App.controller('FileView', function($dialog, $state, $stateParams) {
  var dialog = $dialog.dialog({
    keyboard: true,
    templateUrl: 'partial/file.view.html',
    controller: function($scope, $state, $stateParams, Restangular, dialog) {
      $scope.id = $stateParams.fileId;
      
      // Load files
      Restangular.one('file').getList('list', { id: $stateParams.id }).then(function(data) {
        $scope.files = data.files;
        
        // Search current file
        _.each($scope.files, function(value, key, list) {
          if (value.id == $scope.id) {
            $scope.file = value;
          }
        });
      });
      
      /**
       * Navigate to the next file.
       */
      $scope.nextFile = function() {
        _.each($scope.files, function(value, key, list) {
          if (value.id == $scope.id) {
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
        _.each($scope.files, function(value, key, list) {
          if (value.id == $scope.id) {
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
        window.open('api/file/' + $scope.id + '/data');
      };

      /**
       * Close the file preview.
       */
      $scope.closeFile = function () {
        dialog.close();
      };
    }
  });

  // Returns to document view on file close
  dialog.open().then(function(result) {
    if (result == null) {
      $state.transitionTo('document.view', { id: $stateParams.id });
    }
  });
});