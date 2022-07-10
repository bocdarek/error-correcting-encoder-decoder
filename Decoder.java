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
        List<String> decodedTextWithoutTail = removeTail(decodedTextBin);
        System.out.println("remove: " + String.join(" ", removeTail(decodedTextBin)));
        String decodedTextHex = converter.hexViewFromBinary(decodedTextWithoutTail);
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
            expandedList.add("" + binaryView.charAt(0) + binaryView.charAt(0)
                                + binaryView.charAt(1) + binaryView.charAt(1)
                                + binaryView.charAt(2) + binaryView.charAt(2)
                                + "..");
            binaryView = binaryView.substring(3);
        }
        StringBuilder lastElement = new StringBuilder();
        for (int i = 0; i < binaryView.length(); i++) {
            lastElement.append(binaryView.charAt(i)).append(binaryView.charAt(i));
        }
        lastElement.append(".".repeat(8 - 2 * binaryView.length()));
        expandedList.add(lastElement.toString());
        return expandedList;
    }

    private List<String> createParityView(List<String> expandedView) {
        String lastElement = expandedView.get(expandedView.size() - 1).replace(".", "");
        lastElement = lastElement + "0".repeat(6 - lastElement.length()) + "..";
        expandedView.set(expandedView.size() - 1, lastElement);
        List<String> parityView = new ArrayList<>();
        for (String str : expandedView) {
            int sum = Integer.parseInt(str.charAt(0) + "")
                    + Integer.parseInt(str.charAt(2) + "")
                    + Integer.parseInt(str.charAt(4) + "");
            String newStr = str.substring(0, 6) + (sum % 2 == 0 ? "00" : "11");
            parityView.add(newStr);
        }
        return parityView;
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
        int index = rd.nextInt(bitChar.length);
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
        if (bitArray[6] != bitArray[7]) {
            int sum = Integer.parseInt(bitArray[0] + "")
                    + Integer.parseInt(bitArray[2] + "")
                    + Integer.parseInt(bitArray[4] + "");
            bitArray[6] = (sum % 2 == 0) ? '0' : '1';
            bitArray[7] = bitArray[6];
            return String.valueOf(bitArray);
        }
        int corruptedPair = 0;
        for (int i = 0; i < 6; i += 2) {
            if (bitArray[i] != bitArray[i + 1]) {
                corruptedPair = i / 2;
            }
        }
        int partialSum = 0;
        for (int i = 0; i < 3; i++) {
            if (i == corruptedPair) {
                continue;
            }
            partialSum += Integer.parseInt(bitArray[2 * i] + "");
        }
        char correctChar;
        if (partialSum % 2 == 0) {
            correctChar = bitArray[6];
        } else {
            correctChar = (bitArray[6] == '1') ? '0' : '1';
        }
        bitArray[2 *corruptedPair] = correctChar;
        bitArray[2 *corruptedPair + 1] = correctChar;
        return String.valueOf(bitArray);
    }

    private List<String> decodeBytes(List<String> encodedText) {
        for (int i = 0; i < encodedText.size(); i++) {
            encodedText.set(i, encodedText.get(i).substring(0, 6));
        }
        String bitStream = String.join("", encodedText);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bitStream.length(); i += 2) {
            sb.append(bitStream.charAt(i));
        }
        bitStream = sb.toString();

        List<String> decodedBytes = new ArrayList<>();
        StringBuilder nextByte = new StringBuilder();
        int length = bitStream.length();
        nextByte.append(bitStream.charAt(0));
        for (int i = 1; i < length; i++) {
            if (i % 8 == 0) {
                decodedBytes.add(nextByte.toString());
                nextByte = new StringBuilder();
            }
            nextByte.append(bitStream.charAt(i));
        }
        decodedBytes.add(nextByte.toString());
        return decodedBytes;
    }

    private List<String> removeTail(List<String> bytes) {
        if (bytes.get(bytes.size() - 1).length() < 8) {
            bytes.remove(bytes.size() - 1);
        }
        return bytes;
    }
}
