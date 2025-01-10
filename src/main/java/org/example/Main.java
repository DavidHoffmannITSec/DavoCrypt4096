package org.example;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        DavoCrypt4096 davoCrypt = new DavoCrypt4096();

        //Dateien
        String keyPath = "C:/Users/PC/Documents/"; // Verzeichnis für Schlüssel
        String filePath = "C:/Users/PC/Documents/hashtest.txt";

        try
        {
            davoCrypt.saveKeys(keyPath);
            System.out.println("Schlüssel erfolgreich gespeichert unter: " + keyPath);

            davoCrypt.loadKeys(keyPath);
            System.out.println("Schlüssel erfolgreich geladen aus: " + keyPath);

         /*   davoCrypt.encryptFile(filePath);
            System.out.println("Datei erfolgreich verschlüsselt: " + filePath);

            davoCrypt.decryptFile(filePath);
            System.out.println("Datei erfolgreich entschlüsselt: " + filePath);*/

        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

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
