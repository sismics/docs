'use strict';

/**
 * Document view controller.
 */
App.controller('DocumentView', function ($scope, $state, $stateParams, $location, $dialog, $modal, Restangular) {
  // Load data from server
  Restangular.one('document', $stateParams.id).get().then(function(data) {
    $scope.document = data;
  });

  /**
   * Configuration for file sorting.
   */
  $scope.fileSortableOptions = {
    forceHelperSize: true,
    forcePlaceholderSize: true,
    tolerance: 'pointer',
    handle: '.handle',
    stop: function (e, ui) {
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
      controller: function ($scope, $modalInstance) {
        $scope.name = '';
        $scope.close = function (name) {
          $modalInstance.close(name);
        }
      }
    }).result.then(function (name) {
          if (name == null) {
            return;
          }

          // Share the document
          Restangular.one('share').put({
            name: name,
            id: $stateParams.id
          }).then(function (data) {
                var share = {
                  name: name,
                  id: data.id
                };

                // Display the new share and add it to the local shares
                $scope.showShare(share);
                $scope.document.shares.push(share);
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
          $scope.document.shares = _.reject($scope.document.shares, function(s) {
            return share.id == s.id;
          });
        });
      }
    });
  };
});