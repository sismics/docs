'use strict';

/**
 * Tag controller.
 */
App.controller('Tag', function($scope, $dialog, $state, Tag, Restangular) {
  // Retrieve tags
  Tag.tags().then(function(data) {
    $scope.tags = data.tags;
  });
  
  /**
   * Add a tag.
   */
  $scope.addTag = function() {
    var name = $scope.tag.name;
    $scope.tag.name = '';
    // TODO Check if the tag don't already exists
    Restangular.one('tag').put({ name: name }).then(function(data) {
      $scope.tags.push({ id: data.id, name: name });
    });
  };
  
  /**
   * Delete a tag.
   */
  $scope.deleteTag = function(tag) {
    var title = 'Delete tag';
    var msg = 'Do you really want to delete this tag?';
    var btns = [{result:'cancel', label: 'Cancel'}, {result:'ok', label: 'OK', cssClass: 'btn-primary'}];

    $dialog.messageBox(title, msg, btns)
    .open()
    .then(function(result) {
      if (result == 'ok') {
        Restangular.one('tag', tag.id).remove().then(function() {
          $scope.tags = _.reject($scope.tags, function(t) {
            return tag.id == t.id;
          });
        });
      }
    });
  };
  
  /**
   * Update a tag name.
   */
  $scope.updateTag = function(tag) {
    Restangular.one('tag', tag.id).post('', tag);
  };
});