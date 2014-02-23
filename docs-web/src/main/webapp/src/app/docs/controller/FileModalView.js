'use strict';

/**
 * File modal view controller.
 */
angular.module('docs').controller('FileModalView', function($rootScope, $modalInstance, $scope, $state, $stateParams, Restangular) {
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
          $state.transitionTo('document.view.file', { id: $stateParams.id, fileId: previous.id });
        }
      }
    });
  };

  /**
   * Open the file in a new window.
   */
  $scope.openFile = function() {
    window.open('../api/file/' + $stateParams.fileId + '/data');
  };

  /**
   * Print the file.
   */
  $scope.printFile = function() {
    var popup = window.open('../api/file/' + $stateParams.fileId + '/data', '_blank');
    popup.onload = function () {
      popup.print();
      popup.close();
    }
  };

  /**
   * Close the file preview.
   */
  $scope.closeFile = function () {
    $modalInstance.dismiss();
  };

  // Close the modal when the user exits this state
  var off = $rootScope.$on('$stateChangeStart', function(event, toState) {
    if (!$modalInstance.closed) {
      if (toState.name == 'document.view.file') {
        $modalInstance.close();
      } else {
        $modalInstance.dismiss();
      }
    }
    off();
  });
});