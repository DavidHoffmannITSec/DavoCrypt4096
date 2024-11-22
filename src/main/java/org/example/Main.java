package org.example;

public class Main {
    public static void main(String[] args) {
        DavoCrypt4096 davoCrypt = new DavoCrypt4096();

        String originalText = "Hallo das hier ist ein Test: 🌍! ÄÖÜ äöü 中 ع 🚀";
        System.out.println("Original Text: " + originalText);

        // Verschlüsselung
        String encryptedText = davoCrypt.encrypt(originalText);
        System.out.println("Encrypted Text: " + encryptedText);

        // Entschlüsselung
        String decryptedText = davoCrypt.decrypt(encryptedText);
        System.out.println("Decrypted Text: " + decryptedText);
    }
}
