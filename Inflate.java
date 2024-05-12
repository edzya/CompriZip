import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Inflate {
    
    public static byte[] inflate(byte[] compressedData) throws IOException {
        byte[] decompressedData = decompressZlib(compressedData);
        List<Byte> output = new ArrayList<>();
        int idx = 0;
        while (idx < decompressedData.length) {
            byte blockHeader = decompressedData[idx++];
            if ((blockHeader & 0b10000000) == 0) { 
                int length = ((blockHeader & 0xFF) << 8) | (decompressedData[idx++] & 0xFF);
                for (int i = 0; i < length; i++) {
                    output.add(decompressedData[idx++]);
                }
            } else { 
                int lengthCode = (blockHeader & 0b1111) + 3;
                int distanceCode = ((blockHeader & 0b11110000) >> 4) + 1;
                int distance = ((decompressedData[idx++] & 0xFF) | ((decompressedData[idx++] & 0xFF) << 8)) + getDistanceExtra(decompressedData, idx - 2);
                for (int i = 0; i < lengthCode; i++) {
                    output.add(output.get(output.size() - distance));
                }
            }
        }

        // Convert list to byte array
        byte[] result = new byte[output.size()];
        for (int i = 0; i < output.size(); i++) {
            result[i] = output.get(i);
        }

        return result;
    }

    private static byte[] decompressZlib(byte[] compressedData) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int numBytesToSkip = 2; // Skip the zlib header
        while (numBytesToSkip > 0) {
            int numBytes = Math.min(numBytesToSkip, compressedData.length);
            out.write(compressedData, compressedData.length - numBytes, numBytes);
            numBytesToSkip -= numBytes;
        }
        return out.toByteArray();
    }

    private static int getDistanceExtra(byte[] decompressedData, int idx) {
        int extraBits = decompressedData[idx] & 0b11;
        int base = ((decompressedData[idx] & 0xFF) >> 2) + ((decompressedData[idx + 1] & 0xFF) << 6);
        return base + extraBits;
    }

    public static void main(String[] args) {
        try {
            byte[] compressedData = new byte[]{0x78, (byte)0x9C, (byte)0xCB, 0x48, 0x55, (byte)0xC9, (byte)0xC9, 0x57, 0x08, (byte)0xCF, 0x2F, (byte)0xCA, 0x49, 0x51, 0x08, (byte)0xCF, 0x2F, (byte)0xC9, 0x2C, 0x52, 0x28, 0x49, 0x2C, 0x4A, 0x55, (byte)0xD2, 0x03, 0x00, 0x16, (byte)0xA3, (byte)0xF4, 0x11, 0x00, 0x00, 0x00};
            byte[] inflatedData = inflate(compressedData);
            System.out.println(new String(inflatedData, "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
