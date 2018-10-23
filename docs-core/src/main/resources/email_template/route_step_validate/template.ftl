<#import "../layout.ftl" as layout>
<@layout.email>
  <h2>${app_name} - ${messages['email.template.route_step_validate.subject']}</h2>
  <p>${messages('email.template.route_step_validate.hello', user_name)}</p>
  <p>${messages['email.template.route_step_validate.instruction1']}</p>
  <p>${messages['email.template.route_step_validate.instruction2']}</p>
  <a href="${base_url}/#/document/${document_id}">${document_title}</a>
</@layout.email>