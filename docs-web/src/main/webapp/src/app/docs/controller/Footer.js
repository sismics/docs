'use strict';

/**
 * Footer controller.
 */
angular.module('docs').controller('Footer', function($scope, Restangular, $translate, timeAgoSettings) {
  // Load app data
  Restangular.one('app').get().then(function(data) {
    $scope.app = data;
  });

  $scope.currentLang = $translate.use();

  // Change the current language and save it to local storage
  $scope.changeLanguage = function(lang) {
    $translate.use(lang);
    timeAgoSettings.overrideLang = lang;
    localStorage.overrideLang = lang;
    $scope.currentLang = lang;
  };

  // Set the current language if an override is saved in local storage
  if (!_.isUndefined(localStorage.overrideLang)) {
    $scope.changeLanguage(localStorage.overrideLang);
  }
});