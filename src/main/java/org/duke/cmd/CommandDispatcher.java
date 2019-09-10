package org.duke.cmd;

import org.duke.DukeException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class CommandDispatcher {
    private final Map<String, Predicate<Command>> commandMap
            = new HashMap<>();
    private Predicate<Command> defaultHandler = null;

    /**
     * Bind a command handler, for a type of command
     *
     * @param command Type of command
     * @param handler Handler for command
     */
    public void bindCommand(String command, Predicate<Command> handler) {
        this.commandMap.put(command, handler);
    }

    /**
     * Bind a fallback command handler, for unknown command types
     *
     * @param handler Fallback handler for unknown commands
     */
    public void setUnknownCommandHandler(Predicate<Command> handler) {
        this.defaultHandler = handler;
    }

    /**
     * Start running the listen loop, and respond to commands.
     */
    public boolean dispatchCommand(String userInput) {
        Command command = Command.parse(userInput);
        if (command == null) {
            throw new DukeException("Unable to parse command!");
        }

        Predicate<Command> cmdHandler = commandMap.get(command.getType());
        if (cmdHandler != null) {
            return cmdHandler.test(command);
        } else if (defaultHandler != null) {
            return defaultHandler.test(command);
        } else {
            throw new DukeException(String.format("Unknown command %s.", command.getType()));
        }
    }
}
