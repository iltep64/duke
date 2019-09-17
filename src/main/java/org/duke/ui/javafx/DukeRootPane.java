package org.duke.ui.javafx;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.duke.DukeException;

import java.io.IOException;
import java.util.function.Consumer;

public class DukeRootPane extends BorderPane {

    private final Property<Consumer<String>> inputHandler
            = new SimpleObjectProperty<>();
    @FXML
    private ScrollPane outputScroll;
    @FXML
    private VBox outputCol;
    @FXML
    private Button submitButton;
    @FXML
    private TextArea inputBox;

    public DukeRootPane() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("DukeRootPane.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new DukeException(e);
        }

        submitButton.setOnAction(evt -> onSubmit());
        inputBox.addEventFilter(KeyEvent.ANY, evt -> {
            if (evt.getCode() == KeyCode.ENTER || "\n".equals(evt.getCharacter())) {
                evt.consume();
                this.onSubmit();
            }
        });

        //Bind auto-scroll of message pane
        Timeline scrollAnim = new Timeline(60);
        scrollAnim.getKeyFrames().add(new KeyFrame(DukeFx.SCROLL_DELAY,
                new KeyValue(outputScroll.vvalueProperty(), 1)));
        outputCol.heightProperty().addListener(obs -> scrollAnim.play());
    }

    private void onSubmit() {
        String input = inputBox.getText();
        if (!input.isEmpty()) {
            inputBox.clear();
            displayMessage(UserInfo.USER, input);
            if (inputHandler.getValue() != null) {
                inputHandler.getValue().accept(input);
            }
        }
    }

    public Consumer<String> getInputHandler() {
        return inputHandler.getValue();
    }

    public void setInputHandler(Consumer<String> value) {
        inputHandler.setValue(value);
    }

    public Property<Consumer<String>> inputHandler() {
        return inputHandler;
    }

    public void displayMessage(UserInfo user, String message) {
        Message msg = new Message(user);
        msg.setText(message);
        outputCol.getChildren().add(msg);
    }
}
