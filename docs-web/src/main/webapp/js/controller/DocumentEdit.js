'use strict';

/**
 * Document edition controller.
 */
App.controller('DocumentEdit', function($scope, $http, $state, $stateParams, Restangular) {
  /**
   * Returns true if in edit mode (false in add mode).
   */
  $scope.isEdit = function() {
    return $stateParams.id;
  };
  
  /**
   * In edit mode, load the current document.
   */
  if ($scope.isEdit()) {
    Restangular.one('document', $stateParams.id).get().then(function(data) {
      $scope.document = data;
    });
  }
  
  /**
   * Edit a document.
   */
  $scope.edit = function() {
    var promise = null;
    
    if ($scope.isEdit()) {
      promise = Restangular
        .one('document', $stateParams.id)
        .post('', $scope.document);
      promise.then(function(data) {
          $scope.loadDocuments();
         $state.transitionTo('document.view', { id: $stateParams.id });
        })
    } else {
      promise = Restangular
        .one('document')
        .put($scope.document);
      promise.then(function(data) {
          $scope.document = {};
          $scope.loadDocuments();
        });
    }
    
    // Upload files after edition
    // TODO Handle file upload progression and errors
    promise.then(function(data) {
      _.each($scope.files, function(file) {
        var formData = new FormData();
        formData.append('id', data.id);
        formData.append('file', file);
        $.ajax({
          url: 'api/file',
          type: 'PUT',
          data: formData,
          processData: false,
          contentType: false
        });
      });
    });
  };
  
  /**
   * Cancel edition.
   */
  $scope.cancel = function() {
    if ($scope.isEdit()) {
      $state.transitionTo('document.view', { id: $stateParams.id });
    } else {
      $state.transitionTo('document.default');
    }
  };
});