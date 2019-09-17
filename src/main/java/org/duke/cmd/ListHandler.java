package org.duke.cmd;

import org.duke.Duke;
import org.duke.ui.DukeIO;
import org.duke.util.CounterDecorator;

@Handler.Binding("list")
@Handler.Description("Lists all tasks")
public class ListHandler extends Handler {
    @Override
    protected void handleNoExit(Duke duke, Command command) {
        DukeIO io = duke.getIo();
        io.say("Here are the tasks in your list:");
        io.say(duke.getTaskStorage().stream()
                .map(new CounterDecorator<>(1))
                .map(Object::toString)
                .iterator());
    }
}
