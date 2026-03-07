package org.fxapps.llmfx.tools.code;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;

@Singleton
public class PythonTool {

    @Tool("""
            Run python code. Use this when user asks you to execute a Python script.
            """)
    public String runPythonCode(@P("The code to run") String code) throws IOException, InterruptedException {
        var file = Files.createTempFile("tool", ".py");
        var process = new ProcessBuilder(List.of("python", file.toAbsolutePath().toString())).start();

        var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        var result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line).append(System.lineSeparator());
        }
        reader.close();

        System.out.println("File is: " + file.toAbsolutePath().toString());
        int exitCode = process.waitFor();

        Files.delete(file);

        if (exitCode != 0) {
            throw new RuntimeException("Not able to run script " + exitCode);
        }
        return result.toString();

    }

}
