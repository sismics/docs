'use strict';

/**
 * Tag controller.
 */
angular.module('docs').controller('Tag', function($scope, Restangular, $state) {
  $scope.tag = { name: '', color: '#3a87ad' };

  // Retrieve tags
  $scope.loadTags = function() {
    Restangular.one('tag/list').get().then(function(data) {
      $scope.tags = data.tags;
    });
  };
  $scope.loadTags();

  /**
   * Display a tag.
   */
  $scope.viewTag = function(id) {
    $state.go('tag.edit', { id: id });
  };
  
  /**
   * Add a tag.
   */
  $scope.addTag = function() {
    Restangular.one('tag').put($scope.tag).then(function(data) {
      $scope.tags.push({ id: data.id, name: $scope.tag.name, color: $scope.tag.color });
      $scope.tag = { name: '', color: '#3a87ad' };
    });
  };
});