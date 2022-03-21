package com.sismics.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.sismics.docs.core.constant.ConfigType;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.ConfigDao;
import com.sismics.docs.core.dao.dto.UserDto;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.model.jpa.Config;
import com.sismics.docs.core.util.ConfigUtil;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import org.apache.commons.mail.HtmlEmail;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
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
    private static void sendEmail(String templateName, UserDto recipientUser, String subject, Map<String, Object> paramMap) {
        if (log.isInfoEnabled()) {
            log.info("Sending email from template=" + templateName + " to user " + recipientUser);
        }
        
        try {
            // Build email headers
            HtmlEmail email = new HtmlEmail();
            email.setCharset(StandardCharsets.UTF_8.name());
            ConfigDao configDao = new ConfigDao();

            // Hostname
            String envHostname = System.getenv(Constants.SMTP_HOSTNAME_ENV);
            if (Strings.isNullOrEmpty(envHostname)) {
                email.setHostName(ConfigUtil.getConfigStringValue(ConfigType.SMTP_HOSTNAME));
            } else {
                email.setHostName(envHostname);
            }

            // Port
            int port = ConfigUtil.getConfigIntegerValue(ConfigType.SMTP_PORT);
            String envPort = System.getenv(Constants.SMTP_PORT_ENV);
            if (!Strings.isNullOrEmpty(envPort)) {
                port = Integer.valueOf(envPort);
            }
            email.setSmtpPort(port);
            if (port == 465) {
                email.setSSLOnConnect(true);
            } else if (port == 587) {
                email.setStartTLSRequired(true);
            }

            // Username and password
            String envUsername = System.getenv(Constants.SMTP_USERNAME_ENV);
            String envPassword = System.getenv(Constants.SMTP_PASSWORD_ENV);
            if (Strings.isNullOrEmpty(envUsername) || Strings.isNullOrEmpty(envPassword)) {
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
            String appName = "Teedy";
            if (themeConfig != null) {
                try (JsonReader reader = Json.createReader(new StringReader(themeConfig.getValue()))) {
                    JsonObject themeJson = reader.readObject();
                    appName = themeJson.getString("name", "Teedy");
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
    public static void sendEmail(String templateName, UserDto recipientUser, Map<String, Object> paramMap) {
        java.util.Locale userLocale = LocaleUtil.getLocale(System.getenv(Constants.DEFAULT_LANGUAGE_ENV));
        String subject = MessageUtil.getMessage(userLocale, "email.template." + templateName + ".subject");
        sendEmail(templateName, recipientUser, subject, paramMap);
    }

    /**
     * Parse an email content to be imported.
     *
     * @param part Email part
     * @param mailContent Mail content modified by side-effect
     *
     * @throws MessagingException e
     * @throws IOException e
     */
    public static void parseMailContent(Part part, MailContent mailContent) throws MessagingException, IOException {
        Object content = part.getContent();
        if (content instanceof Multipart) {
            Multipart multiPart = (Multipart) content;
            int partCount = multiPart.getCount();

            for (int partIndex = 0; partIndex < partCount; partIndex++) {
                MimeBodyPart subPart = (MimeBodyPart) multiPart.getBodyPart(partIndex);
                String disposition = subPart.getDisposition();
                if (Part.ATTACHMENT.equalsIgnoreCase(disposition)) {
                    FileContent fileContent = new FileContent();
                    fileContent.name = subPart.getFileName();
                    fileContent.file = AppContext.getInstance().getFileService().createTemporaryFile();
                    Files.copy(subPart.getInputStream(), fileContent.file, StandardCopyOption.REPLACE_EXISTING);
                    fileContent.size = Files.size(fileContent.file);
                    mailContent.fileContentList.add(fileContent);
                } else {
                    parseMailContent(subPart, mailContent);
                }
            }
        } else if (content instanceof Message) {
            // An email attached to an email, traverse its content
            parseMailContent((Message) content, mailContent);
        } else if (content instanceof String) {
            if (mailContent.message == null) {
                // Do not overwrite the content
                if (part.isMimeType("text/plain")) {
                    mailContent.message = (String) content;
                } else if (part.isMimeType("text/html")) {
                    // Convert HTML to plain text
                    mailContent.message = new HtmlToPlainText().getPlainText(Jsoup.parse((String) content));
                }
            }
        } else if (content instanceof InputStream) {
            FileContent fileContent = new FileContent();
            fileContent.file = AppContext.getInstance().getFileService().createTemporaryFile();
            Files.copy((InputStream) content, fileContent.file, StandardCopyOption.REPLACE_EXISTING);
            fileContent.size = Files.size(fileContent.file);
            mailContent.fileContentList.add(fileContent);
        }
    }

    /**
     * Structure defining a parsed email to be imported.
     */
    public static class MailContent {
        private String subject;
        private String message;
        private Date date;

        List<FileContent> fileContentList = Lists.newArrayList();

        public String getSubject() {
            return subject;
        }

        public String getMessage() {
            return message;
        }

        public List<FileContent> getFileContentList() {
            return fileContentList;
        }

        public MailContent setSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Date getDate() {
            return date;
        }

        public MailContent setDate(Date date) {
            this.date = date;
            return this;
        }
    }

    /**
     * Structure defining a file from an email to be imported.
     */
    public static class FileContent {
        private String name;
        private Path file;
        private long size;

        public String getName() {
            return name;
        }

        public Path getFile() {
            return file;
        }

        public long getSize() {
            return size;
        }
    }
}
