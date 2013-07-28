'use strict';

/**
 * Trackino application.
 */
var App = angular.module('docs', ['ui.state', 'ui.bootstrap', 'ui.keypress', 'ui.sortable', 'restangular', 'ngSanitize'])

/**
 * Configuring modules.
 */
.config(function($stateProvider, $httpProvider, $routeProvider, RestangularProvider) {
  // Configuring UI Router
  $stateProvider
  .state('main', {
    url: '',
    views: {
      'page': {
        templateUrl: 'partial/main.html',
        controller: 'Main'
      }
    }
  })
  .state('document', {
    url: '/document',
    abstract: true,
    views: {
      'page': {
        templateUrl: 'partial/document.html',
        controller: 'Document'
      }
    }
  })
  .state('document.default', {
    url: '',
    views: {
      'document': {
        templateUrl: 'partial/document.default.html'
      }
    }
  })
  .state('document.add', {
    url: '/add',
    views: {
      'document': {
        templateUrl: 'partial/document.edit.html',
        controller: 'DocumentEdit'
      }
    }
  })
  .state('document.edit', {
    url: '/edit/:id',
    views: {
      'document': {
        templateUrl: 'partial/document.edit.html',
        controller: 'DocumentEdit'
      }
    }
  })
  .state('document.view', {
    url: '/view/:id',
    views: {
      'document': {
        templateUrl: 'partial/document.view.html',
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
        templateUrl: 'partial/login.html',
        controller: 'Login'
      }
    }
  });
  
  // Configuring Restangular
  RestangularProvider.setBaseUrl('api');
  
  // Configuring $http
  $httpProvider.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded;charset=utf-8';
  $httpProvider.defaults.transformRequest = [function(data) {
    var param = function(obj) {
      var query = '';
      var name, value, fullSubName, subName, subValue, innerObj, i;
      
      for(name in obj) {
        value = obj[name];
        
        if(value instanceof Array) {
          for(i=0; i<value.length; ++i) {
            subValue = value[i];
            fullSubName = name + '[' + i + ']';
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