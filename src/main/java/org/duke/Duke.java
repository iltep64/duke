package org.duke;

import org.duke.cmd.Command;
import org.duke.cmd.CommandDispatcher;
import org.duke.json.JsonParser;
import org.duke.json.JsonWriter;
import org.duke.json.ValueHandler;
import org.duke.task.DeadlineTask;
import org.duke.task.EventTask;
import org.duke.task.Task;
import org.duke.task.TaskType;
import org.duke.ui.DukeIO;
import org.duke.util.CounterDecorator;

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

        this.dispatcher = new CommandDispatcher();
        //Bind command handlers
        this.dispatcher.bindCommand("list", this::displayList);
        this.dispatcher.bindCommand("done", this::markAsDone);
        this.dispatcher.bindCommand("bye", this::handleBye);
        this.dispatcher.bindCommand("deadline", this::makeDeadlineTask);
        this.dispatcher.bindCommand("event", this::makeEventTask);
        this.dispatcher.bindCommand("todo", this::makeToDoTask);
        this.dispatcher.bindCommand("delete", this::deleteTask);
        this.dispatcher.bindCommand("find", this::findTasks);
        this.dispatcher.setUnknownCommandHandler(cmd -> {
            throw new DukeException("I'm sorry, but I don't know what that means. :-(");
        });
        this.io.setCommandDispatcher(this.dispatcher);

    }

    private void addTask(Task t) {
        this.taskList.add(t);
        this.io.say(
                "Got it. I've added this task:",
                "  " + t,
                String.format("Now you have %d task%s in the list.",
                        this.taskList.size(),
                        this.taskList.size() == 1 ? "" : "s")
        );
    }

    private boolean handleBye(Command input) {
        this.io.say("Bye. Hope to see you again soon!");
        return true;
    }

    private boolean displayList(Command input) {
        this.io.say("Here are the tasks in your list:");
        this.io.say(this.taskList.stream()
                .map(Object::toString)
                .map(new CounterDecorator(1))
                .iterator());
        return false;
    }

    private boolean makeDeadlineTask(Command input) {
        String description = input.getArguments();
        String deadline = input.getNamedArguments().get("by");
        if (deadline == null) {
            deadline = "unknown";
        }

        DeadlineTask task = new DeadlineTask(description, deadline);

        this.addTask(task);
        return false;
    }

    private boolean makeEventTask(Command input) {
        String description = input.getArguments();
        String timing = input.getNamedArguments().get("at");
        if (timing == null) {
            timing = "unknown";
        }

        EventTask task = new EventTask(description, timing);

        this.addTask(task);
        return false;
    }

    private boolean makeToDoTask(Command input) {
        this.addTask(new Task(input.getArguments()));
        return false;
    }

    private boolean markAsDone(Command input) {
        int index;
        try {
            index = Integer.parseInt(input.getArguments());
        } catch (NumberFormatException e) {
            throw new DukeException("Index provided was not an integer!", e);
        }

        if (index < 0 || index > this.taskList.size()) {
            throw new DukeException("There's no task with that index!");
        }

        Task selectedTask = this.taskList.get(index - 1);
        selectedTask.markComplete();
        this.io.say("Nice! I've marked this task as done:",
                "  " + selectedTask);
        return false;
    }

    private boolean findTasks(Command input) {
        this.io.say("Here are the matching tasks in your list:");
        String target = input.getArguments().toLowerCase();
        this.io.say(this.taskList.stream()
                .filter(task ->
                        task.getDescription().toLowerCase().contains(target))
                .map(Object::toString)
                .map(new CounterDecorator(1))
                .iterator());
        return false;
    }

    private boolean deleteTask(Command input) {
        int index;
        try {
            index = Integer.parseInt(input.getArguments());
        } catch (NumberFormatException e) {
            throw new DukeException("Index provided was not an integer!", e);
        }

        if (index < 0 || index > this.taskList.size()) {
            throw new DukeException("There's no task with that index!");
        }

        Task selectedTask = this.taskList.remove(index - 1);
        this.io.say("Nice! I've marked this task as done:",
                "  " + selectedTask,
                String.format("Now you have %d task%s in the list.",
                        this.taskList.size(),
                        this.taskList.size() == 1 ? "" : "s"));


        return false;
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
}
