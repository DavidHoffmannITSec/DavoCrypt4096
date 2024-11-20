package org.example;

import java.math.BigInteger;

public class KeyGenerator {
    private BigInteger publicKey;
    private BigInteger privateKey;
    private BigInteger modulus;

    public KeyGenerator() {
        generateKeys();
    }

    private void generateKeys() {
        BigInteger p = generateEnhancedPrime();
        BigInteger q = generateEnhancedPrime();

        modulus = p.multiply(q);

        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));

        publicKey = generateFlexiblePublicExponent(phi);

        privateKey = publicKey.modInverse(phi);
    }

    private BigInteger generateEnhancedPrime() {
        int bitLength = 2048;
        BigInteger candidate = generateRandomBigInteger(bitLength);

        while (!isStrongPrime(candidate)) {
            candidate = candidate.add(BigInteger.TWO);
            candidate = advancedBitMix(candidate);
            candidate = dynamicFeedback(candidate);
            candidate = iterativeTransform(candidate, bitLength);
        }

        return candidate;
    }

    private boolean isStrongPrime(BigInteger number) {
        if (!isPrime(number)) return false;

        BigInteger half = number.subtract(BigInteger.ONE).divide(BigInteger.TWO);
        if (!isPrime(half)) return false;

        BigInteger cubeRoot = BigInteger.valueOf((long) Math.cbrt(number.doubleValue()));
        return isPrime(cubeRoot);
    }

    private BigInteger generateRandomBigInteger(int bitLength) {
        BigInteger result = BigInteger.ONE;
        BigInteger seed = BigInteger.valueOf(getHardwareEntropy());

        for (int i = 0; i < bitLength; i++) {
            seed = seed.xor(seed.shiftLeft(21))
                    .add(BigInteger.valueOf(getDynamicHardwareState())) // Hardware-Status
                    .xor(BigInteger.valueOf(getPseudoPhysicalNoise())) // Simuliertes physikalisches Rauschen
                    .xor(BigInteger.valueOf(getHashBasedEntropy())) // Kombinierte Systementropie
                    .xor(BigInteger.valueOf(getRandomizedThreadState())); // Dynamische Thread-Entropie
            result = result.shiftLeft(1).or(seed.and(BigInteger.ONE));
        }

        return result.setBit(bitLength - 1).setBit(0); // Setze MSB und LSB
    }

    private long getHardwareEntropy() {
        // CPU-Cache-Größe und Prozessinformationen als Basis
        long entropy = Runtime.getRuntime().freeMemory() ^ Runtime.getRuntime().maxMemory();
        entropy ^= Runtime.getRuntime().availableProcessors();
        entropy ^= Runtime.getRuntime().totalMemory();
        return entropy;
    }

    private long getDynamicHardwareState() {
        // Simulierte dynamische Hardware-Schwankungen
        long state = (Runtime.getRuntime().freeMemory() ^ (Runtime.getRuntime().maxMemory() << 3));
        state ^= (long) (Math.random() * Runtime.getRuntime().availableProcessors());
        state ^= Thread.activeCount();
        return state;
    }

    private long getPseudoPhysicalNoise() {
        // Simuliertes physikalisches Rauschen
        double chaos = Math.sin(Math.random() * Math.PI) + Math.cos(Math.random() * Math.PI / 2);
        return Double.doubleToLongBits(chaos);
    }

    private long getHashBasedEntropy() {
        // Kombinierte Systeminformationen als Hash
        long memoryState = Runtime.getRuntime().freeMemory() ^ Runtime.getRuntime().maxMemory();
        long hash = (memoryState ^ Thread.currentThread().threadId()) * 0x9E3779B97F4A7C15L;
        return hash ^ (hash >>> 32);
    }

    private long getRandomizedThreadState() {
        // Dynamische Thread-basierte Entropie
        long threadState = Thread.currentThread().threadId();
        threadState ^= Thread.activeCount() * 31L;
        threadState ^= threadState << 13;
        threadState ^= threadState >>> 7;
        threadState ^= threadState << 17;
        return threadState;
    }

    private BigInteger advancedBitMix(BigInteger value) {
        int keySize = 4096;
        value = value.xor(value.shiftLeft(17)).xor(value.shiftRight(11));
        value = value.multiply(BigInteger.valueOf(0xDEADBEEFL)).mod(BigInteger.TWO.pow(keySize / 2));
        value = rotateBits(value).xor(BigInteger.valueOf(System.nanoTime() & 0xFFFFFFFL));
        value = value.add(mathematicalMask());
        return value;
    }

    private BigInteger rotateBits(BigInteger value) {
        int bitLength = value.bitLength();
        int dynamicShift = (int) ((System.nanoTime() % bitLength) + 1); // Dynamischer Shift zwischen 1 und bitLength
        BigInteger mask = BigInteger.TWO.pow(bitLength).subtract(BigInteger.ONE);

        BigInteger rotated = (value.shiftLeft(dynamicShift).or(value.shiftRight(bitLength - dynamicShift))).and(mask);
        return rotated.xor(rotated.shiftLeft(7)).xor(rotated.shiftRight(5));
    }

    private BigInteger dynamicFeedback(BigInteger value) {
        long entropy = System.nanoTime() ^ Thread.currentThread().threadId();
        BigInteger hash = value.xor(BigInteger.valueOf(System.currentTimeMillis()))
                .xor(BigInteger.valueOf(entropy))
                .multiply(BigInteger.valueOf(0x9E3779B9L));

        return value.add(hash.shiftRight(3)).xor(hash.shiftLeft(2));
    }

    private BigInteger iterativeTransform(BigInteger value, int bitLength) {
        int maxIterations = (int) (System.nanoTime() % 10) + 3; // Dynamischer Wert zwischen 3 und 12
        int iterations = (int) (System.nanoTime() % maxIterations) + 1; // Zufällige Iterationen

        for (int i = 0; i < iterations; i++) {
            value = nonLinearTransform(value, bitLength);
            value = dynamicFeedback(value);
        }
        return value;
    }


    private BigInteger nonLinearTransform(BigInteger value, int bitLength) {
        BigInteger constantPi = generateDynamicConstant("3141592653589793238");
        BigInteger constantE = generateDynamicConstant("2718281828459045235");
        BigInteger constantPhi = generateDynamicConstant("1618033988749894848");

        // Quadratische Transformation, abhängig von constantPi und bitLength
        value = value.multiply(value).mod(constantPi);

        // Dynamische Transformation mit constantPhi
        value = value.add(constantPhi.shiftLeft(bitLength % 31)).xor(constantE);

        // Exponentielle Transformation
        long bitLengthMultiplied = (long) value.bitLength() * 2;
        value = value.xor(BigInteger.valueOf(bitLengthMultiplied)).multiply(constantPhi);

        // Modulo Transformation mit constantE
        value = value.mod(constantE);

        // Kombinierte Modulation mit allen Konstanten
        value = value.multiply(constantPi).xor(constantPhi).mod(BigInteger.TWO.pow(bitLength / 2));

        return value;
    }


    private BigInteger generateDynamicConstant(String baseValue) {
        BigInteger base = new BigInteger(baseValue);
        BigInteger timeFactor = BigInteger.valueOf(System.nanoTime()).xor(BigInteger.valueOf(System.currentTimeMillis()));
        long threadEntropy = Thread.currentThread().threadId() ^ System.nanoTime();
        return base.multiply(timeFactor)
                .xor(BigInteger.valueOf(threadEntropy))
                .shiftLeft(7).mod(BigInteger.TWO.pow(256));
    }

    private BigInteger mathematicalMask() {
        BigInteger goldenRatio = generateDynamicConstant("1618033988749894848"); // Phi
        BigInteger mersennePrime = BigInteger.TWO.pow(31).subtract(BigInteger.ONE); // Mersenne-Primzahl
        long threadEntropy = Thread.currentThread().threadId() ^ System.nanoTime();
        return goldenRatio.multiply(mersennePrime)
                .xor(BigInteger.valueOf(System.nanoTime()))
                .xor(BigInteger.valueOf(threadEntropy));
    }

    private BigInteger generateFlexiblePublicExponent(BigInteger phi) {
        BigInteger e = BigInteger.valueOf(3);

        while (!phi.gcd(e).equals(BigInteger.ONE)) {
            e = e.add(BigInteger.TWO).xor(BigInteger.valueOf(System.nanoTime() & 0xFFFFL));
        }

        return e;
    }

    private boolean isPrime(BigInteger number) {
        if (number.compareTo(BigInteger.TWO) < 0) return false;
        if (number.mod(BigInteger.TWO).equals(BigInteger.ZERO)) return false;

        BigInteger d = number.subtract(BigInteger.ONE);
        int r = 0;

        while (d.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
            d = d.divide(BigInteger.TWO);
            r++;
        }

        for (int i = 0; i < 200; i++) { // Höhere Iterationen für Sicherheit
            BigInteger a = generateRandomBigInteger(number.bitLength() - 1).mod(number.subtract(BigInteger.TWO)).add(BigInteger.TWO);
            BigInteger x = a.modPow(d, number);

            if (x.equals(BigInteger.ONE) || x.equals(number.subtract(BigInteger.ONE))) {
                continue;
            }

            boolean isComposite = true;
            for (int j = 0; j < r - 1; j++) {
                x = x.modPow(BigInteger.TWO, number);
                if (x.equals(BigInteger.ONE)) return false;
                if (x.equals(number.subtract(BigInteger.ONE))) {
                    isComposite = false;
                    break;
                }
            }

            if (isComposite) return false;
        }

        return true;
    }

    public BigInteger getPublicKey() {
        return publicKey;
    }

    public BigInteger getPrivateKey() {
        return privateKey;
    }

    public BigInteger getModulus() {
        return modulus;
    }
}
