package org.fxapps.llmfx.tools.dsl;

import java.util.ArrayList;
import java.util.List;

public final class DSLParser {

    private static final String COMMENT = "#";

    public static List<Command> parse(String dsl) {        
        List<Command> commands = new ArrayList<>();

        String[] lines = dsl.replaceAll("\\\\n", System.lineSeparator()).split("\\R");
        for (String line : lines) {
            var tokens = line.trim().split("\\s+");
            if (tokens.length == 0 || line.startsWith(COMMENT))
                continue;
            var commandName = tokens[0];
            var params = new ArrayList<Param>();
            var command = new Command(commandName, params);

            for (int j = 1; j < tokens.length; j++) {
                var v = tokens[j];

                if (COMMENT.equals(v)) {
                    break;
                }
                if (v == null || v.isBlank()) {
                    continue;
                }
                params.add(new Param(v));
            }
            commands.add(command);

        }
        return commands;
    }

}
