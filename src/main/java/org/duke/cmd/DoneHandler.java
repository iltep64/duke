package org.duke.cmd;

import org.duke.Duke;
import org.duke.DukeException;
import org.duke.task.Task;

@Handler.Binding("done")
@Handler.Description(value = "Marks a task as done.", argument = "Index of task to delete")
public class DoneHandler extends Handler {
    @Override
    public void handleNoExit(Duke duke, Command command) {
        int index;
        try {
            index = Integer.parseInt(command.getArguments());
        } catch (NumberFormatException e) {
            throw new DukeException("Index provided was not an integer!", e);
        }

        if (index < 0 || index > duke.getTaskStorage().size()) {
            throw new DukeException("There's no task with that index!");
        }

        Task selectedTask = duke.getTaskStorage().get(index - 1);
        selectedTask.markComplete();
        duke.getIo().say("Nice! I've marked this task as done:",
                "  " + selectedTask);
    }
}
