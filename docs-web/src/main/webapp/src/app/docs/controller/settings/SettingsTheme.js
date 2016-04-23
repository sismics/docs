'use strict';

/**
 * Settings theme page controller.
 */
angular.module('docs').controller('SettingsTheme', function($scope, Restangular) {
    // Fetch the current theme configuration
    $scope.theme = {
        color: $('.navbar').css('background-color')
    };

    // Update the main color
    $scope.updateColor = function(color) {
        Restangular.one('theme').post('color', {
            color: color
        }).then(function() {
           var stylesheet = $('#theme-stylesheet')[0];
            stylesheet.href = stylesheet.href.replace(/\?.*|$/, '?' + new Date().getTime());
        });
    }
});