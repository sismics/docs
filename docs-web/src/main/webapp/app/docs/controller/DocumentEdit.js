'use strict';

/**
 * Document edition controller.
 */
App.controller('DocumentEdit', function($rootScope, $scope, $q, $http, $state, $stateParams, Restangular, Tag) {
  // Alerts
  $scope.alerts = [];
  
  /**
   * Close an alert.
   */
  $scope.closeAlert = function(index) {
    $scope.alerts.splice(index, 1);
  };
  
  /**
   * Returns a promise for typeahead title.
   */
  $scope.getTitleTypeahead = function($viewValue) {
    var deferred = $q.defer();
    Restangular.one('document')
    .getList('list', {
      limit: 5,
      sort_column: 1,
      asc: true,
      search: $viewValue
    }).then(function(data) {
      deferred.resolve(_.uniq(_.pluck(data.documents, 'title'), true));
    });
    return deferred.promise;
  };
  
  /**
   * Returns true if in edit mode (false in add mode).
   */
  $scope.isEdit = function() {
    return $stateParams.id;
  };

  /**
   * Reset the form to add a new document.
   */
  $scope.resetForm = function() {
    $scope.document = {
      tags: [],
      language: 'fra'
    };
    $scope.newFiles = [];
  };

  /**
   * Edit a document.
   */
  $scope.edit = function() {
    var promise = null;
    var document = angular.copy($scope.document);
    
    // Transform date to timestamp
    if (document.create_date instanceof Date) {
      document.create_date = document.create_date.getTime();
    }
    
    // Extract ids from tags
    document.tags = _.pluck(document.tags, 'id');
    
    if ($scope.isEdit()) {
      promise = Restangular
      .one('document', $stateParams.id)
      .post('', document);
    } else {
      promise = Restangular
      .one('document')
      .put(document);
    }
    
    // Upload files after edition
    promise.then(function(data) {
      $scope.fileProgress = 0;
      
      // When all files upload are over, move on
      var navigateNext = function() {
        if ($scope.isEdit()) {
          // Go back to the edited document
          $scope.pageDocuments();
          $state.transitionTo('document.view', { id: $stateParams.id });
        } else {
          // Reset the scope and stay here
          var fileUploadCount = _.size($scope.newFiles);
          $scope.alerts.unshift({
            type: 'success',
            msg: 'Document successfully added (with ' + fileUploadCount + ' file' + (fileUploadCount > 1 ? 's' :  '') + ')'
          });
          $scope.resetForm();
          $scope.loadDocuments();
        }
      }
      
      if (_.size($scope.newFiles) == 0) {
        navigateNext();
      } else {
        $scope.fileIsUploading = true;
        $rootScope.pageTitle = '0% - Sismics Docs';
        
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
              headers: { 'Content-Type': undefined },
              transformRequest: function(data) { return data; }
            });
          
          // TODO Handle progression when $q.notify will be released
          
          promiseFile.then(function() {
            $scope.fileProgress += 100 / _.size($scope.newFiles);
            $rootScope.pageTitle = Math.round($scope.fileProgress) + '% - Sismics Docs';
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
            $scope.fileProgress = 0;
            $rootScope.pageTitle = 'Sismics Docs';
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

  /**
   * In edit mode, load the current document.
   */
  if ($scope.isEdit()) {
    Restangular.one('document', $stateParams.id).get().then(function(data) {
      $scope.document = data;
    });
  } else {
    $scope.resetForm();
  }
});