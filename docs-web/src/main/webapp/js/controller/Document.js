'use strict';

/**
 * Document controller.
 */
App.controller('Document', function($scope, $state, Restangular) {
  /**
   * Documents table sort status.
   */
  $scope.sortColumn = 3;
  $scope.asc = false;
  $scope.offset = 0;
  $scope.currentPage = 1;
  $scope.limit = 10;
  $scope.isAdvancedSearchCollapsed = true;
  
  /**
   * Initialize search criterias.
   */
  $scope.initSearch = function() {
    $scope.search = {
      query: '',
      createDateMin: null,
      createDateMax: null,
      tags: []
    };
  };
  $scope.initSearch();
  
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
      search: $scope.search.query,
      create_date_min: $scope.isAdvancedSearchCollapsed || !$scope.search.createDateMin ? null : $scope.search.createDateMin.getTime(),
      create_date_max: $scope.isAdvancedSearchCollapsed || !$scope.search.createDateMax ? null : $scope.search.createDateMax.getTime(),
      'tags[]': $scope.isAdvancedSearchCollapsed ? null : _.pluck($scope.search.tags, 'id')
    })
    .then(function(data) {
      $scope.documents = data.documents;
      $scope.totalDocuments = data.total; // TODO This is not really the total number of documents
      $scope.numPages = Math.ceil(data.total / $scope.limit);
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
   * Watch for search change.
   */
  $scope.$watch('search', function(prev, next) {
    $scope.loadDocuments();
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