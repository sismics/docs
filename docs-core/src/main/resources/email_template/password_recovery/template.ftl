<#import "../layout.ftl" as layout>
<@layout.email>
  <h2>${app_name} - ${messages['email.template.password_recovery.subject']}</h2>
  <p>${messages('email.template.password_recovery.hello', user_name)}</p>
  <p>${messages['email.template.password_recovery.instruction1']}</p>
  <p>${messages['email.template.password_recovery.instruction2']}</p>
  <a href="${base_url}/#/passwordreset/${password_recovery_key}">${messages['email.template.password_recovery.click_here']}</a>
</@layout.email>