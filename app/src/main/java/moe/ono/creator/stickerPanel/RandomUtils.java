package moe.ono.creator.stickerPanel;

import java.util.Random;

public class RandomUtils {
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    private static final String ALPHABET_NUMERIC = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final String ALPHABET_NUMERIC_UPPER = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random RANDOM = new Random();

    public static String getRandomName() {
        return getRandomString(ALPHABET_NUMERIC_UPPER, 16);
    }

    public static String getRandomStringLower(int length) {
        return getRandomString(ALPHABET, length);
    }

    public static String getRandomStringAlphaNumeric(int length) {
        return getRandomString(ALPHABET_NUMERIC, length);
    }

    private static String getRandomString(String source, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(source.length());
            sb.append(source.charAt(index));
        }
        return sb.toString();
    }

    public static String getRandomString(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(ALPHABET_NUMERIC_UPPER.charAt(number));
        }
        return sb.toString();
    }

}