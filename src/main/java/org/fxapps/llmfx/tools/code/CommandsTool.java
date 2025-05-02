package org.fxapps.llmfx.tools.code;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;

@Singleton
public class CommandsTool {

    @Tool("""
            Run a command in the terminal and return the output.
            """)
    public String runCommand(@P("The command to run and its arguments") List<String> command) throws Exception {

        var process = new ProcessBuilder(command).start();
        // Reading the output from the command
        var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        var result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line).append(System.lineSeparator());
        }
        reader.close();

        // Waiting for the command to complete and checking its exit value
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Command failed with exit code: " + exitCode);
        }
        return result.toString();

    }

    @Tool("Get the name of the operating system")
    public String osName() {
        return System.getProperty("os.name");
    }

}
