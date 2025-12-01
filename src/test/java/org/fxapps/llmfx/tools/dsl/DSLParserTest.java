package org.fxapps.llmfx.tools.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DSLParserTest {

    @Test
    public void testDSL() {
        var commands = DSLParser.parse("""
                a 1 2
                b 1 x 2
                """);
        var a = commands.get(0);
        var b = commands.get(1);
        assertEquals(a.name(), "a");
        assertEquals(a.params().size(), 2);
        assertEquals(a.params().get(0).value(), "1");
        assertEquals(a.params().get(1).value(), "2");

        assertEquals(a.params().get(0).asDouble(), 1d);
        assertEquals(a.params().get(1).asInt(), 2);

        assertEquals(b.name(), "b");
        assertEquals(b.params().size(), 3);
        assertEquals(b.params().get(0).value(), "1");
        assertEquals(b.params().get(1).value(), "x");
        assertEquals(b.params().get(2).value(), "2");
    }

    @Test

    public void testBadParamType() {
        var commands = DSLParser.parse("""
                z x
                """);
        var z = commands.get(0);
        Assertions.assertThrows(IllegalArgumentException.class, () -> z.params().get(0).asDouble());

    }

}
