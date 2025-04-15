package com.example.myapplication;

import android.os.AsyncTask;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import android.util.Log;

public class EmailSender {

    public static void sendEmail(final String toEmail, final String otp) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                final String fromEmail = "franzizleo21@gmail.com"; // Your Gmail address
                final String password = "mssi upja nwgj ojeh "; // Your App Password

                // Set up the properties for Gmail SMTP
                Properties properties = new Properties();
                properties.put("mail.smtp.host", "smtp.gmail.com");
                properties.put("mail.smtp.port", "465"); // Port for SSL
                properties.put("mail.smtp.auth", "true");
                properties.put("mail.smtp.socketFactory.port", "465"); // SSL Port
                properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

                // Create a session with your email and password
                Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(fromEmail, password);
                    }
                });

                try {
                    // Create the email message
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(fromEmail));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                    message.setSubject("Your OTP Code");
                    message.setText("Your OTP code is: " + otp);

                    // Send the email
                    Transport.send(message);
                    Log.d("EmailSender", "OTP Email sent successfully!");
                } catch (Exception e) {
                    Log.e("EmailSender", "Failed to send OTP email: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }
}
