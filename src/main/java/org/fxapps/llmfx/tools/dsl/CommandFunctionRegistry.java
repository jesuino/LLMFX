package org.fxapps.llmfx.tools.dsl;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

// TODO: create a registry and then go trhough the list of commands and run the factory from the registry
public class CommandFunctionRegistry<T> {

    private HashMap<String, Function<List<Param>, T>> registry;

    public CommandFunctionRegistry() {
        registry = new HashMap<>();
    }

    public void register(String commandName,
            Function<List<Param>, T> function) {
        registry.put(commandName, function);

    }

    public Optional<T> run(Command command) {
        var cmdFn = registry.get(command.name());

        if (cmdFn == null) {
            return Optional.empty();
        }

        try {
            var result = cmdFn.apply(command.params());
            return Optional.ofNullable(result);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(
                    "Not enough parameters to run '" + command.name() + "'. Check the command signature and try again.",
                    e);
        }
    }

}
