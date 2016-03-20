'use strict';

/**
 * ACL directive.
 */
angular.module('docs').directive('acl', function() {
  return {
    restrict: 'E',
    template: '<span ng-if="data.type"><em>{{ data.type == \'SHARE\' ? \'Shared\' : (data.type == \'USER\' ? \'User\' : \'Group\') }}</em> {{ data.name }}</span>',
    replace: true,
    scope: {
      data: '='
    }
  }
});