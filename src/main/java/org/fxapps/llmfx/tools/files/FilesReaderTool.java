package org.fxapps.llmfx.tools.files;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/*
 * A tool to read and search files
 */
public class FilesReaderTool {

    @Tool("Returns a file content. You only have access to the current working directory")
    public String readFileContent(@P("The path to the file to be read.") String file) throws IOException {
        var basePath = Paths.get(".");
        var filePath = basePath.resolve(file);
        return Files.readString(filePath);
    }

    @Tool("Search for files that matches a given glob pattern")
    public List<String> searchFiles(@P("A glob file pattern to search the files") String pattern) throws IOException {
        var filesList = new ArrayList<String>();
        filesList.clear();
        final var currentPath = Paths.get(".");

        Files.walkFileTree(currentPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attribs) throws IOException {
                var fs = FileSystems.getDefault();
                var matcher = fs.getPathMatcher("glob:" + pattern);
                var name = file.getFileName();
                if (matcher.matches(name)) {
                    filesList.add(currentPath.relativize(file).toString());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return filesList;

    }

}
