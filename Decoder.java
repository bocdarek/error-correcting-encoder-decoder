package correcter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Decoder {

    private final NumberConverter converter = new NumberConverter();
    private final Random rd = new Random();

    private final File file1 = new File("send.txt");
    private final File file2 = new File("encoded.txt");
    private final File file3 = new File("received.txt");
    private final File file4 = new File("decoded.txt");

    public void encode() {
        System.out.println("\n" + file1.getName() + ":");
        String text = readTextFromFile(file1);
        System.out.println("text view: " + text);
        String textHex = converter.hexViewFromText(text);
        System.out.println("hex view: " + textHex);
        String textBin = converter.binViewFromHex(textHex);
        System.out.println("bin view: " + textBin);
        System.out.println("\n" + file2.getName() + ":");
        List<String> expandedView = expand(textBin);
        System.out.println("expand: " + String.join(" ", expandedView));
        List<String> parityView = createParityView(expandedView);
        System.out.println("parity: " + String.join(" ", parityView));
        String hexView = converter.hexViewFromBinary(parityView);
        System.out.println("hex view: " + hexView);
        saveBytesToFile(hexView, file2);
    }

    public void send() {
        System.out.println("\n" + file2.getName() + ":");
        String textHex = readBytesFromFile(file2);
        System.out.println("hex view: " + textHex);
        String textBin = converter.binViewFromHex(textHex);
        System.out.println("bin view: " + textBin);
        System.out.println("\n" + file3.getName() + ":");
        List<String> receivedTextBin = sendToReceiver(textBin);
        System.out.println("bin view: " + String.join(" ", receivedTextBin));
        String receivedTextHex = converter.hexViewFromBinary(receivedTextBin);
        System.out.println("hex view: " + receivedTextHex);
        saveBytesToFile(receivedTextHex, file3);
    }

    public void decode() {
        System.out.println("\n" + file3.getName() + ":");
        String receivedTextHex = readBytesFromFile(file3);
        System.out.println("hex view: " + receivedTextHex);
        String receivedTextBin = converter.binViewFromHex(receivedTextHex);
        System.out.println("bin view: " + receivedTextBin);
        System.out.println("\n" + file4.getName() + ":");
        List<String> correctedTextBin = correctMsg(receivedTextBin);
        System.out.println("correct: " + String.join(" ", correctedTextBin));
        List<String> decodedTextBin = decodeBytes(correctedTextBin);
        System.out.println("decode: " + String.join(" ", decodedTextBin));
        String decodedTextHex = converter.hexViewFromBinary(decodedTextBin);
        System.out.println("hex view: " + decodedTextHex);
        String decodedText = converter.hexViewToText(decodedTextHex);
        System.out.println("text view: " + decodedText);
        saveTextToFile(decodedText, file4);
    }

    private String readTextFromFile(File file) {
        String text = "";
        try (Scanner sc = new Scanner(file)) {
            text = sc.nextLine().trim();
        } catch (FileNotFoundException e) {
            System.out.println("Error!. Failed to read from the file.");
        }
        return text;
    }

    private String readBytesFromFile(File file) {
        List<String> hexArray = new ArrayList<>();
        try (FileInputStream fin = new FileInputStream(file)) {
            int i;
            while ((i = fin.read()) != -1) {
                hexArray.add(converter.charToHex((char) i));
            }
        } catch (IOException e) {
            System.out.println("Error!. Failed to read from the file.");
        }
        return String.join(" ", hexArray);
    }

    private List<String> expand(String binaryView) {
        binaryView = binaryView.replace(" ", "");
        List<String> expandedList = new ArrayList<>();
        while (binaryView.length() > 3) {
            expandedList.add(".." + binaryView.charAt(0) + "." + binaryView.charAt(1)
                    + binaryView.charAt(2) + binaryView.charAt(3) + ".");
            binaryView = binaryView.substring(4);
        }
        return expandedList;
    }

    private List<String> createParityView(List<String> expandedView) {
        List<String> parityView = new ArrayList<>();
        for (String str : expandedView) {
            char parityBit1 = calculateParityBit(str, 2, 4, 6);
            char parityBit2 = calculateParityBit(str, 2, 5, 6);
            char parityBit3 = calculateParityBit(str, 4, 5, 6);
            String sb = String.valueOf(parityBit1) + parityBit2 + str.charAt(2)
                    + parityBit3 + str.substring(4, 7) + 0;
            parityView.add(sb);
        }
        return parityView;
    }

    private char calculateParityBit(String str, int a, int b, int c) {
        int sum = Integer.parseInt(str.charAt(a) + "")
                 + Integer.parseInt(str.charAt(b) + "")
                 + Integer.parseInt(str.charAt(c) + "");
        return (sum % 2 == 1) ? '1' : '0';
    }

    private void saveBytesToFile(String hexText, File file) {
        String[] hexArray = hexText.split(" ");
        try (FileOutputStream fout = new FileOutputStream(file)) {
            for (String s : hexArray) {
                fout.write(converter.hexToChar(s));
            }
        } catch (IOException e) {
            System.out.println("Error! Failed to save to the file.");
        }
    }

    private void saveTextToFile(String str, File file) {
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(str);
        } catch (IOException e) {
            System.out.println("Error! Failed to save to the file.");
        }
    }

    private List<String> sendToReceiver(String binText) {
        List<String> encodedMsg = new ArrayList<>();
        String[] bytes = binText.split(" ");
        for (String str : bytes) {
            encodedMsg.add(injectBinaryNoise(str));
        }
        return encodedMsg;
    }

    private String injectBinaryNoise(String num) {
        char[] bitChar = num.toCharArray();
        int index = rd.nextInt(bitChar.length - 1);
        bitChar[index] = bitChar[index] == '0' ? '1' : '0';
        return String.valueOf(bitChar);
    }

    private List<String> correctMsg(String text) {
        String[] bytes = text.split(" ");
        List<String> correctedBytes = new ArrayList<>();
        for (String element : bytes) {
            correctedBytes.add(removeNoise(element));
        }
        return correctedBytes;
    }

    private String removeNoise(String binText) {
        char[] bitArray = binText.toCharArray();
        int controlSum1 = calculateControlSum(bitArray, 0, 2, 4, 6);
        int controlSum2 = calculateControlSum(bitArray, 1, 2, 5, 6);
        int controlSum4 = calculateControlSum(bitArray, 3, 4, 5, 6);
        int corruptedBit = -1;
        if (controlSum1 % 2 != 0) {
            corruptedBit += 1;
        }
        if (controlSum2 % 2 != 0) {
            corruptedBit += 2;
        }
        if (controlSum4 % 2 != 0) {
            corruptedBit += 4;
        }
        if (corruptedBit != -1) {
            bitArray[corruptedBit] = (bitArray[corruptedBit] == '0' ? '1' : '0');
        }
        return String.valueOf(bitArray);
    }

    private int calculateControlSum(char[] bitArray, int a, int b, int c, int d) {
        return Integer.parseInt(bitArray[a] + "") + Integer.parseInt(bitArray[b] + "")
                + Integer.parseInt(bitArray[c] + "") + Integer.parseInt(bitArray[d] + "");
    }

    private List<String> decodeBytes(List<String> encodedText) {
        List<String> decodedBytes = new ArrayList<>();
        for (int i = 0; i < encodedText.size(); i += 2) {
            String byte1 = encodedText.get(i);
            String byte2 = encodedText.get(i + 1);
            decodedBytes.add(parseByteFromTwo(byte1, byte2));
        }
        return decodedBytes;
    }

    private String parseByteFromTwo(String byte1, String byte2) {
        return "" + byte1.charAt(2) + byte1.charAt(4) + byte1.charAt(5) + byte1.charAt(6)
                + byte2.charAt(2) + byte2.charAt(4) + byte2.charAt(5) + byte2.charAt(6);
    }
}
