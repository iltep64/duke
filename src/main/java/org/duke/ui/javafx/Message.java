package org.duke.ui.javafx;

import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.duke.DukeException;

import java.io.IOException;

public class Message extends HBox {
    @FXML
    private Label userIcon;

    @FXML
    private Node message;

    public Message(UserInfo user, Node message) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Message.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new DukeException(e);
        }

        this.message = message;

        userIcon.setText(user.getName());
        userIcon.setGraphic(new ImageView(user.getImage()));
        userIcon.setTextFill(user.getNameColor());

        Pos textAlignment = user.getAlignmentPosition();
        //message.setAlignment(textAlignment);
        userIcon.setAlignment(textAlignment);

        setAlignment(textAlignment);
        setBackground(user.getBackground());

        if (user.getAlignmentPosition().getHpos() == HPos.LEFT) {
            this.getChildren().addAll(userIcon, message);
        } else {
            this.getChildren().addAll(message, userIcon);
        }
    }

    public Node getMessage() {
        return message;
    }

    public void setMessage(Node message) {
        this.message = message;
    }
/*
    public StringProperty textProperty() {
        return messageText.textProperty();
    }

    public String getText() {
        return messageText.getText();
    }

    public void setText(String value) {
        messageText.setText(value);
    }
    */
}
