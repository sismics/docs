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
      id: '=',
      relations: '=',
      ref: '@',
      ngDisabled: '='
    },
    controller: function($scope, $q, Restangular) {
      /**
       * Add a relation.
       */
      $scope.addRelation = function($item) {
        // Add the new relation
        $scope.relations.push({
          id: $item.id,
          title: $item.title,
          source: true
        });
        $scope.input = '';
      };
      
      /**
       * Remove a relation.
       */
      $scope.deleteRelation = function(deleteRelation) {
        $scope.relations = _.reject($scope.relations, function(relation) {
          return relation.id === deleteRelation.id;
        });
      };

      /**
       * Returns a promise for typeahead document.
       */
      $scope.getDocumentTypeahead = function($viewValue) {
        var deferred = $q.defer();
        Restangular.one('document/list')
            .get({
              limit: 5,
              sort_column: 1,
              asc: true,
              search: $viewValue
            }).then(function(data) {
              deferred.resolve(_.reject(data.documents, function(document) {
                var duplicate = _.find($scope.relations, function(relation) {
                  if (document.id === relation.id) {
                    return relation;
                  }
                });

                return document.id === $scope.id || duplicate;
              }));
            });
        return deferred.promise;
      };
    },
    link: function(scope, element, attr, ctrl) {
    }
  }
});