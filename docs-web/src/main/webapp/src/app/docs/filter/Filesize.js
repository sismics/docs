'use strict';

/**
 * Format file sizes.
 */
angular.module('docs').filter('filesize', function($translate) {
  return function(text) {
    if (!text) {
      return '';
    }

    var size = parseInt(text);
    if (size > 1000000) { // 1MB
      return Math.round(size / 1000000) + $translate.instant('filter.filesize.mb');
    }
    return Math.round(size / 1000) + $translate.instant('filter.filesize.kb');
  }
});