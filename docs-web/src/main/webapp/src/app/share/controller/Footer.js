'use strict';

/**
 * Footer controller.
 */
angular.module('share').controller('Footer', function($scope, $rootScope, Restangular, $translate, tmhDynamicLocale, $locale) {
  // Load app data
  Restangular.one('app').get().then(function(data) {
    $scope.app = data;
  });

  // Save the current language to local storage
  $rootScope.$on('$translateChangeSuccess', function() {
    $scope.currentLang = $translate.use();
    localStorage.overrideLang = $scope.currentLang;
    tmhDynamicLocale.set($scope.currentLang).then(function () {
      $rootScope.dateFormat = $locale.DATETIME_FORMATS.shortDate;
      $rootScope.dateTimeFormat = $locale.DATETIME_FORMATS.short;
    });
  });

  // Change the current language
  $scope.changeLanguage = function(lang) {
    $translate.use(lang);
  };
});