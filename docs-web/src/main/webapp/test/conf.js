'use strict';

exports.config = {
  seleniumServerJar: '../node_modules/selenium/lib/runner/selenium-server-standalone-2.20.0.jar',
  framework: 'jasmine',
  rootElement: 'html',
  baseUrl: 'http://localhost:9999/docs-web/src/?protractor',
  capabilities: {
    'browserName': 'chrome'
  },
 
  specs: [
    'specs/**/*.js'
  ],

  jasmineNodeOpts: {
    isVerbose: true,
    showColors: true,
    defaultTimeoutInterval: 30000
  }
};