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
        target: $scope.acl.target.name,
        perm: 'READ',
        type: $scope.acl.target.type
      }, {
        source: $stateParams.id,
        target: $scope.acl.target.name,
        perm: 'WRITE',
        type: $scope.acl.target.type
      }];
    } else {
      acls = [{
        source: $stateParams.id,
        target: $scope.acl.target.name,
        perm: $scope.acl.perm,
        type: $scope.acl.target.type
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
          var output = [];

          // Add the type to use later
          output.push.apply(output,  _.map(data.users, function(user) {
            user.type = 'USER';
            return user;
          }));
          output.push.apply(output, _.map(data.groups, function(group) {
            group.type = 'GROUP';
            return group;
          }));

          // Send the data to the typeahead directive
          deferred.resolve(output, true);
        });
    return deferred.promise;
  };
});