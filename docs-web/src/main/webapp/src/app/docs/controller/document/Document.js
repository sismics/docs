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
  $scope.limit = _.isUndefined(localStorage.documentsPageSize) ? 10 : localStorage.documentsPageSize;
  $scope.search = $state.params.search ? $state.params.search : '';
  $scope.setSearch = function(search) { $scope.search = search };

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
  $scope.$watch('search', function() {
    if (timeoutPromise) {
      // Cancel previous timeout
      $timeout.cancel(timeoutPromise);
    }

    if ($state.current.name == 'document.default'
        || $state.current.name == 'document.default.search') {
      $state.go($scope.search == '' ?
          'document.default' : 'document.default.search', {
        search: $scope.search
      }, {
        location: 'replace',
        notify: false
      });
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
   * Watch for page size change.
   */
  $scope.$watch('limit', function(next, prev) {
    localStorage.documentsPageSize = next;
    if (next == prev) {
      return;
    }
    $scope.loadDocuments();
  });
  
  /**
   * Display a document.
   */
  $scope.viewDocument = function(id) {
    $state.go('document.view', { id: id });
  };

  // Load tags
  var tags = [];
  Restangular.one('tag/list').getList().then(function(data) {
    tags = data.tags;
  });

  /**
   * Find children tags.
   * @param parent
   */
  $scope.getChildrenTags = function(parent) {
    return _.filter(tags, function(tag) {
      return tag.parent == parent;
    });
  };
});