package com.github.websend;

import java.math.BigInteger;
import java.security.MessageDigest;

public class Util {

    public static String stringArrayToString(String[] strings) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            buffer.append(strings[i]);
        }
        return buffer.toString();
    }

    public static String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance(Main.getSettings().getHashingAlgorithm());
            md.update(input.getBytes());
            BigInteger bigInt = new BigInteger(1, md.digest());
            String result = bigInt.toString(16);
            if ((result.length() % 2) != 0) {
                result = "0" + result;
            }
            return result;
        } catch (Exception ex) {
            Main.getMainLogger().info("Failed to hash password to " + Main.getSettings().getHashingAlgorithm());
            return "";
        }
    }
}
