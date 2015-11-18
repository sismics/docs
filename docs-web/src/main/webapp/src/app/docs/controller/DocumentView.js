'use strict';

/**
 * Document view controller.
 */
angular.module('docs').controller('DocumentView', function ($scope, $state, $stateParams, $location, $dialog, $modal, Restangular, $timeout) {
  // Load document data from server
  Restangular.one('document', $stateParams.id).get().then(function(data) {
    $scope.document = data;
  }, function(response) {
    $scope.error = response;
  });

  // Load comments from server
  Restangular.one('comment', $stateParams.id).get().then(function(data) {
    $scope.comments = data.comments;
  }, function(response) {
    $scope.commentsError = response;
  });

  /**
   * Add a comment.
   */
  $scope.comment = '';
  $scope.addComment = function() {
    if ($scope.comment.length == 0) {
      return;
    }

    Restangular.one('comment').put({
      id: $stateParams.id,
      content: $scope.comment
    }).then(function(data) {
      $scope.comment = '';
      $scope.comments.push(data);
    });
  };

  /**
   * Delete a comment.
   */
  $scope.deleteComment = function(comment) {
    Restangular.one('comment', comment.id).remove().then(function() {
      $scope.comments.splice($scope.comments.indexOf(comment), 1);
    });
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
        Restangular.one('document', document.id).remove().then(function() {
          $scope.loadDocuments();
          $state.go('document.default');
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
                $scope.document.acls = angular.copy($scope.document.acls);
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
});