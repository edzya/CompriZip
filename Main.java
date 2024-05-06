import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    // LZ77 Constants
    private static final int WINDOW_SIZE = 15;
    private static final int LOOK_AHEAD_BUFFER_SIZE = 7;

    // Huffman Node
    static class HuffmanNode implements Comparable<HuffmanNode> {
        int frequency;
        char character;
        HuffmanNode left, right;

        public HuffmanNode(char character, int frequency) {
            this.character = character;
            this.frequency = frequency;
        }

        @Override
        public int compareTo(HuffmanNode node) {
            return this.frequency - node.frequency;
        }
    }

    // LZ77 Token
    public static class LZ77Token {
        private int offset;
        private int length;
        private char nextCharacter;

        public LZ77Token(int offset, int length, char nextCharacter) {
            this.offset = offset;
            this.length = length;
            this.nextCharacter = nextCharacter;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String command;

        do {
            System.out.println("Ievadiet komandu (comp, decomp, size, equal, about, exit): ");
            command = scanner.nextLine().trim();

            switch (command) {
                case "comp":
                    compCommand(scanner);
                    break;
                case "decomp":
                    decompCommand(scanner);
                    break;
                case "size":
                    sizeCommand(scanner);
                    break;
                case "equal":
                    equalCommand(scanner);
                    break;
                case "about":
                    aboutCommand();
                    break;
                case "exit":
                    System.out.println("Programma tiek beigta.");
                    break;
                default:
                    System.out.println("Nepareiza komanda. Lūdzu, mēģiniet vēlreiz.");
                    break;
            }
        } while (!command.equals("exit"));
        scanner.close();
    }

    private static void compCommand(Scanner scanner) {
        System.out.println("Enter source file name:");
        String sourceFileName = scanner.nextLine().trim();

        System.out.println("Enter archive name:");
        String archiveName = scanner.nextLine().trim();

        try {
            String input = new String(Files.readAllBytes(Paths.get(sourceFileName)));
            String compressedData = compress(input);
            Files.write(Paths.get(archiveName), compressedData.getBytes());
            System.out.println("Compression successful.");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void decompCommand(Scanner scanner) {
        System.out.println("Enter archive name:");
        String archiveName = scanner.nextLine().trim();

        System.out.println("Enter file name:");
        String fileName = scanner.nextLine().trim();

        try {
            String compressedData = new String(Files.readAllBytes(Paths.get(archiveName)));
            String decompressedData = decompress(compressedData);
            Files.write(Paths.get(fileName), decompressedData.getBytes());
            System.out.println("Decompression successful.");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void sizeCommand(Scanner scanner) {
        System.out.println("Enter file name:");
        String fileName = scanner.nextLine().trim();

        File file = new File(fileName);
        if (file.exists()) {
            System.out.println("File size: " + file.length() + " bytes");
        } else {
            System.out.println("File does not exist.");
        }
    }

    private static void equalCommand(Scanner scanner) {
        System.out.println("Enter first file name:");
        String firstFileName = scanner.nextLine().trim();

        System.out.println("Enter second file name:");
        String secondFileName = scanner.nextLine().trim();

        try {
            byte[] firstFileData = Files.readAllBytes(Paths.get(firstFileName));
            byte[] secondFileData = Files.readAllBytes(Paths.get(secondFileName));

            boolean isEqual = Arrays.equals(firstFileData, secondFileData);
            System.out.println("Files are equal: " + isEqual);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void aboutCommand() {
        System.out.println("Informācija par programmas izstrādātājiem:");
        System.out.println("ByteBenders : 1.Grupa");
        System.out.println("Ronalds Gackis - 231CDB005");
        System.out.println("Edgars Zoltners - 113RIC073");
        System.out.println("Kaspars Skrinda - 231CDB002");
    }

    // Compress using LZ77 followed by Huffman coding and RLE
    public static String compress(String input) {
        List<LZ77Token> lz77Tokens = lz77Compress(input);
        String encodedLZ77 = encodeLZ77Tokens(lz77Tokens);
        String huffmanCompressed = huffmanCompress(encodedLZ77);
        return runLengthEncode(huffmanCompressed);
    }

    // Decompress using LZ77
    public static String decompress(String input) {
        byte[] compData = input.getBytes();
        byte[] decData;
        try {
            decData = lz77Decompression(compData);
            return new String(decData);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    // LZ77 Compression
    private static List<LZ77Token> lz77Compress(String input) {
        List<LZ77Token> tokens = new ArrayList<>();
        int currentIndex = 0;

        while (currentIndex < input.length()) {
            int matchLength = 0;
            int matchIndex = 0;

            int startWindowIndex = Math.max(0, currentIndex - WINDOW_SIZE);
            int endWindowIndex = currentIndex;
            int endLookAheadBuffer = Math.min(currentIndex + LOOK_AHEAD_BUFFER_SIZE, input.length());
            for (int i = startWindowIndex; i < endWindowIndex; i++) {
                int len = findMatchLength(input, i, currentIndex, endLookAheadBuffer);
                if (len > matchLength) {
                    matchLength = len;
                    matchIndex = currentIndex - i;
                }
            }

            if (matchLength > 0) {
                tokens.add(new LZ77Token(matchIndex, matchLength, input.charAt(currentIndex + matchLength - 1)));
                currentIndex += matchLength;
            } else {
                tokens.add(new LZ77Token(0, 0, input.charAt(currentIndex)));
                currentIndex++;
            }
        }

        return tokens;
    }

    private static int findMatchLength(String input, int windowIndex, int currentIndex, int endLookAheadBuffer) {
        int maxLength = Math.min(LOOK_AHEAD_BUFFER_SIZE, endLookAheadBuffer - currentIndex);
        int length = 0;

        while (length < maxLength && input.charAt(windowIndex + length) == input.charAt(currentIndex + length)) {
            length++;
        }

        return length;
    }

    // Encode LZ77 Tokens
    private static String encodeLZ77Tokens(List<LZ77Token> tokens) {
        StringBuilder encoded = new StringBuilder();
        for (LZ77Token token : tokens) {
            encoded.append(token.offset).append(",").append(token.length).append(",").append(token.nextCharacter).append(";");
        }
        return encoded.toString();
    }

    // Huffman Compression
    private static String huffmanCompress(String input) {
        Map<Character, Integer> frequency = getFrequency(input);
        HuffmanNode root = buildTree(frequency);
        Map<Character, String> codes = new HashMap<>();
        generateCodes(root, "", codes);
        StringBuilder encoded = new StringBuilder();
        for (char c : input.toCharArray()) {
            encoded.append(codes.get(c));
        }
        return encoded.toString();
    }

    private static Map<Character, Integer> getFrequency(String text) {
        Map<Character, Integer> frequency = new HashMap<>();
        for (char c : text.toCharArray()) {
            frequency.put(c, frequency.getOrDefault(c, 0) + 1);
        }
        return frequency;
    }

    private static HuffmanNode buildTree(Map<Character, Integer> freq) {
        PriorityQueue<HuffmanNode> priorityQueue = new PriorityQueue<>();
        for (Map.Entry<Character, Integer> entry : freq.entrySet()) {
            priorityQueue.add(new HuffmanNode(entry.getKey(), entry.getValue()));
        }

        while (priorityQueue.size() > 1) {
            HuffmanNode left = priorityQueue.poll();
            HuffmanNode right = priorityQueue.poll();
            HuffmanNode sum = new HuffmanNode('\0', left.frequency + right.frequency);
            sum.left = left;
            sum.right = right;
            priorityQueue.add(sum);
        }

        return priorityQueue.poll();
    }

    private static void generateCodes(HuffmanNode node, String code, Map<Character, String> codes) {
        if (node != null) {
            if (node.left == null && node.right == null) {
                codes.put(node.character, code);
            }
            generateCodes(node.left, code + "0", codes);
            generateCodes(node.right, code + "1", codes);
        }
    }

    // Run-Length Encoding
    private static String runLengthEncode(String input) {
        StringBuilder encoded = new StringBuilder();
        char currentChar = input.charAt(0);
        int count = 1;

        for (int i = 1; i < input.length(); i++) {
            if (input.charAt(i) == currentChar) {
                count++;
            } else {
                encoded.append(currentChar).append(count);
                currentChar = input.charAt(i);
                count = 1;
            }
        }

        // Append the last character sequence
        encoded.append(currentChar).append(count);
        return encoded.toString();
    }
    public static String huffmanDecompress(String compressedData, HuffmanNode root) {
        StringBuilder decompressed = new StringBuilder();
        HuffmanNode current = root;

        for (char bit : compressedData.toCharArray()) {
            if (bit == '0') {
                current = current.left;
            } else {
                current = current.right;
            }

            if (current.left == null && current.right == null) {
                decompressed.append(current.character);
                current = root; // Reset to the root for the next character
            }
        }

        return decompressed.toString();
    }

    // LZ77 Decompression
    public static byte[] lz77Decompression(byte[] data) throws IOException {
        List<Byte> output = new ArrayList<>();
        byte[] window = new byte[WINDOW_SIZE];
        int windowPointer = 0;
        boolean running = true;

        try (DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(data))) {
            while (running) {
                byte flag = inputStream.readByte();

                for (int i = 0; i < 8; i++) {
                    if ((flag & (1 << i)) == 0) {
                        // Reference to previous occurrence in the window
                        if (inputStream.available() < 2) {
                            running = false;
                            break;
                        }

                        int byte1 = inputStream.readUnsignedByte();
                        int byte2 = inputStream.readUnsignedByte();
                        int offset = (byte1 << 4) | (byte2 >> 4);
                        int length = (byte2 & 0x0F) + 3;

                        for (int j = 0; j < length; j++) {
                            byte value = window[(windowPointer - offset + WINDOW_SIZE) % WINDOW_SIZE];
                            output.add(value);
                            window[windowPointer] = value;
                            windowPointer = (windowPointer + 1) % WINDOW_SIZE;
                        }
                    } else {
                        // Literal byte
                        if (inputStream.available() < 1) {
                            running = false;
                            break;
                        }

                        byte literal = inputStream.readByte();
                        output.add(literal);
                        window[windowPointer] = literal;
                        windowPointer = (windowPointer + 1) % WINDOW_SIZE;
                    }
                }
            }
        }

        // Convert the List<Byte> to byte[]
        byte[] outputArray = new byte[output.size()];
        for (int i = 0; i < outputArray.length; i++) {
            outputArray[i] = output.get(i);
        }
        return outputArray;
    }
}
