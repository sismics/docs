'use strict';

/**
 * Document view controller.
 */
angular.module('docs').controller('DocumentView', function ($scope, $state, $stateParams, $location, $dialog, $modal, Restangular, $upload, $q) {
  // Load data from server
  Restangular.one('document', $stateParams.id).get().then(function(data) {
    $scope.document = data;
  });

  // Watch for ACLs change and group them for easy displaying
  $scope.$watch('document.acls', function(acls) {
    $scope.acls = _.groupBy(acls, function(acl) {
      return acl.id;
    });
  });

  // Initialize add ACL
  $scope.acl = { perm: 'READ' };

  /**
   * Configuration for file sorting.
   */
  $scope.fileSortableOptions = {
    forceHelperSize: true,
    forcePlaceholderSize: true,
    tolerance: 'pointer',
    handle: '.handle',
    stop: function () {
      // Send new positions to server
      $scope.$apply(function () {
        Restangular.one('file').post('reorder', {
          id: $stateParams.id,
          order: _.pluck($scope.files, 'id')
        });
      });
    }
  };

  /**
   * Load files from server.
   */
  $scope.loadFiles = function () {
    Restangular.one('file').getList('list', { id: $stateParams.id }).then(function (data) {
      $scope.files = data.files;
      // TODO Keep currently uploading files
    });
  };
  $scope.loadFiles();

  /**
   * Navigate to the selected file.
   */
  $scope.openFile = function (file) {
    $state.transitionTo('document.view.file', { id: $stateParams.id, fileId: file.id })
  };

  /**
   * Delete a document.
   */
  $scope.deleteDocument = function (document) {
    var title = 'Delete document';
    var msg = 'Do you really want to delete this document?';
    var btns = [
      {result: 'cancel', label: 'Cancel'},
      {result: 'ok', label: 'OK', cssClass: 'btn-primary'}
    ];

    $dialog.messageBox(title, msg, btns, function (result) {
      if (result == 'ok') {
        Restangular.one('document', document.id).remove().then(function () {
          $scope.loadDocuments();
          $state.transitionTo('document.default');
        });
      }
    });
  };

  /**
   * Delete a file.
   */
  $scope.deleteFile = function (file) {
    var title = 'Delete file';
    var msg = 'Do you really want to delete this file?';
    var btns = [
      {result: 'cancel', label: 'Cancel'},
      {result: 'ok', label: 'OK', cssClass: 'btn-primary'}
    ];

    $dialog.messageBox(title, msg, btns, function (result) {
      if (result == 'ok') {
        Restangular.one('file', file.id).remove().then(function () {
          $scope.loadFiles();
        });
      }
    });
  };

  /**
   * Open the share dialog.
   */
  $scope.share = function () {
    $modal.open({
      templateUrl: 'partial/docs/document.share.html',
      controller: 'DocumentModalShare'
    }).result.then(function (name) {
          if (name == null) {
            return;
          }

          // Share the document
          Restangular.one('share').put({
            name: name,
            id: $stateParams.id
          }).then(function (acl) {
                // Display the new share ACL and add it to the local ACLs
                $scope.showShare(acl);
                $scope.document.acls.push(acl);
              })
        });
  };

  /**
   * Display a share.
   */
  $scope.showShare = function(share) {
    // Show the link
    var link = $location.absUrl().replace($location.path(), '').replace('#', '') + 'share.html#/share/' + $stateParams.id + '/' + share.id;
    var title = 'Shared document';
    var msg = 'You can share this document by giving this link. ' +
        'Note that everyone having this link can see the document.<br/>' +
        '<input class="form-control share-link" type="text" readonly="readonly" value="' + link + '" />';
    var btns = [
      {result: 'unshare', label: 'Unshare', cssClass: 'btn-danger'},
      {result: 'close', label: 'Close'}
    ];

    $dialog.messageBox(title, msg, btns, function (result) {
      if (result == 'unshare') {
        // Unshare this document and update the local shares
        Restangular.one('share', share.id).remove().then(function () {
          $scope.document.acls = _.reject($scope.document.acls, function(s) {
            return share.id == s.id;
          });
        });
      }
    });
  };

  /**
   * File has been drag & dropped.
   * @param files
   */
  $scope.fileDropped = function(files) {
    if (!$scope.document.writable) {
      return;
    }

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
      file: file,
      fields: {
        id: $stateParams.id
      }
    })
        .progress(function (e) {
          newfile.progress = parseInt(100.0 * e.loaded / e.total);
        })
        .success(function (data) {
          newfile.id = data.id;
        });
  };

  /**
   * Delete an ACL.
   * @param acl
   */
  $scope.deleteAcl = function(acl) {
    Restangular.one('acl/' + $stateParams.id + '/' + acl.perm + '/' + acl.id, null).remove().then(function () {
      $scope.document.acls = _.reject($scope.document.acls, function(s) {
        return angular.equals(acl, s);
      });
    });
  };

  /**
   * Add an ACL.
   */
  $scope.addAcl = function() {
    $scope.acl.source = $stateParams.id;
    Restangular.one('acl').put($scope.acl).then(function(acl) {
      $scope.acl = { perm: 'READ' };
      if (_.isUndefined(acl.id)) {
        return;
      }
      $scope.document.acls.push(acl);
      $scope.document.acls = angular.copy($scope.document.acls);
    });
  };

  $scope.getTargetAclTypeahead = function($viewValue) {
    var deferred = $q.defer();
    Restangular.one('acl/target/search')
        .get({
          search: $viewValue
        }).then(function(data) {
          deferred.resolve(_.pluck(data.users, 'username'), true);
        });
    return deferred.promise;
  };
});