'use strict';

/**
 * Tag edit controller.
 */
angular.module('docs').controller('TagEdit', function($scope, $stateParams, Restangular, $dialog, $state) {
  // Retrieve the tag
  Restangular.one('tag', $stateParams.id).get().then(function(data) {
    $scope.tag = data;

    // Replace the tag from the list with this reference
    _.each($scope.tags, function(tag, i) {
      if (tag.id == $scope.tag.id) {
        $scope.tags[i] = $scope.tag;
      }
    });
  });

  /**
   * Update a tag.
   */
  $scope.edit = function() {
    // Update the server
    Restangular.one('tag', $scope.tag.id).post('', $scope.tag);
  };

  /**
   * Delete a tag.
   */
  $scope.deleteTag = function(tag) {
    var title = 'Delete tag';
    var msg = 'Do you really want to delete this tag?';
    var btns = [
      {result: 'cancel', label: 'Cancel'},
      {result: 'ok', label: 'OK', cssClass: 'btn-primary'}
    ];

    $dialog.messageBox(title, msg, btns, function(result) {
      if (result == 'ok') {
        Restangular.one('tag', tag.id).remove().then(function() {
          $scope.loadTags();
          $state.go('tag.default');
        });
      }
    });
  };
});