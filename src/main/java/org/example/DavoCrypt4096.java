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
		validateInput(plaintext, "Plaintext");

		byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
		int maxPlaintextLength = Math.max(1, modulus.bitLength() / 8 - 42);

		String encryptedData = (plaintextBytes.length > maxPlaintextLength)
				? splitAndEncrypt(plaintextBytes, maxPlaintextLength)
				: encryptBlock(plaintextBytes);

		// Generiere Signatur mit Salt
		String salt = generateSalt();
		String signature = generateSignature(plaintextBytes, salt);

		// Verschlüsselter Text + Signatur + Salt
		return encryptedData + ":" + signature + ":" + salt;
	}

	private String splitAndEncrypt(byte[] plaintextBytes, int maxBlockSize) {
		StringBuilder encryptedBuilder = new StringBuilder();
		int offset = 0;

		while (offset < plaintextBytes.length) {
			int blockSize = Math.min(maxBlockSize, plaintextBytes.length - offset);
			byte[] block = new byte[blockSize];
			System.arraycopy(plaintextBytes, offset, block, 0, blockSize);

			encryptedBuilder.append(encryptBlock(block)).append(":");
			offset += blockSize;
		}

		return encryptedBuilder.substring(0, encryptedBuilder.length() - 1); // Entferne letztes ":"
	}

	private String encryptBlock(byte[] block) {
		BigInteger blockInt = new BigInteger(1, block);
		BigInteger encrypted = blockInt.modPow(publicKey, modulus);
		return Base64.getEncoder().encodeToString(encrypted.toByteArray());
	}

	public String decrypt(String ciphertext) {
		validateInput(ciphertext, "Ciphertext");

		String[] parts = ciphertext.split(":");
		if (parts.length < 3) {
			throw new SecurityException("Invalid ciphertext format. Missing signature or salt.");
		}

		// Hole Signatur und Salt
		String receivedSignature = parts[parts.length - 2];
		String receivedSalt = parts[parts.length - 1];

		// Hole den verschlüsselten Text
		String encryptedBase64 = String.join(":", java.util.Arrays.copyOf(parts, parts.length - 2));

		// Entschlüsseln
		String decryptedText = encryptedBase64.contains(":")
				? decryptSplitCiphertext(encryptedBase64)
				: decryptBlock(encryptedBase64);

		// Signatur überprüfen
		byte[] decryptedBytes = decryptedText.getBytes(StandardCharsets.UTF_8);
		String calculatedSignature = generateSignature(decryptedBytes, receivedSalt);

		if (!calculatedSignature.equals(receivedSignature)) {
			throw new SecurityException("Signature validation failed. Data integrity is compromised.");
		}

		System.out.println("Integrität und Authentizität erfolgreich geprüft.");
		return decryptedText;
	}

	private String decryptSplitCiphertext(String ciphertext) {
		String[] blocks = ciphertext.split(":");
		ByteArrayOutputStream decryptedStream = new ByteArrayOutputStream();

		for (String block : blocks) {
			byte[] decryptedBytes = decryptBlockToBytes(block);
			try {
				decryptedStream.write(decryptedBytes);
			} catch (IOException e) {
				throw new RuntimeException("Error processing block during decryption.", e);
			}
		}

		return decryptedStream.toString(StandardCharsets.UTF_8);
	}

	private String decryptBlock(String encryptedBase64) {
		byte[] decryptedBytes = decryptBlockToBytes(encryptedBase64);
		return new String(decryptedBytes, StandardCharsets.UTF_8);
	}

	private byte[] decryptBlockToBytes(String encryptedBase64) {
		BigInteger ciphertextInt = new BigInteger(1, Base64.getDecoder().decode(encryptedBase64));
		BigInteger decrypted = ciphertextInt.modPow(privateKey, modulus);
		byte[] decryptedBytes = decrypted.toByteArray();

		// Entferne führende Nullen
		return (decryptedBytes.length > 0 && decryptedBytes[0] == 0)
				? java.util.Arrays.copyOfRange(decryptedBytes, 1, decryptedBytes.length)
				: decryptedBytes;
	}

	private String generateSignature(byte[] data, String salt) {
		BigInteger signature = BigInteger.ZERO;
		BigInteger saltValue = new BigInteger(salt.getBytes(StandardCharsets.UTF_8));

		// Verbesserter Signaturalgorithmus
		for (byte b : data) {
			signature = signature.xor(BigInteger.valueOf(b & 0xFF));
			signature = signature.multiply(saltValue).add(BigInteger.valueOf(0x9E3779B97F4A7C15L)).mod(modulus);
		}

		return Base64.getEncoder().encodeToString(signature.toByteArray());
	}

	private String generateSalt() {
		long timestamp = System.currentTimeMillis();
		return Base64.getEncoder().encodeToString(BigInteger.valueOf(timestamp).toByteArray());
	}

	private void validateInput(String input, String name) {
		if (input == null || input.isEmpty()) {
			throw new IllegalArgumentException(name + " cannot be null or empty.");
		}
	}

	public BigInteger getPublicKey() {
		return publicKey;
	}

	public BigInteger getModulus() {
		return modulus;
	}
}
