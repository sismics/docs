'use strict';

/**
 * Audit log directive.
 */
angular.module('docs').directive('auditLog', function() {
  return {
    restrict: 'E',
    templateUrl: 'partial/docs/directive.auditlog.html',
    replace: true,
    scope: {
      logs: '='
    }
  }
});