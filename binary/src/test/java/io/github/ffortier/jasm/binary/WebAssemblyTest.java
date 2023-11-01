package io.github.ffortier.jasm.binary;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WebAssemblyTest implements WithAssertions {
    private final WebAssembly parser = new WebAssembly();

    @Test
    public void someTest() throws IOException {
        Module mod;
        try (final var in = Files.newInputStream(Path.of(System.getenv("TEST_HELLO_WASM")))) {
            mod = parser.compile(in);
        }
        System.out.println(mod);
        assertThat(mod.sections().size()).isGreaterThan(0);
    }

}
