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
  };
  
  // Send an image
  $scope.sendingImage = false;
  $scope.sendImage = function(type, image) {
    // Build the payload
    var formData = new FormData();
    formData.append('image', image);

    // Send the file
    var done = function() {
      $scope.$apply(function() {
        $scope.sendingImage = false;
        $scope[type] = null;
      });
    };
    $scope.sendingImage = true;
    $.ajax({
      type: 'PUT',
      url: '../api/theme/image/' + type,
      data: formData,
      cache: false,
      contentType: false,
      processData: false,
      success: function() {
        done();
      },
      error: function() {
        done();
      }
    });
  };
});