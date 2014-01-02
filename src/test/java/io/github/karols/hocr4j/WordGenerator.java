package io.github.karols.hocr4j;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.generator.java.lang.StringGenerator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

public class WordGenerator extends Generator<Word> {

    BoundsGenerator boundsGenerator = new BoundsGenerator();
    StringGenerator stringGenerator = new StringGenerator();

    public WordGenerator() {
        super(Word.class);
    }

    public Word generate(SourceOfRandomness random, GenerationStatus status,
                         int left, int top, int right, int bottom) {
        return new Word(
                stringGenerator.generate(random, status),
                boundsGenerator.generate(random, status, left, top, right, bottom));
    }

    @Override
    public Word generate(SourceOfRandomness random, GenerationStatus status) {
        return new Word(
                stringGenerator.generate(random, status),
                boundsGenerator.generate(random, status));
    }
}
