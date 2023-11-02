package io.github.ffortier.jasm.asm;

import io.github.ffortier.jasm.binary.Module;
import io.github.ffortier.jasm.binary.WebAssembly;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TranspilerTest {

    @Test
    public void transpile() throws IOException {
        final var transpiler = new Transpiler();

        Module mod;
        
        try (final var in = Files.newInputStream(Path.of(System.getenv("TEST_HELLO_WASM")))) {
            mod = new WebAssembly().compile(in);
        }

        transpiler.linkModule("console", new Console());

        final var instance = transpiler.transpile(mod, Hello.class);
    }

    public interface Hello {

    }

    public static class Console {
        public void log() {
        }
    }
}