package com.example.Spendly;

import android.util.Log;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {

    public static void sendEmail(final String toEmail, final String otp) {
        // Create an ExecutorService with a single background thread
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                final String fromEmail = "franzizleo21@gmail.com";
                // TODO: Replace this with your Gmail App Password
                // To get an App Password:
                // 1. Go to your Google Account settings
                // 2. Enable 2-Step Verification if not already enabled
                // 3. Go to Security > App passwords
                // 4. Select "Mail" and "Other (Custom name)"
                // 5. Name it "Spendly App"
                // 6. Copy the 16-character password and paste it here
                final String password = "ewuz hzio rnjt ypbp"; // Replace with your actual app password

                // Gmail SMTP
                Properties properties = new Properties();
                properties.put("mail.smtp.host", "smtp.gmail.com");
                properties.put("mail.smtp.port", "465"); // Port for SSL
                properties.put("mail.smtp.auth", "true");
                properties.put("mail.smtp.socketFactory.port", "465"); // SSL Port
                properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

                // Create session
                Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(fromEmail, password);
                    }
                });

                try {
                    // Email message
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(fromEmail));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                    message.setSubject("Your OTP Code");
                    message.setText("Your OTP code is: " + otp);

                    // Send email
                    Transport.send(message);
                    Log.d("EmailSender", "OTP Email sent successfully to: " + toEmail);
                } catch (Exception e) {
                    Log.e("EmailSender", "Failed to send OTP email to " + toEmail + ": " + e.getMessage());
                    e.printStackTrace();
                    // Add more specific error handling
                    if (e.getMessage().contains("AuthenticationFailedException")) {
                        Log.e("EmailSender", "Gmail authentication failed. Please check the app password.");
                        Log.e("EmailSender", "Email: " + fromEmail);
                        Log.e("EmailSender", "Password length: " + password.length());
                        Log.e("EmailSender", "Password contains spaces: " + password.contains(" "));
                    } else if (e.getMessage().contains("SendFailedException")) {
                        Log.e("EmailSender", "Failed to send email. Please check the recipient email address.");
                    } else if (e.getMessage().contains("SSLHandshakeException")) {
                        Log.e("EmailSender", "SSL handshake failed. Please check your network connection.");
                    }
                }
            }
        });

        executorService.shutdown(); // Shut down the executor
    }
}
