package org.duke.cmd;

import org.duke.Duke;
import org.duke.DukeException;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
     * Bind a command handler, for a type of command.
     *
     * @param command Type of command
     * @param handler Handler for command
     */
    public void bindCommand(String command, Handler handler) {
        this.commandMap.put(command, handler);
        this.handlers.add(handler);
    }

    /**
     * Bind a fallback command handler, for unknown command types.
     *
     * @param handler Fallback handler for unknown commands
     */
    public void setUnknownCommandHandler(Handler handler) {
        this.defaultHandler = handler;
    }

    /**
     * Bind several command handlers at once.
     *
     * This uses the {@link Handler.Binding} annotations on each class
     * to autodiscover command types.
     * @param handlers Handlers to bind.
     */
    public final void bindCommands(Handler... handlers) {
        for (Handler handler : handlers) {
            Handler.Binding[] binds = handler.getClass()
                    .getAnnotationsByType(Handler.Binding.class);
            this.handlers.add(handler);
            for (Handler.Binding bind : binds) {
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

    /**
     * Returns the set of currently bound handlers.
     * @return Currently bound handlers.
     */
    public Set<Handler> getHandlers() {
        return Collections.unmodifiableSet(this.handlers);
    }

    /**
     * Returns the handler bound to this command type.
     * @param cmd Command type
     * @return The bound handler, or null if no binding is found.
     */
    public Handler getHandlerFor(String cmd) {
        return commandMap.get(cmd);
    }
}
