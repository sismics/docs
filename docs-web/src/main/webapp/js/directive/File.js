'use strict';

/**
 * File upload directive.
 */
App.directive('file', function() {
  return {
    restrict: 'E',
    template: '<input type="file" />',
    replace: true,
    require: 'ngModel',
    link: function(scope, element, attr, ctrl) {
      var listener = function() {
        scope.$apply(function() {
            attr.multiple ? ctrl.$setViewValue(element[0].files) : ctrl.$setViewValue(element[0].files[0]);
        });
      }
      element.bind('change', listener);
    }
  }
});