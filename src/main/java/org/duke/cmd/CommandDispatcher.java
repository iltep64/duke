package org.duke.cmd;

import org.duke.Duke;
import org.duke.DukeException;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class CommandDispatcher {
    private final Set<Handler> handlers
            = new TreeSet<>(Comparator.comparing(Handler::getPrimaryBinding));
    private final Map<String, Handler> commandMap
            = new HashMap<>();
    private Handler defaultHandler = null;
    private final Duke duke;

    public CommandDispatcher(Duke duke) {
        this.duke = duke;
    }

    /**
     * Bind a command handler, for a type of command
     *
     * @param command Type of command
     * @param handler Handler for command
     */
    public void bindCommand(String command, Handler handler) {
        this.commandMap.put(command, handler);
        this.handlers.add(handler);
    }

    /**
     * Bind a fallback command handler, for unknown command types
     *
     * @param handler Fallback handler for unknown commands
     */
    public void setUnknownCommandHandler(Handler handler) {
        this.defaultHandler = handler;
    }

    public final void bindCommands(Handler... handlers) {
        for(Handler handler : handlers) {
            Handler.Binding[] binds = handler.getClass().getAnnotationsByType(Handler.Binding.class);
            this.handlers.add(handler);
            for(Handler.Binding bind : binds) {
                this.commandMap.put(bind.value(), handler);
            }
        }
    }

    /**
     * Start running the listen loop, and respond to commands.
     */
    public boolean dispatchCommand(String userInput) {
        Command command = Command.parse(userInput);
        if (command == null) {
            throw new DukeException("Unable to parse command!");
        }

        Handler cmdHandler = commandMap.get(command.getType());
        if (cmdHandler != null) {
            return cmdHandler.handle(duke, command);
        } else if (defaultHandler != null) {
            return defaultHandler.handle(duke, command);
        } else {
            throw new DukeException(String.format("Unknown command %s.", command.getType()));
        }
    }

    public Set<Handler> getHandlers() {
        return Collections.unmodifiableSet(this.handlers);
    }
}
