package org.example;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

public class KeyGenerator {
    private BigInteger publicKey;
    private BigInteger privateKey;
    private BigInteger modulus;

    // Caching häufiger Konstanten
    private static final BigInteger CONSTANT_PI = new BigInteger("3141592653589793238");
    private static final BigInteger CONSTANT_E = new BigInteger("2718281828459045235");
    private static final BigInteger CONSTANT_PHI = new BigInteger("1618033988749894848");

    public KeyGenerator() {
        generateKeys();
    }

    private void generateKeys() {
        BigInteger[] primes = generateParallelPrimes();

        BigInteger p = primes[0];
        BigInteger q = primes[1];

        modulus = p.multiply(q);

        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

        publicKey = generateFlexiblePublicExponent(phi);

        privateKey = publicKey.modInverse(phi);
    }

    private BigInteger[] generateParallelPrimes() {
        CompletableFuture<BigInteger> futureP = CompletableFuture.supplyAsync(this::generateEnhancedPrime);
        CompletableFuture<BigInteger> futureQ = CompletableFuture.supplyAsync(this::generateEnhancedPrime);

        return new BigInteger[]{futureP.join(), futureQ.join()};
    }

    private BigInteger generateEnhancedPrime() {
        int bitLength = 2048;
        BigInteger candidate = generateRandomBigInteger(bitLength);
        BigInteger cachedMod = BigInteger.TWO.pow(bitLength / 2);

        while (!isStrongPrime(candidate)) {
            candidate = candidate.add(BigInteger.TWO);
            candidate = advancedBitMix(candidate, cachedMod);
            candidate = dynamicFeedback(candidate); // Sicherheitsmechanismus aus Algorithmus 2
            candidate = iterativeTransform(candidate, bitLength); // Stärkere Bitmuster-Streuung
        }

        return candidate;
    }

    private BigInteger generateRandomBigInteger(int bitLength) {
        BigInteger random = BigInteger.ONE;
        long seed = getEntropySeed();

        for (int i = 0; i < bitLength; i++) {
            seed = (seed ^ (seed << 13)) ^ (seed >> 7) ^ (seed << 17);
            random = random.shiftLeft(1).or(BigInteger.valueOf(seed & 1));
        }

        return random.setBit(bitLength - 1).setBit(0); // Setze MSB und LSB
    }

    private long getEntropySeed() {
        long entropy = System.nanoTime();
        entropy ^= Thread.currentThread().threadId();
        entropy ^= Runtime.getRuntime().freeMemory();
        return entropy;
    }

    private boolean isStrongPrime(BigInteger number) {
        if (!isPrime(number)) return false;
        BigInteger half = number.subtract(BigInteger.ONE).divide(BigInteger.TWO);
        return isPrime(half);
    }

    private boolean isPrime(BigInteger number) {
        if (number.compareTo(BigInteger.TWO) < 0) return false;

        BigInteger d = number.subtract(BigInteger.ONE);
        int r = 0;

        while (d.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
            d = d.divide(BigInteger.TWO);
            r++;
        }

        return performMillerRabinTest(number, d, r);
    }

    private boolean performMillerRabinTest(BigInteger number, BigInteger d, int r) {
        final int rounds = 50;
        for (int i = 0; i < rounds; i++) {
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

    private BigInteger advancedBitMix(BigInteger value, BigInteger cachedMod) {
        value = value.xor(value.shiftLeft(17)).xor(value.shiftRight(11));
        value = value.multiply(CONSTANT_PI).mod(cachedMod);
        return value;
    }

    private BigInteger dynamicFeedback(BigInteger value) {
        long entropy = System.nanoTime() ^ Thread.currentThread().threadId();
        BigInteger hash = value.xor(BigInteger.valueOf(System.currentTimeMillis()))
                .xor(BigInteger.valueOf(entropy))
                .multiply(BigInteger.valueOf(0x9E3779B9L));
        return value.add(hash.shiftRight(3)).xor(hash.shiftLeft(2));
    }

    private BigInteger iterativeTransform(BigInteger value, int bitLength) {
        int maxIterations = Math.max(3, bitLength / (Runtime.getRuntime().availableProcessors() * 128));
        int iterations = Math.min(maxIterations, 10);

        BigInteger cachedMod = BigInteger.TWO.pow(bitLength / 2);

        for (int i = 0; i < iterations; i++) {
            value = nonLinearTransform(value, bitLength, cachedMod);
            value = dynamicFeedback(value); // Dynamisches Feedback für zusätzliche Sicherheit
        }
        return value;
    }

    private BigInteger nonLinearTransform(BigInteger value, int bitLength, BigInteger cachedMod) {
        value = value.multiply(value).mod(CONSTANT_PI);
        value = value.add(CONSTANT_PHI.shiftLeft(bitLength % 31)).xor(CONSTANT_E);
        long bitLengthMultiplied = (long) value.bitLength() * 2;
        value = value.xor(BigInteger.valueOf(bitLengthMultiplied)).multiply(CONSTANT_PHI);
        value = value.mod(CONSTANT_E);
        value = value.multiply(CONSTANT_PI).xor(CONSTANT_PHI).mod(cachedMod);

        return value;
    }

    private BigInteger generateFlexiblePublicExponent(BigInteger phi) {
        BigInteger e = BigInteger.valueOf(65537);
        if (!phi.gcd(e).equals(BigInteger.ONE)) {
            e = BigInteger.valueOf(3);
            while (!phi.gcd(e).equals(BigInteger.ONE)) {
                e = e.add(BigInteger.TWO);
            }
        }
        return e;
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
