'use strict';

/**
 * Tag selection directive.
 */
angular.module('docs').directive('selectTag', function() {
  return {
    restrict: 'E',
    templateUrl: 'partial/docs/directive.selecttag.html',
    replace: true,
    scope: {
      tags: '=',
      ref: '@',
      ngDisabled: '='
    },
    controller: function($scope, Restangular) {
      // Retrieve tags
      Restangular.one('tag/list').get().then(function(data) {
        $scope.allTags = data.tags;
      });
      
      /**
       * Add a tag.
       */
      $scope.addTag = function($event) {
        // Does the new tag exists
        var tag = _.find($scope.allTags, function(tag) {
          if (tag.name === $scope.input) {
            return tag;
          }
        });
        
        // Does the new tag is already in the model
        var duplicate = _.find($scope.tags, function(tag2) {
          if (tag && tag2.id === tag.id) {
            return tag2;
          }
        });
        
        // Add the new tag
        if (tag) {
          if (!duplicate) {
            if (!$scope.tags) $scope.tags = [];
            $scope.tags.push(tag);
          }
          $scope.input = '';
        }
        
        if ($event) {
          $event.preventDefault();
        }
      };
      
      /**
       * Remove a tag.
       */
      $scope.deleteTag = function(deleteTag) {
        $scope.tags = _.reject($scope.tags, function(tag) {
          return tag.id === deleteTag.id;
        })
      };
    },
    link: function(scope, element, attr, ctrl) {
    }
  }
});