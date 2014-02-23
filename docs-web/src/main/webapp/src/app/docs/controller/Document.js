'use strict';

/**
 * Document controller.
 */
angular.module('docs').controller('Document', function($scope, $timeout, $state, Restangular) {
  /**
   * Documents table sort status.
   */
  $scope.sortColumn = 3;
  $scope.asc = false;
  $scope.offset = 0;
  $scope.currentPage = 1;
  $scope.limit = 10;
  $scope.search = '';

  // A timeout promise is used to slow down search requests to the server
  // We keep track of it for cancellation purpose
  var timeoutPromise;
  
  /**
   * Load new documents page.
   */
  $scope.pageDocuments = function() {
    Restangular.one('document')
        .getList('list', {
          offset: $scope.offset,
          limit: $scope.limit,
          sort_column: $scope.sortColumn,
          asc: $scope.asc,
          search: $scope.search
        })
        .then(function (data) {
          $scope.documents = data.documents;
          $scope.totalDocuments = data.total;
        });
  };
  
  /**
   * Reload documents.
   */
  $scope.loadDocuments = function() {
    $scope.offset = 0;
    $scope.currentPage = 1;
    $scope.pageDocuments();
  };
  
  /**
   * Watch for current page change.
   */
  $scope.$watch('currentPage', function(prev, next) {
    if (prev == next) {
      return;
    }
    $scope.offset = ($scope.currentPage - 1) * $scope.limit;
    $scope.pageDocuments();
  });
  
  /**
   * Watch for search scope change.
   */
  $scope.$watch('search', function(prev, next) {
    if (timeoutPromise) {
      // Cancel previous timeout
      $timeout.cancel(timeoutPromise);
    }

    // Call API later
    timeoutPromise = $timeout(function () {
      $scope.loadDocuments();
    }, 200);
  }, true);
  
  /**
   * Sort documents.
   */
  $scope.sortDocuments = function(sortColumn) {
    if (sortColumn == $scope.sortColumn) {
      $scope.asc = !$scope.asc;
    } else {
      $scope.asc = true;
    }
    $scope.sortColumn = sortColumn;
    $scope.loadDocuments();
  };
  
  /**
   * Go to add document form.
   */
  $scope.addDocument = function() {
    $state.transitionTo('document.add');
  };
  
  /**
   * Go to edit document form.
   */
  $scope.editDocument = function(id) {
    $state.transitionTo('document.edit', { id: id });
  };
  
  /**
   * Display a document.
   */
  $scope.viewDocument = function(id) {
    $state.transitionTo('document.view', { id: id });
  };
});