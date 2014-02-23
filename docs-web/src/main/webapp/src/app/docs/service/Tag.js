'use strict';

/**
 * Tag service.
 */
angular.module('docs').factory('Tag', function(Restangular) {
  var tags = null;
  
  return {
    /**
     * Returns tags.
     * @param force If true, force reloading data
     */
    tags: function(force) {
      return Restangular.one('tag/list').get();
    }
  }
});