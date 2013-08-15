'use strict';

/**
 * Sismics Docs application.
 */
var App = angular.module('docs',
    // Dependencies
    ['ui.state', 'ui.route', 'ui.bootstrap', 'ui.keypress', 'ui.validate',
      'ui.sortable', 'restangular', 'ngSanitize', 'ngMobile', 'colorpicker.module']
  )

/**
 * Configuring modules.
 */
.config(function($stateProvider, $httpProvider, RestangularProvider) {
  // Configuring UI Router
  $stateProvider
  .state('main', {
    url: '',
    views: {
      'page': {
        templateUrl: 'partial/docs/main.html',
        controller: 'Main'
      }
    }
  })
  .state('tag', {
    url: '/tag',
    views: {
      'page': {
        templateUrl: 'partial/docs/tag.html',
        controller: 'Tag'
      }
    }
  })
  .state('settings', {
    url: '/settings',
    abstract: true,
    views: {
      'page': {
        templateUrl: 'partial/docs/settings.html',
        controller: 'Settings'
      }
    }
  })
    .state('settings.default', {
      url: '',
      views: {
        'settings': {
          templateUrl: 'partial/docs/settings.default.html',
          controller: 'SettingsDefault'
        }
      }
    })
    .state('settings.account', {
      url: '/account',
      views: {
        'settings': {
          templateUrl: 'partial/docs/settings.account.html',
          controller: 'SettingsAccount'
        }
      }
    })
    .state('settings.session', {
      url: '/session',
      views: {
        'settings': {
          templateUrl: 'partial/docs/settings.session.html',
          controller: 'SettingsSession'
        }
      }
    })
    .state('settings.log', {
      url: '/log',
      views: {
        'settings': {
          templateUrl: 'partial/docs/settings.log.html',
          controller: 'SettingsLog'
        }
      }
    })
    .state('settings.user', {
      url: '/user',
      views: {
        'settings': {
          templateUrl: 'partial/docs/settings.user.html',
          controller: 'SettingsUser'
        }
      }
    })
      .state('settings.user.edit', {
        url: '/edit/:username',
        views: {
          'user': {
            templateUrl: 'partial/docs/settings.user.edit.html',
            controller: 'SettingsUserEdit'
          }
        }
      })
      .state('settings.user.add', {
        url: '/add',
        views: {
          'user': {
            templateUrl: 'partial/docs/settings.user.edit.html',
            controller: 'SettingsUserEdit'
          }
        }
      })
  .state('document', {
    url: '/document',
    abstract: true,
    views: {
      'page': {
        templateUrl: 'partial/docs/document.html',
        controller: 'Document'
      }
    }
  })
    .state('document.default', {
      url: '',
      views: {
        'document': {
          templateUrl: 'partial/docs/document.default.html',
          controller: 'DocumentDefault'
        }
      }
    })
    .state('document.add', {
      url: '/add',
      views: {
        'document': {
          templateUrl: 'partial/docs/document.edit.html',
          controller: 'DocumentEdit'
        }
      }
    })
    .state('document.edit', {
      url: '/edit/:id',
      views: {
        'document': {
          templateUrl: 'partial/docs/document.edit.html',
          controller: 'DocumentEdit'
        }
      }
    })
    .state('document.view', {
      url: '/view/:id',
      views: {
        'document': {
          templateUrl: 'partial/docs/document.view.html',
          controller: 'DocumentView'
        }
      }
    })
      .state('document.view.file', {
        url: '/file/:fileId',
        views: {
          'file': {
            controller: 'FileView'
          }
        }
      })
  .state('login', {
    url: '/login',
    views: {
      'page': {
        templateUrl: 'partial/docs/login.html',
        controller: 'Login'
      }
    }
  });
  
  // Configuring Restangular
  RestangularProvider.setBaseUrl('api');
  
  // Configuring $http to act like jQuery.ajax
  $httpProvider.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded;charset=utf-8';
  $httpProvider.defaults.headers.put['Content-Type'] = 'application/x-www-form-urlencoded;charset=utf-8';
  $httpProvider.defaults.transformRequest = [function(data) {
    var param = function(obj) {
      var query = '';
      var name, value, fullSubName, subName, subValue, innerObj, i;
      
      for(name in obj) {
        value = obj[name];
        
        if(value instanceof Array) {
          for(i=0; i<value.length; ++i) {
            subValue = value[i];
            fullSubName = name;
            innerObj = {};
            innerObj[fullSubName] = subValue;
            query += param(innerObj) + '&';
          }
        } else if(value instanceof Object) {
          for(subName in value) {
            subValue = value[subName];
            fullSubName = name + '[' + subName + ']';
            innerObj = {};
            innerObj[fullSubName] = subValue;
            query += param(innerObj) + '&';
          }
        }
        else if(value !== undefined && value !== null) {
          query += encodeURIComponent(name) + '=' + encodeURIComponent(value) + '&';
        }
      }
      
      return query.length ? query.substr(0, query.length - 1) : query;
    };
    
    return angular.isObject(data) && String(data) !== '[object File]' ? param(data) : data;
  }];
})

/**
 * Application initialization.
 */
.run(function($rootScope, $state, $stateParams) {
  $rootScope.$state = $state;
  $rootScope.$stateParams = $stateParams;
});