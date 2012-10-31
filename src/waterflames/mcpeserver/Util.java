package waterflames.mcpeserver;

import java.math.BigInteger;

public class Util {

    static final String HEXES = "0123456789ABCDEF";

    public static String toHex(String arg) throws Exception {
        return String.format("%x", new BigInteger(arg.getBytes("UTF-8")));
    }

    public static String getHex(byte[] raw) {
        if (raw == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
                    .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static byte[] splitBytes(byte[] inputBytes, int offset, int length) throws Exception {
        byte[] outputBytes;
        outputBytes = new byte[length];
        System.arraycopy(inputBytes, offset, outputBytes, 0, length);
        return outputBytes;
    }

    public static final byte[] intToByteArray(int value) {
        return new byte[]{
                    (byte) (value >>> 24),
                    (byte) (value >>> 16),
                    (byte) (value >>> 8),
                    (byte) value};
    }
}