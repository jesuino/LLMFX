package org.fxapps.llmfx.tools.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

public class FilesWriterTool {

    @Tool("Writes text content to a file")
    public String writeContentToFile(@P("The path of the file to be written") String path,
            @P("The content that will be written") String content) throws IOException {
        var currentDir = Paths.get(".");
        var p = currentDir.resolve(path);
        Files.writeString(p, content);
        return "The file " + p + " was sucessfully updated";
    }

    @Tool("Deletes a file")
    public String deleteFile(@P("The path of the file to be deleted") String path) throws IOException {
        var currentDir = Paths.get(".");
        var p = currentDir.resolve(path);
        Files.delete(p);
        return "The file " + p + " was sucessfully deleted";
    }

}
