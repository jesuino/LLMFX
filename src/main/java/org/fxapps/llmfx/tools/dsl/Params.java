package org.fxapps.llmfx.tools.dsl;

import java.util.ArrayList;

public class Params extends ArrayList<Param> {

    public Double getDouble(int i) {
        return get(i).asDouble();
    }

    public int getInt(int i) {
        return get(i).asInt();
    }

    public String getStr(int i) {
        return get(i).value();
    }

}
