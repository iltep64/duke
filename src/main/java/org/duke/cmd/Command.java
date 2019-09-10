package org.duke.cmd;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Represents parsed user input, divided into a command type and arguments.
 */
public class Command {
    private static final Pattern switchBoundary = Pattern.compile("\\s+/");
    private static final Pattern cmdBoundary = Pattern.compile("\\s+");
    private final String type;
    private final String arguments;
    private final Map<String, String> namedArguments;

    private Command(String type, String arguments, Map<String, String> namedArguments) {
        this.type = type;
        this.arguments = arguments;
        this.namedArguments = namedArguments;
    }

    /**
     * From user input, parse it into a {@link Command}.
     * <p>
     * First word is the main command type.
     * The main arguments are the remainder of words,
     * up til the first named argument or end of string.
     * <p>
     * Each named argument starts with a switch "/(name)".
     * The argument spans up til the next named argument or end of string.
     *
     * @param input User input
     * @return Parsed command, or null if invalid
     */
    public static Command parse(String input) {
        input = input.trim();

        //Split on the presence of any switches
        String[] splits = switchBoundary.split(input);

        //First split value is "cmd-type args..."
        String[] mainArgs = cmdBoundary.split(splits[0], 2);
        if (mainArgs[0].isEmpty()) {
            return null;
        }

        String type = mainArgs[0];
        //If no arguments, leave as empty string
        String arguments = mainArgs.length > 1 ? mainArgs[1] : "";
        Map<String, String> namedArguments = new HashMap<>();

        if (splits.length > 1) {
            //Each split value is "switch-name args..."
            //with no preceding slash
            for (int i = 1; i < splits.length; i++) {
                if (splits[i].isEmpty()) {
                    continue;
                }
                //Split at first whitespace
                String[] switchArgs = cmdBoundary.split(splits[i], 2);
                String switchName = switchArgs[0];
                //If no arguments, leave as empty string
                String switchArguments = switchArgs.length > 1 ? switchArgs[1] : "";
                namedArguments.put(switchName, switchArguments);
            }
        }
        return new Command(type, arguments, Collections.unmodifiableMap(namedArguments));
    }

    public String getType() {
        return type;
    }

    public String getArguments() {
        return arguments;
    }

    public Map<String, String> getNamedArguments() {
        return namedArguments;
    }
}
