package org.duke.json;

import java.util.ArrayList;

/**
 * This is a structure capable of receiving consecutive JSON values from a JSON array,
 * and finally producing a T value.
 *
 * @param <T> Value to produce
 */
public interface ArrayHandler<T> {
    /**
     * Construct a {@link ValueHandler} reading a list of values, using the given value handler.
     * @param valueHandler Value handler for each element.
     * @param <T> Element type
     * @return A {@link ValueHandler} reading an {@link ArrayList} of {@code T} values.
     */
    static <T> ValueHandler<ArrayList<T>> listOf(ValueHandler<T> valueHandler) {
        return new ValueHandler<>() {
            public ArrayHandler<ArrayList<T>> handleArray() {
                return new ListValue<>(valueHandler);
            }
        };
    }

    /**
     * Handle one new JSON value in the JSON array.
     *
     * @param receiver {@link ValueHandler} callback
     */
    void handleElement(Receiver receiver);

    /**
     * Handle the end of the array, and return the final value.
     *
     * @return Completed value
     */
    T handleEnd();

    class ListValue<T> implements ArrayHandler<ArrayList<T>> {
        private final ArrayList<T> list = new ArrayList<>();
        private final ValueHandler<T> valueHandler;

        public ListValue(ValueHandler<T> valueHandler) {
            this.valueHandler = valueHandler;
        }

        public void handleElement(Receiver receiver) {
            T elem = receiver.receive(valueHandler);
            list.add(elem);
        }

        public ArrayList<T> handleEnd() {
            return list;
        }
    }
}
