'use strict';

/**
 * ACL directive.
 */
angular.module('docs').directive('acl', function() {
  return {
    restrict: 'E',
    template: '<span ng-show="data.type"><em>{{ \'acl.\' + data.type | translate }}</em> {{ data.name }}</span>',
    replace: true,
    scope: {
      data: '='
    }
  }
});