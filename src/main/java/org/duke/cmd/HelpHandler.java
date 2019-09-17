package org.duke.cmd;

import org.duke.Duke;
import org.duke.ui.DukeIO;

@Handler.Binding("help")
@Handler.Description("Shows command help")
public class HelpHandler extends Handler {
    @Override
    protected void handleNoExit(Duke duke, Command command) {
        DukeIO io = duke.getIo();
        for (Handler handler : duke.getDispatcher().getHandlers()) {
            io.say(handler.getDescriptionLine(), handler.getSyntaxLine());
        }
    }
}
