'use strict';

const recursive = require('recursive-readdir');
const ora = require('ora');
const inquirer = require('inquirer');
const preferences = require('preferences');
const fs = require('fs');
const request = require('request').defaults({
  jar: true
});
// request.debug = true;

// Load preferences
const prefs = new preferences('com.sismics.docs.importer',{
  importer: {}
}, {
  encrypt: false,
  format: 'yaml'
});

// Welcome message
console.log('Sismics Docs Importer 1.0.0, https://www.sismicsdocs.com' +
  '\n\n' +
  'This program let you import files from your system to Sismics Docs' +
  '\n');

// Ask for the base URL
const askBaseUrl = () => {
  inquirer.prompt([
    {
      type: 'input',
      name: 'baseUrl',
      message: 'What is the base URL of your Docs? (eg. https://docs.mycompany.com)',
      default: prefs.importer.baseUrl
    }
  ]).then(answers => {
    // Save base URL
    prefs.importer.baseUrl = answers.baseUrl;

    // Test base URL
    const spinner = ora({
      text: 'Checking connection to Docs',
      spinner: 'flips'
    }).start();
    request(answers.baseUrl + '/api/app', function (error, response) {
      if (!response || response.statusCode !== 200) {
        spinner.fail('Connection to Docs failed: ' + error);
        askBaseUrl();
        return;
      }

      spinner.succeed('Connection OK');
      askCredentials();
    });
  });
};
askBaseUrl();

// Ask for credentials
const askCredentials = () => {
  console.log('');

  inquirer.prompt([
    {
      type: 'input',
      name: 'username',
      message: 'Account\'s username?',
      default: prefs.importer.username
    },
    {
      type: 'password',
      name: 'password',
      message: 'Account\'s password?',
      default: prefs.importer.password
    }
  ]).then(answers => {
    // Save credentials
    prefs.importer.username = answers.username;
    prefs.importer.password = answers.password;

    // Test credentials
    const spinner = ora({
      text: 'Checking connection to Docs',
      spinner: 'flips'
    }).start();
    request.post({
      url: prefs.importer.baseUrl + '/api/user/login',
      form: {
        username: answers.username,
        password: answers.password,
        remember: true
      }
    }, function (error, response) {
      if (!response || response.statusCode !== 200) {
        spinner.fail('Username or password incorrect');
        askCredentials();
        return;
      }

      spinner.succeed('Authentication OK');
      askPath();
    });
  });
};

// Ask for the path
const askPath = () => {
  console.log('');

  inquirer.prompt([
    {
      type: 'input',
      name: 'path',
      message: 'What is the folder path you want to import?',
      default: prefs.importer.path
    }
  ]).then(answers => {
    // Save path
    prefs.importer.path = answers.path;

    // Test path
    const spinner = ora({
      text: 'Checking import path',
      spinner: 'flips'
    }).start();
    fs.lstat(answers.path, (err, stats) => {
      if (err || !stats.isDirectory()) {
        spinner.fail('Please enter a valid directory path');
        return;
      }

      recursive(answers.path, function (err, files) {
        spinner.succeed(files.length + ' files in this directory');
        // TODO Then?
      });
    });
  });
};
