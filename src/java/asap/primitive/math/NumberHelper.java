package asap.primitive.math;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import asap.primitive.exception.ExceptionTemplate;

public class NumberHelper {

	@SuppressWarnings("serial")
	public static class NumberException extends ExceptionTemplate {

		public NumberException(String messageFormat, Object... messageArgs) {
			super(messageFormat, messageArgs);
		}
	}

	public static int getBitLength(long value) {
		long tmpValueHighestBit = Long.highestOneBit(value);
		return (Long.numberOfTrailingZeros(tmpValueHighestBit) + 1);
	}

	public static short fitRange(short value, short min, short max) {
		return (value < min) ? min : (value > max) ? max : value;
	}

	public static int fitRange(int value, int min, int max) {
		return (value < min) ? min : (value > max) ? max : value;
	}

	public static long fitRange(long value, long min, long max) {
		return (value < min) ? min : (value > max) ? max : value;
	}

	public static <T> T cast(Long value, Class<T> valueClass) throws NumberException {
		T tmpResult = null;
		int tmpClassLength = 0;
		if (!valueClass.isAssignableFrom(Number.class)) {
			throw new NumberException("Classe '%s' não representa um tipo numérico", valueClass.getSimpleName());
		} else if (valueClass.isAssignableFrom(Byte.class)) {
			tmpClassLength = Byte.SIZE;
			tmpResult = valueClass.cast(value.byteValue());
		} else if (valueClass.isAssignableFrom(Short.class)) {
			tmpClassLength = Short.SIZE;
			tmpResult = valueClass.cast(value.shortValue());
		} else if (valueClass.isAssignableFrom(Integer.class)) {
			tmpClassLength = Integer.SIZE;
			tmpResult = valueClass.cast(value.intValue());
		} else if (valueClass.isAssignableFrom(Long.class)) {
			tmpClassLength = Long.SIZE;
			tmpResult = valueClass.cast(value);
		} else if (valueClass.isAssignableFrom(BigInteger.class)) {
			tmpClassLength = Long.SIZE;
			tmpResult = valueClass.cast(BigInteger.valueOf(value));
		} else {
			throw new NumberException("Não há conversão numérica para a classe '%s'", valueClass.getSimpleName());
		}
		long tmpValueLength = (Long.SIZE - Long.numberOfLeadingZeros(value));
		if (tmpValueLength > tmpClassLength) {
			throw new NumberException("Classe '%s' de %d bits não é suficiente para o valor numérico de %d bits",
					valueClass.getSimpleName(), tmpClassLength, tmpValueLength);
		}
		return tmpResult;
	}

	public static <T> long asLong(T value) throws NumberException {
		Long tmpLongValue = null;
		if (value instanceof Byte) {
			tmpLongValue = ((Byte) value).longValue();
		} else if (value instanceof Short) {
			tmpLongValue = ((Short) value).longValue();
		} else if (value instanceof Integer) {
			tmpLongValue = ((Integer) value).longValue();
		} else if (value instanceof Long) {
			tmpLongValue = (Long) value;
		} else if (value instanceof BigInteger) {
			tmpLongValue = ((BigInteger) value).longValue();
		} else {
			throw new NumberException("Classe '%s' não pode ser convertida para valor numérico",
					value.getClass().getSimpleName());
		}
		return tmpLongValue;
	}

	public static int[] copyOf(int[] sourceArray) {
		return (sourceArray == null) ? null : Arrays.copyOf(sourceArray, sourceArray.length);
	}

	public static int[] copyOf(int[] sourceArray, int newLength) {
		return (sourceArray == null) ? null : Arrays.copyOf(sourceArray, newLength);
	}

	public static List<String> toStringList(int[] array) {
		List<String> tmpList = new ArrayList<String>();
		for (int tmpValue : array) {
			tmpList.add(Integer.toString(tmpValue));
		}
		return tmpList;
	}

	public static int[] toIntArray(List<String> list) {
		int[] tmpArray = new int[list.size()];
		for (int tmpIndex = 0; tmpIndex < tmpArray.length; tmpIndex++) {
			Integer tmpValue = Integer.parseInt(list.get(tmpIndex));
			tmpArray[tmpIndex] = (tmpValue == null) ? 0 : tmpValue;
		}
		return tmpArray;
	}

	public static List<Integer> asList(int[] array) {
		List<Integer> tmpList = new ArrayList<Integer>();
		for (int tmpValue : array) {
			tmpList.add(tmpValue);
		}
		return tmpList;
	}

	public static int[] asIntArray(List<Integer> list) {
		int[] tmpArray = new int[list.size()];
		for (int tmpIndex = 0; tmpIndex < tmpArray.length; tmpIndex++) {
			Integer tmpValue = list.get(tmpIndex);
			tmpArray[tmpIndex] = (tmpValue == null) ? 0 : tmpValue;
		}
		return tmpArray;
	}

	public static Long[] asLongArray(Integer[] integerArray) {
		Long[] tmpLongArray = new Long[integerArray.length];
		for (int tmpIndex = 0; tmpIndex < integerArray.length; tmpIndex++) {
			tmpLongArray[tmpIndex] = (long) integerArray[tmpIndex];
		}
		return tmpLongArray;
	}

	public static String formatInteger(long value) {
		return String.format("%,d", value);
	}

	public static final NumberFormat DECIMAL_FORMAL = NumberFormat.getNumberInstance(Locale.getDefault());

	public static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.getDefault());

	private static String CURRENCY_PREFIX = Currency.getInstance("BRL").getSymbol();

	public static String formatCurrency(float value) {
		return String.format("%s %,.2f", CURRENCY_PREFIX, value);
	}

	protected static abstract class AbstractNumberMap<K, V> {

		protected Map<K, V> map;

		public AbstractNumberMap() {
			this.map = new HashMap<K, V>();
		}

		public <K1 extends K, V1 extends V> AbstractNumberMap(AbstractNumberMap<K1, V1> anotherMap) {
			this.map = new HashMap<K, V>(anotherMap.map);
		}

		public Set<K> keys() {
			return this.map.keySet();
		}

		public void set(K key, V value) {
			this.map.put(key, value);
		}

		public <K1 extends K, V1 extends V> void setAll(AbstractNumberMap<K1, V1> anotherMap) {
			this.map.putAll(anotherMap.map);
		}
	}

	public static class IntegerMap<K> extends AbstractNumberMap<K, Long> {

		public IntegerMap() {
			super();
		}

		public <K1 extends K> IntegerMap(IntegerMap<K1> anotherMap) {
			super(anotherMap);
		}

		public long get(K key) {
			Long tmpValue = this.map.get(key);
			return (tmpValue == null) ? 0L : tmpValue;
		}

		public void add(K key, long value) {
			this.map.put(key, (this.get(key) + value));
		}

		public void addAll(IntegerMap<K> anotherMap) {
			for (Map.Entry<K, Long> tmpAnotherEntry : anotherMap.map.entrySet()) {
				K tmpAnotherKey = tmpAnotherEntry.getKey();
				this.map.put(tmpAnotherKey, (this.get(tmpAnotherKey) + tmpAnotherEntry.getValue()));
			}
		}

		public void increment(K key) {
			this.add(key, 1);
		}

		public void decrement(K key) {
			this.add(key, (-1));
		}
	}

	public static class IntegerLimits {

		protected Long lowest;

		protected Long highest;

		public IntegerLimits() {
			this.lowest = null;
			this.highest = null;
		}

		public void update(long value) {
			if ((this.lowest == null) || (value < this.lowest)) {
				this.lowest = value;
			}
			if ((this.highest == null) || (value > this.highest)) {
				this.highest = value;
			}
		}

		public void update(IntegerLimits anotherLimits) {
			this.update(anotherLimits.getLowest());
			this.update(anotherLimits.getHighest());
		}

		public Long getLowest() {
			return (this.lowest != null) ? this.lowest : 0;
		}

		public Long getHighest() {
			return (this.highest != null) ? this.highest : 0;
		}
	}

	public static class FloatMap<K> extends AbstractNumberMap<K, Float> {

		public FloatMap() {
			super();
		}

		public <K1 extends K> FloatMap(FloatMap<K1> anotherMap) {
			super(anotherMap);
		}

		public float get(K key) {
			Float tmpValue = this.map.get(key);
			return (tmpValue == null) ? 0.0f : tmpValue;
		}

		public void add(K key, float value) {
			this.map.put(key, (this.get(key) + value));
		}

		public void addAll(FloatMap<K> anotherMap) {
			for (Map.Entry<K, Float> tmpAnotherEntry : anotherMap.map.entrySet()) {
				K tmpAnotherKey = tmpAnotherEntry.getKey();
				this.map.put(tmpAnotherKey, (this.get(tmpAnotherKey) + tmpAnotherEntry.getValue()));
			}
		}
	}

	public static class FloatLimits {

		protected Float lowest;

		protected Float highest;

		public FloatLimits() {
			this.lowest = null;
			this.highest = null;
		}

		public void update(Float value) {
			if ((this.lowest == null) || (value < this.lowest)) {
				this.lowest = value;
			}
			if ((this.highest == null) || (value > this.highest)) {
				this.highest = value;
			}
		}

		public void update(FloatLimits anotherLimits) {
			this.update(anotherLimits.getLowest());
			this.update(anotherLimits.getHighest());
		}

		public Float getLowest() {
			return (this.lowest != null) ? this.lowest : 0.0f;
		}

		public Float getHighest() {
			return (this.highest != null) ? this.highest : 0.0f;
		}
	}
}
