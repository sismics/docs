'use strict';

/**
 * Footer controller.
 */
angular.module('docs').controller('Footer', function($scope, $rootScope, Restangular, $translate, timeAgoSettings) {
  // Load app data
  Restangular.one('app').get().then(function(data) {
    $scope.app = data;
  });

  // Save the current language to local storage
  $rootScope.$on('$translateChangeSuccess', function() {
    $scope.currentLang = $translate.use();
    timeAgoSettings.overrideLang = $scope.currentLang;
    localStorage.overrideLang = $scope.currentLang;
  });

  // Change the current language
  $scope.changeLanguage = function(lang) {
    $translate.use(lang);
  };
});