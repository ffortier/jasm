package io.github.ffortier.jasm.binary;

import static java.util.Collections.unmodifiableList;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class WebAssembly {
    private static final byte[] WASM_HEADER = { 0x00, 0x61, 0x73, 0x6d, 0x01, 0x00, 0x00, 0x00 };

    private static void readWasmHeader(BufferedInputStream in) throws IOException {
        byte[] buf = in.readNBytes(WASM_HEADER.length);

        if (!Arrays.equals(WASM_HEADER, buf)) {
            throw new IOException("invalid wasm header");
        }
    }

    public Module compile(InputStream in) throws IOException {
        try (final var buffered = new BufferedInputStream(in)) {
            return compile(buffered);
        }
    }

    public Module compile(BufferedInputStream in) throws IOException {
        readWasmHeader(in);
        
        final var sections = new ArrayList<Section>();

        var section = Section.read(in);

        while (section.isPresent()) {
            sections.add(section.get());
            section = Section.read(in);
        }

        return new Module(unmodifiableList(sections));
    }
}
