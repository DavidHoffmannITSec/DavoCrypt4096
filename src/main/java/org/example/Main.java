package org.example;

public class Main
{
    public static void main(String[] args)
    {
        DavoCrypt4096 davoCrypt = new DavoCrypt4096();

        // Beispiel für Verschlüsselung und Entschlüsselung
        String plaintext = "Hello, World!";
        System.out.println("Original Text: " + plaintext);

        String encryptedText = davoCrypt.encrypt(plaintext);
        System.out.println("Encrypted Text: " + encryptedText);

        String decryptedText = davoCrypt.decrypt(encryptedText);
        System.out.println("Decrypted Text: " + decryptedText);
    }
}