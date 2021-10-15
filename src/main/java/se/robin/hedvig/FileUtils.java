package se.robin.hedvig;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.stream.Stream;

public class FileUtils {

    public static Stream<String> streamLines(File file, Charset charset) {
        try {
            return Files.lines(file.toPath(), charset);
        } catch (IOException e) {
            throw new RuntimeException("Could not stream file lines", e);
        }
    }
}
