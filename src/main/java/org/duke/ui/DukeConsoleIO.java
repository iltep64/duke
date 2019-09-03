package org.duke.ui;

import org.duke.DukeException;
import org.duke.cmd.CommandDispatcher;

import java.util.Iterator;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Supplier;

/**
 * CLI interface for Duke, wrapping standard input and output.
 */
public class DukeConsoleIO implements DukeIO {

    private static final String lineRule =
            "____________________________________________________________";

    private final Scanner scanner;
    private CommandDispatcher commandDispatcher;
    private boolean printingDialogBlock;

    /**
     * Constructs a DukeIO instance wrapping standard input.
     * @param dispatcher Command dispatcher to use
     */
    public DukeConsoleIO(CommandDispatcher dispatcher) {
        this.scanner = new Scanner(System.in);
        Objects.requireNonNull(dispatcher, "dispatcher");
        this.commandDispatcher = dispatcher;
    }

    /**
     * Output several lines, in context of the current dialog block.
     *
     * @param lines Lines to print.
     */
    @Override
    public void say(Iterator<String> lines) {
        if (!this.printingDialogBlock) {
            //Print start of reply line
            System.out.println(lineRule);
            this.printingDialogBlock = true;
        }

        //Print each given line
        while (lines.hasNext()) {
            System.out.println(lines.next());
        }
    }

    @Override
    public <T> T withDialogBlock(Supplier<T> action, T fallback) {
        this.printingDialogBlock = false;
        try {
            return action.get();
        } catch (DukeException e) {
            this.sayError(e);
            return fallback;
        } finally {
            if (this.printingDialogBlock) {
                //Print end of reply line, and extra empty line
                System.out.println(lineRule);
                System.out.println();
            }
        }
    }


    /**
     * Start running the listen loop, and respond to commands.
     */
    @Override
    public void listen() {
        //While there is still input from user
        while (scanner.hasNextLine()) {
            //Read single line of user input, and remove extra spaces
            String userInput = scanner.nextLine();
            boolean shouldExit = this.withDialogBlock(
                    () -> commandDispatcher.dispatchCommand(userInput),
                    false);
            if (shouldExit) {
               break;
            }
        }
    }
}