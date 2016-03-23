'use strict';

/**
 * Settings group page controller.
 */
angular.module('docs').controller('SettingsGroup', function($scope, $state, Restangular) {
  /**
   * Load groups from server.
   */
  $scope.loadGroups = function() {
    Restangular.one('group').get().then(function(data) {
      $scope.groups = data.groups;
    });
  };
  
  $scope.loadGroups();
  
  /**
   * Edit a group.
   */
  $scope.editGroup = function(group) {
    $state.go('settings.group.edit', { name: group.name });
  };
});