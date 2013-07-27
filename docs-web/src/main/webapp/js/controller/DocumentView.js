'use strict';

/**
 * Document view controller.
 */
App.controller('DocumentView', function($rootScope, $scope, $state, $stateParams, Restangular) {
  // Load data from server
  $scope.document = Restangular.one('document', $stateParams.id).get();
  Restangular.one('file').getList('list', { id: $stateParams.id }).then(function(data) {
    $rootScope.files = data.files;
  });
  
  /**
   * Navigate to the selected file.
   */
  $scope.openFile = function(file) {
    $state.transitionTo('document.view.file', { id: $stateParams.id, fileId: file.id })
  }
});