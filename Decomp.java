import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Decomp {
private static final int WINDOW_SIZE = 4096;

public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    System.out.print("Compressed data input: ");
    String input = scanner.nextLine();

    System.out.print("Decompressed data output: ");
    String output = scanner.nextLine();

    try {
        // Read the compressed file into a byte array
        byte[] compData = readFile2BA(new File(input));
        // Decompress the data
        byte[] decData = lz77Decompression(compData);

        // Write the decompressed data to the output file
        writeBA2File(decData, new File(output));

        // Output the size of the decompressed file
        System.out.println("Size: " + decData.length + " bytes.");
    } catch (IOException e) {
        e.printStackTrace();
    }
}

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

private static byte[] readFile2BA(File file) throws IOException {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        return outputStream.toByteArray();
    }
}

private static void writeBA2File(byte[] data, File file) throws IOException {
    try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
        outputStream.write(data);
        outputStream.flush();
    }
}
}