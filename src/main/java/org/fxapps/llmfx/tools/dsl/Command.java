package org.fxapps.llmfx.tools.dsl;

import java.util.List;

/*
A DSL command parsed from the LLM/User inputed content
*/
public record Command(String name, List<Param> params) {


}
