package org.example;

import java.math.BigInteger;

public class Main {
    public static void main(String[] args) {
        DavoCrypt4096 davoCrypt = new DavoCrypt4096();

        encryptText();


    }

    public static void encryptText()
    {
        DavoCrypt4096 davoCrypt = new DavoCrypt4096();

        String text = "Hallo das hier ist ein Test: 🌍! ÄÖÜ äöü 中 ع 🚀";
        System.out.println("Original Text: " + text + "\n");

        // Verschlüsselung
        String encryptedText = davoCrypt.encrypt(text);
        System.out.println("Encrypted Text: " + encryptedText);

        // Entschlüsselung
        String decryptedText = davoCrypt.decrypt(encryptedText);
        System.out.println("\nDecrypted Text: " + decryptedText);
    }
}
