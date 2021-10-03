package dtsvetkov.bloom;

import junit.framework.TestCase;
import java.io.InputStream;
import java.util.*;

public class BloomFilterTest extends TestCase {

    protected final static String WORDS_FILE = "words";
    protected int totalLines = 0;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.totalLines = getLinesCount(WORDS_FILE);
    }

    public void testBasics() {
        // normal constructors
        BloomFilter.create(1, 0.03);
        BloomFilter.create(1, 1);
        BloomFilter.create(1, 0.03, true);
        BloomFilter.create(1, 1, true);

        // getters
        BloomFilter bf = BloomFilter.create(1, 2, true);
        assertEquals(bf.getNumBits(), 1);
        assertEquals(bf.getNumHashFunctions(), 2);
        assertEquals(bf.isFastHash(), true);

        // invalid arguments
        try {
            BloomFilter.create(-1, 1);
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            BloomFilter.create(1, -1);
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            BloomFilter.create(-1, 0.3);
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            BloomFilter.create(1, 2.0);
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            BloomFilter.create(1, -0.1);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }


    public void testFalsePositiveRate() {
        // using 0.5 part of "words" file to be inserted into Set and BF
        int part = (int) (0.5 * totalLines);

        Set<String> set = new HashSet<>(part);
        // perform test for these desired false positive probabilities
        List<Double> listFPP = Arrays.asList(0.01, 0.03, 0.05, 0.10);

        for (double fpp : listFPP) {
            BloomFilter bloomFilter = BloomFilter.create(part, fpp);
            Scanner scanner = new Scanner(getResourceStream(WORDS_FILE));
            // put part of text file into set and BF
            int cnt = 0;
            while (scanner.hasNext() && cnt < part) {
                String s = scanner.next();
                set.add(s);
                bloomFilter.put(s);
                cnt++;
            }
            // now check the rest part of file for false positives
            int falsePositiveCnt = 0;
            int restCnt = 0;
            while (scanner.hasNext()) {
                restCnt++;
                String s = scanner.next();
                if (bloomFilter.mightContain(s) && !set.contains(s)) {
                    falsePositiveCnt++;
                }
            }
            double actualFPRate = (double) falsePositiveCnt / restCnt;
            System.out.println(String.format("desired fpp=%.3f; actual FP rate=%.3f (%d out of %d)",
                    fpp, actualFPRate, falsePositiveCnt, restCnt));

            // check if deviation is less than 1%
            assertTrue(Math.abs(fpp - actualFPRate) < 0.01);
        }
    }

    public void testPositives() {
        BloomFilter bloomFilter = BloomFilter.create(totalLines, 0.01);
        // first put all of the words into BF
        Scanner scanner = new Scanner(getResourceStream(WORDS_FILE));
        while (scanner.hasNext()) {
            String s = scanner.next();
            bloomFilter.put(s);
        }
        scanner.close();

        // now check if BF reports true for each of them
        scanner = new Scanner(getResourceStream(WORDS_FILE));
        int positiveEntries = 0;
        while (scanner.hasNext()) {
            String s = scanner.next();
            if (bloomFilter.mightContain(s)) {
                positiveEntries++;
            }
        }
        scanner.close();

        assertEquals(positiveEntries, totalLines);
    }

    public void testFalseNegatives() {
        // using 0.5 part of "words" file to be inserted into Set and BF
        int part = (int) (0.5 * totalLines);

        Set<String> set = new HashSet<>(part);
        BloomFilter bloomFilter = BloomFilter.create(part, 0.01, true);
        Scanner scanner = new Scanner(getResourceStream(WORDS_FILE));
        // put part of text file into set and BF
        int cnt = 0;
        while (scanner.hasNext() && cnt < part) {
            String s = scanner.next();
            set.add(s);
            bloomFilter.put(s);
            cnt++;
        }
        // now check the rest part of file for false negatives
        int falseNegativeCnt = 0;
        while (scanner.hasNext()) {
            String s = scanner.next();
            // increment falseNegativeCnt when BF says that string never been
            // inserted, but it actually has been
            if (!bloomFilter.mightContain(s) && set.contains(s)) {
                falseNegativeCnt++;
            }
        }
        // falseNegativeCnt should be zero
        assertEquals(falseNegativeCnt, 0);
    }

    private int getLinesCount(String fileName) {
        Scanner scanner = new Scanner(this.getClass().getClassLoader().getResourceAsStream(fileName));
        int result = 0;
        while (scanner.hasNext()) {
            result++;
            scanner.next();
        }
        return result;
    }

    private InputStream getResourceStream(String fileName) {
        return this.getClass().getClassLoader().getResourceAsStream(fileName);
    }

}
