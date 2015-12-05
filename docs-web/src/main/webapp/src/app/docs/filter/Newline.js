'use strict';

/**
 * Filter converting new lines in <br />.
 */
angular.module('docs').filter('newline', function() {
  return function(text) {
    if (!text) {
      return '';
    }
    return text.replace(/\n/g, '<br/>');
  }
});