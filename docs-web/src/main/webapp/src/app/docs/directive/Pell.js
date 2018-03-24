'use strict';

/**
 * Pell directive.
 */
angular.module('docs').directive('pellEditor', function ($timeout) {
  return {
    restrict: 'E',
    template: '<div class="pell"></div>',
    require: 'ngModel',
    replace: true,
    link: function (scope, element, attrs, ngModelCtrl) {
      var editor = pell.init({
        element: element[0],
        defaultParagraphSeparator: 'p',
        onChange: function (html) {
          $timeout(function () {
            ngModelCtrl.$setViewValue(html);
          });
        }
      });

      ngModelCtrl.$render = function() {
        editor.content.innerHTML = ngModelCtrl.$viewValue || '';
      };
    }
  };
});