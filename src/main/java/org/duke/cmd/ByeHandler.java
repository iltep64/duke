package org.duke.cmd;

import org.duke.Duke;

@Handler.Binding("bye")
@Handler.Description("Exits the program.")
public class ByeHandler extends Handler {
    @Override
    public boolean handle(Duke duke, Command command) {
        duke.getIo().say("Bye. Hope to see you again soon!");
        return true;
    }
}
