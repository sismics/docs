'use strict';

/**
 * Sismics Docs application.
 */
angular.module('docs',
    // Dependencies
    ['ui.router', 'ui.route', 'ui.bootstrap', 'ui.keypress', 'ui.validate', 'dialog', 'ngProgress', 'monospaced.qrcode',
      'ui.sortable', 'restangular', 'ngSanitize', 'ngTouch', 'colorpicker.module', 'angularFileUpload']
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
    .state('settings.security', {
      url: '/security',
      views: {
        'settings': {
          templateUrl: 'partial/docs/settings.security.html',
          controller: 'SettingsSecurity'
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
    .state('settings.group', {
        url: '/group',
        views: {
          'settings': {
            templateUrl: 'partial/docs/settings.group.html',
            controller: 'SettingsGroup'
          }
        }
      })
      .state('settings.group.edit', {
        url: '/edit/:name',
        views: {
          'group': {
            templateUrl: 'partial/docs/settings.group.edit.html',
            controller: 'SettingsGroupEdit'
          }
        }
      })
      .state('settings.group.add', {
        url: '/add',
        views: {
          'group': {
            templateUrl: 'partial/docs/settings.group.edit.html',
            controller: 'SettingsGroupEdit'
          }
        }
      })
  .state('settings.vocabulary', {
    url: '/vocabulary',
    views: {
      'settings': {
        templateUrl: 'partial/docs/settings.vocabulary.html',
        controller: 'SettingsVocabulary'
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
      .state('document.default.search', {
        url: '/search/:search'
      })
      .state('document.default.file', {
        url: '/file/:fileId',
        views: {
          'file': {
            controller: 'FileView'
          }
        }
      })
    .state('document.add', {
      url: '/add?files',
      views: {
        'document': {
          templateUrl: 'partial/docs/document.edit.html',
          controller: 'DocumentEdit'
        }
      }
    })
    .state('document.edit', {
      url: '/edit/:id?files',
      views: {
        'document': {
          templateUrl: 'partial/docs/document.edit.html',
          controller: 'DocumentEdit'
        }
      }
    })
    .state('document.view', {
      url: '/view/:id',
      redirectTo: 'document.view.content',
      views: {
        'document': {
          templateUrl: 'partial/docs/document.view.html',
          controller: 'DocumentView'
        }
      }
    })
      .state('document.view.content', {
        url: '/content',
        views: {
          'tab': {
            templateUrl: 'partial/docs/document.view.content.html',
            controller: 'DocumentViewContent'
          }
        }
      })
      .state('document.view.content.file', {
        url: '/file/:fileId',
        views: {
          'file': {
            controller: 'FileView'
          }
        }
      })
      .state('document.view.permissions', {
        url: '/permissions',
        views: {
          'tab': {
            templateUrl: 'partial/docs/document.view.permissions.html',
            controller: 'DocumentViewPermissions'
          }
        }
      })
      .state('document.view.activity', {
        url: '/activity',
        views: {
          'tab': {
            templateUrl: 'partial/docs/document.view.activity.html',
            controller: 'DocumentViewActivity'
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
  })
  .state('user', {
    url: '/user',
    views: {
      'page': {
        templateUrl: 'partial/docs/usergroup.html',
        controller: 'UserGroup'
      }
    }
  })
    .state('user.profile', {
      url: '/:username',
      views: {
        'sub': {
          templateUrl: 'partial/docs/user.profile.html',
          controller: 'UserProfile'
        }
      }
    })
  .state('group', {
    url: '/group',
    views: {
      'page': {
        templateUrl: 'partial/docs/usergroup.html',
        controller: 'UserGroup'
      }
    }
  })
    .state('group.profile', {
      url: '/:name',
      views: {
        'sub': {
          templateUrl: 'partial/docs/group.profile.html',
          controller: 'GroupProfile'
        }
      }
    });
  
  // Configuring Restangular
  RestangularProvider.setBaseUrl('../api');
  
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
  $rootScope.pageTitle = 'Sismics Docs';
})
/**
 * Redirection support for ui-router.
 * Thanks to https://github.com/acollard
 * See https://github.com/angular-ui/ui-router/issues/1584#issuecomment-76993045
 */
.run(function($rootScope, $state){
  $rootScope.$on('$stateChangeStart', function(event, toState, toParams) {
    var redirect = toState.redirectTo;
    if (redirect) {
      event.preventDefault();
      $state.go(redirect, toParams);
    }
  });
})
/**
 * Initialize ngProgress.
 */
.run(function($rootScope, ngProgressFactory, $http) {
  $rootScope.ngProgress = ngProgressFactory.createInstance();

  // Watch for the number of XHR running
  $rootScope.$watch(function() {
    return $http.pendingRequests.length > 0
  }, function(count) {
    if (count == 0) {
      $rootScope.ngProgress.complete();
    } else {
      $rootScope.ngProgress.start();
    }
  });
});