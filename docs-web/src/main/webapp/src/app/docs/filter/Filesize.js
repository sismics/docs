'use strict';

/**
 * Format file sizes.
 */
angular.module('docs').filter('filesize', function() {
  return function(text) {
    if (!text) {
      return '';
    }

    var size = parseInt(text);
    if (size > 1000000) { // 1MB
      return Math.round(size / 1000000) + 'MB';
    }
    return Math.round(size / 1000) + 'kB';
  }
});