'use strict';

/**
 * File view controller.
 */
App.controller('FileView', function($dialog, $state, $stateParams) {
  var dialog = $dialog.dialog({
    keyboard: true,
    templateUrl: 'partial/file.view.html',
    controller: function($rootScope, $scope, $state, $stateParams) {
      $scope.id = $stateParams.fileId;
      
      // Search current file
      _.each($rootScope.files, function(value, key, list) {
        if (value.id == $scope.id) {
          $scope.file = value;
        }
      });
      
      /**
       * Navigate to the next file.
       */
      $scope.nextFile = function() {
        _.each($rootScope.files, function(value, key, list) {
          if (value.id == $scope.id) {
            var next = $rootScope.files[key + 1];
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
        _.each($rootScope.files, function(value, key, list) {
          if (value.id == $scope.id) {
            var previous = $rootScope.files[key - 1];
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
    }
  });
  
  dialog.open().then(function(result) {
    if (result == null) {
      $state.transitionTo('document.view', { id: $stateParams.id });
    }
  });
});