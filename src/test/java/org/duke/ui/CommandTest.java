package org.duke.ui;

import org.duke.cmd.Command;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CommandTest {
    @Test
    public void testBasicCommand() {
        Command c = Command.parse("hello world");
        assertNotNull(c);
        assertEquals(c.getType(), "hello");
        assertEquals(c.getArguments(), "world");
        assertTrue(c.getNamedArguments().isEmpty());
    }

    @Test
    public void testSwitchCommand() {
        Command c = Command.parse("one two three /four /five six");
        assertNotNull(c);
        assertEquals(c.getType(), "one");
        assertEquals(c.getArguments(), "two three");
        assertEquals(c.getNamedArguments().size(), 2);
        assertEquals(c.getNamedArguments().get("four"), "");
        assertEquals(c.getNamedArguments().get("five"), "six");
    }
}
