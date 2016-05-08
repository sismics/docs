'use strict';

/**
 * Settings theme page controller.
 */
angular.module('docs').controller('SettingsTheme', function($scope, $rootScope, Restangular) {
  // Fetch the current theme configuration
  Restangular.one('theme').get().then(function(data) {
    $scope.theme = data;
    $rootScope.appName = $scope.theme.name;
  });

  // Update the theme
  $scope.update = function() {
    $scope.theme.name = $scope.theme.name.length == 0 ? 'Sismics Docs' : $scope.theme.name;
    Restangular.one('theme').post('', $scope.theme).then(function() {
      var stylesheet = $('#theme-stylesheet')[0];
      stylesheet.href = stylesheet.href.replace(/\?.*|$/, '?' + new Date().getTime());
      $rootScope.appName = $scope.theme.name;
    });
  }
});