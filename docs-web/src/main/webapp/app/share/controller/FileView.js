'use strict';

/**
 * File view controller.
 */
App.controller('FileView', function($modal, $state, $stateParams) {
  var modal = $modal.open({
    windowClass: 'modal modal-fileview',
    templateUrl: 'partial/share/file.view.html',
    controller: function($rootScope, $scope, $state, $stateParams, Restangular) {
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
       * Print the file.
       */
      $scope.printFile = function() {
        var popup = window.open('api/file/' + $stateParams.fileId + '/data', '_blank');
        popup.onload = function () {
          popup.print();
          popup.close();
        }
      };

      /**
       * Close the file preview.
       */
      $scope.closeFile = function () {
        modal.dismiss();
      };

      // Close the modal when the user exits this state
      var off = $rootScope.$on('$stateChangeStart', function(event, toState){
        if (!modal.closed) {
          if (toState.name == 'share.file') {
            modal.close();
          } else {
            modal.dismiss();
          }
        }
        off();
      });
    }
  });

  // Returns to share view on file close
  modal.closed = false;
  modal.result.then(function() {
    modal.closed = true;
  },function(result) {
    modal.closed = true;
    $state.transitionTo('share', { documentId: $stateParams.documentId, shareId: $stateParams.shareId });
  });
});