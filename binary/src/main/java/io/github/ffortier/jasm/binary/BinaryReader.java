package io.github.ffortier.jasm.binary;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class BinaryReader {

    public static int leb128(BufferedInputStream in) throws IOException {
        int result = 0;
        int shift = 0;

        while (true) {
            int b = in.read();
            result |= (b & 0b0111_1111) << shift;
            if ((b & 0b1000_0000) == 0)
                return result;
            shift += 7;
        }
    }

    public static int leb128(ByteBuffer bb) {
        int result = 0;
        int shift = 0;

        while (true) {
            int b = bb.get();
            result |= (b & 0b0111_1111) << shift;
            if ((b & 0b1000_0000) == 0)
                return result;
            shift += 7;
        }
    }

    public static String name(ByteBuffer bb) {
        final var size = leb128(bb);
        final var bytes = new byte[size];

        bb.get(bytes);

        return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes)).toString();
    }
}
