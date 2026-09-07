package app;

import elemental2.core.Int8Array;
import elemental2.core.Uint8Array;

public class ByteUtil {

    public static byte[] uint8ArrayToBytes(Uint8Array uint8Array) {
        int length = uint8Array.length;
        byte[] byteArray = new byte[length];
        for (int i = 0; i < length; i++) {
            byteArray[i] = (byte) uint8Array.getAt(i).shortValue();
        }
        return byteArray;
    }

    public static Int8Array bytesToInt8Array(byte[] input) {
        // Assuming 'byteArray' is a Java byte[]
        double[] doubleArray = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            doubleArray[i] = input[i];
        }
        return new Int8Array(doubleArray);
    }
}
