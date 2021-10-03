package dtsvetkov.bloom;

import junit.framework.TestCase;
import java.util.Scanner;

public class Murmur3_32Test extends TestCase {

    public void testHashes()  {
        Scanner scannerWords = new Scanner(this.getClass().getClassLoader().getResourceAsStream("words"));
        Scanner scannerHashes = new Scanner(this.getClass().getClassLoader().getResourceAsStream("words-hashes"));
        while (scannerWords.hasNext()) {
            String word = scannerWords.next();
            Integer hash = scannerHashes.nextInt();
            int hash2 = Murmur3_32.hashString(word);
            assertEquals((int) hash, hash2);
        }
    }


}
