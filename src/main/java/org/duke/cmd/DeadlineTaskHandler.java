package org.duke.cmd;

import org.duke.Duke;
import org.duke.task.DeadlineTask;

@Handler.Binding("deadline")
@Handler.Description(value = "Creates a new deadline task", argument = "Description of task")
@Handler.NamedArgument(value = "by", description = "Time of deadline")
public class DeadlineTaskHandler extends Handler {
    @Override
    protected void handleNoExit(Duke duke, Command command) {
        String description = command.getArguments();
        String deadline = command.getNamedArguments().get("by");
        if (deadline == null) {
            deadline = "unknown";
        }

        DeadlineTask task = new DeadlineTask(description, deadline);
        duke.addTask(task);
    }
}
