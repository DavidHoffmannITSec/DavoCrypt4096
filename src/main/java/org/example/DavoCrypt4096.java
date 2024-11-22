package org.example;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DavoCrypt4096 {
	private final KeyGenerator keyGenerator;
	private BigInteger publicKey;
	private BigInteger privateKey;
	private BigInteger modulus;

	public DavoCrypt4096() {
		keyGenerator = new KeyGenerator();
		initializeKeys();
	}

	private void initializeKeys() {
		this.publicKey = keyGenerator.getPublicKey();
		this.privateKey = keyGenerator.getPrivateKey();
		this.modulus = keyGenerator.getModulus();
	}

	public String encrypt(String plaintext) {
		if (plaintext == null || plaintext.isEmpty()) {
			throw new IllegalArgumentException("Plaintext cannot be null or empty.");
		}

		byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
		int maxPlaintextLength = Math.max(1, modulus.bitLength() / 8 - 42);

		// Prüfen, ob der Text zu groß ist
		String encryptedData;
		if (plaintextBytes.length > maxPlaintextLength) {
			encryptedData = splitAndEncrypt(plaintextBytes, maxPlaintextLength);
		} else {
			BigInteger plaintextInt = new BigInteger(1, plaintextBytes);
			BigInteger encrypted = plaintextInt.modPow(publicKey, modulus);
			encryptedData = Base64.getEncoder().encodeToString(encrypted.toByteArray());
		}

		// Generiere eine Signatur für die Klartextdaten
		String signature = generateSignature(plaintextBytes);

		// Verschlüsselter Text und Signatur
		return encryptedData + ":" + signature;
	}

	private String splitAndEncrypt(byte[] plaintextBytes, int maxBlockSize) {
		StringBuilder encryptedBuilder = new StringBuilder();
		int offset = 0;

		while (offset < plaintextBytes.length) {
			int blockSize = Math.min(maxBlockSize, plaintextBytes.length - offset);
			byte[] block = new byte[blockSize];
			System.arraycopy(plaintextBytes, offset, block, 0, blockSize);

			BigInteger blockInt = new BigInteger(1, block);
			BigInteger encryptedBlock = blockInt.modPow(publicKey, modulus);
			encryptedBuilder.append(Base64.getEncoder().encodeToString(encryptedBlock.toByteArray())).append(":");

			offset += blockSize;
		}

		return encryptedBuilder.substring(0, encryptedBuilder.length() - 1); // Entferne das letzte ":"
	}

	public String decrypt(String ciphertext) {
		if (ciphertext == null || ciphertext.isEmpty()) {
			throw new IllegalArgumentException("Ciphertext cannot be null or empty.");
		}

		// Trenne verschlüsselten Text und Signatur
		String[] parts = ciphertext.split(":");
		if (parts.length < 2) {
			throw new SecurityException("Invalid ciphertext format. Signature is missing.");
		}

		// Hole die Signatur
		String receivedSignature = parts[parts.length - 1];

		// Hole den verschlüsselten Text (ohne Signatur)
		StringBuilder encryptedDataBuilder = new StringBuilder();
		for (int i = 0; i < parts.length - 1; i++) {
			if (i > 0) encryptedDataBuilder.append(":");
			encryptedDataBuilder.append(parts[i]);
		}
		String encryptedBase64 = encryptedDataBuilder.toString();

		String decryptedText;
		if (encryptedBase64.contains(":")) {
			decryptedText = decryptSplitCiphertext(encryptedBase64);
		} else {
			BigInteger ciphertextInt = new BigInteger(1, Base64.getDecoder().decode(encryptedBase64));
			BigInteger decrypted = ciphertextInt.modPow(privateKey, modulus);
			byte[] decryptedBytes = decrypted.toByteArray();

			// Entferne führende Nullen
			if (decryptedBytes.length > 0 && decryptedBytes[0] == 0) {
				decryptedBytes = java.util.Arrays.copyOfRange(decryptedBytes, 1, decryptedBytes.length);
			}

			decryptedText = new String(decryptedBytes, StandardCharsets.UTF_8);
		}

		// Überprüfe die Signatur
		byte[] decryptedBytes = decryptedText.getBytes(StandardCharsets.UTF_8);
		String calculatedSignature = generateSignature(decryptedBytes);

		if (calculatedSignature.equals(receivedSignature)) {
			System.out.println("Integrität und Authentizität wurden erfolgreich geprüft.");
		} else {
			throw new SecurityException("Signature validation failed. Data integrity is compromised.");
		}

		return decryptedText;
	}

	private String decryptSplitCiphertext(String ciphertext) {
		String[] blocks = ciphertext.split(":");
		ByteArrayOutputStream decryptedStream = new ByteArrayOutputStream();

		for (String block : blocks) {
			BigInteger ciphertextInt = new BigInteger(1, Base64.getDecoder().decode(block));
			BigInteger decryptedBlock = ciphertextInt.modPow(privateKey, modulus);
			byte[] decryptedBytes = decryptedBlock.toByteArray();

			// Entferne führende Nullen, die durch BigInteger hinzugefügt werden können
			if (decryptedBytes.length > 0 && decryptedBytes[0] == 0) {
				decryptedBytes = java.util.Arrays.copyOfRange(decryptedBytes, 1, decryptedBytes.length);
			}

			try {
				decryptedStream.write(decryptedBytes);
			} catch (IOException e) {
				throw new RuntimeException("Error processing block during decryption", e);
			}
		}

		return decryptedStream.toString(StandardCharsets.UTF_8);
	}

	private String generateSignature(byte[] data) {
		BigInteger signature = BigInteger.ZERO;

		// Simpler Signaturalgorithmus basierend auf XOR und Feedback
		for (byte b : data) {
			signature = signature.xor(BigInteger.valueOf(b & 0xFF)).multiply(BigInteger.valueOf(31));
			signature = signature.add(BigInteger.valueOf(0x9E3779B9L)).mod(modulus);
		}

		return Base64.getEncoder().encodeToString(signature.toByteArray());
	}

	public BigInteger getPublicKey() {
		return publicKey;
	}

	public BigInteger getModulus() {
		return modulus;
	}
}
