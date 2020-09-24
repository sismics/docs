(function() {
  var app;

  app = angular.module("ngOnboarding", []);

  app.provider("ngOnboardingDefaults", function() {
    return {
      options: {
        overlay: true,
        overlayOpacity: 0.4,
        overlayClass: 'onboarding-overlay',
        popoverClass: 'onboarding-popover',
        titleClass: 'onboarding-popover-title',
        contentClass: 'onboarding-popover-content',
        arrowClass: 'onboarding-arrow',
        buttonContainerClass: 'onboarding-button-container',
        buttonClass: "btn",
        showButtons: true,
        nextButtonText: '<span class="fa fa-arrow-right"></span>',
        previousButtonText: '<span class="fa fa-arrow-left"></span>',
        showDoneButton: true,
        doneButtonText: '<span class="fa fa-check"></span>',
        closeButtonClass: 'onboarding-close-button',
        closeButtonText: '<span class="fa fa-times"></span>',
        stepClass: 'onboarding-step-info',
        showStepInfo: true
      },
      $get: function() {
        return this.options;
      },
      set: function(keyOrHash, value) {
        var k, v, _results;
        if (typeof keyOrHash === 'object') {
          _results = [];
          for (k in keyOrHash) {
            v = keyOrHash[k];
            _results.push(this.options[k] = v);
          }
          return _results;
        } else {
          return this.options[keyOrHash] = value;
        }
      }
    };
  });

  app.directive('onboardingPopover', [
    'ngOnboardingDefaults', '$sce', '$timeout', function(ngOnboardingDefaults, $sce, $timeout) {
      return {
        restrict: 'E',
        scope: {
          enabled: '=',
          steps: '=',
          onFinishCallback: '&onFinishCallback',
          index: '=?stepIndex'
        },
        replace: true,
        link: function(scope, element, attrs) {
          var attributesToClear, curStep, setupOverlay, setupPositioning;
          curStep = null;
          attributesToClear = ['title', 'top', 'right', 'bottom', 'left', 'width', 'height', 'position'];
          scope.stepCount = scope.steps.length;
          scope.next = function() {
            return scope.index = scope.index + 1;
          };
          scope.previous = function() {
            return scope.index = scope.index - 1;
          };
          scope.close = function() {
            scope.enabled = false;
            setupOverlay(false);
            if (scope.onFinishCallback) {
              return scope.onFinishCallback();
            }
          };
          scope.$watch('index', function(newVal, oldVal) {
            var attr, k, v, _i, _len;
            if (newVal === null) {
              scope.enabled = false;
              setupOverlay(false);
              return;
            }
            curStep = scope.steps[scope.index];
            scope.lastStep = scope.index + 1 === scope.steps.length;
            scope.showNextButton = scope.index + 1 < scope.steps.length;
            scope.showPreviousButton = scope.index > 0;
            for (_i = 0, _len = attributesToClear.length; _i < _len; _i++) {
              attr = attributesToClear[_i];
              scope[attr] = null;
            }
            for (k in ngOnboardingDefaults) {
              v = ngOnboardingDefaults[k];
              if (curStep[k] === void 0) {
                scope[k] = v;
              }
            }
            for (k in curStep) {
              v = curStep[k];
              scope[k] = v;
            }
            scope.description = $sce.trustAsHtml(scope.description);
            scope.nextButtonText = $sce.trustAsHtml(scope.nextButtonText);
            scope.previousButtonText = $sce.trustAsHtml(scope.previousButtonText);
            scope.doneButtonText = $sce.trustAsHtml(scope.doneButtonText);
            scope.closeButtonText = $sce.trustAsHtml(scope.closeButtonText);
            setupOverlay();
            return setupPositioning();
          });
          setupOverlay = function(showOverlay) {
            if (showOverlay == null) {
              showOverlay = true;
            }
            $('.onboarding-focus').removeClass('onboarding-focus');
            if (showOverlay) {
              if (curStep['attachTo'] && scope.overlay) {
                return $(curStep['attachTo']).addClass('onboarding-focus');
              }
            }
          };
          setupPositioning = function() {
            var attachTo, bottom, left, right, top, xMargin, yMargin;
            attachTo = curStep['attachTo'];
            scope.position = curStep['position'];
            xMargin = 15;
            yMargin = 15;
            if (attachTo) {
              if (!(scope.left || scope.right)) {
                left = null;
                right = null;
                if (scope.position === 'right') {
                  left = $(attachTo).offset().left + $(attachTo).outerWidth() + xMargin;
                } else if (scope.position === 'left') {
                  right = $(window).width() - $(attachTo).offset().left + xMargin;
                } else if (scope.position === 'top' || scope.position === 'bottom') {
                  left = $(attachTo).offset().left;
                }
                if (curStep['xOffset']) {
                  if (left !== null) {
                    left = left + curStep['xOffset'];
                  }
                  if (right !== null) {
                    right = right - curStep['xOffset'];
                  }
                }
                scope.left = left;
                scope.right = right;
              }
              if (!(scope.top || scope.bottom)) {
                top = null;
                bottom = null;
                if (scope.position === 'left' || scope.position === 'right') {
                  top = $(attachTo).offset().top + $(attachTo).outerHeight() / 2 - 14;
                } else if (scope.position === 'bottom') {
                  top = $(attachTo).offset().top + $(attachTo).outerHeight() + yMargin;
                } else if (scope.position === 'top') {
                  bottom = $(window).height() - $(attachTo).offset().top + yMargin;
                }
                if (curStep['yOffset']) {
                  if (top !== null) {
                    top = top + curStep['yOffset'];
                  }
                  if (bottom !== null) {
                    bottom = bottom - curStep['yOffset'];
                  }
                }
                scope.top = top;
                scope.bottom = bottom;
              }
            }
            if (scope.position && scope.position.length) {
              return scope.positionClass = "onboarding-" + scope.position;
            } else {
              return scope.positionClass = null;
            }
          };
          if (scope.steps.length && !scope.index) {
            return scope.index = 0;
          }
        },
        template: "<div class='onboarding-container' ng-show='enabled'>\n  <div class='{{overlayClass}}' ng-style='{opacity: overlayOpacity}', ng-show='overlay'></div>\n  <div class='{{popoverClass}} {{positionClass}}' ng-style=\"{width: width, height: height, left: left, top: top, right: right, bottom: bottom}\">\n    <div class='{{arrowClass}}'></div>\n    <h3 class='{{titleClass}}' ng-show='title' ng-bind='title'></h3>\n    <a href='' ng-click='close()' class='{{closeButtonClass}}' ng-bind-html='closeButtonText'></a>\n    <div class='{{contentClass}}'>\n      <p ng-bind-html='description'></p>\n    </div>\n    <div class='{{buttonContainerClass}}' ng-show='showButtons'>\n      <span ng-show='showStepInfo' class='{{stepClass}}'>{{index + 1}}/{{stepCount}}</span>\n      <a href='' ng-click='previous()' ng-show='showPreviousButton' class='{{buttonClass}}' ng-bind-html='previousButtonText'></a>\n      <a href='' ng-click='next()' ng-show='showNextButton' class='{{buttonClass}}' ng-bind-html='nextButtonText'></a>\n      <a href='' ng-click='close()' ng-show='showDoneButton && lastStep' class='{{buttonClass}}' ng-bind-html='doneButtonText'></a>\n    </div>\n  </div>\n</div>"
      };
    }
  ]);

}).call(this);
