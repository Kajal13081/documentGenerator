package org.example.backend;

import java.io.IOException;

public interface Exporter {
    void export(String path, String content) throws IOException;
}
