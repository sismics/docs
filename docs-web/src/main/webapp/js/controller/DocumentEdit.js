'use strict';

/**
 * Document edition controller.
 */
App.controller('DocumentEdit', function($scope, $state, $stateParams, Restangular) {
  /**
   * Returns true if in edit mode (false in add mode).
   */
  $scope.isEdit = function() {
    return $stateParams.id;
  };
  
  /**
   * Edit a document.
   */
  $scope.edit = function() {
    if ($scope.isEdit()) {
      // TODO
    } else {
      Restangular
        .one('document')
        .put($scope.document)
        .then(function() {
          $scope.document = {};
          $scope.loadDocuments();
        });
    }
  };
  
  /**
   * Cancel edition.
   */
  $scope.cancel = function() {
    $state.transitionTo('document');
  };
});