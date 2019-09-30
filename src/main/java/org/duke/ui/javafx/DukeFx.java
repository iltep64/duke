package org.duke.ui.javafx;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.duke.Duke;
import org.duke.cmd.CommandDispatcher;
import org.duke.cmd.Handler;
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
    public static final Font MONO_FONT = Font.font("monospace", 20);

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
        private ArrayList<Node> dialogNodes;

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
                Label line = new Label(lines.next());
                line.setFont(BASE_FONT);
                dialogNodes.add(line);
            }
        }

        @Override
        public void sayBriefCommand(Handler handler) {
            TextFlow desc = makeDescriptionLine(handler);
            desc.setPadding(new Insets(5));
            dialogNodes.add(desc);
        }

        private TextFlow makeDescriptionLine(Handler handler) {
            Text name = new Text(handler.getPrimaryBinding());
            name.setFont(MONO_FONT);
            Text desc = new Text(" - " + handler.getDescriptionText().value());
            desc.setFont(BASE_FONT);
            return new TextFlow(name, desc);
        }

        private Text makeArgumentNode(String text, boolean optional) {
            Color color;
            if (optional) {
                text = " [" + text + "]";
                color = Color.GREEN;
            } else {
                text = " <" + text + ">";
                color = Color.BROWN;
            }
            Text node = new Text(text);
            node.setFont(MONO_FONT);
            node.setFill(color);
            return node;
        }

        @Override
        public void sayCommand(Handler handler) {
            dialogNodes.add(makeDescriptionLine(handler));

            Text name = new Text(handler.getPrimaryBinding());
            name.setFont(MONO_FONT);
            TextFlow syntaxLine = new TextFlow(name);

            if (!handler.getDescriptionText().argument().isEmpty()) {
                String argDesc = handler.getDescriptionText().argument();
                Text posArgument = makeArgumentNode(argDesc, handler.getDescriptionText().optional());
                syntaxLine.getChildren().add(posArgument);
            }

            for (Handler.NamedArgument namedArgument : handler.getNamedArguments()) {
                String argDesc = "/" + namedArgument.value() + " " + namedArgument.description();
                syntaxLine.getChildren().add(makeArgumentNode(argDesc, true));
            }

            dialogNodes.add(syntaxLine);

            if (handler.getBindings().length > 1) {

                Text aliasPrefix = new Text("Aliases:");
                aliasPrefix.setFont(BASE_FONT);
                StringBuilder sb = new StringBuilder();
                for (Handler.Binding bind : handler.getBindings()) {
                    if (bind.value().equals(handler.getPrimaryBinding())) {
                        continue;
                    }
                    if (sb.length() > 0) {
                        sb.append(',');
                    }
                    sb.append(' ').append(bind.value());
                }

                Text aliases = new Text(sb.toString());
                aliases.setFont(MONO_FONT);
                TextFlow aliasLine = new TextFlow(aliasPrefix, aliases);
                dialogNodes.add(aliasLine);
            }
        }

        @Override
        public <T> T withDialogBlock(Supplier<T> action, T fallback) {
            T ret = fallback;
            dialogNodes = new ArrayList<>();
            try {
                ret = action.get();
            } catch (Exception e) {
                Label err = new Label(e.getMessage());
                err.setFont(BASE_FONT);
                err.setTextFill(Color.RED);
                dialogNodes.add(err);
            }
            ArrayList<Node> lines = dialogNodes;
            dialogNodes = null;
            Platform.runLater(() -> root.displayMessage(UserInfo.DUKE, lines));
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

