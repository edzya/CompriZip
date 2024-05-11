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

    // Structure for tree nodes
    static class Node {
        char character;
        int freq;
        Node left, right;

        // Constructor to initialize a new node
        Node(char character, int freq) {
            this.character = character;
            this.freq = freq;
            this.left = null;
            this.right = null;
        }
    }

    // Structure for min heap
    static class MinHeap {
        int size;
        Node[] array;

        // Constructor to initialize a new min heap
        MinHeap(int size) {
            this.size = size;
            this.array = new Node[size];
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String command;

        do {
            System.out.println("Enter command (comp, decomp, size, equal, about, exit): ");
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
                    System.out.println("Exiting program.");
                    break;
                default:
                    System.out.println("Invalid command. Please try again.");
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
            byte[] inputBytes = Files.readAllBytes(Paths.get(sourceFileName));
            byte[] compressedData = compress(new String(inputBytes));
            Files.write(Paths.get(archiveName), compressedData);
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
        System.out.println("Information about the developers:");
        System.out.println("ByteBenders : Group 1");
        System.out.println("Ronalds Gackis - 231CDB005");
        System.out.println("Edgars Zoltners - 113RIC073");
        System.out.println("Kaspars Skrinda - 231CDB002");
    }

    // Compress using LZ77 followed by Huffman coding
    public static byte[] compress(String input) {
        try {
            List<LZ77Token> lz77Tokens = lz77Compress(input);
            byte[] encodedLZ77 = encodeLZ77Tokens(lz77Tokens);
            String huffmanCompressed = huffmanCompress(encodedLZ77);
            return toByteArray(huffmanCompressed);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static String huffmanCompress(byte[] encodedLZ77) {
        Map<Character, Integer> frequency = getFrequency(new String(encodedLZ77));
        HuffmanNode root = buildTree(frequency);
        Map<Character, String> codes = new HashMap<>();
        generateCodes(root, "", codes);
        StringBuilder encoded = new StringBuilder();
        for (byte b : encodedLZ77) {
            encoded.append(codes.get((char) b));
        }
        return encoded.toString();
    }

    // Decompress using LZ77
    public static String decompress(String input) {
        byte[] compData = input.getBytes();
        try {
            return new String(lz77Decompression(compData));
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
    private static byte[] encodeLZ77Tokens(List<LZ77Token> tokens) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (LZ77Token token : tokens) {
            int packedToken = (token.offset << 12) | token.length;
            outputStream.write((packedToken >> 8) & 0xFF);
            outputStream.write(packedToken & 0xFF);
            outputStream.write(token.nextCharacter);
        }
        return outputStream.toByteArray();
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

    private static byte[] toByteArray(String input) {
        StringBuilder binaryString = new StringBuilder(input.length() * 8); // Initial capacity
        for (char c : input.toCharArray()) {
            String binary = Integer.toBinaryString(c);
            while (binary.length() < 8) {
                binary = "0" + binary;
            }
            binaryString.append(binary);
        }

        // Convert binary string to byte array
        int length = binaryString.length();
        byte[] byteArray = new byte[length / 8];
        for (int i = 0; i < length; i += 8) {
            String byteString = binaryString.substring(i, i + 8);
            byteArray[i / 8] = (byte) Integer.parseInt(byteString, 2);
        }

        return byteArray;
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
                current = root;
            }
        }

        return decompressed.toString();
    }

    public static String decompress(String compressedData, HuffmanNode root) {
        StringBuilder decompressed = new StringBuilder();
        HuffmanNode current = root;

        for (char bit : compressedData.toCharArray()) {
            if (current.left == null && current.right == null) {
                decompressed.append(current.character);
                current = root; 
            }

            if (bit == '0') {
                current = current.left;
            } else {
                current = current.right;
            }
        }
        if (current.left == null && current.right == null) {
            decompressed.append(current.character);
        }

        return decompressed.toString();
    }

    // LZ77 Decompression
    private static byte[] lz77Decompression(byte[] data) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] window = new byte[WINDOW_SIZE];
        int windowPointer = 0;
        boolean running = true;

        try (DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(data))) {
            while (running) {
                if (inputStream.available() < 1) {
                    break;
                }

                byte flag = inputStream.readByte();

                for (int i = 0; i < 8; i++) {
                    if ((flag & (1 << i)) == 0) {
                        if (inputStream.available() < 2) {
                            running = false;
                            break;
                        }

                        int byte1 = inputStream.readUnsignedByte();
                        int byte2 = inputStream.readUnsignedByte();
                        int offset = (byte1 << 4) | (byte2 >> 4);
                        int length = (byte2 & 0x0F) + 3;

                        for (int j = 0; j < length; j++) {
                            int index = (windowPointer - offset + WINDOW_SIZE) % WINDOW_SIZE;
                            byte value = window[index < 0 ? index + WINDOW_SIZE : index];
                            output.write(value);
                            window[windowPointer] = value;
                            windowPointer = (windowPointer + 1) % WINDOW_SIZE;
                        }
                    } else {
                        if (inputStream.available() < 1) {
                            running = false;
                            break;
                        }

                        byte literal = inputStream.readByte();
                        output.write(literal);
                        window[windowPointer] = literal;
                        windowPointer = (windowPointer + 1) % WINDOW_SIZE;
                    }
                }
            }
        }

        return output.toByteArray();
    }
}
