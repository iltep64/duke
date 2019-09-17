package org.duke.cmd;

import org.duke.Duke;
import org.duke.DukeException;
import org.duke.TaskStorage;
import org.duke.task.Task;

@Handler.Binding("delete")
@Handler.Description(value = "Deletes a task", argument = "Index of task")
public class DeleteHandler extends Handler {
    @Override
    protected void handleNoExit(Duke duke, Command command) {
        int index;
        try {
            index = Integer.parseInt(command.getArguments());
        } catch (NumberFormatException e) {
            throw new DukeException("Index provided was not an integer!", e);
        }

        TaskStorage taskStorage = duke.getTaskStorage();
        if (index < 0 || index > taskStorage.size()) {
            throw new DukeException("There's no task with that index!");
        }

        Task selectedTask = taskStorage.remove(index - 1);
        duke.getIo().say("Nice! I've marked this task as done:",
                "  " + selectedTask,
                String.format("Now you have %d task%s in the list.",
                        taskStorage.size(),
                        taskStorage.size() == 1 ? "" : "s"));
    }
}
