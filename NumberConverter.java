package correcter;

import java.util.ArrayList;
import java.util.List;

public class NumberConverter {

    public String charToBinary(char ch) {
        StringBuilder sb = new StringBuilder();
        int num = ch;
        while (num > 0) {
            sb.append(num % 2);
            num /= 2;
        }
        //fill with leading zeros
        while (sb.length() < 8) {
            sb.append(0);
        }
        return sb.reverse().toString();
    }

    public char binaryToChar(String num) {
        return (char) Integer.parseInt(num, 2);
    }

    public String charToHex(char ch) {
        String digits = "0123456789ABCDEF";
        return "" + digits.charAt(ch / 16) + digits.charAt(ch % 16);
    }

    public char hexToChar(String num) {
        return (char) Integer.parseInt(num, 16);
    }

    public String binaryToHex(String num) {
        return charToHex(binaryToChar(num));

    }

    public String hexToBinary(String num) {
        return charToBinary(hexToChar(num));
    }

    public String hexViewFromText(String text) {
        String[] hexArray = new String[text.length()];
        for (int i = 0; i < text.length(); i++) {
            hexArray[i] = charToHex(text.charAt(i));
        }
        return String.join(" ", hexArray);
    }

    public String binViewFromHex(String text) {
        String[] hexArray = text.split(" ");
        String[] binArray = new String[hexArray.length];
        for (int i = 0; i < hexArray.length; i++) {
            binArray[i] = hexToBinary(hexArray[i]);
        }
        return String.join(" ", binArray);
    }

    public String hexViewFromBinary(List<String> binaryList) {
        List<String> hexList = new ArrayList<>();
        for (String s : binaryList) {
            hexList.add(binaryToHex(s));
        }
        return String.join(" ", hexList);
    }

    public String hexViewToText(String hexText) {
        String[] hexArray = hexText.split(" ");
        char[] charArray = new char[hexArray.length];
        for (int i = 0; i < hexArray.length; i++) {
            charArray[i] = hexToChar(hexArray[i]);
        }
        return String.valueOf(charArray);
    }
}
