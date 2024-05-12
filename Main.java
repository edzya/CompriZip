import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.Deflater;

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

    private static HuffmanNode huffmanRoot;
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
            byte[] compressedData = compressWithDeflater(new String(inputBytes)); // Using compressWithDeflater instead of compress
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
            byte[] compressedData = Files.readAllBytes(Paths.get(archiveName));
            byte[] decompressedData = decompress(compressedData);
            Files.write(Paths.get(fileName), decompressedData);
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

    private static byte[] decompress(byte[] compressedData) {
        String utf8String = new String(compressedData, StandardCharsets.UTF_8);
        String binaryString = utf8ToBinary(utf8String);
    
        // Perform decompression based on the compression type
        if (isLZ77Compressed(compressedData)) {
            return lz77Decompress(binaryString);
        } else {
            return huffmanDecompress(binaryString);
        }
    }
    
    private static byte[] huffmanDecompress(String binaryString) {
        HuffmanNode current = huffmanRoot; // Initialize current with the root of the Huffman tree
    
        List<Byte> result = new ArrayList<>();
        int index = 0;
    
        while (index < binaryString.length()) {
            if (current == null) {
                throw new IllegalStateException("Invalid Huffman tree: encountered null node.");
            }
    
            if (current.left == null && current.right == null) {
                // Reached a leaf node, add the character to the result
                result.add((byte) current.character);
                System.out.println("Decompressing Huffman: " + current.character);
                current = huffmanRoot; // Reset current to the root
            } else {
                if (binaryString.charAt(index) == '0') {
                    current = current.left;
                } else {
                    current = current.right;
                }
                index++;
            }
        }
    
        // Convert the list of bytes to a byte array
        byte[] output = new byte[result.size()];
        for (int i = 0; i < result.size(); i++) {
            output[i] = result.get(i);
        }
        return output;
    }
    
      

    // Decompression method for LZ77
    private static byte[] lz77Decompress(String binaryString) {
        List<LZ77Token> tokens = new ArrayList<>();
        for (int i = 0; i < binaryString.length(); i += 3) {
            int packedToken = Integer.parseInt(binaryString.substring(i, i + 12), 2);
            int offset = packedToken >> 12;
            int length = packedToken & 0xFFF;
            char nextCharacter = (char) Integer.parseInt(binaryString.substring(i + 12, i + 16), 2);
            tokens.add(new LZ77Token(offset, length, nextCharacter));
            System.out.println("Decompressing LZ77: Offset = " + offset + ", Length = " + length + ", Next Character = " + nextCharacter);
        }
    
        StringBuilder decompressed = new StringBuilder();
        for (LZ77Token token : tokens) {
            int startIndex = decompressed.length() - token.offset;
            int endIndex = startIndex + token.length;
            for (int i = startIndex; i < endIndex; i++) {
                decompressed.append(decompressed.charAt(i));
            }
            decompressed.append(token.nextCharacter);
        }
    
        // Convert the StringBuilder to a byte array
        byte[] output = new byte[decompressed.length()];
        for (int i = 0; i < decompressed.length(); i++) {
            output[i] = (byte) decompressed.charAt(i);
        }
        return output;
    }
    
    

    // Check if the data is compressed using LZ77
    private static boolean isLZ77Compressed(byte[] compressedData) {
        return compressedData.length % 3 == 0;
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
    private static byte[] encodeLZ77Tokens(List<LZ77Token> tokens) {
        StringBuilder binaryString = new StringBuilder();
        for (LZ77Token token : tokens) {
            binaryString.append(String.format("%12s", Integer.toBinaryString(token.offset)).replace(' ', '0'));
            binaryString.append(String.format("%3s", Integer.toBinaryString(token.length)).replace(' ', '0'));
            binaryString.append(String.format("%4s", Integer.toBinaryString(token.nextCharacter)).replace(' ', '0'));
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
    
        huffmanRoot = priorityQueue.poll(); // Store the root of the Huffman tree
        return huffmanRoot;
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

    // Compress using Deflater
    private static byte[] compressWithDeflater(String input) {
        try {
            Deflater deflater = new Deflater();
            deflater.setInput(input.getBytes("UTF-8"));
            deflater.finish();
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            deflater.end();
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Utility method to convert UTF-8 string to binary string
    private static String utf8ToBinary(String utf8String) {
        StringBuilder binaryString = new StringBuilder();
        byte[] bytes = utf8String.getBytes(StandardCharsets.UTF_8);
        for (byte b : bytes) {
            binaryString.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        return binaryString.toString();
    }
}
