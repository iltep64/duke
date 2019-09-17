package org.duke.ui.javafx;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
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

    public static final Duration SCROLL_DELAY = Duration.millis(200);
    private final DukeFxIO io = new DukeFxIO();
    public static final Font BASE_FONT = Font.font(20);

    private static final BorderStroke stroke = new BorderStroke(Color.BLACK,
            BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT);
    public static final Border MSGBOX_BORDER = new Border(stroke);

    private DukeRootPane root;
    private Stage stage;


    @Override
    public void start(Stage stage) {
        this.stage = stage;

        io.startDuke();
        stage.setOnCloseRequest(
                evt -> io.shutdown());

        root = new DukeRootPane();
        root.setInputHandler(io::sendCommand);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Duke");
        stage.show();
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

        void startDuke() {
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
                root.displayMessage(UserInfo.DUKE, sb.toString());
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

