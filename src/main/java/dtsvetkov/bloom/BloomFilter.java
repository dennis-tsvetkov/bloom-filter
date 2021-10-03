package dtsvetkov.bloom;

import java.util.BitSet;

public class BloomFilter {

    private BitSet bits;
    private int numBits;
    private int numHashFunctions;
    private boolean fastHash = false;

    /**
     * Constructor is private and not supposed to be called,
     * one of {@code create()} methods should be used instead
     */
    private BloomFilter() {
    }

    /**
     * Creates an instance of BloomFilter using calculated lenght of bitset
     * and number of hash functions, based on the following parameters:
     *
     * @param expectedInsertions       expected amount of items to be inserted into
     * @param falsePositiveProbability desired false positive probability in range from 0 to 1
     * @return A BloomFilter
     */
    public static BloomFilter create(int expectedInsertions, double falsePositiveProbability) {
        return create(expectedInsertions, falsePositiveProbability, false);
    }

    /**
     * Creates an instance of BloomFilter using calculated length of bitset
     * and number of hash functions, based on the following parameters:
     *
     * @param expectedInsertions       expected amount of items to be inserted into
     * @param falsePositiveProbability desired false positive probability in range from 0 to 1
     * @param fastHash                 if set to <b>true</b>, for each insert first hash will
     *                                 be calculated using Murmur3 algorithm, and subsequent ones using fast transform of this hash;
     *                                 when this parameter is set to <b>false</b> (default), Murmur3 will be used for every hash,
     *                                 which gives lower false positive probability in result.
     * @return A BloomFilter
     */
    public static BloomFilter create(int expectedInsertions, double falsePositiveProbability, boolean fastHash) {
        checkArgument(expectedInsertions > 0, "Number of expected insertions must be positive.");
        checkArgument(falsePositiveProbability > 0.0 && falsePositiveProbability < 1.0,
                "False positive probability must be greater than 0.0 and less than 1.0");

        int numBits = calcNumBits(expectedInsertions, falsePositiveProbability);
        int numHashFunctions = calcNumOfHashFunctions(expectedInsertions, numBits);
        return create(numBits, numHashFunctions, fastHash);
    }

    /**
     * Creates an instance of BloomFilter with the following parameters:
     *
     * @param nbits            length of bitset
     * @param numHashFunctions number of hash functions
     * @return A BloomFilter
     */
    public static BloomFilter create(int nbits, int numHashFunctions) {
        return create(nbits, numHashFunctions, false);
    }

    /**
     * Creates an instance of BloomFilter with the following parameters:
     *
     * @param nbits            length of bitset
     * @param numHashFunctions number of hash functions
     * @param fastHash         if set to <b>true</b>, for each insert first hash will
     *                         be calculated using Murmur3 algorithm, and subsequent ones using fast transform of this hash;
     *                         when this parameter is set to <b>false</b> (default), Murmur3 will be used for every hash,
     *                         which gives lower false positive probability in result.
     * @return A BloomFilter
     */
    public static BloomFilter create(int nbits, int numHashFunctions, boolean fastHash) {
        checkArgument(nbits > 0, "Length of bitset must be positive.");
        checkArgument(numHashFunctions > 0, "Number of hash functions be positive.");
        BloomFilter instance = new BloomFilter();
        instance.fastHash = fastHash;
        instance.numBits = nbits;
        instance.bits = new BitSet(nbits);
        instance.numHashFunctions = numHashFunctions;
        return instance;
    }

    /**
     * Automatically calculates optimal number of hash functions based on:
     *
     * @param expectedInsertions expected number of insertions
     * @param numBits            length of bitset
     * @return optimal number of hash functions
     */
    private static int calcNumOfHashFunctions(int expectedInsertions, int numBits) {
        return Math.max(1, (int) Math.round((double) numBits / expectedInsertions * Math.log(2)));
    }

    /**
     * Automatically calculates length of bitset based on:
     *
     * @param expectedInsertions       expected number of insertions
     * @param falsePositiveProbability desired false positive probability in range from 0 to 1
     * @return optimal length of bitset
     */
    private static int calcNumBits(int expectedInsertions, double falsePositiveProbability) {
        return (int) (-expectedInsertions * Math.log(falsePositiveProbability) / (Math.log(2) * Math.log(2)));
    }

    private static void checkArgument(boolean condition, String errorMessage) throws IllegalArgumentException {
        if (!condition) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private int getHashFast(String s, int hashIndex, int previousHash) {
        if (hashIndex == 0) {
            return Murmur3_32.hashString(s);
        } else {
            return Murmur3_32.finalMix(previousHash, s.length());
        }
    }

    private int getHashAdvanced(String s, int hashIndex) {
        return Murmur3_32.hashString(s + hashIndex);
    }

    private int getHash(String s, int hashIndex, int previousHash) {
        if (this.fastHash) {
            return getHashFast(s, hashIndex, previousHash);
        } else {
            return getHashAdvanced(s, hashIndex);
        }
    }

    /**
     * Puts the string <b>s</b> into filter
     *
     * @param s the string
     * @return <b>true</b> if any of bits in bitset has been changed, and <b>false</b> otherwise
     */
    public boolean put(String s) {
        boolean bitsChanged = false;
        int hash = 0;
        for (int i = 0; i < numHashFunctions; i++) {
            // get i-th hash
            hash = getHash(s, i, hash);
            // make it positive
            hash &= Integer.MAX_VALUE;
            // check if bit with that index is gonna change
            int bitIndex = hash % numBits;
            if (!bits.get(bitIndex)) {
                // set the bit
                bits.set(bitIndex);
                bitsChanged = true;
            }
        }
        return bitsChanged;
    }

    /**
     * Checks if there is a chance that the string <b>s</b> was added into BloomFilter already.
     *
     * @param s the string to check
     * @return <b>true</b> if BloomFilter might contain such string,
     * and <b>false</b> if such string definitely never been inserted.
     */
    public boolean mightContain(String s) {
        int hash = 0;
        for (int i = 0; i < numHashFunctions; i++) {
            // get i-th hash
            hash = getHash(s, i, hash);
            // make it positive
            hash = hash & Integer.MAX_VALUE;
            if (!bits.get(hash % numBits)) {
                return false;
            }
        }
        return true;
    }

    public int getNumBits() {
        return numBits;
    }

    public int getNumHashFunctions() {
        return numHashFunctions;
    }

    public boolean isFastHash() {
        return fastHash;
    }
}
