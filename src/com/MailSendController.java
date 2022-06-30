package com;


import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javafx.scene.image.ImageView;
import java.io.File;
import java.net.URL;
import java.util.*;

public class MailSendController implements Initializable {

    @FXML
    private Button btnAttach;

    @FXML
    private Button btnSend;

    @FXML
    private HTMLEditor htmEditor;

    @FXML
    private VBox VboxAttach;

    @FXML
    private TextField txtFrom;

    @FXML
    private TextField txtSubject;

    @FXML
    private Label lblMessage;

    @FXML
    private TextField txtTo;
    String from;

    List<File> files = new ArrayList<>();
    private boolean sendMail(String from, String to, String subject, String body) {
//        set the server details
        boolean b = false;
        SendMail details = new SendMail(from, to, subject, body);

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
            Message message = createText(session, details);

            Multipart multipart = new MimeMultipart();

            files.forEach(file -> {
                addAttachment(multipart, file);
            });
            message.setContent(multipart);


            message.setSentDate(new Date());

//            send the message
            Transport.send(message);
            b = true;
        } catch (MessagingException e) {
            b = false;
        }
        return b;
    }

    public static Message createText(Session session, SendMail details) {
        Message message = null;
        try {

            message = new MimeMessage(session);
            message.setFrom(new InternetAddress(details.from));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(details.to));
            message.setSubject(details.subject);
            String msg = details.body;
            message.setContent(msg, "text/html");
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return message;

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



    public File filePath() {
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if(file == null) {
            return null;
        }
        VboxAttach.getChildren().add(attachedFile(file.getName()));
        return  file;
    }

    public Node attachedFile(String fileName){
        HBox hbox = new HBox();
        String extension = fileName.split("\\.")[fileName.split("\\.").length - 1].toLowerCase();
        System.out.println(extension);
        Image image;
        switch (extension) {
            case "pdf" :
                image = new Image("/com/resource/pdf.png");
                break;
            case "docx":
            case "odt":
                image = new Image("/com/resource/docx-file.png");
                break;
            case "png":
            case "jpg":
            case "jpeg":
                image = new Image("/com/resource/picture.png");
                break;
            case "xls":
            case "xlsx":
            case "xlsm":
            case "xlsb":
                image = new Image("/com/resource/sheets.png");
                break;
            case "mp4":
            case "mkv":
                image = new Image("/com/resource/clapperboard.png");
                break;
            default:
               image =  new Image("/com/resource/data-storage.png");
                break;
        }
        System.out.println(image.getUrl());
        ImageView imageView = new ImageView(image);
        ImageView btnGraphic = new ImageView(new Image("/com/resource/cancel.png"));
        Label label = new Label(fileName);
        label.setPrefWidth(300.0);
        Button btnDelete = new Button();
        btnDelete.setGraphic(btnGraphic);
        btnGraphic.setFitWidth(20.0);
        btnGraphic.setFitHeight(20.0);
        btnDelete.setStyle("-fx-background-color: transparent");
        btnDelete.setCursor(Cursor.HAND);
        btnDelete.setOnAction(e -> {
            removeFile(fileName);
        });
        imageView.setFitWidth(20.0);
        imageView.setFitHeight(20.0);
        Insets insets = new Insets(10);
        hbox.setPadding(insets);
        hbox.setSpacing(15.0);

        hbox.getChildren().addAll(imageView, label, btnDelete);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPrefWidth(370.0);
        hbox.widthProperty().addListener((observable, oldVal, newVal) -> {
            if((double)newVal > 300)
            label.setPrefWidth((double)newVal - 60.0);
        });
        return hbox;
    }

    public void removeFile(String fileName) {
            files.removeIf(f -> {
                boolean test = f.getName().equals(fileName);
                return test;
            });
            reloadAttachment();
    }

    public void reloadAttachment() {
        VboxAttach.getChildren().clear();

        files.forEach(file -> {
            VboxAttach.getChildren().add(attachedFile(file.getName()));
        });
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnAttach.setStyle("-fx-background-color: transparent");

        btnSend.setOnAction(e -> {

            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    String from = txtFrom.getText();
                    String to = txtTo.getText();
                    String subject = txtSubject.getText();
                    String body = htmEditor.getHtmlText();
                    System.out.println(body);

                    if(sendMail(from, to, subject, body)) {
                        System.out.println("success");
                        Platform.runLater(() -> {
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
                        });
                        return null;
                    }
                    System.out.println("error");

                    Platform.runLater(() -> {
                        lblMessage.setText("Message not sent!");
                        lblMessage.setTextFill(Color.RED);
                    });
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    System.out.println("finished");
                }
            };
            new Thread(task).start();

        });
        btnAttach.setOnAction(e  -> {
            files.add(filePath());
        });
    }

    public void addAttachment(Multipart multipart, File file) {
        MimeBodyPart messageBodyPart = new MimeBodyPart();

        String filePath = file.getAbsolutePath();
        String fileName = file.getName();
        DataSource source = new FileDataSource(filePath);
        try {
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(fileName);
            multipart.addBodyPart(messageBodyPart);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
