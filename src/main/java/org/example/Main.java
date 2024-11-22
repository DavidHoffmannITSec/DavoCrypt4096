package org.example;

public class Main {
    public static void main(String[] args) {
        DavoCrypt4096 davoCrypt = new DavoCrypt4096();

        String originalText = "Hallo 🌍! ÄÖÜ äöü 中 ع 🚀";
        System.out.println("Original Text: " + originalText);

        // Verschlüsselung
        String encryptedText = davoCrypt.encrypt(originalText);
        System.out.println("Encrypted Text: " + encryptedText);

        // Entschlüsselung
        String decryptedText = davoCrypt.decrypt(encryptedText);
        System.out.println("Decrypted Text: " + decryptedText);

        // Validierung
        if (originalText.equals(decryptedText)) {
            System.out.println("\n\nEncryption and decryption were successful!");
        } else {
            System.out.println("\n\nSomething went wrong!");
        }
    }
}
