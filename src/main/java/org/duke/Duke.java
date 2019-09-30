package org.duke;

import org.duke.cmd.ByeHandler;
import org.duke.cmd.Command;
import org.duke.cmd.CommandDispatcher;
import org.duke.cmd.DeadlineTaskHandler;
import org.duke.cmd.DeleteHandler;
import org.duke.cmd.DoneHandler;
import org.duke.cmd.EventTaskHandler;
import org.duke.cmd.FindHandler;
import org.duke.cmd.Handler;
import org.duke.cmd.HelpHandler;
import org.duke.cmd.ListHandler;
import org.duke.cmd.TodoTaskHandler;
import org.duke.task.Task;
import org.duke.ui.DukeIO;

/**
 * Main class for Duke.
 */
public class Duke {

    private static final String[] initialGreeting = new String[]{
            "Hello! I'm Duke",
            "What can I do for you?",
            "Type 'help' for a list of commands."
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
