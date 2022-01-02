'use strict';

/**
 * Share application.
 */
angular.module('share',
    // Dependencies
    ['ui.router', 'ui.bootstrap', 'restangular', 'ngSanitize', 'ngTouch', 'pascalprecht.translate', 'tmh.dynamicLocale']
  )

/**
 * Configuring modules.
 */
.config(function($locationProvider, $stateProvider, $httpProvider, RestangularProvider, $translateProvider, tmhDynamicLocaleProvider) {
  $locationProvider.hashPrefix('');

  // Configuring UI Router
  $stateProvider
  .state('main', {
    url: '',
    views: {
      'page': {
        templateUrl: 'partial/share/main.html',
        controller: 'Main'
      }
    }
  })
  .state('share', {
    url: '/share/:documentId/:shareId',
    views: {
      'page': {
        templateUrl: 'partial/share/share.html',
        controller: 'Share'
      }
    }
  })
    .state('share.file', {
      url: '/file/:fileId',
      views: {
        'file': {
          controller: 'FileView'
        }
      }
    })
  .state('403', {
    url: '/403',
    views: {
      'page': {
        templateUrl: 'partial/share/403.html'
      }
    }
  });
  
  // Configuring Restangular
  RestangularProvider.setBaseUrl('../api');

  // Configuring Angular Translate
  $translateProvider
      .useSanitizeValueStrategy('escapeParameters')
      .useStaticFilesLoader({
        prefix: 'locale/',
        suffix: '.json?@build.date@'
      })
      .registerAvailableLanguageKeys(['en', 'es', 'fr', 'de', 'el', 'ru', 'it', 'pl', 'zh_CN', 'zh_TW'], {
        'en_*': 'en',
        'es_*': 'es',
        'fr_*': 'fr',
        'de_*': 'de',
        'el_*': 'el',
        'ru_*': 'ru',
        'it_*': 'it',
        'pl_*': 'pl',
        '*': 'en'
      })
      .fallbackLanguage('en');

  if (!_.isUndefined(localStorage.overrideLang)) {
    // Set the current language if an override is saved in local storage
    $translateProvider.use(localStorage.overrideLang);
  } else {
    // Or else determine the language based on the user's browser
    $translateProvider.determinePreferredLanguage();
  }

  // Configuring tmhDynamicLocale
  tmhDynamicLocaleProvider.localeLocationPattern('locale/angular-locale_{{locale}}.js');
  
  // Configuring $http to act like jQuery.ajax
  $httpProvider.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded;charset=utf-8';
  $httpProvider.defaults.headers.put['Content-Type'] = 'application/x-www-form-urlencoded;charset=utf-8';
  $httpProvider.defaults.headers.delete = {
    'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8'
  };
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
.run(function($rootScope, $state, $stateParams, Restangular) {
  $rootScope.$state = $state;
  $rootScope.$stateParams = $stateParams;

  // Fetch the current theme configuration
  $rootScope.appName = '';
  Restangular.one('theme').get().then(function(data) {
    $rootScope.appName = data.name;
  });
});