package correcter;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        System.out.print("Write a mode: ");
        Scanner sc = new Scanner(System.in);
        String mode = sc.nextLine().trim().toLowerCase();

        Decoder decoder = new Decoder();
        switch (mode) {
            case "encode":
                decoder.encode();
                break;
            case "send":
                decoder.send();
                break;
            case "decode":
                decoder.decode();
                break;
            default:
                System.out.println("Wrong mode!");
                break;
        }
    }
}
