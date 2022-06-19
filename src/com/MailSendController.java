package com;


import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.web.HTMLEditor;
import javafx.util.Duration;
import org.jsoup.Jsoup;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MailSendController implements Initializable {

    @FXML
    private Button btnAttach;

    @FXML
    private Button btnSend;

    @FXML
    private HTMLEditor htmEditor;

    @FXML
    private TextField txtFrom;

    @FXML
    private TextField txtSubject;

    @FXML
    private Label lblMessage;

    @FXML
    private TextField txtTo;
    String from;
    private boolean sendMail(String from, String to, String subject, String body) {
//        set the server details
        boolean b = false;

        try {
//            get the default comm.session or start a new one.
            Properties props = System.getProperties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class",
                    "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "465");
            String finalFrom = from;
            Session session = Session.getDefaultInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(finalFrom, "your_application_specific_password");
                }
            });
//            create a new message
            Message message = new MimeMessage(session);
//            set the message fields
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));

//            set the message subject and body
            message.setSubject(subject);
            message.setText(body);

//            set other header information
            message.setSentDate(new Date());

//            send the message
            Transport.send(message);
            b = true;
        } catch (MessagingException e) {
            b = false;
        }
        return b;
    }

    public static String getText(String htmlText) {

        String result = "";

        Pattern pattern = Pattern.compile("<[^>]*>");
        Matcher matcher = pattern.matcher(htmlText);
        final StringBuffer text = new StringBuffer(htmlText.length());

        while (matcher.find()) {
            matcher.appendReplacement(
                    text,
                    " ");
        }

        matcher.appendTail(text);

        result = text.toString().trim();

        return result;
    }

    public Timeline createBlinker(Label label) {
        Timeline blink = new Timeline(
                new KeyFrame(
                        Duration.seconds(0),
                        new KeyValue(
                                label.opacityProperty(),
                                1,
                                Interpolator.DISCRETE
                        )
                ),
                new KeyFrame(
                        Duration.seconds(0.5),
                        new KeyValue(
                                label.opacityProperty(),
                                0,
                                Interpolator.DISCRETE
                        )
                ),
                new KeyFrame(
                        Duration.seconds(1),
                        new KeyValue(
                                label.opacityProperty(),
                                1,
                                Interpolator.DISCRETE
                        )
                )
        );
        blink.setCycleCount(3);

        return blink;
    }

    private FadeTransition createFader(Label label) {
        FadeTransition fade = new FadeTransition(Duration.seconds(30), label);
        fade.setFromValue(1);
        fade.setToValue(0);

        return fade;
    }

    //    public static String html2text(String html) {
//        return Jsoup.parse(html).text();
//    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnSend.setOnAction(e -> {
            String from = txtFrom.getText();
            String to = txtTo.getText();
            String subject = txtSubject.getText();
            String body = getText(htmEditor.getHtmlText());

            if(sendMail(from, to, subject, body)) {
                lblMessage.setTextFill(Color.GREEN);
                Timeline blinker = createBlinker(lblMessage);
                blinker.setOnFinished(event -> lblMessage.setText("Message sent successfully!"));
                FadeTransition fader = createFader(lblMessage);

                SequentialTransition blinkThenFade = new SequentialTransition(
                        lblMessage,
                        blinker,
                        fader
                );
                blinkThenFade.play();
                return;
            }
            if (!sendMail(from, to, subject, body)){
                lblMessage.setText("Message not sent!");
                lblMessage.setTextFill(Color.RED);
                return;
            }
        });
    }
}
