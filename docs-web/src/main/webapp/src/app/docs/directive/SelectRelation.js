'use strict';

/**
 * Relation selection directive.
 */
angular.module('docs').directive('selectRelation', function() {
  return {
    restrict: 'E',
    templateUrl: 'partial/docs/directive.selectrelation.html',
    replace: true,
    scope: {
      relations: '=',
      ref: '@',
      ngDisabled: '='
    },
    controller: function($scope, $q, Restangular) {
      /**
       * Add a relation.
       */
      $scope.addRelation = function($item) {
        // Does the new relation is already in the model
        var duplicate = _.find($scope.relations, function(relation) {
          if ($item.id == relation.id) {
            return relation;
          }
        });

        // Add the new relation
        if (!duplicate) {
          $scope.relations.push({
            id: $item.id,
            title: $item.title,
            source: true
          });
        }
        $scope.input = '';
      };
      
      /**
       * Remove a relation.
       */
      $scope.deleteRelation = function(deleteRelation) {
        $scope.relations = _.reject($scope.relations, function(relation) {
          return relation.id == deleteRelation.id;
        })
      };

      /**
       * Returns a promise for typeahead title.
       */
      $scope.getDocumentTypeahead = function($viewValue) {
        var deferred = $q.defer();
        Restangular.one('document')
            .getList('list', {
              limit: 5,
              sort_column: 1,
              asc: true,
              search: $viewValue
            }).then(function(data) {
          deferred.resolve(data.documents);
        });
        return deferred.promise;
      };
    },
    link: function(scope, element, attr, ctrl) {
    }
  }
});