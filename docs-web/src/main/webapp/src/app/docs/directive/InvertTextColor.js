'use strict';

/**
 * Invert text color for more legibility directive.
 */
angular.module('docs').directive('invertTextColor', function () {
  return {
    restrict: 'A',
    link: function(scope, element, attrs) {
      attrs.$observe('invertTextColor', function(hex) {
        if (!hex || hex.length !== 7) {
          return;
        }

        hex = hex.slice(1);
        var r = parseInt(hex.slice(0, 2), 16),
            g = parseInt(hex.slice(2, 4), 16),
            b = parseInt(hex.slice(4, 6), 16);
        element.css('color', (r * 0.299 + g * 0.587 + b * 0.114) > 186
            ? '#000000'
            : '#FFFFFF');
      });
    }
  }
});