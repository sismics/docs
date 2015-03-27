'use strict';

/**
 * Document default controller.
 */
angular.module('docs').controller('DocumentDefault', function($scope, $state, Restangular, $upload) {
  // Load app data
  Restangular.one('app').get().then(function(data) {
    $scope.app = data;
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
      for (var i = 0; i < files.length; i++) {
        var file = files[i];
        $scope.uploadFile(file);
      }
    }
  };

  /**
   * Uppload a file.
   * @param file
   */
  $scope.uploadFile = function(file) {
    // Add the uploading file to the UI
    var newfile = {
      progress: 0,
      name: file.name,
      create_date: new Date().getTime(),
      mimetype: file.type
    };
    $scope.files.push(newfile);

    // Upload the file
    $upload.upload({
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
  $scope.deleteFile = function (file) {
    Restangular.one('file', file.id).remove().then(function () {
      $scope.loadFiles();
    });
  };
});