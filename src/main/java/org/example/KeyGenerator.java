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
        BigInteger seed = BigInteger.valueOf(System.nanoTime());

        for (int i = 0; i < bitLength; i++) {
            seed = seed.xor(seed.shiftLeft(21))
                    .add(BigInteger.valueOf(System.currentTimeMillis()))
                    .xor(BigInteger.valueOf(Thread.currentThread().threadId()))
                    .xor(BigInteger.valueOf(getUserInputEntropy())); // Benutzerinteraktion
            result = result.shiftLeft(1).or(seed.and(BigInteger.ONE));
        }

        return result.setBit(bitLength - 1).setBit(0); // Setze MSB und LSB
    }

    private long getUserInputEntropy() {
        // Simulierte Benutzerinteraktion als zusätzliche Entropiequelle
        long entropy = System.nanoTime();
        entropy ^= entropy << 13;
        entropy ^= entropy >>> 7;
        entropy ^= entropy << 17;
        return entropy;
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

        // Optimierte Transformationen
        value = value.multiply(value).xor(BigInteger.valueOf((long) value.bitLength() * 3));
        value = value.add(BigInteger.valueOf(value.bitCount() % 10));
        value = value.xor(constantPhi).mod(constantE);
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
