import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Main {
    // LZ77 Constants
    private static final int WINDOW_SIZE = 64;
    private static final int LOOK_AHEAD_BUFFER_SIZE = 16;

    public static byte[] LZ77compress(String input) {
        List<Byte> compressedBytes = new ArrayList<>();
        int currentIndex = 0;

        while (currentIndex < input.length()) {
            int matchLength = 0;
            int matchIndex = 0;

            // Search for the longest match in the window
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

            // If match is found, add token (offset, length, next_character)
            if (matchLength > 0) {
                compressedBytes.add((byte) (matchIndex >> 8)); // First byte of offset
                compressedBytes.add((byte) (matchIndex & 0xFF)); // Second byte of offset
                compressedBytes.add((byte) matchLength); // Length
                compressedBytes.add((byte) input.charAt(currentIndex + matchLength - 1)); // Next character
                currentIndex += matchLength;
            } else {
                compressedBytes.add((byte) 0); // Offset
                compressedBytes.add((byte) 0); // Length
                compressedBytes.add((byte) input.charAt(currentIndex)); // Next character
                currentIndex++;
            }
        }

        byte[] result = new byte[compressedBytes.size()];
        for (int i = 0; i < compressedBytes.size(); i++) {
            result[i] = compressedBytes.get(i);
        }
        return result;
    }

    private static int findMatchLength(String input, int windowIndex, int currentIndex, int endLookAheadBuffer) {
        int maxLength = Math.min(LOOK_AHEAD_BUFFER_SIZE, endLookAheadBuffer - currentIndex);
        int length = 0;

        while (length < maxLength && input.charAt(windowIndex + length) == input.charAt(currentIndex + length)) {
            length++;
        }

        return length;
    }

    public static class LZ77Token {
        private int offset;
        private int length;
        private char nextCharacter;

        public LZ77Token(int offset, int length, char nextCharacter) {
            this.offset = offset;
            this.length = length;
            this.nextCharacter = nextCharacter;
        }

        // Getters
        public int getOffset() {
            return offset;
        }

        public int getLength() {
            return length;
        }

        public char getNextCharacter() {
            return nextCharacter;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter source file name:");
        String sourceFileName = sc.nextLine().trim();
        
        System.out.println("Enter archive name:");
        String archiveName = sc.nextLine().trim();
        

        //String input = "abracadabra";
        StringBuilder content = new StringBuilder();
        try (Scanner scanner = new Scanner(new File(sourceFileName))) {
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine()); // Append each line to StringBuilder
            }
            
        } catch (IOException e) {
            System.out.println("Iekritu te");
            e.printStackTrace();
        }
        
        
        String input = content.toString();
        
        
        byte[] compressedData = LZ77compress(input);
        System.out.println(compressedData.length);
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(archiveName));
            out.write(compressedData);
            out.close();
        } catch (Exception ex) {
            System.out.print(ex.getMessage());

        }
    }

}