'use strict';

/**
 * Document controller.
 */
angular.module('docs').controller('Document', function ($scope, $rootScope, $timeout, $state, Restangular, $q, $filter) {
  /**
   * Scope variables.
   */
  $scope.sortColumn = 3;
  $scope.asc = false;
  $scope.offset = 0;
  $scope.currentPage = 1;
  $scope.limit = _.isUndefined(localStorage.documentsPageSize) ? 10 : localStorage.documentsPageSize;
  $scope.search = $state.params.search ? $state.params.search : '';
  $scope.searchOpened = false;
  $scope.setSearch = function (search) { $scope.search = search };
  $scope.searchDropdownAnchor = angular.element(document.querySelector('.search-dropdown-anchor'));
  $scope.paginationShown = true;
  $scope.advsearch = {};

  // A timeout promise is used to slow down search requests to the server
  // We keep track of it for cancellation purpose
  var timeoutPromise;
  
  /**
   * Load new documents page.
   */
  $scope.pageDocuments = function () {
    Restangular.one('document/list')
        .get({
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
  $scope.loadDocuments = function () {
    $scope.offset = 0;
    $scope.currentPage = 1;
    $scope.pageDocuments();
  };
  
  /**
   * Watch for current page change.
   */
  $scope.$watch('currentPage', function (prev, next) {
    if (prev === next) {
      return;
    }
    $scope.offset = ($scope.currentPage - 1) * $scope.limit;
    $scope.pageDocuments();
  });
  
  /**
   * Watch for search scope change.
   */
  $scope.$watch('search', function () {
    if (timeoutPromise) {
      // Cancel previous timeout
      $timeout.cancel(timeoutPromise);
    }

    if ($state.current.name === 'document.default'
        || $state.current.name === 'document.default.search') {
      $state.go($scope.search === '' ?
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
  $scope.sortDocuments = function (sortColumn) {
    if (sortColumn === $scope.sortColumn) {
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
  $scope.$watch('limit', function (next, prev) {
    localStorage.documentsPageSize = next;
    if (next === prev) {
      return;
    }
    $scope.loadDocuments();
  });
  
  /**
   * Display a document.
   */
  $scope.viewDocument = function (id) {
    $state.go('document.view', { id: id });
  };

  // Load tags
  $scope.tags = [];
  Restangular.one('tag/list').get().then(function (data) {
    $scope.tags = data.tags;
  });

  /**
   * Find children tags.
   * @param parent
   */
  $scope.getChildrenTags = function(parent) {
    return _.filter($scope.tags, function(tag) {
      return tag.parent === parent;
    });
  };

  /**
   * Returns a promise for typeahead user.
   */
  $scope.getUserTypeahead = function($viewValue) {
    var deferred = $q.defer();
    Restangular.one('user/list')
      .get({
        search: $viewValue,
        sort_column: 1,
        asc: true
      }).then(function(data) {
      deferred.resolve(_.pluck(_.filter(data.users, function(user) {
        return user.username.indexOf($viewValue) !== -1;
      }), 'username'));
    });
    return deferred.promise;
  };

  /**
   * Hack to reload the pagination directive after language change.
   */
  $rootScope.$on('$translateChangeSuccess', function () {
    $scope.paginationShown = false;
    $timeout(function () {
      $scope.paginationShown = true;
    });
  });

  /**
   * Open the advanced search panel.
   */
  $scope.openSearch = function () {
    var opened = $scope.searchOpened;
    $timeout(function () {
      $scope.searchOpened = !opened;
    });
  };

  /**
   * Start the advanced search.
   */
  $scope.startSearch = function () {
    var search = '';
    if (!_.isEmpty($scope.advsearch.search_simple)) {
      search += $scope.advsearch.search_simple + ' ';
    }
    if (!_.isEmpty($scope.advsearch.search_fulltext)) {
      search += 'full:' + $scope.advsearch.search_fulltext + ' ';
    }
    if (!_.isEmpty($scope.advsearch.creator)) {
      search += 'by:' + $scope.advsearch.creator + ' ';
    }
    if (!_.isEmpty($scope.advsearch.language)) {
      search += 'lang:' + $scope.advsearch.language + ' ';
    }
    if (!_.isUndefined($scope.advsearch.after_date)) {
      search += 'after:' + $filter('date')($scope.advsearch.after_date, 'yyyy-MM-dd') + ' ';
    }
    if (!_.isUndefined($scope.advsearch.before_date)) {
      search += 'before:' + $filter('date')($scope.advsearch.before_date, 'yyyy-MM-dd') + ' ';
    }
    if (!_.isEmpty($scope.advsearch.tags)) {
      search += _.reduce($scope.advsearch.tags, function(s, t) {
          return s + 'tag:' + t.name + ' ';
        }, '');
    }
    $scope.search = search;
    $scope.searchOpened = false;
  };

  $scope.clearSearch = function () {
    $scope.advsearch = {};
    $scope.search = '';
    $scope.searchOpened = false;
  };
});