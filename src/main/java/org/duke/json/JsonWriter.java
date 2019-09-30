package org.duke.json;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * This class handles serialization of Java values into JSON values.
 */
public class JsonWriter implements AutoCloseable {

    private final Writer writer;
    private int indentationLevel = 0;
    private final ValueContext valueContext = new ValueContext();

    /**
     * Constructs a JsonWriter wrapping a output writer.
     *
     * @param writer Output writer
     */
    public JsonWriter(Writer writer) {
        this.writer = new BufferedWriter(writer);
    }

    private static String escapeString(String unescaped) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < unescaped.length(); i++) {
            char c = unescaped.charAt(i);
            switch (c) {
            case '\\':
            case '\"':
            case '/':
                sb.append("\\").append(c);
                break;
            case '\b':
                sb.append("\\b");
                break;
            case '\f':
                sb.append("\\f");
                break;
            case '\n':
                sb.append("\\n");
                break;
            case '\r':
                sb.append("\\r");
                break;
            case '\t':
                sb.append("\\t");
                break;
            default:
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Writes a JSON value, given a function expecting a {@link ValueContext}.
     *
     * @param coder Encoding function.
     */
    public void writeValue(Consumer<ValueContext> coder) {
        coder.accept(this.valueContext);
    }

    /**
     * Writes the given value, using a handler from {@link Registry}.
     *
     * @param value Java value to write
     * @param <T>   Type of value
     */
    public <T> void writeValue(T value) {
        this.valueContext.writeValue(value);
    }

    private JsonWriter beginLine() {
        try {
            this.writer.write('\n');
            for (int i = 0; i < this.indentationLevel; i++) {
                this.writer.write('\t');
            }
        } catch (IOException e) {
            throw new JsonException("IO error", e);
        }
        return this;
    }

    private JsonWriter appendQuoted(String unescaped) {
        try {
            String escaped = JsonWriter.escapeString(unescaped);
            this.writer.write('"');
            this.writer.write(escaped);
            this.writer.write('"');
        } catch (IOException e) {
            throw new JsonException("IO error", e);
        }
        return this;
    }

    private JsonWriter append(String literal) {
        try {
            this.writer.write(literal);
        } catch (IOException e) {
            throw new JsonException("IO error", e);
        }
        return this;
    }

    private JsonWriter append(char literal) {
        try {
            this.writer.write(literal);
        } catch (IOException e) {
            throw new JsonException("IO error", e);
        }
        return this;
    }

    /**
     * Flushes the underlying Writer.
     *
     * @throws IOException Throws any exception from flushing the underlying stream.
     */
    public void flush() throws IOException {
        this.writer.flush();
    }

    @Override
    public void close() throws Exception {
        this.writer.close();
    }

    /**
     * This represents the context, of currently reading a JSON value.
     */
    public class ValueContext {
        public void writeString(String s) {
            JsonWriter.this.appendQuoted(s);
        }

        public void writeNumber(double d) {
            JsonWriter.this.append(Double.toString(d));
        }

        public void writeBoolean(boolean b) {
            JsonWriter.this.append(Boolean.toString(b));
        }

        public void writeNull() {
            JsonWriter.this.append("null");
        }

        public void writeArray(Consumer<ArrayContext> coder) {
            try (ArrayContext a = new ArrayContext()) {
                coder.accept(a);
            }
        }

        public void writeObject(Consumer<ObjectContext> coder) {
            try (ObjectContext o = new ObjectContext()) {
                coder.accept(o);
            }
        }

        public <T> void writeValue(T value) {
            if (value == null) {
                this.writeNull();
                return;
            }
            this.writeValue(value, value.getClass());
        }

        public <T> void writeValue(T value, Class<? extends T> clazz) {
            BiConsumer<ValueContext, T> coder = Registry.getEncoder(clazz);
            coder.accept(this, value);
        }

        public <T> void writeValues(Iterator<T> it, BiConsumer<ValueContext, T> coder) {
            this.writeArray(ctx -> {
                while (it.hasNext()) {
                    T value = it.next();
                    ctx.writeElem(vctx -> coder.accept(vctx, value));
                }
            });
        }

        public <K, T> void writeDict(Map<K, T> it, BiConsumer<ValueContext, T> coder) {
            this.writeObject(ctx -> {
                for (Map.Entry<K, T> entry : it.entrySet()) {
                    T value = entry.getValue();
                    ctx.writeField(entry.getKey().toString(),
                            vctx -> coder.accept(vctx, value));
                }
            });
        }
    }

    private abstract class BlockContext implements AutoCloseable {
        private static final String separator = ",";
        private boolean hasPrev = false;

        protected BlockContext() {
            JsonWriter.this.append(this.blockStart());
            JsonWriter.this.indentationLevel++;
            JsonWriter.this.beginLine();
        }

        protected abstract char blockStart();

        protected abstract char blockEnd();

        protected void startObject() {
            if (this.hasPrev) {
                JsonWriter.this.append(separator).beginLine();
            } else {
                this.hasPrev = true;
            }
        }

        @Override
        public void close() {
            JsonWriter.this.indentationLevel--;
            JsonWriter.this.beginLine().append(this.blockEnd());
        }
    }

    /**
     * This represents the context, of currently reading a JSON object.
     */
    public class ObjectContext extends BlockContext {
        protected char blockStart() {
            return '{';
        }

        protected char blockEnd() {
            return '}';
        }

        public void writeField(String name, Consumer<ValueContext> coder) {
            this.startObject();
            JsonWriter.this.appendQuoted(name).append(": ");
            coder.accept(JsonWriter.this.valueContext);
        }

        public <T> void writeField(String name, T value) {
            this.writeField(name, vctx -> vctx.writeValue(value));
        }
    }

    /**
     * This represents the context, of currently reading a JSON array.
     */
    public class ArrayContext extends BlockContext {
        protected char blockStart() {
            return '[';
        }

        protected char blockEnd() {
            return ']';
        }

        public <T> void writeElem(Consumer<ValueContext> coder) {
            this.startObject();
            coder.accept(JsonWriter.this.valueContext);
        }
    }
}
