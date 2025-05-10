package com.example.myapplication;

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
                final String password = "mssi upja nwgj ojeh "; // Replace with your actual password

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
                    Log.d("EmailSender", "OTP Email sent successfully!");
                } catch (Exception e) {
                    Log.e("EmailSender", "Failed to send OTP email: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        executorService.shutdown(); // Shut down the executor
    }
}
