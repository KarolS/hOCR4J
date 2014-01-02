package io.github.karols.hocr4j;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import java.util.ArrayList;

public class ParagraphGenerator extends Generator<Paragraph> {


    LineGenerator lineGenerator = new LineGenerator();
    BoundsGenerator boundsGenerator = new BoundsGenerator();

    public ParagraphGenerator() {
        super(Paragraph.class);
    }


    @Override
    public Paragraph generate(SourceOfRandomness random, GenerationStatus status) {
        return generate(random, status, 0, 0, 3000, 3000);
    }

    public Paragraph generate(SourceOfRandomness random, GenerationStatus status,
                              int left, int top, int right, int bottom) {

        int elemCount = (int) Math.sqrt(status.size()) + 1;

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
            absoluteOffsets[i] = (int) (relativeOffsets[i] * (bottom - top) + top);
        }
        ArrayList<Line> elems = new ArrayList<Line>(elemCount);
        for (int i = 0; i < elemCount; i++) {
            int newRight = (int) (random.nextDouble(0.5, 1.0) * (right - left) + left);
            Line l = lineGenerator.generate(random, status,
                    left, absoluteOffsets[i],
                    newRight, absoluteOffsets[i + 1]);
            elems.add(l);
        }

        return new Paragraph(elems);
    }
}
