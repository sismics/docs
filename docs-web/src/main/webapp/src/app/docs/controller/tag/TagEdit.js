'use strict';

/**
 * Tag edit controller.
 */
angular.module('docs').controller('TagEdit', function($scope, $stateParams, Restangular, $dialog, $state, $translate) {
  // Retrieve the tag
  Restangular.one('tag', $stateParams.id).get().then(function(data) {
    $scope.tag = data;

    // Replace the tag from the list with this reference
    _.each($scope.tags, function(tag, i) {
      if (tag.id === $scope.tag.id) {
        $scope.tags[i] = $scope.tag;
      }
    });
  });

  /**
   * Update a tag.
   */
  $scope.edit = function() {
    // Update the server
    Restangular.one('tag', $scope.tag.id).post('', $scope.tag).then(function () {
    }, function (e) {
      if (e.data.type === 'CircularReference') {
        var title = $translate.instant('tag.edit.circular_reference_title');
        var msg = $translate.instant('tag.edit.circular_reference_message');
        var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
        $dialog.messageBox(title, msg, btns);
      }
    });
  };

  /**
   * Delete a tag.
   */
  $scope.deleteTag = function(tag) {
    var title = $translate.instant('tag.edit.delete_tag_title');
    var msg = $translate.instant('tag.edit.delete_tag_message');
    var btns = [
      {result: 'cancel', label: $translate.instant('cancel')},
      {result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}
    ];

    $dialog.messageBox(title, msg, btns, function(result) {
      if (result === 'ok') {
        Restangular.one('tag', tag.id).remove().then(function() {
          $scope.loadTags();
          $state.go('tag.default');
        });
      }
    });
  };
});