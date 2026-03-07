package org.fxapps.llmfx.tools.dsl;

/*
    Holds a DSL parameter value
*/
public record Param(String value) {

    public Double asDouble() {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Can't parse to number", e);
        }
    }

    public int asInt() {
        return asDouble().intValue();
    }

    @Override
    public final String toString() {
        return value;
    }

}
