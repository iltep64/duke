package org.duke.cmd;

import org.duke.Duke;
import org.duke.task.Task;

@Handler.Binding("todo")
@Handler.Description(value = "Creates a todo task", argument = "Description of task")
public class TodoTaskHandler extends Handler {
    @Override
    protected void handleNoExit(Duke duke, Command command) {
        duke.addTask(new Task(command.getArguments()));
    }
}
