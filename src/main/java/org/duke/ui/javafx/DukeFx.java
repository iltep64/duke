package org.duke.ui.javafx;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.duke.Duke;
import org.duke.cmd.CommandDispatcher;
import org.duke.ui.DukeIO;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class DukeFx extends Application {

    private static final Duration SCROLL_DELAY = Duration.millis(200);
    private final TextArea inputBox = new TextArea();
    private final DukeFxIO io = new DukeFxIO();
    private final Font baseFont = Font.font(20);

    private final BorderStroke stroke = new BorderStroke(Color.BLACK,
            BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT);
    private final Border msgboxBorder = new Border(stroke);
    private Stage stage;
    private VBox outputCol;

    public static void main(String[] args) {
        Application.launch(args);
    }

    private void onSubmit() {
        String input = inputBox.getText();
        if (!input.isEmpty()) {
            inputBox.clear();
            displayMessage(UserInfo.USER, input);
            io.sendCommand(input);
        }
    }

    private void displayMessage(UserInfo user, String message) {
        Label msgText = new Label(message);
        msgText.setFont(baseFont);

        Label userLabel = new Label(user.getName());
        userLabel.setFont(baseFont);
        userLabel.setTextFill(user.getNameColor());
        userLabel.setUnderline(true);

        Pos textAlignment = user.getAlignmentPosition();
        msgText.setAlignment(textAlignment);
        userLabel.setAlignment(textAlignment);

        VBox msg = new VBox(userLabel, msgText);
        msg.setAlignment(textAlignment);
        msg.setPadding(new Insets(10));
        msg.setBorder(msgboxBorder);
        msg.setBackground(user.getBackground());

        outputCol.getChildren().add(msg);
    }

    private BorderPane makeUserInputPane() {
        inputBox.setFont(baseFont);
        inputBox.addEventFilter(KeyEvent.ANY, evt -> {
            if (evt.getCode() == KeyCode.ENTER || "\n".equals(evt.getCharacter())) {
                evt.consume();
                this.onSubmit();
            }
        });

        Button submitBtn = new Button("Submit");
        submitBtn.setFont(baseFont);
        submitBtn.setDefaultButton(true);
        submitBtn.setMaxHeight(Double.MAX_VALUE);
        submitBtn.setOnAction(evt -> this.onSubmit());

        BorderPane pane = new BorderPane(inputBox);
        pane.setRight(submitBtn);
        return pane;
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        io.startDuke();
        stage.setOnCloseRequest(
                evt -> io.shutdown());

        outputCol = new VBox();
        outputCol.setPadding(new Insets(100));
        outputCol.setSpacing(50);

        ScrollPane outputScroll = new ScrollPane(outputCol);
        outputScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        outputScroll.setFitToWidth(true);

        //Bind auto-scroll of message pane
        Timeline scrollAnim = new Timeline(60);
        scrollAnim.getKeyFrames().add(new KeyFrame(SCROLL_DELAY,
                new KeyValue(outputScroll.vvalueProperty(), 1)));
        outputCol.heightProperty().addListener(obs -> scrollAnim.play());

        BorderPane mainPane = new BorderPane(outputScroll);
        mainPane.setBottom(makeUserInputPane());

        Scene scene = new Scene(mainPane); // Setting the scene to be our Label
        stage.setScene(scene); // Setting the stage to show our screen
        stage.show(); // Render the stage.
    }

    private void startClose() {
        KeyFrame shutdownFrame = new KeyFrame(Duration.seconds(2),
                evt -> {
                    stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
                    stage.close();
                });
        new Timeline(shutdownFrame).play();
    }

    public class DukeFxIO implements DukeIO {
        private final ExecutorService dukeExecutor;
        private final Duke duke;
        private CommandDispatcher dispatcher;
        private ArrayList<String> dialogLines;

        DukeFxIO() {
            dukeExecutor = Executors.newSingleThreadExecutor();
            duke = new Duke(this);
        }

        void shutdown() {
            try {
                dukeExecutor.submit(duke::save).get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            dukeExecutor.shutdown();
        }

        private void startDuke() {
            dukeExecutor.execute(duke::run);
        }

        void sendCommand(String input) {
            dukeExecutor.submit(() -> {
                boolean shouldExit = withDialogBlock(
                        () -> dispatcher.dispatchCommand(input),
                        false);
                if (shouldExit) {
                    Platform.runLater(DukeFx.this::startClose);
                }
            });
        }

        @Override
        public void say(Iterator<String> lines) {
            while (lines.hasNext()) {
                dialogLines.add(lines.next());
            }
        }

        @Override
        public <T> T withDialogBlock(Supplier<T> action, T fallback) {
            T ret = fallback;
            dialogLines = new ArrayList<>();
            try {
                ret = action.get();
            } catch (Exception e) {
                dialogLines.add(e.getMessage());
            }
            ArrayList<String> lines = dialogLines;
            dialogLines = null;
            Platform.runLater(() -> {
                StringBuilder sb = new StringBuilder();
                for (String line : lines) {
                    sb.append(line).append('\n');
                }
                if (!lines.isEmpty()) {
                    //Delete trailing newline
                    sb.deleteCharAt(sb.length() - 1);
                }
                displayMessage(UserInfo.DUKE, sb.toString());
            });
            return ret;
        }

        @Override
        public void listen() {
        }

        @Override
        public void setCommandDispatcher(CommandDispatcher commandDispatcher) {
            this.dispatcher = commandDispatcher;
        }
    }
}

