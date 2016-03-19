'use strict';

/**
 * Document view permissions controller.
 */
angular.module('docs').controller('DocumentViewPermissions', function ($scope, $stateParams, Restangular, $q) {
  // Watch for ACLs change and group them for easy displaying
  $scope.$watch('document.acls', function(acls) {
    $scope.acls = _.groupBy(acls, function(acl) {
      return acl.id;
    });
  });

  // Initialize add ACL
  $scope.acl = { perm: 'READ' };

  /**
   * Delete an ACL.
   */
  $scope.deleteAcl = function(acl) {
    Restangular.one('acl/' + $stateParams.id + '/' + acl.perm + '/' + acl.id, null).remove().then(function () {
      $scope.document.acls = _.reject($scope.document.acls, function(s) {
        return angular.equals(acl, s);
      });
    });
  };

  /**
   * Add an ACL.
   */
  $scope.addAcl = function() {
    // Compute ACLs to add
    $scope.acl.source = $stateParams.id;
    var acls = [];
    if ($scope.acl.perm == 'READWRITE') {
      acls = [{
        source: $stateParams.id,
        username: $scope.acl.username,
        perm: 'READ'
      }, {
        source: $stateParams.id,
        username: $scope.acl.username,
        perm: 'WRITE'
      }];
    } else {
      acls = [{
        source: $stateParams.id,
        username: $scope.acl.username,
        perm: $scope.acl.perm
      }];
    }

    // Add ACLs
    _.each(acls, function(acl) {
      Restangular.one('acl').put(acl).then(function(acl) {
        if (_.isUndefined(acl.id)) {
          return;
        }
        $scope.document.acls.push(acl);
        $scope.document.acls = angular.copy($scope.document.acls);
      });
    });

    // Reset form
    $scope.acl = { perm: 'READ' };
  };

  /**
   * Auto-complete on ACL target.
   */
  $scope.getTargetAclTypeahead = function($viewValue) {
    var deferred = $q.defer();
    Restangular.one('acl/target/search')
        .get({
          search: $viewValue
        }).then(function(data) {
          deferred.resolve(_.pluck(data.users, 'name'), true);
        });
    return deferred.promise;
  };
});