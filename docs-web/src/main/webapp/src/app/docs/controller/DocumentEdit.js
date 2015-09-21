'use strict';

/**
 * Document edition controller.
 */
angular.module('docs').controller('DocumentEdit', function($rootScope, $scope, $q, $http, $state, $stateParams, Restangular) {
  // Alerts
  $scope.alerts = [];

  // Orphan files to add
  $scope.orphanFiles = $stateParams.files ? $stateParams.files.split(',') : [];

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
   * Workflow:
   * Edit/add the file -> upload local files -> attach orphan files -> redirect to edited document or stay if adding
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
      promise = Restangular.one('document', $stateParams.id).post('', document);
    } else {
      promise = Restangular.one('document').put(document);
    }

    // Attach orphan files after edition
    var attachOrphanFiles = function(data) {
      var promises = [];
      _.each($scope.orphanFiles, function(fileId) {
        promises.push(Restangular.one('file/' + fileId).post('', { id: data.id }));
      });
      $scope.orphanFiles = [];
      return $q.all(promises);
    };
    
    // Upload files after edition
    promise.then(function(data) {
      $scope.fileProgress = 0;
      
      // When all files upload are over, attach orphan files and move on
      var navigateNext = function() {
        attachOrphanFiles(data).then(function(resolve) {
          if ($scope.isEdit()) {
            // Go back to the edited document
            $scope.pageDocuments();
            $state.go('document.view', { id: $stateParams.id });
          } else {
            // Reset the scope and stay here
            var fileUploadCount = _.size($scope.newFiles) + resolve.length;
            $scope.alerts.unshift({
              type: 'success',
              msg: 'Document successfully added (with ' + fileUploadCount + ' file' + (fileUploadCount > 1 ? 's' :  '') + ')'
            });

            $scope.resetForm();
            $scope.loadDocuments();
          }
        });
      };
      
      if (_.size($scope.newFiles) == 0) {
        navigateNext();
      } else {
        $scope.fileIsUploading = true;
        $rootScope.pageTitle = '0% - Sismics Docs';
        
        // Send a file from the input file array and return a promise
        var sendFile = function(key) {
          var deferred = $q.defer();
          var getProgressListener = function(deferred) {
            return function(event) {
              deferred.notify(event);
            };
          };

          // Build the payload
          var file = $scope.newFiles[key];
          var formData = new FormData();
          formData.append('id', data.id);
          formData.append('file', file);

          // Send the file
          $.ajax({
            type: 'PUT',
            url: '../api/file',
            data: formData,
            cache: false,
            contentType: false,
            processData: false,
            success: function(response) {
              deferred.resolve(response);
            },
            error: function(jqXHR, textStatus, errorThrown) {
              deferred.reject(errorThrown);
            },
            xhr: function() {
              var myXhr = $.ajaxSettings.xhr();
              myXhr.upload.addEventListener(
                  'progress', getProgressListener(deferred), false);
              return myXhr;
            }
          });

          // Update progress bar and title on progress
          var startProgress = $scope.fileProgress;
          deferred.promise.then(null, null, function(e) {
            var done = 1 - (e.total - e.position) / e.total;
            var chunk = 100 / _.size($scope.newFiles);
            $scope.fileProgress = startProgress + done * chunk;
            $rootScope.pageTitle = Math.round($scope.fileProgress) + '% - Sismics Docs';
          });

          return deferred.promise;
        };
        
        // Upload files sequentially
        var key = 0;
        var then = function() {
          key++;
          if ($scope.newFiles[key]) {
            sendFile(key).then(then); // TODO Handle upload error
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
      $state.go('document.view', { id: $stateParams.id });
    } else {
      $state.go('document.default');
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