package org.duke.cmd;

import org.duke.Duke;
import org.duke.ui.DukeIO;
import org.duke.util.CounterDecorator;

@Handler.Binding("find")
@Handler.Description(value = "Find tasks that match.", argument = "String to search for")
public class FindHandler extends Handler {
    @Override
    protected void handleNoExit(Duke duke, Command command) {
        DukeIO io = duke.getIo();
        io.say("Here are the matching tasks in your list:");
        String target = command.getArguments().toLowerCase();
        io.say(duke.getTaskList().stream()
                .filter(task ->
                        task.getDescription().toLowerCase().contains(target))
                .map(Object::toString)
                .map(new CounterDecorator(1))
                .iterator());
    }
}
