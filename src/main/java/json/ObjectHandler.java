package json;
import java.util.*;
import java.util.function.*;

public interface ObjectHandler<T> {
	public void handleField(String name, Receiver receiver);
	public T handleEnd();

	public static <T> ValueHandler<Map<String, T>> mapOf(ValueHandler<T> valueHandler) {
		return new ValueHandler<>() {
			public ObjectHandler<Map<String, T>> handleObject() {
				return new DictValue<>(valueHandler);
			}
		};
	}

	public class DictValue<T> implements ObjectHandler<Map<String, T>> {
		private final HashMap<String, T> map = new HashMap<>();
		private final ValueHandler<T> valueHandler;
		public DictValue(ValueHandler<T> valueHandler) {
			this.valueHandler = valueHandler;
		}
		public void handleField(String name, Receiver receiver) {
			T elem = receiver.receive(this.valueHandler);
			this.map.put(name, elem);
		}
		public Map<String, T> handleEnd() {
			return map;
		}
	}
}