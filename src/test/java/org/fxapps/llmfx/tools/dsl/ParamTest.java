package org.fxapps.llmfx.tools.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ParamTest {

    @Test
    public void testParam() {
        var p = new Param("1.2");
        assertEquals(1, p.asInt());
        assertEquals(1.2d, p.asDouble());
        assertEquals("1.2", p.value());
    }
    
}
