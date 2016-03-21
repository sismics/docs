'use strict';

/**
 * Settings group edition page controller.
 */
angular.module('docs').controller('SettingsGroupEdit', function($scope, $dialog, $state, $stateParams, Restangular, $q) {
  /**
   * Returns true if in edit mode (false in add mode).
   */
  $scope.isEdit = function() {
    return $stateParams.name;
  };
  
  /**
   * In edit mode, load the current group.
   */
  if ($scope.isEdit()) {
    Restangular.one('group', $stateParams.name).get().then(function(data) {
      $scope.group = data;
    });
  }

  /**
   * Update the current group.
   */
  $scope.edit = function() {
    var promise = null;
    var group = angular.copy($scope.group);

    if ($scope.isEdit()) {
      promise = Restangular
        .one('group', $stateParams.name)
        .post('', group);
    } else {
      promise = Restangular
        .one('group')
        .put(group);
    }
    
    promise.then(function() {
      $scope.loadGroups();
      if ($scope.isEdit()) {
        $state.go('settings.group');
      } else {
        // Go to edit this group to add members
        $state.go('settings.group.edit', { name: group.name });
      }
    });
  };

  /**
   * Delete the current group.
   */
  $scope.remove = function() {
    var title = 'Delete group';
    var msg = 'Do you really want to delete this group?';
    var btns = [{result:'cancel', label: 'Cancel'}, {result:'ok', label: 'OK', cssClass: 'btn-primary'}];

    $dialog.messageBox(title, msg, btns, function(result) {
      if (result == 'ok') {
        Restangular.one('group', $stateParams.name).remove().then(function() {
          $scope.loadGroups();
          $state.go('settings.group');
        }, function() {
          $state.go('settings.group');
        });
      }
    });
  };

  /**
   * Returns a promise for typeahead group.
   */
  $scope.getGroupTypeahead = function($viewValue) {
    var deferred = $q.defer();
    Restangular.one('group')
        .getList('', {
          sort_column: 1,
          asc: true
        }).then(function(data) {
      deferred.resolve(_.pluck(_.filter(data.groups, function(group) {
        return group.name.indexOf($viewValue) !== -1;
      }), 'name'));
    });
    return deferred.promise;
  };

  /**
   * Returns a promise for typeahead user.
   */
  $scope.getUserTypeahead = function($viewValue) {
    var deferred = $q.defer();
    Restangular.one('user')
        .getList('list', {
          search: $viewValue,
          sort_column: 1,
          asc: true
        }).then(function(data) {
      deferred.resolve(_.pluck(_.filter(data.users, function(user) {
        return user.username.indexOf($viewValue) !== -1;
      }), 'username'));
    });
    return deferred.promise;
  };

  /**
   * Add a new member.
   */
  $scope.addMember = function(member) {
    $scope.member = '';
    Restangular.one('group/' + $stateParams.name).put({
      username: member
    }).then(function() {
      if ($scope.group.members.indexOf(member) === -1) {
        $scope.group.members.push(member);
      }
    });
  };

  /**
   * Remove a member.
   */
  $scope.removeMember = function(member) {
    Restangular.one('group/' + $stateParams.name, member).remove().then(function() {
      $scope.group.members.splice($scope.group.members.indexOf(member), 1);
    });
  };
});