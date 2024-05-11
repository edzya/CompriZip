import java.util.PriorityQueue;
import java.util.HashMap;

class HuffmanNode implements Comparable<HuffmanNode> {
    char character;
    int frequency;
    HuffmanNode left;
    HuffmanNode right;

    public HuffmanNode(char character, int frequency) {
        this.character = character;
        this.frequency = frequency;
    }

    @Override
    public int compareTo(HuffmanNode other) {
        return this.frequency - other.frequency;
    }
}

public class Huffman {

    public static void main(String[] args) {
        String inputText = "ABRACADABRA"; 

        HashMap<Character, Integer> frequencyMap = new HashMap<>();
        for (char c : inputText.toCharArray()) {
            frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
        }

        PriorityQueue<HuffmanNode> minHeap = new PriorityQueue<>();
        for (char c : frequencyMap.keySet()) {
            minHeap.offer(new HuffmanNode(c, frequencyMap.get(c)));
        }

        while (minHeap.size() > 1) {
            HuffmanNode left = minHeap.poll();
            HuffmanNode right = minHeap.poll();
            HuffmanNode merged = new HuffmanNode('\0', left.frequency + right.frequency);
            merged.left = left;
            merged.right = right;
            minHeap.offer(merged);
        }

        HuffmanNode root = minHeap.poll();

        HashMap<Character, String> huffmanCodes = new HashMap<>();
        generateHuffmanCodes(root, "", huffmanCodes);

        StringBuilder encodedText = new StringBuilder();
        for (char c : inputText.toCharArray()) {
            encodedText.append(huffmanCodes.get(c));
        }

        System.out.println("Encoded text: " + encodedText.toString());

        StringBuilder decodedText = new StringBuilder();
        HuffmanNode current = root;
        for (char bit : encodedText.toString().toCharArray()) {
            if (bit == '0') {
                current = current.left;
            } else {
                current = current.right;
            }
            if (current.character != '\0') {
                decodedText.append(current.character);
                current = root;
            }
        }

        System.out.println("Decoded text: " + decodedText.toString());
    }

    private static void generateHuffmanCodes(HuffmanNode node, String code, HashMap<Character, String> huffmanCodes) {
        if (node == null) {
            return;
        }
        if (node.character != '\0') {
            huffmanCodes.put(node.character, code);
        }
        generateHuffmanCodes(node.left, code + "0", huffmanCodes);
        generateHuffmanCodes(node.right, code + "1", huffmanCodes);
    }
}
