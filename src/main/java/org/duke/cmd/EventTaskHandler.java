package org.duke.cmd;

import org.duke.Duke;
import org.duke.task.EventTask;

@Handler.Binding("event")
@Handler.Description(value = "Create a event task", argument = "Description of task")
@Handler.NamedArgument(value = "at", description = "Time of event")
public class EventTaskHandler extends Handler {
    @Override
    protected void handleNoExit(Duke duke, Command command) {
        String description = command.getArguments();
        String timing = command.getNamedArguments().get("at");
        if (timing == null) {
            timing = "unknown";
        }

        EventTask task = new EventTask(description, timing);
        duke.addTask(task);
    }
}
