package org.duke.json;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Registry mapping Java classes to appropriate JSON encoder functions.
 */
public class Registry {
    private static final HashMap<Class<?>, BiConsumer<JsonWriter.ValueContext, ?>> encoderMap
            = new HashMap<>();
    private static final HashMap<Class<?>, BiConsumer<JsonWriter.ValueContext, ?>> encoderCache
            = new HashMap<>();

    static {
        register(List.class, (ctx, list) ->
                ctx.writeValues(
                        ((List<?>) list).iterator(),
                        JsonWriter.ValueContext::writeValue));
        register(Iterable.class, (ctx, itb) ->
                ctx.writeValues(
                        ((Iterable<?>) itb).iterator(),
                        JsonWriter.ValueContext::writeValue));
        register(Map.class, (ctx, map) ->
                ctx.writeDict(
                        (Map<?, ?>) map,
                        JsonWriter.ValueContext::writeValue));
        register(Number.class, (ctx, val) -> ctx.writeNumber(val.doubleValue()));
        register(String.class, JsonWriter.ValueContext::writeString);
        register(Boolean.class, JsonWriter.ValueContext::writeBoolean);
        register(Object.class, (ctx, obj) -> ctx.writeString(obj.toString()));
    }

    public static <T> void register(
            Class<T> clazz,
            BiConsumer<JsonWriter.ValueContext, T> encoder) {
        encoderMap.put(clazz, encoder);
        encoderCache.clear();
    }

    private static <T> BiConsumer<JsonWriter.ValueContext, T> getEncoderInner(Class<? extends T> clazz) {
        Class<?> current = clazz;
        while (!Object.class.equals(current)) {
            BiConsumer<JsonWriter.ValueContext, ?> encoder = encoderMap.get(current);
            if (encoder != null) {
                //noinspection unchecked
                return (BiConsumer<JsonWriter.ValueContext, T>) encoder;
            }
            current = current.getSuperclass();
        }
        current = clazz;
        while (!Object.class.equals(current)) {
            for (Type iface : current.getInterfaces()) {
                if (iface instanceof Class) {
                    Class<?> c = (Class<?>) iface;
                    BiConsumer<JsonWriter.ValueContext, ?> encoder = encoderMap.get(c);
                    if (encoder != null) {
                        //noinspection unchecked
                        return (BiConsumer<JsonWriter.ValueContext, T>) encoder;
                    }
                }
            }
            current = current.getSuperclass();
        }

        BiConsumer<JsonWriter.ValueContext, ?> encoder = encoderMap.get(Object.class);
        if (encoder != null) {
            //noinspection unchecked
            return (BiConsumer<JsonWriter.ValueContext, T>) encoder;
        }
        throw new JsonException("No handler for class %s", clazz);
    }

    public static <T> BiConsumer<JsonWriter.ValueContext, T> getEncoder(Class<? extends T> clazz) {
        if (encoderCache.containsKey(clazz)) {
            //noinspection unchecked
            return (BiConsumer<JsonWriter.ValueContext, T>) encoderCache.get(clazz);
        }
        BiConsumer<JsonWriter.ValueContext, T> encoder = getEncoderInner(clazz);
        encoderCache.put(clazz, encoder);
        return encoder;
    }

}
