'use strict';

/**
 * Document default controller.
 */
angular.module('docs').controller('DocumentDefault', function ($scope, $rootScope, $state, Restangular, Upload, $translate, $uibModal, $dialog, User) {
  // Load user audit log
  Restangular.one('auditlog').get().then(function (data) {
    $scope.logs = data.logs;
  });

  // Load unlinked files
  $scope.loadFiles = function () {
    Restangular.one('file/list').get().then(function (data) {
      $scope.files = data.files;
    });
  };
  $scope.loadFiles();

  // File has been drag & dropped
  $scope.fileDropped = function (files) {
    if (files && files.length) {
      // Adding files to the UI
      var newfiles = [];
      _.each(files, function (file) {
        var newfile = {
          progress: 0,
          name: file.name,
          create_date: new Date().getTime(),
          mimetype: file.type,
          status: $translate.instant('document.default.upload_pending')
        };
        $scope.files.push(newfile);
        newfiles.push(newfile);
      });

      // Uploading files sequentially
      var key = 0;
      var then = function () {
        if (files[key]) {
          $scope.uploadFile(files[key], newfiles[key++]).then(then);
        }
      };
      then();
    }
  };

  // Upload a file
  $scope.uploadFile = function (file, newfile) {
    // Upload the file
    newfile.status = $translate.instant('document.default.upload_progress');
    return Upload.upload({
      method: 'PUT',
      url: '../api/file',
      file: file
    })
        .progress(function (e) {
          newfile.progress = parseInt(100.0 * e.loaded / e.total);
        })
        .success(function (data) {
          // Update local model with real data
          newfile.id = data.id;
          newfile.size = data.size;

          // New file uploaded, increase used quota
          $rootScope.userInfo.storage_current += data.size;
        })
        .error(function (data) {
          newfile.status = $translate.instant('document.default.upload_error');
          if (data.type === 'QuotaReached') {
            newfile.status += ' - ' + $translate.instant('document.default.upload_error_quota');
          }
        });
  };

  // Navigate to the selected file
  $scope.openFile = function (file) {
    $state.go('document.default.file', { fileId: file.id })
  };

  // Delete a file
  $scope.deleteFile = function ($event, file) {
    $event.stopPropagation();

    Restangular.one('file', file.id).remove().then(function () {
      // File deleted, decrease used quota
      $rootScope.userInfo.storage_current -= file.size;

      // Update local data
      $scope.loadFiles();
    });
    return false;
  };

  // Returns checked files
  $scope.checkedFiles = function () {
    return _.where($scope.files, { checked: true });
  };

  // Change checked status
  $scope.changeChecked = function (checked) {
    _.each($scope.files, function (file) {
      file.checked = checked;
    })
  };

  // Add a document with checked files
  $scope.addDocument = function () {
    $state.go('document.add', { files: _.pluck($scope.checkedFiles(), 'id') });
  };

  // Open the feedback modal
  $scope.openFeedback = function () {
    $uibModal.open({
      templateUrl: 'partial/docs/feedback.html',
      controller: 'ModalFeedback'
    }).result.then(function (content) {
      if (content === null) {
        return;
      }

      Restangular.withConfig(function (RestangularConfigurer) {
        RestangularConfigurer.setBaseUrl('https://api.teedy.io');
      }).one('api').post('feedback', {
        content: content
      }).then(function () {
        var title = $translate.instant('feedback.sent_title');
        var msg = $translate.instant('feedback.sent_message');
        var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
        $dialog.messageBox(title, msg, btns);
      });
    });
  };

  // Load active routes
  Restangular.one('document/list').get({
    asc: false,
    sort_column: 3,
    limit: 10,
    search: 'workflow:me'
  }).then(function (data) {
    $scope.documentsWorkflow = data.documents;
  });

  // Onboarding
  $translate('onboarding.step1.title').then(function () {
    User.userInfo().then(function(userData) {
      if (!userData.onboarding || $(window).width() < 1000) {
        return;
      }
      Restangular.one('user').post('onboarded');
      $rootScope.userInfo.onboarding = false;

      $rootScope.onboardingEnabled = true;

      $rootScope.onboardingSteps = [
        {
          title: $translate.instant('onboarding.step1.title'),
          description: $translate.instant('onboarding.step1.description'),
          position: 'centered',
          width: 300
        },
        {
          title: $translate.instant('onboarding.step2.title'),
          description: $translate.instant('onboarding.step2.description'),
          attachTo: '#document-add-btn',
          position: 'right',
          width: 300
        },
        {
          title: $translate.instant('onboarding.step3.title'),
          description: $translate.instant('onboarding.step3.description'),
          attachTo: '#quick-upload-zone',
          position: 'left',
          width: 300
        },
        {
          title: $translate.instant('onboarding.step4.title'),
          description: $translate.instant('onboarding.step4.description'),
          attachTo: '#search-box',
          position: 'right',
          width: 300
        },
        {
          title: $translate.instant('onboarding.step5.title'),
          description: $translate.instant('onboarding.step5.description'),
          attachTo: '#navigation-tag',
          position: "right",
          width: 300
        }
      ];
    });
  });
});