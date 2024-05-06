import java.util.*;
import java.io.*;

public class Huffman {
    public static void main(String[] args) {
        String fileDir = System.getProperty("user.dir");
        String filePath = fileDir + "/CompriZip/test1.html";

        StringBuilder text = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String compressedData = compress(text.toString());
        System.out.println("Compressed Data: " + compressedData);

        // Assuming you have the root node declared globally
        HuffmanNode root = buildTree(getFrequency(text.toString()));
        String tests = "1000000010011111100110110101001001011001100101001011111000101011101010010111010111000100111110111010110100001111110000011011000101111110110111";
        String decompressedData = decompress(compressedData, root);
        System.out.println("Decompressed Data: " + decompressedData);
    }

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

    public static Map<Character, Integer> getFrequency(String text) {
        Map<Character, Integer> frequency = new HashMap<>();
        for (char c : text.toCharArray()) {
            frequency.put(c, frequency.getOrDefault(c, 0) + 1);
        }
        return frequency;
    }

    public static HuffmanNode buildTree(Map<Character, Integer> freq) {
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

    public static void generateCodes(HuffmanNode node, String code, Map<Character, String> codes) {
        if (node != null) {
            if (node.left == null && node.right == null) {
                codes.put(node.character, code);
            }
            generateCodes(node.left, code + "0", codes);
            generateCodes(node.right, code + "1", codes);
        }
    }

    public static String compress(String text) {
        Map<Character, Integer> frequency = getFrequency(text);
        HuffmanNode root = buildTree(frequency);
        Map<Character, String> codes = new HashMap<>();
        generateCodes(root, "", codes);
        StringBuilder encoded = new StringBuilder();
        for (char c : text.toCharArray()) {
            encoded.append(codes.get(c));
        }
        return encoded.toString();
    }

    public static String decompress(String compressedData, HuffmanNode root) {
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
}
