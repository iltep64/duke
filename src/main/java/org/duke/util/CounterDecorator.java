package org.duke.util;

import java.util.function.Function;

/**
 * Helper class to annotate sequential strings with a number prefix.
 */
public class CounterDecorator<T> implements Function<T, CounterDecorator.IntPair<T>> {
    private int counter;

    public CounterDecorator(int start) {
        this.counter = start;
    }

    public IntPair<T> apply(T value) {
        return new CounterDecorator.IntPair<>(this.counter++, value);
    }

    public static class IntPair<T> {
        public final int count;
        public final T value;

        public IntPair(int count, T value) {
            this.count = count;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("%d: %s", this.count, this.value);
        }
    }
}
