'use strict';

/**
 * Document controller.
 */
angular.module('docs').controller('Document', function ($scope, $rootScope, $timeout, $state, Restangular, $q, $filter, $uibModal) {
  /**
   * Scope variables.
   */
  $scope.sortColumn = 3;
  $scope.asc = false;
  $scope.offset = 0;
  $scope.currentPage = 1;
  $scope.limit = _.isUndefined(localStorage.documentsPageSize) ? '10' : localStorage.documentsPageSize;
  $scope.displayMode = _.isUndefined(localStorage.displayMode) ? 'list' : localStorage.displayMode;
  $scope.search = $state.params.search ? $state.params.search : '';
  $scope.setSearch = function (search) { $scope.search = search };
  $scope.searchOpened = false;
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
          $scope.suggestions = data.suggestions;
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
    $scope.offset = ($scope.currentPage - 1) * parseInt($scope.limit);
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

    $scope.extractNavigatedTag();

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
   * Watch for display mode change.
   */
  $scope.$watch('displayMode', function (next) {
    localStorage.displayMode = next;
  });

  /**
   * Display a document.
   */
  $scope.viewDocument = function (id) {
    $state.go('document.view', { id: id });
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
      var simplesearch = _.map($scope.advsearch.search_simple.split(/\s+/), function (simple) {
        return 'simple:' + simple
      });
      search += simplesearch.join(' ') + ' ';
    }
    if (!_.isEmpty($scope.advsearch.search_fulltext)) {
      var fulltext = _.map($scope.advsearch.search_fulltext.split(/\s+/), function (full) {
        return 'full:' + full
      });
      search += fulltext.join(' ') + ' ';
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
    if (!_.isUndefined($scope.advsearch.after_update_date)) {
      search += 'uafter:' + $filter('date')($scope.advsearch.after_update_date, 'yyyy-MM-dd') + ' ';
    }
    if (!_.isUndefined($scope.advsearch.before_update_date)) {
      search += 'ubefore:' + $filter('date')($scope.advsearch.before_update_date, 'yyyy-MM-dd') + ' ';
    }
    if (!_.isEmpty($scope.advsearch.tags)) {
      search += _.reduce($scope.advsearch.tags, function(s, t) {
          return s + 'tag:' + t.name + ' ';
        }, '');
    }
    if ($scope.advsearch.shared) {
      search += 'shared:yes ';
    }
    if ($scope.advsearch.workflow) {
      search += 'workflow:me ';
    }
    $scope.search = search;
    $scope.searchOpened = false;
  };

  /**
   * Clear the search.
   */
  $scope.clearSearch = function () {
    $scope.advsearch = {};
    $scope.search = '';
    $scope.searchOpened = false;
  };

  /**
   * Import an EML file.
   */
  $scope.importEml = function (file) {
    // Open the import modal
    $uibModal.open({
      templateUrl: 'partial/docs/import.html',
      controller: 'ModalImport',
      resolve: {
        file: function () {
          return file;
        }
      }
    }).result.then(function (data) {
      if (data === null) {
        return;
      }

      $scope.viewDocument(data.id);
      $scope.loadDocuments();
    });
  };

  // Tag navigation
  $scope.tags = [];
  $scope.navigatedFilter = { parent: '' };
  $scope.navigatedTag = undefined;
  $scope.navigationEnabled = _.isUndefined(localStorage.navigationEnabled) ?
    true : localStorage.navigationEnabled === 'true';

  Restangular.one('tag/list').get().then(function (data) {
    $scope.tags = data.tags;
    _.each($scope.tags, function (tag) {
      tag.children = _.where($scope.tags, { parent: tag.id });
    });
    $scope.extractNavigatedTag();
  });

  /**
   * Comparator for the navigation tag filter.
   */
  $scope.navigatedComparator = function (actual, expected) {
    if (expected === '') {
      return _.isUndefined(actual);
    }
    return angular.equals(actual, expected);
  };

  /**
   * Navigate to a specific tag.
   */
  $scope.navigateToTag = function (tag) {
    if (tag) {
      $scope.search = 'tag:' + tag.name;
    } else {
      $scope.search = '';
    }
  };

  /**
   * Navigate one tag up.
   */
  $scope.navigateUp = function () {
    if (!$scope.navigatedTag) {
      return;
    }
    $scope.navigateToTag(_.findWhere($scope.tags, { id: $scope.navigatedTag.parent }));
  };

  /**
   * Get the current navigation breadcrumb.
   */
  $scope.getCurrentNavigation = function () {
    if (!$scope.navigatedTag) {
      return [];
    }

    var nav = [];
    nav.push($scope.navigatedTag);
    var current = $scope.navigatedTag;
    while (current.parent) {
      current = _.findWhere($scope.tags, { id: current.parent });
      if (!current) {
        break;
      }
      nav.push(current);
    }
    return nav.reverse();
  };

  /**
   * Extract the current navigated tag from the search query.
   * Called each time the search query changes.
   */
  $scope.extractNavigatedTag = function () {
    // Find the current tag in the search query
    var tagFound = /(^| )tag:([^ ]*)/.exec($scope.search);
    if (tagFound) {
      tagFound = tagFound[2];
      // We search only for exact match
      $scope.navigatedTag = _.findWhere($scope.tags, { name: tagFound });
    } else {
      $scope.navigatedTag = undefined;
    }
    if ($scope.navigatedTag) {
      $scope.navigatedFilter = {parent: $scope.navigatedTag.id};
    } else {
      $scope.navigatedFilter = {parent: ''};
    }
  };

  /**
   * Toggle the navigation context.
   */
  $scope.navigationToggle = function () {
    $scope.navigationEnabled = !$scope.navigationEnabled;
    localStorage.navigationEnabled = $scope.navigationEnabled;
  };

  $scope.getTagChildrenShort = function (tag) {
    var children = tag.children;
    if (children.length > 2) {
      children = children.slice(0, 2);
    }

    return _.pluck(children, 'name').join(', ') + (tag.children.length > 2 ? '...' : '');
  };

  /**
   * Add a tag in the current navigation context.
   */
  $scope.addTagHere = function () {
    $uibModal.open({
      templateUrl: 'partial/docs/document.add.tag.html',
      controller: 'DocumentModalAddTag'
    }).result.then(function (tag) {
      if (tag === null) {
        return;
      }

      // Create the tag
      tag.parent = $scope.navigatedTag ? $scope.navigatedTag.id : undefined;
      Restangular.one('tag').put(tag).then(function (data) {
        // Add the new tag to the list
        tag.id = data.id;
        tag.children = [];
        $scope.tags.push(tag);
      })
    });
  };
});