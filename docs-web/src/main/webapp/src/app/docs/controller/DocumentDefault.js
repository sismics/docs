'use strict';

/**
 * Document default controller.
 */
angular.module('docs').controller('DocumentDefault', function($scope, $state, Restangular, $upload) {
  // Load app data
  Restangular.one('app').get().then(function(data) {
    $scope.app = data;
  });

  // Load user audit log
  Restangular.one('auditlog').get().then(function(data) {
    $scope.logs = data.logs;
  });

  /**
   * Load unlinked files.
   */
  $scope.loadFiles = function() {
    Restangular.one('file').getList('list').then(function (data) {
      $scope.files = data.files;
      // TODO Keep currently uploading files
    });
  };
  $scope.loadFiles();

  /**
   * File has been drag & dropped.
   * @param files
   */
  $scope.fileDropped = function(files) {
    if (files && files.length) {
      // Adding files to the UI
      var newfiles = [];
      _.each(files, function(file) {
        var newfile = {
          progress: 0,
          name: file.name,
          create_date: new Date().getTime(),
          mimetype: file.type,
          status: 'Pending...'
        };
        $scope.files.push(newfile);
        newfiles.push(newfile);
      });

      // Uploading files sequentially
      var key = 0;
      var then = function() {
        if (files[key]) {
          $scope.uploadFile(files[key], newfiles[key++]).then(then);
        }
      };
      then();
    }
  };

  /**
   * Upload a file.
   * @param file
   * @param newfile
   */
  $scope.uploadFile = function(file, newfile) {
    // Upload the file
    newfile.status = 'Uploading...';
    return $upload.upload({
      method: 'PUT',
      url: '../api/file',
      file: file
    })
        .progress(function (e) {
          newfile.progress = parseInt(100.0 * e.loaded / e.total);
        })
        .success(function (data) {
          newfile.id = data.id;
        });
  };

  /**
   * Navigate to the selected file.
   */
  $scope.openFile = function (file) {
    $state.transitionTo('document.default.file', { fileId: file.id })
  };

  /**
   * Delete a file.
   */
  $scope.deleteFile = function ($event, file) {
    $event.stopPropagation();

    Restangular.one('file', file.id).remove().then(function () {
      $scope.loadFiles();
    });
    return false;
  };

  /**
   * Returns checked files.
   */
  $scope.checkedFiles = function() {
    return _.where($scope.files, { checked: true });
  };

  /**
   * Add a document with checked files.
   */
  $scope.addDocument = function() {
    $state.transitionTo('document.add', { files: _.pluck($scope.checkedFiles(), 'id') });
  };
});