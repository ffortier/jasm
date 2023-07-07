package jasm.jvm;

import java.nio.ByteBuffer;

public record ClassFile(ConstantPool[] constantPool) {

    private static final byte[] MAGIC = new byte[] { (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE };
    private static final short MINOR_VERSION = (short) 0;
    private static final short MAJOR_VERSION = (short) 61; // jdk17

    public void dump(ByteBuffer bb) {
        bb.put(MAGIC);
        bb.putShort(MAJOR_VERSION);
        bb.putShort(MINOR_VERSION);
        bb.putShort((short) (constantPool.length + 1));
    }

    public record ConstantPool() {

    }
}
