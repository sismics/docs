'use strict';

/**
 * Inline edition directive.
 * Thanks to http://jsfiddle.net/joshdmiller/NDFHg/
 */
App.directive('inlineEdit', function() {
  return {
    restrict: 'E',
    scope: {
      value: '=',
      editCallback: '&onEdit'
    },
    template: '<span ng-click="edit()" ng-bind="value"></span><input type="text" ng-model="value" />',
    link: function (scope, element, attrs) {
      // Let's get a reference to the input element, as we'll want to reference it.
      var inputElement = angular.element(element.children()[1]);
      var el = inputElement[0];
      
      // This directive should have a set class so we can style it.
      element.addClass('inline-edit');
      
      // Initially, we're not editing.
      scope.editing = false;
      
      // ng-click handler to activate edit-in-place
      scope.edit = function () {
        scope.editing = true;
        scope.oldValue = el.value;
        
        // We control display through a class on the directive itself. See the CSS.
        element.addClass('active');
        
        // And we must focus the element. 
        // `angular.element()` provides a chainable array, like jQuery so to access a native DOM function, 
        // we have to reference the first element in the array.
        el.focus();
        el.selectionStart = 0;
        el.selectionEnd = el.value.length;
      };
      
      // When we leave the input, we're done editing.
      inputElement.on('blur', function() {
        scope.editing = false;
        element.removeClass('active');
        
        // Invoke parent scope callback
        if (scope.editCallback && scope.oldValue != el.value) {
          scope.$apply(function() {
            if (scope.value) {
              scope.editCallback().then(null, function() {
                scope.value = scope.oldValue;
              });
            } else {
              scope.value = scope.oldValue;
            }
          });
        }
      });
    }
  };
});