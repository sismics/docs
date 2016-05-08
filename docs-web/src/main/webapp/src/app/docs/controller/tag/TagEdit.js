'use strict';

/**
 * Tag edit controller.
 */
angular.module('docs').controller('TagEdit', function($scope, $stateParams, Restangular) {
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
});