package io.github.karols.hocr4j;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

public class BoundsGenerator extends Generator<Bounds> {

    public BoundsGenerator() {
        super(Bounds.class);
    }

    public Bounds generate(SourceOfRandomness random, GenerationStatus status,
                           int left, int top, int right, int bottom) {
        int sz = status.size();
        int l = left + (left == right ? 0 : random.nextInt(right - left));
        int t = top + (top == bottom ? 0 : random.nextInt(bottom - top));
        int r = l + (left == right ? 0 : random.nextInt(right - l));
        int b = t + (top == bottom ? 0 : random.nextInt(bottom - t));
        return new Bounds(l, t, r, b);
    }

    @Override
    public Bounds generate(SourceOfRandomness random, GenerationStatus status) {
        return generate(random, status, 0, 0, 3000, 3000);
    }
}
