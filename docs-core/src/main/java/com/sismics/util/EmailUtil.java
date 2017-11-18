package com.sismics.util;

import com.google.common.base.Strings;
import com.sismics.docs.core.constant.ConfigType;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.jpa.ConfigDao;
import com.sismics.docs.core.model.jpa.Config;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.ConfigUtil;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

/**
 * Emails utilities.
 * 
 * @author jtremeaux
 */
public class EmailUtil {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(EmailUtil.class);

    /**
     * Returns an email content as string.
     * The content is formatted from the given Freemarker template and parameters.
     * 
     * @param templateName Template name
     * @param paramRootMap Map of Freemarker parameters
     * @param locale Locale
     * @return Template as string
     * @throws Exception e
     */
    private static String getFormattedHtml(String templateName, Map<String, Object> paramRootMap, Locale locale) throws Exception {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setClassForTemplateLoading(EmailUtil.class, "/email_template");
        cfg.setObjectWrapper(new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_23).build());
        Template template = cfg.getTemplate(templateName + "/template.ftl");
        paramRootMap.put("messages", new ResourceBundleModel(MessageUtil.getMessage(locale),
                new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_23).build()));
        StringWriter sw = new StringWriter();
        template.process(paramRootMap, sw);
        
        return sw.toString();
    }
    
    /**
     * Sending an email to a user.
     * 
     * @param templateName Template name
     * @param recipientUser Recipient user
     * @param subject Email subject
     * @param paramMap Email parameters
     */
    public static void sendEmail(String templateName, User recipientUser, String subject, Map<String, Object> paramMap) {
        if (log.isInfoEnabled()) {
            log.info("Sending email from template=" + templateName + " to user " + recipientUser);
        }
        
        try {
            // Build email headers
            HtmlEmail email = new HtmlEmail();
            email.setCharset("UTF-8");
            ConfigDao configDao = new ConfigDao();

            // Hostname
            String envHostname = System.getenv(Constants.SMTP_HOSTNAME_ENV);
            if (envHostname == null) {
                email.setHostName(ConfigUtil.getConfigStringValue(ConfigType.SMTP_HOSTNAME));
            } else {
                email.setHostName(envHostname);
            }

            // Port
            String envPort = System.getenv(Constants.SMTP_PORT_ENV);
            if (envPort == null) {
                email.setSmtpPort(ConfigUtil.getConfigIntegerValue(ConfigType.SMTP_PORT));
            } else {
                email.setSmtpPort(Integer.valueOf(envPort));
            }

            // Username and password
            String envUsername = System.getenv(Constants.SMTP_USERNAME_ENV);
            String envPassword = System.getenv(Constants.SMTP_PASSWORD_ENV);
            if (envUsername == null || envPassword == null) {
                Config usernameConfig = configDao.getById(ConfigType.SMTP_USERNAME);
                Config passwordConfig = configDao.getById(ConfigType.SMTP_PASSWORD);
                if (usernameConfig != null && passwordConfig != null) {
                    email.setAuthentication(usernameConfig.getValue(), passwordConfig.getValue());
                }
            } else {
                email.setAuthentication(envUsername, envPassword);
            }

            // Recipient
            email.addTo(recipientUser.getEmail(), recipientUser.getUsername());

            // Application name
            Config themeConfig = configDao.getById(ConfigType.THEME);
            String appName = "Sismics Docs";
            if (themeConfig != null) {
                try (JsonReader reader = Json.createReader(new StringReader(themeConfig.getValue()))) {
                    JsonObject themeJson = reader.readObject();
                    appName = themeJson.getString("name", "Sismics Docs");
                }
            }

            // From email address (defined only by configuration value in the database)
            email.setFrom(ConfigUtil.getConfigStringValue(ConfigType.SMTP_FROM), appName);

            // Locale (defined only by environment variable)
            java.util.Locale userLocale = LocaleUtil.getLocale(System.getenv(Constants.DEFAULT_LANGUAGE_ENV));

            // Subject and content
            email.setSubject(appName + " - " + subject);
            email.setTextMsg(MessageUtil.getMessage(userLocale, "email.no_html.error"));

            // Add automatic parameters
            String baseUrl = System.getenv(Constants.BASE_URL_ENV);
            if (Strings.isNullOrEmpty(baseUrl)) {
                log.error("DOCS_BASE_URL environnement variable needs to be set for proper email links");
                baseUrl = ""; // At least the mail will be sent...
            }
            paramMap.put("base_url", baseUrl);
            paramMap.put("app_name", appName);

            // Build HTML content from Freemarker template
            String htmlEmailTemplate = getFormattedHtml(templateName, paramMap, userLocale);
            email.setHtmlMsg(htmlEmailTemplate);

            // Send the email
            email.send();
        } catch (Exception e) {
            log.error("Error sending email with template=" + templateName + " to user " + recipientUser, e);
        }
    }

    /**
     * Sending an email to a user.
     *
     * @param templateName Template name
     * @param recipientUser Recipient user
     * @param paramMap Email parameters
     */
    public static void sendEmail(String templateName, User recipientUser, Map<String, Object> paramMap) {
        java.util.Locale userLocale = LocaleUtil.getLocale(System.getenv(Constants.DEFAULT_LANGUAGE_ENV));
        String subject = MessageUtil.getMessage(userLocale, "email.template." + templateName + ".subject");
        sendEmail(templateName, recipientUser, subject, paramMap);
    }
}
