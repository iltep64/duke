package org.duke;

import org.duke.cmd.*;
import org.duke.json.JsonParser;
import org.duke.json.JsonWriter;
import org.duke.json.ValueHandler;
import org.duke.task.DeadlineTask;
import org.duke.task.EventTask;
import org.duke.task.Task;
import org.duke.task.TaskType;
import org.duke.ui.DukeIO;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Main class for Duke.
 */
public class Duke {

    private static final String[] initialGreeting = new String[]{
            "Hello! I'm Duke",
            "What can I do for you?"
    };
    private static final String SAVE_PATH = "./duke.json";
    private final CommandDispatcher dispatcher;
    private final DukeIO io;
    private ArrayList<Task> taskList;
    public Duke(DukeIO io) {
        this.io = io;

        this.dispatcher = new CommandDispatcher(this);
        //Bind command handlers
        this.dispatcher.bindCommands(
                new ListHandler(),
                new DoneHandler(),
                new ByeHandler(),
                new FindHandler(),
                new TodoTaskHandler(),
                new DeadlineTaskHandler(),
                new EventTaskHandler(),
                new DeleteHandler(),
                new HelpHandler()
        );
        this.dispatcher.setUnknownCommandHandler(new Handler() {
            @Override
            protected void handleNoExit(Duke duke, Command command) {
                throw new DukeException("I'm sorry, but I don't know what that means. :-(");
            }
        });
        this.io.setCommandDispatcher(this.dispatcher);
    }

    public CommandDispatcher getDispatcher() {
        return dispatcher;
    }

    public void addTask(Task t) {
        this.taskList.add(t);
        this.io.say(
                "Got it. I've added this task:",
                "  " + t,
                String.format("Now you have %d task%s in the list.",
                        this.taskList.size(),
                        this.taskList.size() == 1 ? "" : "s")
        );
    }

    private ArrayList<Task> loadStorage() {
        try (FileReader read = new FileReader(SAVE_PATH)) {
            return JsonParser.parse(read, ValueHandler.listOf(new TaskType.Builder()));
        } catch (FileNotFoundException e) {
            return new ArrayList<>();
        } catch (IOException e) {
            throw new DukeException("Unable to load saved data", e);
        }
    }

    private void saveStorage(ArrayList<Task> tasks) {
        try (FileWriter write = new FileWriter(SAVE_PATH);
             JsonWriter jw = new JsonWriter(write)) {
            jw.writeValue(tasks);
        } catch (Exception e) {
            throw new DukeException(e);
        }
    }

    public void run() {
        this.loadStorage();
        //Start off greeting the user.
        this.io.withDialogBlock(() -> {
            this.io.say(initialGreeting);
            this.taskList = this.loadStorage();
        });

        //Start listen loop.
        this.io.listen();
    }

    public void save() {
        this.io.withDialogBlock(() -> this.saveStorage(this.taskList));
    }

    public DukeIO getIo() {
        return this.io;
    }

    public ArrayList<Task> getTaskList() {
        return this.taskList;
    }
}
