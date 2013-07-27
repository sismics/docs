'use strict';

/**
 * Document controller.
 */
App.controller('Document', function($scope, $state, Restangular) {
  /**
   * Load documents.
   */
  $scope.loadDocuments = function() {
    Restangular.one('document')
    .getList('list', {
      offset: 0,
      limit: 30
    })
    .then(function(data) {
      $scope.documents = data.documents;
    });
  };
  
  /**
   * Go to add document form.
   */
  $scope.addDocument = function() {
    $state.transitionTo('document.add');
  };
  
  $scope.viewDocument = function(id) {
    $state.transitionTo('document.view', { id: id });
  };
  
  // Initial documents loading
  $scope.loadDocuments();
});