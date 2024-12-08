import java.io.*;
import java.nio.file.*;
import java.util.*;

public class LZWFileEncryptor {

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java LZWFileEncryptor <encrypt|decrypt> <inputFile> <outputFile>");
            return;
        }

        String mode = args[0];
        String inputFile = args[1];
        String outputFile = args[2];

        try {
            if (mode.equalsIgnoreCase("encrypt")) {
                encrypt(inputFile, outputFile);
            } else if (mode.equalsIgnoreCase("decrypt")) {
                decrypt(inputFile, outputFile);
            } else {
                System.out.println("Invalid mode. Use 'encrypt' or 'decrypt'.");
            }
        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
        }
    }

    public static void encrypt(String inputFile, String outputFile) throws IOException {
        String content = readFile(inputFile);
        List<Integer> compressed = compress(content);
        writeCompressedFile(compressed, outputFile);
    }

    public static void decrypt(String inputFile, String outputFile) throws IOException {
        List<Integer> compressed = readCompressedFile(inputFile);
        String decompressed = decompress(compressed);
        writeFile(outputFile, decompressed);
    }

    private static String readFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(new File(filePath).toPath()));
    }

    private static void writeFile(String filePath, String content) throws IOException {
        Files.write(new File(filePath).toPath(), content.getBytes());
    }

    private static List<Integer> compress(String input) {
        int dictSize = 256;
        Map<String, Integer> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put("" + (char) i, i);
        }

        String w = "";
        List<Integer> result = new ArrayList<>();

        for (char c : input.toCharArray()) {
            String wc = w + c;
            if (dictionary.containsKey(wc)) {
                w = wc;
            } else {
                result.add(dictionary.get(w));
                dictionary.put(wc, dictSize++);
                w = "" + c;
            }
        }

        if (!w.isEmpty()) {
            result.add(dictionary.get(w));
        }

        return result;
    }

    private static String decompress(List<Integer> compressed) {
        int dictSize = 256;
        Map<Integer, String> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put(i, "" + (char) i);
        }

        String w = "" + (char) (int) compressed.remove(0);
        StringBuilder result = new StringBuilder(w);

        for (int k : compressed) {
            String entry;
            if (dictionary.containsKey(k)) {
                entry = dictionary.get(k);
            } else if (k == dictSize) {
                entry = w + w.charAt(0);
            } else {
                throw new IllegalArgumentException("Bad compressed k: " + k);
            }

            result.append(entry);

            dictionary.put(dictSize++, w + entry.charAt(0));

            w = entry;
        }

        return result.toString();
    }

    private static void writeCompressedFile(List<Integer> compressed, String filePath) throws IOException {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(filePath))) {
            for (int code : compressed) {
                out.writeInt(code);
            }
        }
    }

    private static List<Integer> readCompressedFile(String filePath) throws IOException {
        List<Integer> compressed = new ArrayList<>();
        try (DataInputStream in = new DataInputStream(new FileInputStream(filePath))) {
            while (in.available() > 0) {
                compressed.add(in.readInt());
            }
        }
        return compressed;
    }
}
