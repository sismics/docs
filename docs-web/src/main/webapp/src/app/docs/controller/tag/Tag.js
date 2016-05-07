'use strict';

/**
 * Tag controller.
 */
angular.module('docs').controller('Tag', function($scope, $dialog, Restangular) {
  $scope.tag = { name: '', color: '#3a87ad' };

  // Retrieve tags
  Restangular.one('tag/list').get().then(function(data) {
    $scope.tags = data.tags;
  });
  
  /**
   * Add a tag.
   */
  $scope.addTag = function() {
    Restangular.one('tag').put($scope.tag).then(function(data) {
      $scope.tags.push({ id: data.id, name: $scope.tag.name, color: $scope.tag.color });
      $scope.tag = { name: '', color: '#3a87ad' };
    });
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
          $scope.tags = _.reject($scope.tags, function(t) {
            return tag.id == t.id;
          });
        });
      }
    });
  };
  
  /**
   * Update a tag.
   */
  $scope.updateTag = function(tag) {
    // Update the server
    return Restangular.one('tag', tag.id).post('', tag);
  };
});