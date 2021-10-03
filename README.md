# The project
This maven-based project contains an implementation of [bloom filter](https://en.wikipedia.org/wiki/Bloom_filter). No dependency is used except JUnit.
Internally 32-bit Murmur3 algorithm is used for hashing, which is also implemented here.

### Constructors
There are two primary ways to instantiate BloomFilter:
- with directly specified length of bitset and number of hash functions:
` create(int nbits, int numHashFunctions) `
- and using number of expected insertions and desired false positive probabilities:
` create(int expectedInsertions, double falsePositiveProbability)` 
in this case length of bitset and number of hash functions will be calculated automatically.

### fastHash
Some of constructors also have a parameter called `fastHash`. It is used internally in methods `put()` and `mightContain()` and defines the way how the sequence of hashes for particular string is being calculated.
With this parameters we can handle with potential hash collisions.

When `fastHash` is `true` the sequence of hashes is calculated as a chain from first hash, (as it is implemented, for instance, in Google Guava library) like this:
Let's assume the input string is `apple`, so
- 1st hash is `Murmur3('apple')`
- 2nd hash is `arithmetic_transform_of(1st_hash)`
- 3rd hash is `arithmetic_transform_of(2nd_hash)`
- and so on
Obviously, when we got collision in 1st hash, all of the rest will get collisions too.

When `fastHash` is `false` (which is default) the sequence of hashes is being generated from the __string itself__ and a __salt__ and contains independent values, like this:
- input string is `apple`
- 1st hash is gonna be `Murmur3('apple0')`
- 2nd hash is `Murmur3('apple1')`
- 3rd hash is `Murmur3('apple2')`
- and so on
In this case if we got collision in the first hash, there is very small chance to get it on the second hash and almost impossible in the third one and so on. But bad news is that calculating Murmur3 many times is a bit __slower__ than simple arithmetic transformation.
