'use strict';

/**
 * File view controller.
 */
App.controller('FileView', function($rootScope, $state, $scope, $stateParams) {
  $scope.id = $stateParams.fileId;
  
  /**
   * Navigate to the next file.
   */
  $scope.nextFile = function() {
    _.each($rootScope.files, function(value, key, list) {
      if (value.id == $scope.id) {
        var next = $rootScope.files[key + 1];
        if (next) {
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
          $state.transitionTo('document.view.file', { id: $stateParams.id, fileId: previous.id });
        }
      }
    });
  };
});