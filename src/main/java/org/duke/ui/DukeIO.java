package org.duke.ui;

import org.duke.DukeException;
import org.duke.cmd.CommandDispatcher;
import org.duke.cmd.Handler;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Supplier;

/**
 * Abstract interface for Duke, providing a way to read user input
 * and output messages.
 */
public interface DukeIO {

    /**
     * Output several lines, in context of the current dialog block.
     *
     * @param lines Lines to print.
     */
    void say(Iterator<String> lines);

    /**
     * Output several lines, in context of the current dialog block.
     *
     * @param lines Lines to print.
     */
    default void say(String... lines) {
        say(Arrays.asList(lines).iterator());
    }

    default void sayCommand(Handler handler) {
        say(handler.getDescriptionLine());
    }

    default void sayBriefCommand(Handler handler) {
        say(handler.getDescriptionLine(), handler.getSyntaxLine());
    }

    /**
     * Output an error, in context of the current dialog block.
     *
     * @param e Exception to print
     */
    default void sayError(DukeException e) {
        this.say(String.format("☹ OOPS!!! %s", e.getMessage()));
    }


    /**
     * Run an action in the context of a dialog block.
     *
     * @param action Action to run
     */
    default void withDialogBlock(Runnable action) {
        withDialogBlock(() -> {
            action.run();
            return null;
        }, null);
    }


    /**
     * Run a value-returning function in the context of a dialog block
     *
     * @param action   Function to run
     * @param fallback Value to return, on error
     * @param <T>      Return value type
     * @return Result of function, or fallback if error encountered.
     */
    <T> T withDialogBlock(Supplier<T> action, T fallback);

    void listen();

    void setCommandDispatcher(CommandDispatcher commandDispatcher);
}
