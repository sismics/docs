'use strict';

/**
 * Image error event directive.
 */
angular.module('docs').directive('imgError', function() {
  return {
    restrict: 'A',
    link: function(scope, element, attrs) {
      element.bind('error', function() {
        // call the function that was passed
        scope.$apply(attrs.imgError);
      });
    }
  };
})