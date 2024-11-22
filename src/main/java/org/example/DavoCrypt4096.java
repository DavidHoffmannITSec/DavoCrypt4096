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
		byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
		int maxPlaintextLength = Math.max(1, modulus.bitLength() / 8 - 42); // Sicherstellen, dass die Länge positiv ist

		// Prüfen, ob der Text zu groß ist
		if (plaintextBytes.length > maxPlaintextLength) {
			return splitAndEncrypt(plaintextBytes, maxPlaintextLength);
		}

		// Einzelner Block verschlüsseln
		BigInteger plaintextInt = new BigInteger(1, plaintextBytes);
		BigInteger encrypted = plaintextInt.modPow(publicKey, modulus);
		return Base64.getEncoder().encodeToString(encrypted.toByteArray());
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
		if (ciphertext.contains(":")) {
			return decryptSplitCiphertext(ciphertext);
		}

		// Einzelner Block entschlüsseln
		BigInteger ciphertextInt = new BigInteger(1, Base64.getDecoder().decode(ciphertext));
		BigInteger decrypted = ciphertextInt.modPow(privateKey, modulus);
		return new String(decrypted.toByteArray(), StandardCharsets.UTF_8);
	}

	private String decryptSplitCiphertext(String ciphertext) {
		String[] blocks = ciphertext.split(":");
		ByteArrayOutputStream decryptedStream = new ByteArrayOutputStream();

		for (String block : blocks) {
			BigInteger ciphertextInt = new BigInteger(1, Base64.getDecoder().decode(block));
			BigInteger decryptedBlock = ciphertextInt.modPow(privateKey, modulus);
			try {
				decryptedStream.write(decryptedBlock.toByteArray());
			} catch (IOException e) {
				throw new RuntimeException("Error processing block during decryption", e);
			}
		}

		return new String(decryptedStream.toByteArray(), StandardCharsets.UTF_8);
	}

	public BigInteger getPublicKey() {
		return publicKey;
	}

	public BigInteger getModulus() {
		return modulus;
	}
}
