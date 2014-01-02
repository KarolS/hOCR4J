package io.github.karols.hocr4j;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.generator.java.lang.StringGenerator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import java.util.ArrayList;

public class LineGenerator extends Generator<Line> {

    StringGenerator stringGenerator = new StringGenerator();
    BoundsGenerator boundsGenerator = new BoundsGenerator();

    public LineGenerator() {
        super(Line.class);
    }


    @Override
    public Line generate(SourceOfRandomness random, GenerationStatus status) {
        return generate(random, status, 0, 0, 3000, 100);
    }

    public Line generate(SourceOfRandomness random, GenerationStatus status,
                         int left, int top, int right, int bottom) {
        int elemCount = status.size();

        double[] relativeOffsets = new double[elemCount + 1];
        double currentOffset = random.nextDouble(0.1, 1);
        for (int i = 0; i < relativeOffsets.length; i++) {
            relativeOffsets[i] = currentOffset;
            currentOffset += random.nextDouble(0.1, 1);
        }
        for (int i = 0; i < relativeOffsets.length; i++) {
            relativeOffsets[i] /= currentOffset;
        }

        int[] absoluteOffsets = new int[relativeOffsets.length];
        for (int i = 0; i < relativeOffsets.length; i++) {
            absoluteOffsets[i] = (int) (relativeOffsets[i] * (right - left) + left);
        }

        ArrayList<Word> elems = new ArrayList<Word>(elemCount);
        for (int i = 0; i < elemCount; i++) {
            Bounds b = boundsGenerator.generate(random, status,
                    absoluteOffsets[i], top,
                    absoluteOffsets[i + 1], bottom);
            String w = stringGenerator.generate(random, status);
            elems.add(new Word(w, b));
        }

        return new Line(elems);
    }
}
