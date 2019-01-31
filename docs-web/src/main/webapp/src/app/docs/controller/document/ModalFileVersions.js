'use strict';

/**
 * Modal file versions controller.
 */
angular.module('docs').controller('ModalFileVersions', function ($scope, $state, $stateParams, $uibModalInstance, Restangular, file) {
  Restangular.one('file/' + file.id + '/versions').get().then(function (data) {
    $scope.files = data.files;
  });

  $scope.openFile = function (file) {
    $state.go('document.view.content.file', { id: $stateParams.id, fileId: file.id })
  };

  $scope.close = function() {
    $uibModalInstance.close();
  };
});