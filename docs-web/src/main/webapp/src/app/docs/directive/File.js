'use strict';

/**
 * File upload directive.
 */
angular.module('docs').directive('file', function() {
  return {
    restrict: 'E',
    template: '<input type="file" />',
    replace: true,
    require: 'ngModel',
    link: function(scope, element, attr, ctrl) {
      element.bind('change', function() {
        scope.$apply(function() {
          console.log('is multiple?', attr.multiple);
          console.log('setting file directive value', attr.multiple ? element[0].files : element[0].files[0]);
          attr.multiple ? ctrl.$setViewValue(element[0].files) : ctrl.$setViewValue(element[0].files[0]);
        });
      });
    }
  }
});