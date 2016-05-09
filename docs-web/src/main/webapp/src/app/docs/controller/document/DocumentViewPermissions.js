'use strict';

/**
 * Document view permissions controller.
 */
angular.module('docs').controller('DocumentViewPermissions', function($scope) {
  // Watch for ACLs change and group them for easy displaying
  $scope.$watch('document.inherited_acls', function(acls) {
    $scope.inheritedAcls = _.groupBy(acls, function(acl) {
      return acl.source_id + acl.id;
    });
  });
});