'use strict';
 
describe('document', function () {
  it('should create and delete a document', function () {
    browser.get('');

    // Login as admin
    element(by.model('user.username')).sendKeys('admin');
    element(by.model('user.password')).sendKeys('admin');
    element(by.css('.login-box button[type="submit"]')).click();

    // Create a document
    element(by.partialLinkText('Add a document')).click();
    element(by.model('document.title')).sendKeys('My test document');
    element(by.buttonText('Add')).click();

    // Open the last document
    element(by.css('.table-documents tbody tr:nth-child(1)')).click();

    // Delete the document
    element(by.partialButtonText('Delete')).click();
    element(by.partialButtonText('OK')).click();
  });
});