package utils;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class TemporaryFile extends File implements Closeable {
    private File file;

    public TemporaryFile(@NotNull Path pathname) {
        super(pathname.toString());
    }

    @Override
    public void close() throws IOException {
        file.delete();
    }
}