'use strict';

/**
 * Tag controller.
 */
App.controller('Tag', function($scope, $state, Tag, Restangular) {
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
    Restangular.one('tag').put({ name: name }).then(function(data) {
      $scope.tags.push({ id: data.id, name: name });
    });
  };
  
  /**
   * Delete a tag.
   */
  $scope.deleteTag = function(tag) {
    Restangular.one('tag', tag.id).remove().then(function() {
      $scope.tags = _.reject($scope.tags, function(t) {
        return tag.id == t.id;
      });
    });
  };
});