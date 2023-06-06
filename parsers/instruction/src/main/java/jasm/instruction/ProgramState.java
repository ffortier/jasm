package jasm.instruction;

import java.nio.ByteBuffer;

public interface ProgramState {
    default void t() {
        final var ff = ByteBuffer.allocate(32);

    }
}
