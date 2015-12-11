'use strict';

/**
 * Filter converting new lines in <br />.
 */
angular.module('share').filter('newline', function() {
  return function(text) {
    if (!text) {
      return '';
    }
    return text.replace(/\n/g, '<br/>');
  }
});