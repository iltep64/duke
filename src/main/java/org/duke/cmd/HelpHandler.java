package org.duke.cmd;

import org.duke.Duke;
import org.duke.DukeException;
import org.duke.ui.DukeIO;

@Handler.Binding("help")
@Handler.Description(value = "Shows command help", argument = "Command name", optional = true)
public class HelpHandler extends Handler {
    @Override
    protected void handleNoExit(Duke duke, Command command) {
        DukeIO io = duke.getIo();
        if (command.getArguments().isEmpty()) {
            for (Handler handler : duke.getDispatcher().getHandlers()) {
                io.sayBriefCommand(handler);
            }
            return;
        }
        Handler handler = duke.getDispatcher().getHandlerFor(command.getArguments());
        if (handler == null) {
            throw new DukeException("No such command!");
        }
        io.sayCommand(handler);
    }
}
