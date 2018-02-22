'use strict';

/**
 * Modal import controller.
 */
angular.module('docs').controller('ModalImport', function ($scope, $uibModalInstance, file, $q, $timeout) {
  // Payload
  var formData = new FormData();
  formData.append('file', file, file.name);

  // Send the file
  var deferred = $q.defer();
  var getProgressListener = function(deferred) {
    return function(event) {
      deferred.notify(event);
    };
  };

  $.ajax({
    type: 'PUT',
    url: '../api/document/eml',
    data: formData,
    cache: false,
    contentType: false,
    processData: false,
    success: function(response) {
      deferred.resolve(response);
    },
    error: function(jqXHR) {
      deferred.reject(jqXHR);
    },
    xhr: function() {
      var myXhr = $.ajaxSettings.xhr();
      myXhr.upload.addEventListener(
        'progress', getProgressListener(deferred), false);
      return myXhr;
    }
  });

  deferred.promise.then(function(data) {
    $uibModalInstance.close(data);
  }, function(data) {
    $scope.errorQuota = data.responseJSON && data.responseJSON.type === 'QuotaReached';
    if (!$scope.errorQuota) {
      $scope.errorGeneral = true;
    }
    $timeout(function () {
      $uibModalInstance.close(null);
    }, 3000);
  }, function(e) {
    $scope.progress = e.loaded / e.total;
  });
});