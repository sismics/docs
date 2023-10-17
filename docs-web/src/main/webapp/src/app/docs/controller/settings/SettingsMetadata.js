'use strict';

/**
 * Settings metadata page controller.
 */
angular.module('docs').controller('SettingsMetadata', function($scope, Restangular) {
  // Load metadata
  Restangular.one('metadata').get({
    sort_column: 1,
    asc: true
  }).then(function(data) {
    $scope.metadata = data.metadata;
  });

  // Add a metadata
  $scope.addMetadata = function() {
    Restangular.one('metadata').put($scope.newmetadata).then(function(data) {
      $scope.metadata.push(data);
      $scope.newmetadata = {};
    });
  };

  // Delete a metadata
  $scope.deleteMetadata = function(meta) {
    Restangular.one('metadata', meta.id).remove().then(function() {
      $scope.metadata.splice($scope.metadata.indexOf(meta), 1);
    });
  };

  // Update a metadata
  $scope.updateMetadata = function(meta) {
    Restangular.one('metadata', meta.id).post('', meta);
  };
});