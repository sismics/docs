'use strict';

/**
 * Filter converting new lines in <br />
 */
App.filter('newline', function() {
  return function(text) {
    if (!text) {
      return '';
    }
    return text.replace(/\n/g, '<br/>');
  }
})