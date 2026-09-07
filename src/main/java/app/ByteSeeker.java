package app;

import elemental2.core.Int8Array;
import elemental2.core.TypedArray;
import elemental2.core.Uint8Array;

import java.nio.charset.StandardCharsets;

public class ByteSeeker {

    private final Int8Array baseArr;
    private int nextIdx = 0;

    public ByteSeeker(Uint8Array baseArr) {
        this.baseArr = new Int8Array(baseArr);
    }

    public String readS(int length) {
        byte[] arr = new byte[length];
        int charCount = 0;

        for (int i = 0; i < length; i++) {
            byte value = baseArr.getAt(nextIdx + i).byteValue();
            if (value == 0) { // null terminator
                break;
            }
            arr[i] = value;
            charCount++;
        }
        nextIdx += length;
        return new String(arr, 0, charCount, StandardCharsets.UTF_8);
    }

    public int readI() {
        byte[] bytes = new byte[]{
                baseArr.getAt(nextIdx).byteValue(),
                baseArr.getAt(nextIdx + 1).byteValue(),
                baseArr.getAt(nextIdx + 2).byteValue(),
                baseArr.getAt(nextIdx + 3).byteValue(),
        };
        nextIdx += 4;
        return (bytes[0] & 0xFF) |
                ((bytes[1] & 0xFF) << 8) |
                ((bytes[2] & 0xFF) << 16) |
                ((bytes[3] & 0xFF) << 24);
    }

    public long readQ() {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            bytes[i] = baseArr.getAt(nextIdx).byteValue();
            nextIdx++;
        }
        return leBytesToLong(bytes);
    }

    private static long leBytesToLong(byte[] b) {
        return ((long) b[0] & 0xFF) |
                ((long) b[1] & 0xFF) << 8 |
                ((long) b[2] & 0xFF) << 16 |
                ((long) b[3] & 0xFF) << 24 |
                ((long) b[4] & 0xFF) << 32 |
                ((long) b[5] & 0xFF) << 40 |
                ((long) b[6] & 0xFF) << 48 |
                ((long) b[7] & 0xFF) << 56;
    }

    public void skip(int count) {
        nextIdx += count;
    }

    public void writeRaw(String path, int offset, int size) {
        Fs.writeFileSync(path, baseArr.subarray(offset, offset + size));
    }

    public TypedArray getRaw(int offset, int size) {
        return baseArr.subarray(offset, offset + size);
    }
}
