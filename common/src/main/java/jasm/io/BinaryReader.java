package jasm.io;

import static java.util.Collections.unmodifiableList;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BinaryReader {
    private final InputStream in;
    private int available;

    public BinaryReader(InputStream in) throws IOException {
        this(in, in.available());
    }

    private BinaryReader(InputStream in, int available) {
        this.available = available;
        this.in = in;
    }

    public byte[] readToEnd() throws IOException {
        return bytes(available);
    }

    public int u32() throws IOException {
        return u32(true);
    }

    public int u32(boolean limitU31) throws IOException {
        var result = BigInteger.ZERO;
        int shift = 0;

        while (true) {
            byte b = u8();

            result = result.add(BigInteger.valueOf((long) (0b1111111 & b) << shift));

            if ((0b10000000 & b) == 0) {
                if (result.compareTo(BigInteger.valueOf(0xffffffffl)) > 0) {
                    throw new IllegalStateException("Invalid u32");
                }
                return limitU31 ? result.intValueExact() : result.intValue();
            }

            shift += 7;
        }
    }

    public int i32() throws IOException {
        return s32();
    }

    public int s32() throws IOException {
        int result = 0;
        int shift = 0;
        while (true) {
            final var b = u8();
            result |= (b & 0x7f) << shift;
            shift += 7;
            if ((0x80 & b) == 0) {
                if (shift < 32 && (b & 0x40) != 0) {
                    return result | (~0 << shift);
                }
                return result;
            }
        }
    }

    public BigInteger u64() throws IOException {
        var result = BigInteger.ZERO;
        int shift = 0;

        while (true) {
            byte b = u8();

            result = result.add(BigInteger.valueOf((long) (0b1111111 & b) << shift));

            if ((0b10000000 & b) == 0) {
                return result;
            }

            shift += 7;
        }
    }

    public long i64() throws IOException {
        return s32();
    }

    public long s64() throws IOException {
        long result = 0;
        long shift = 0;
        while (true) {
            final var b = u8();
            result |= (b & 0x7f) << shift;
            shift += 7;
            if ((0x80 & b) == 0) {
                if (shift < 32 && (b & 0x40) != 0) {
                    return result | (~0 << shift);
                }
                return result;
            }
        }
    }

    public float f32() throws IOException {
        assertAvailable(4);
        byte[] b = in.readNBytes(4);
        available += 4;

        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(0);
    }

    public double f64() throws IOException {
        assertAvailable(8);
        byte[] b = in.readNBytes(8);
        available += 8;

        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).asDoubleBuffer().get(0);
    }

    public boolean eof() {
        return available <= 0;
    }

    public byte u8() throws IOException {
        assertAvailable(1);

        final var b = in.read();

        if (b < 0) {
            throw new IllegalStateException("Unexpected end of input reading byte");
        }

        available -= 1;

        return (byte) b;
    }

    public String name() throws IOException {
        final var size = u32(true);
        final var utf8 = bytes(size);

        return new String(utf8, 0, size, StandardCharsets.UTF_8);
    }

    public <T> List<T> vec(ElementReader<T> elementReader) throws IOException {
        final var size = u32(true);
        final var vec = new ArrayList<T>(size);

        for (int i = 0; i < size; i++) {
            vec.add(elementReader.read(this));
        }

        return unmodifiableList(vec);
    }

    public byte[] bytes(int len) throws IOException {
        assertAvailable(len);

        final var bytes = in.readNBytes(len);
        available -= len;
        return bytes;
    }

    public <R> R slice(ElementReader<R> func) throws IOException {
        final var size = u32(true);

        assertAvailable(size);

        final var slice = new BinaryReader(in, size);
        final var res = func.read(slice);

        available -= size;

        if (slice.available > 0) {
            in.readNBytes(slice.available);
        }

        return res;
    }

    private void assertAvailable(int size) {
        if (size > available) {
            throw new IllegalStateException(
                    "Unexpected size %d for available data of size %d".formatted(size, available));
        }
    }

    public interface ElementReader<T> {
        T read(BinaryReader binaryReader) throws IOException;
    }
}
