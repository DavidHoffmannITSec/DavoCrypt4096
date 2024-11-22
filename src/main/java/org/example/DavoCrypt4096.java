package org.example;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

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
		String paddedPlaintext = addPadding(plaintext);
		BigInteger plaintextInt = new BigInteger(paddedPlaintext.getBytes(StandardCharsets.UTF_8));

		if (plaintextInt.compareTo(modulus) >= 0) {
			throw new IllegalArgumentException("Plaintext too large for encryption. Split into smaller parts.");
		}

		BigInteger encrypted = plaintextInt.modPow(publicKey, modulus);
		return Base64.getEncoder().encodeToString(encrypted.toByteArray());
	}

	public String decrypt(String ciphertext) {
		BigInteger ciphertextInt = new BigInteger(Base64.getDecoder().decode(ciphertext));
		BigInteger decrypted = ciphertextInt.modPow(privateKey, modulus);

		String paddedPlaintext = new String(decrypted.toByteArray(), StandardCharsets.UTF_8);
		return removePadding(paddedPlaintext);
	}

	private String addPadding(String plaintext) {
		String hash = simpleHash(plaintext);
		StringBuilder padded = new StringBuilder(plaintext + ":" + hash);

		// Erzwinge eine Mindestgröße
		int minSize = modulus.bitLength() / 8 - 11; // Max RSA-Größe
		while (padded.length() < minSize) {
			padded.insert(0, '0'); // Padding mit führenden Nullen
		}

		return padded.toString();
	}



	private String removePadding(String paddedPlaintext) {
		int separatorIndex = paddedPlaintext.lastIndexOf(':');
		if (separatorIndex == -1) {
			throw new IllegalArgumentException("Invalid padding");
		}

		String plaintext = paddedPlaintext.substring(0, separatorIndex);
		String hash = paddedPlaintext.substring(separatorIndex + 1);

		if (!hash.equals(simpleHash(plaintext))) {
			throw new IllegalArgumentException("Padding verification failed. Data integrity compromised.");
		}

		return plaintext;
	}

	private String simpleHash(String input) {
		BigInteger hash = BigInteger.ZERO;
		byte[] bytes = input.getBytes(StandardCharsets.UTF_8);

		for (byte b : bytes) {
			hash = hash.xor(BigInteger.valueOf(b & 0xFF));
			hash = hash.multiply(BigInteger.valueOf(31)).mod(BigInteger.valueOf(1_000_000_007)); // Prime modulus
		}

		return hash.toString(16); // Hex-String
	}

	public BigInteger getPublicKey() {
		return publicKey;
	}

	public BigInteger getModulus() {
		return modulus;
	}
}
