'use strict';

/**
 * Add space between element directive.
 */
angular.module('docs').directive('addSpaceBetween', function () {
  return function (scope, element) {
    if(!scope.$last) {
      element.after('&#32;');
    }
  }
});