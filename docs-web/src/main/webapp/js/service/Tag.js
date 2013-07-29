'use strict';

/**
 * Tag service.
 */
App.factory('Tag', function(Restangular) {
  var tags = null;
  
  return {
    /**
     * Returns tags.
     * @param force If true, force reloading data
     */
    tags: function(force) {
      if (tags == null || force) {
        tags = Restangular.one('tag/list').get();
      }
      return tags;
    },
    
    /**
     * Login an user.
     */
    login: function(user) {
      return Restangular.one('user').post('login', user);
    }
  }
});