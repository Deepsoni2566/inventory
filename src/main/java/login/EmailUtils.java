package login;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailUtils {

    private static final String SENDER_EMAIL = "giiiansuniyo@gmail.com";
    private static final String APP_PASSWORD = "nddx peii qdkg opgj"; // Gmail App Password

    // Generic email sender with both plain text and HTML fallback
    public static void sendEmail(String recipient, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject);

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText("This email requires HTML support.", "utf-8");

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(body, "text/html; charset=utf-8");

            Multipart multipart = new MimeMultipart("alternative");
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(htmlPart);

            message.setContent(multipart);

            Transport.send(message);
            System.out.println("📧 Email sent successfully to " + recipient);
        } catch (MessagingException e) {
            System.err.println("❌ Email sending failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Admin summary sender
    public static void sendAdminReport(String subject, String htmlBody) {
        sendEmail("sonideep022@gmail.com", subject, htmlBody);
    }

    public static void main(String[] args) {
        // Test HTML summary
        String sampleHtml = "<h2>📊 Daily Report</h2><p>Inventory: 250 items</p><p>Sales: £123.45</p>";
        sendAdminReport("Daily Inventory Test", sampleHtml);
    }
}
