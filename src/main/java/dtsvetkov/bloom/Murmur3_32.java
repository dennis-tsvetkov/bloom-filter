package dtsvetkov.bloom;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class Murmur3_32 {

    /**
     * There is no need to instantiate that class at all since all methods are static and stateless,
     * so constructor is hidden.
     */
    private Murmur3_32() {
    }

    /**
     * Returns 32-bit Murmur3 hash of string <b>s</b> using default system charset
     *
     * @param s the string
     * @return Murmur3 hash
     */
    public static int hashString(String s) {
        return hashString(s, Charset.defaultCharset());
    }

    /**
     * Returns 32-bit Murmur3 hash of string <b>s</b>
     *
     * @param s       the string
     * @param charset string charset
     * @return Murmur3 hash
     */
    public static int hashString(String s, Charset charset) {
        int hash = 0;
        // get bytes of string into ByteBuffer
        ByteBuffer bb = ByteBuffer
                .wrap(s.getBytes(charset))
                .order(ByteOrder.LITTLE_ENDIAN);
        // get the size of byte array, will be needed at final step
        int length = bb.limit();
        int v = 0;
        // mur bytes by chunks of 4 bytes (32 bit int)
        while (bb.position() + 4 <= bb.limit()) {
            v = murValue(bb.getInt());
            hash = murHash(hash, v);
        }
        // mur remaining bytes, if any
        if (bb.hasRemaining()) {
            int offset = 0;
            v = 0;
            while (bb.hasRemaining()) {
                v ^= (bb.get() & 0xFF) << offset;
                offset += 8;
            }
            hash ^= murValue(v);
        }
        // final checksum mix with length
        return finalMix(hash, length);
    }

    /**
     * See https://en.wikipedia.org/wiki/MurmurHash for details.
     */
    private static int murValue(int value) {
        value *= 0xcc9e2d51;
        value = Integer.rotateLeft(value, 15);
        value *= 0x1b873593;
        return value;
    }

    /**
     * See https://en.wikipedia.org/wiki/MurmurHash for details.
     */
    private static int murHash(int hash, int value) {
        hash ^= value;
        hash = Integer.rotateLeft(hash, 13);
        hash = hash * 5 + 0xe6546b64;
        return hash;
    }

    /**
     * See https://en.wikipedia.org/wiki/MurmurHash for details.
     */
    protected static int finalMix(int hash, int length) {
        hash ^= length;
        hash ^= hash >>> 16;
        hash *= 0x85ebca6b;
        hash ^= hash >>> 13;
        hash *= 0xc2b2ae35;
        hash ^= hash >>> 16;
        return hash;
    }
}
