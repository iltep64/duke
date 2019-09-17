package org.duke;

import org.duke.cmd.*;
import org.duke.task.Task;
import org.duke.ui.DukeIO;

/**
 * Main class for Duke.
 */
public class Duke {

    private static final String[] initialGreeting = new String[]{
            "Hello! I'm Duke",
            "What can I do for you?"
    };
    private final CommandDispatcher dispatcher;
    private final DukeIO io;
    private TaskStorage taskStorage;

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
        this.taskStorage.add(t);
        this.io.say(
                "Got it. I've added this task:",
                "  " + t,
                String.format("Now you have %d task%s in the list.",
                        this.taskStorage.size(),
                        this.taskStorage.size() == 1 ? "" : "s")
        );
    }

    public void run() {
        //Start off greeting the user.
        this.io.withDialogBlock(() -> {
            this.io.say(initialGreeting);
            this.taskStorage = TaskStorage.load();
        });

        //Start listen loop.
        this.io.listen();
    }

    public DukeIO getIo() {
        return this.io;
    }

    public void save() {
        this.io.withDialogBlock(taskStorage::save);
    }

    public TaskStorage getTaskStorage() {
        return taskStorage;
    }
}
