'use strict';

/**
 * Settings vocabulary page controller.
 */
angular.module('docs').controller('SettingsVocabulary', function($scope, Restangular) {
  $scope.entries = [];

  // Watch for vocabulary selection change
  $scope.$watch('vocabulary', function(name) {
    if (_.isUndefined(name) || name == '') {
      $scope.entries = [];
      return;
    }

    // Load entries
    Restangular.one('vocabulary', name).get().then(function(result) {
      $scope.entries = result.entries;
    });
  });

  // Delete an entry
  $scope.deleteEntry = function(entry) {
    Restangular.one('vocabulary', entry.id).remove().then(function() {
      $scope.entries.splice($scope.entries.indexOf(entry), 1);
    });
  };

  // Update an entry
  $scope.updateEntry = function(entry) {
    Restangular.one('vocabulary', entry.id).post('', entry);
  };

  // Add an entry
  $scope.addEntry = function(entry) {
    entry.name = $scope.vocabulary;
    Restangular.one('vocabulary').put(entry).then(function(data) {
      $scope.entries.push(data);
      $scope.entry = {};
    });
  };
});