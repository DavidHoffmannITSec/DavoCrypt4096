package org.example;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
		validateInput(plaintext, "Plaintext");

		byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
		int maxPlaintextLength = Math.max(1, modulus.bitLength() / 8 - 42);

		String encryptedData = (plaintextBytes.length > maxPlaintextLength)
				? splitAndEncrypt(plaintextBytes, maxPlaintextLength)
				: encryptBlock(plaintextBytes);

		String salt = generateSalt();
		String signature = generateSignature(plaintextBytes, salt);

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

		return encryptedBuilder.substring(0, encryptedBuilder.length() - 1);
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

		String receivedSignature = parts[parts.length - 2];
		String receivedSalt = parts[parts.length - 1];
		String encryptedBase64 = String.join(":", java.util.Arrays.copyOf(parts, parts.length - 2));

		String decryptedText = encryptedBase64.contains(":")
				? decryptSplitCiphertext(encryptedBase64)
				: decryptBlock(encryptedBase64);

		byte[] decryptedBytes = decryptedText.getBytes(StandardCharsets.UTF_8);
		String calculatedSignature = generateSignature(decryptedBytes, receivedSalt);

		if (!constantTimeEquals(calculatedSignature, receivedSignature)) {
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

		return (decryptedBytes.length > 0 && decryptedBytes[0] == 0)
				? java.util.Arrays.copyOfRange(decryptedBytes, 1, decryptedBytes.length)
				: decryptedBytes;
	}

	private String generateSignature(byte[] data, String salt) {
		// Kombiniere Daten und Salt
		byte[] saltedData = combineDataAndSalt(data, salt);

		// Hash berechnen mit DavoHash512
		byte[] hash = DavoHash512.hash(new String(saltedData, StandardCharsets.UTF_8));

		// Hash in Signatur umwandeln
		BigInteger hashInt = new BigInteger(1, hash);
		BigInteger signature = hashInt.modPow(privateKey, modulus);

		return Base64.getEncoder().encodeToString(signature.toByteArray());
	}

	private byte[] combineDataAndSalt(byte[] data, String salt) {
		byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);
		byte[] combined = new byte[data.length + saltBytes.length];
		System.arraycopy(data, 0, combined, 0, data.length);
		System.arraycopy(saltBytes, 0, combined, data.length, saltBytes.length);
		return combined;
	}

	private String generateSalt() {
		// Sammle dynamische Entropiequellen
		long nanoTime = System.nanoTime();
		long freeMemory = Runtime.getRuntime().freeMemory();
		long totalMemory = Runtime.getRuntime().totalMemory();
		long threadId = Thread.currentThread().threadId();
		int threadPriority = Thread.currentThread().getPriority();
		int hashCode = System.identityHashCode(this);

		// Kombiniere Entropiequellen
		long initialEntropy = nanoTime ^ freeMemory ^ totalMemory ^ threadId ^ threadPriority ^ hashCode;

		// Umgebungsabhängige Entropie
		String envData = String.join("", System.getenv().values());
		long envHash = envData.hashCode();

		// Erzeuge den Basissalt
		BigInteger saltValue = BigInteger.valueOf(initialEntropy)
				.xor(BigInteger.valueOf(envHash))
				.multiply(BigInteger.valueOf(0x9E3779B97F4A7C15L)) // Goldener Schnitt
				.add(BigInteger.valueOf(threadId))
				.mod(BigInteger.TWO.pow(256)); // Begrenzung auf 256 Bits

		// Dynamische Rückkopplungsschleife
		for (int i = 0; i < 10; i++) {
			long dynamicFactor = (System.nanoTime() ^ ((long) i * threadPriority)) + freeMemory;
			saltValue = saltValue.multiply(BigInteger.valueOf(dynamicFactor))
					.xor(BigInteger.valueOf(System.currentTimeMillis() >> i))
					.mod(BigInteger.TWO.pow(256));
		}

		// Nichtlineare Permutationen (ähnlich wie S-Box)
		saltValue = saltValue.xor(saltValue.shiftLeft(13))
				.xor(saltValue.shiftRight(7))
				.multiply(BigInteger.valueOf(31))
				.mod(BigInteger.TWO.pow(256));

		// Base64-Kodierung des endgültigen Salt-Werts
		return Base64.getEncoder().encodeToString(saltValue.toByteArray());
	}

	private boolean constantTimeEquals(String a, String b) {
		if (a.length() != b.length()) return false;

		int result = 0;
		for (int i = 0; i < a.length(); i++) {
			result |= a.charAt(i) ^ b.charAt(i);
		}
		return result == 0;
	}

	private void validateInput(String input, String name) {
		if (input == null || input.isEmpty()) {
			throw new IllegalArgumentException(name + " cannot be null or empty.");
		}
	}

	public void encryptFile(String filePath) throws IOException {
		Path path = Path.of(filePath);
		byte[] fileBytes = Files.readAllBytes(path); // Lies die Datei als Bytes
		String encryptedData = encrypt(new String(fileBytes, StandardCharsets.ISO_8859_1)); // Konvertiere Bytes zu String

		// Speichere die verschlüsselten Daten in der Originaldatei
		Files.writeString(path, encryptedData, StandardCharsets.ISO_8859_1, StandardOpenOption.TRUNCATE_EXISTING);
	}

	public void decryptFile(String filePath) throws IOException {
		Path path = Path.of(filePath);
		byte[] fileBytes = Files.readAllBytes(path); // Lies die verschlüsselte Datei als Bytes
		String decryptedData = decrypt(new String(fileBytes, StandardCharsets.ISO_8859_1)); // Entschlüssele den String

		// Schreibe die entschlüsselten Daten zurück in die Originaldatei
		Files.writeString(path, decryptedData, StandardCharsets.ISO_8859_1, StandardOpenOption.TRUNCATE_EXISTING);
	}

	/**
	 * Speichert die aktuellen Schlüssel (Public, Private, Modulus) in den angegebenen Pfad.
	 *
	 * @param directoryPath Der Pfad des Verzeichnisses, in dem die Schlüssel gespeichert werden sollen.
	 * @throws IOException Wenn ein Fehler beim Schreiben der Dateien auftritt.
	 */
	public void saveKeys(String directoryPath) throws IOException {
		Path publicKeyPath = Path.of(directoryPath, "public.key");
		Path privateKeyPath = Path.of(directoryPath, "private.key");
		Path modulusPath = Path.of(directoryPath, "modulus.key");

		// Speichern der Schlüssel als Textdateien
		Files.writeString(publicKeyPath, publicKey.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		Files.writeString(privateKeyPath, privateKey.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		Files.writeString(modulusPath, modulus.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

		System.out.println("Keys erfolgreich gespeichert in: " + directoryPath);
	}

	/**
	 * Lädt die Schlüssel (Public, Private, Modulus) aus den angegebenen Dateien und initialisiert sie.
	 *
	 * @param directoryPath Der Pfad des Verzeichnisses, aus dem die Schlüssel geladen werden sollen.
	 * @throws IOException Wenn ein Fehler beim Lesen der Dateien auftritt.
	 */
	public void loadKeys(String directoryPath) throws IOException {
		Path publicKeyPath = Path.of(directoryPath, "public.key");
		Path privateKeyPath = Path.of(directoryPath, "private.key");
		Path modulusPath = Path.of(directoryPath, "modulus.key");

		// Lesen der Schlüssel aus den Dateien
		String publicKeyString = Files.readString(publicKeyPath).trim();
		String privateKeyString = Files.readString(privateKeyPath).trim();
		String modulusString = Files.readString(modulusPath).trim();

		// Initialisierung der Schlüssel
		this.publicKey = new BigInteger(publicKeyString);
		this.privateKey = new BigInteger(privateKeyString);
		this.modulus = new BigInteger(modulusString);

		System.out.println("Keys erfolgreich geladen aus: " + directoryPath);
	}

	public BigInteger getPublicKey() {
		return publicKey;
	}

	public BigInteger getModulus() {
		return modulus;
	}
}
