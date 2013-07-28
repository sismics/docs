'use strict';

/**
 * Document edition controller.
 */
App.controller('DocumentEdit', function($scope, $q, $http, $state, $stateParams, Restangular) {
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
    } else {
      promise = Restangular
      .one('document')
      .put($scope.document);
    }
    
    // Upload files after edition
    promise.then(function(data) {
      var promises = [];
      $scope.fileProgress = 0;
      
      // When all files upload are over, move on
      var navigateNext = function() {
        if ($scope.isEdit()) {
          $scope.loadDocuments();
          $state.transitionTo('document.view', { id: $stateParams.id });
        } else {
          $scope.document = {};
          $scope.loadDocuments();
        }
      }
      
      if (_.size($scope.newFiles) == 0) {
        navigateNext();
      } else {
        $scope.fileIsUploading = true;
        
        // Send a file from the input file array and return a promise
        var sendFile = function(key) {
          // Build the payload
          var file = $scope.newFiles[key];
          var formData = new FormData();
          formData.append('id', data.id);
          formData.append('file', file);
          
          // Send the file
          var promiseFile = $http.put('api/file',
            formData, {
            headers: { 'Content-Type': false },
            transformRequest: function(data) { return data; }
          });
          
          // TODO Handle progression when $q.notify will be released
          
          promiseFile.then(function() {
            $scope.fileProgress += 100 / _.size($scope.newFiles);
          });
          
          return promiseFile;
        };
        
        // Upload files sequentially
        var key = 0;
        var then = function() {
          key++;
          if ($scope.newFiles[key]) {
            sendFile(key).then(then);
          } else {
            $scope.fileIsUploading = false;
            navigateNext();
          }
        };
        sendFile(key).then(then);
      }
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