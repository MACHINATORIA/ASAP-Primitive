package asap.primitive.bits.bitStore;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import asap.primitive.bits.BitHelper;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreBooleanFieldMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreEnumerationFieldMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreIntegerArrayFieldMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreIntegerFieldMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreItemType;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreMapException;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreViewFieldMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreViewItemMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreViewMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreViewPieceMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreViewRUFMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreViewRecordMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreEnumerationFieldMap.BitStoreEnumerationFieldItemMap;
import asap.primitive.bits.bitStore.BitStoreSource.BitStoreAccessException;
import asap.primitive.bits.bitStore.BitStoreSource.BitStoreAuthenticationException;
import asap.primitive.bits.bitStore.BitStoreSource.BitStoreDeviceException;
import asap.primitive.bits.bitStore.BitStoreSource.BitStoreMemorySource;
import asap.primitive.bits.bitStore.BitStoreSource.BitStoreRecordSource;
import asap.primitive.bits.bitStore.BitStoreSource.BitStoreSessionException;
import asap.primitive.bits.bitStore.BitStoreSource.BitStoreSetupException;
import asap.primitive.bytes.ByteHelper;
import asap.primitive.dateTime.DateTimeHelper;
import asap.primitive.string.ColumnsStringBuffer;
import asap.primitive.string.IndentedStringBuilder;
import asap.primitive.string.StringHelper;

public class BitStoreData {

	@SuppressWarnings("serial")
	public static class BitStoreDataException extends BitStoreException {

		public BitStoreDataException(Throwable cause) {
			super(cause);
		}

		public BitStoreDataException(String messageFormat, Object... messageArgs) {
			super(messageFormat, messageArgs);
		}

		public BitStoreDataException(Throwable cause, String messageFormat, Object... messageArgs) {
			super(cause, messageFormat, messageArgs);
		}
	}

	public static abstract class BitStoreItemData {

		protected final BitStoreViewItemMap viewItemMap;

		protected final BitStoreRecordData parentRecord;

		protected boolean isLittleEndianBytes;

		protected boolean isLittleEndianBits;

		public boolean hiden;

		protected BitStoreItemData(BitStoreViewItemMap bitStoreViewItemMap, BitStoreRecordData parentRecord) {
			this.viewItemMap = bitStoreViewItemMap;
			this.parentRecord = parentRecord;
			this.isLittleEndianBytes = this.parentRecord.isLittleEndianBytes();
			this.isLittleEndianBits = this.parentRecord.isLittleEndianBits();
			this.hiden = this.viewItemMap.getName().startsWith("_");
		}

		public String getName() {
			return this.viewItemMap.getName();
		}

		public String getPath() {
			return String.format("%s.%s.%s", this.parentRecord.parentView.getName(), this.parentRecord.getName(),
					this.getName());
		}

		public String getDescription() {
			return this.getViewItemMap().getDescription();
		}

		public BitStoreItemType getDataType() {
			return this.getViewItemMap().getType();
		}

		public BitStoreViewItemMap.ItemType getItemType() {
			return this.getViewItemMap().getItemType();
		}

		public int getBitLength() {
			return this.getViewItemMap().getBitLength();
		}

		public int getByteLength() {
			return this.getViewItemMap().getByteLength();
		}

		public int getIndex() {
			return this.getViewItemMap().getIndex();
		}

		public BitStoreViewPieceMap[] getPieces() {
			return this.getViewItemMap().getPieces();
		}

		public BitStoreViewItemMap getViewItemMap() {
			return this.viewItemMap;
		}

		public BitStoreRecordData getParentRecord() {
			return this.parentRecord;
		}

		public abstract byte[] getBytes()
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException;

		public abstract void setBytes(byte[] bytes)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException;

		public abstract <T> T getAs(Class<T> valueClass)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException;

		public abstract <T> void setWith(T value)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException;

		@Override
		public String toString() {
			IndentedStringBuilder tmpResult = new IndentedStringBuilder();
			tmpResult.append("%s {", this.getClass().getSimpleName());
			//
			ColumnsStringBuffer tmpAttributesString = new ColumnsStringBuffer(0, 0);
			tmpAttributesString.addLine("name", this.viewItemMap.getName());
			tmpAttributesString.addLine("description", this.viewItemMap.getDescription());
			tmpAttributesString.addLine("bitLength:", String.format("%d", this.viewItemMap.getBitLength()));
			String tmpStringValue = null;
			try {
				tmpStringValue = getAs(String.class);
			} catch (BitStoreException e) {
				tmpStringValue = e.getLocalizedMessage();
			}
			tmpAttributesString.addLine(new String[] { "value:", tmpStringValue });
			tmpResult.appendIndented(tmpAttributesString.getResult(4, 2));
			//
			tmpResult.append("}");
			return tmpResult.getResult();
		}

		protected <T> void checkValueClass(Class<T> valueClass, Class<?>... supportedClasses)
				throws BitStoreDataException {
			for (Class<?> tmpSupportedClass : supportedClasses) {
				if (valueClass.isAssignableFrom(tmpSupportedClass)) {
					return;
				}
			}
			throw new BitStoreDataException("Campo '%s' do tipo '%s' não pode ser convertido de/para '%s'",
					this.viewItemMap.getName(), this.viewItemMap.getType().name(), valueClass.getSimpleName());
		}

		protected <T> long convertToLong(T value) throws BitStoreDataException {
			Long tmpLongValue = null;
			if (value instanceof Byte) {
				tmpLongValue = ((Byte) value).longValue();
			} else if (value instanceof Short) {
				tmpLongValue = ((Short) value).longValue();
			} else if (value instanceof Integer) {
				tmpLongValue = ((Integer) value).longValue();
			} else if (value instanceof Long) {
				tmpLongValue = (Long) value;
			} else {
				throw new BitStoreDataException("Valor do tipo '%s' não pode ser atribuído ao campo '%s' do tipo '%s'",
						value.getClass().getSimpleName(), this.viewItemMap.getName(),
						this.viewItemMap.getType().name());
			}
			return tmpLongValue;
		}

		protected <T> void setInteger(T value)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			long tmpValue = this.convertToLong(value);
			long tmpValueHighestBit = Long.highestOneBit(tmpValue);
			int tmpValueLength = (Long.numberOfTrailingZeros(tmpValueHighestBit) + 1);
			if ((tmpValueHighestBit > 0) && (tmpValueLength > this.viewItemMap.getBitLength())) {
				throw new BitStoreDataException(
						"Valor '%d' do tipo '%s' ( %d bits ) excede o tamanho do campo '%s' do tipo '%s' ( %d bits ) da visão '%s'",
						tmpValue, value.getClass().getSimpleName(), tmpValueLength, this.viewItemMap.getName(),
						this.viewItemMap.getType().name(), this.viewItemMap.getBitLength(),
						this.viewItemMap.getParentViewRecord().getParentView().name);
			}
			int tmpByteLength = this.viewItemMap.getByteLength();
			this.setBytes(this.isLittleEndianBytes ? ByteHelper.toLittleEndian(tmpValue, tmpByteLength)
					: ByteHelper.toBigEndian(tmpValue, tmpByteLength));
		}

		protected <T> T getInteger(Class<T> valueClass, Long value) throws BitStoreDataException {
			T tmpResult = null;
			int tmpMaximumResultLength = 0;
			if (valueClass.isAssignableFrom(Byte.class)) {
				tmpMaximumResultLength = Byte.SIZE;
				tmpResult = valueClass.cast(value.byteValue());
			} else if (valueClass.isAssignableFrom(Short.class)) {
				tmpMaximumResultLength = Short.SIZE;
				tmpResult = valueClass.cast(value.shortValue());
			} else if (valueClass.isAssignableFrom(Integer.class)) {
				tmpMaximumResultLength = Integer.SIZE;
				tmpResult = valueClass.cast(value.intValue());
			} else if (valueClass.isAssignableFrom(Long.class)) {
				tmpMaximumResultLength = Long.SIZE;
				tmpResult = valueClass.cast(value);
			} else {
				throw new BitStoreDataException("Campo '%s' do tipo '%s' não pode ser convertido para '%s'",
						this.viewItemMap.getName(), this.viewItemMap.getType().name(), valueClass.getSimpleName());
			}
			if (this.viewItemMap.getBitLength() > tmpMaximumResultLength) {
				throw new BitStoreDataException(
						"Tamanho do tipo '%s' ( no máximo %d bits ) não é suficiente para o campo '%s' do tipo '%s' ( %d bits )",
						valueClass.getSimpleName(), tmpMaximumResultLength, this.viewItemMap.getName(),
						this.viewItemMap.getType().name(), this.viewItemMap.getBitLength());
			}
			return tmpResult;
		}

		protected <T> T getInteger(Class<T> valueClass)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			byte[] tmpGetBytes = this.getBytes();
			return this.getInteger(valueClass, this.isLittleEndianBytes ? ByteHelper.fromLittleEndian(tmpGetBytes)
					: ByteHelper.fromBigEndian(tmpGetBytes));
		}

		protected boolean[] getBits()
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			boolean[] tmpResult = new boolean[this.viewItemMap.getBitLength()];
			byte[] tmpBytes = this.getBytes();
			for (int tmpBitIndex = 0; tmpBitIndex < tmpResult.length; tmpBitIndex++) {
				int tmpByteIndex = (tmpBitIndex / Byte.SIZE);
				int tmpByteMask = (1 << (tmpBitIndex % Byte.SIZE));
				tmpResult[tmpBitIndex] = ((tmpBytes[tmpByteIndex] & tmpByteMask) != 0);
			}
			return tmpResult;
		}

		protected void setBits(boolean[] bitArray)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			if (bitArray.length != this.viewItemMap.getBitLength()) {
				throw new BitStoreDataException(
						"Quantidade de bits %d inválida para o campo '%s' do tipo '%s' ( %d bits )", bitArray.length,
						this.viewItemMap.getName(), this.viewItemMap.getType().name(), this.viewItemMap.getBitLength());
			}
			byte[] tmpBytes = new byte[this.viewItemMap.getByteLength()];
			for (int tmpBitIndex = 0; tmpBitIndex < bitArray.length; tmpBitIndex++) {
				int tmpByteIndex = (tmpBitIndex / Byte.SIZE);
				int tmpByteMask = (1 << (tmpBitIndex % Byte.SIZE));
				if (bitArray[tmpBitIndex]) {
					tmpBytes[tmpByteIndex] |= tmpByteMask;
				} else {
					tmpBytes[tmpByteIndex] &= ~tmpByteMask;
				}
			}
			this.setBytes(tmpBytes);
		}
	}

	public static class BitStoreZeroedData extends BitStoreItemData {

		public BitStoreZeroedData(BitStoreViewRUFMap bitStoreViewRUFMap, BitStoreRecordData parentRecordData) {
			super(bitStoreViewRUFMap, parentRecordData);
		}

		@Override
		public <T> T getAs(Class<T> valueClass) throws BitStoreDataException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			this.checkValueClass(valueClass, byte[].class, String.class);
			int tmpBitLength = this.viewItemMap.getBitLength();
			byte[] tmpBytes = new byte[this.viewItemMap.getByteLength()];
			if (valueClass.isAssignableFrom(String.class)) {
				return valueClass.cast(BitHelper.bitArrayToString(tmpBytes,
						BitHelper.getBitArrayPadLength(tmpBitLength), tmpBitLength));
			}
			return valueClass.cast(tmpBytes);
		}

		@Override
		public <T> void setWith(T value) throws BitStoreDataException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			this.checkValueClass(value.getClass(), byte[].class);
			this.setBytes((byte[]) value);
		}

		@Override
		public byte[] getBytes() throws BitStoreDeviceException, BitStoreSessionException,
				BitStoreAuthenticationException, BitStoreAccessException {
			return new byte[this.viewItemMap.getByteLength()];
		}

		@Override
		public void setBytes(byte[] bytes) throws BitStoreDataException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			if (bytes.length != this.viewItemMap.getByteLength()) {
				throw new BitStoreDataException("Bytes inválidos para o campo '%s' do registro '%s'",
						this.viewItemMap.getName(), this.viewItemMap.getParentViewRecord().name);
			}
			for (byte tmpByte : bytes) {
				if (tmpByte != 0) {
					throw new BitStoreDataException(
							"Valor diferente de zero atribuido ao campo '%s' do registro '%s' na visão '%s' do mapa '%s'",
							this.viewItemMap.getName(), this.viewItemMap.getParentViewRecord().name,
							this.viewItemMap.getParentViewRecord().getParentView().name,
							this.viewItemMap.getParentViewRecord().getParentView().getStoreMap().name);
				}
			}
		}
	}

	public static abstract class BitStoreFieldData extends BitStoreItemData {

		protected final BitStoreViewFieldMap viewFieldMap;

		protected BitStoreFieldData(BitStoreViewFieldMap bitStoreViewFieldMap, BitStoreRecordData parentRecordData) {
			super(bitStoreViewFieldMap, parentRecordData);
			this.viewFieldMap = bitStoreViewFieldMap;
		}

		public BitStoreViewFieldMap getViewFieldMap() {
			return this.viewFieldMap;
		}

		@Override
		public byte[] getBytes()
				throws BitStoreMapException, BitStoreSetupException, BitStoreDataException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			int tmpFieldBitLength = this.viewFieldMap.getFieldMap().getBitLength();
			byte[] tmpMainViewBuffer = BitHelper.createBitArrayBuffer(tmpFieldBitLength);
			int tmpMainViewOffset = this.isLittleEndianBits ? 0 : BitHelper.getBitArrayPadLength(tmpFieldBitLength);
			for (BitStoreViewPieceMap tmpPieceMap : this.viewFieldMap.getFieldMap().getMainViewPieces()) {
				this.parentRecord.getRecordSource().getPiece(tmpPieceMap, this.isLittleEndianBits, tmpMainViewBuffer,
						tmpMainViewOffset);
				tmpMainViewOffset += tmpPieceMap.length;
			}
			byte[] tmpResultBuffer = new byte[this.viewFieldMap.getByteLength()];
			int tmpBitLength = this.viewFieldMap.getBitLength();
			tmpMainViewOffset = this.isLittleEndianBits ? 0 : ((tmpMainViewBuffer.length * Byte.SIZE) - tmpBitLength);
			int tmpResultOffset = this.isLittleEndianBits ? 0 : BitHelper.getBitArrayPadLength(tmpBitLength);
			BitHelper.bitArrayCopy(this.isLittleEndianBits, tmpMainViewBuffer, tmpMainViewOffset, tmpResultBuffer,
					tmpResultOffset, tmpBitLength);
			return tmpResultBuffer;
		}

		@Override
		public void setBytes(byte[] bytes)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			if (bytes.length != this.viewFieldMap.getByteLength()) {
				throw new BitStoreDataException("Bytes inválidos para o campo '%s' do registro '%s'",
						this.viewFieldMap.getName(), this.viewFieldMap.getParentViewRecord().name);
			}
			byte[] tmpMainBytes;
			int tmpMainBitLength = this.viewFieldMap.getFieldMap().getBitLength();
			int tmpBitLength = this.viewFieldMap.getBitLength();
			if (tmpMainBitLength == tmpBitLength) {
				tmpMainBytes = bytes;
			} else {
				if (this.isLittleEndianBits) {
					tmpMainBytes = BitHelper.copyOfLeftAlignedBitArray(bytes, tmpBitLength, tmpMainBitLength);
				} else {
					tmpMainBytes = BitHelper.copyOfRightAlignedBitArray(bytes, tmpBitLength, tmpMainBitLength);
				}
			}
			int tmpMainOffset = this.isLittleEndianBits ? 0 : BitHelper.getBitArrayPadLength(tmpMainBitLength);
			for (BitStoreViewPieceMap tmpPieceMap : this.viewFieldMap.getFieldMap().getMainViewPieces()) {
				this.parentRecord.getRecordSource().setPiece(tmpPieceMap, this.isLittleEndianBits, tmpMainBytes,
						tmpMainOffset);
				tmpMainOffset += tmpPieceMap.length;
			}
		}
	}

	public static class BitStoreBooleanFieldData extends BitStoreFieldData {

		protected final String trueString;

		protected final String falseString;

		protected BitStoreBooleanFieldData(BitStoreBooleanFieldMap bitStoreBooleanFieldMap,
				BitStoreViewFieldMap bitStoreViewFieldMap, BitStoreRecordData parentRecordData) {
			super(bitStoreViewFieldMap, parentRecordData);
			this.trueString = bitStoreBooleanFieldMap.getTrueString();
			this.falseString = bitStoreBooleanFieldMap.getFalseString();
		}

		@Override
		public <T> T getAs(Class<T> valueClass)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			this.checkValueClass(valueClass, Boolean.class, Byte.class, Short.class, Integer.class, Long.class,
					String.class);
			if (valueClass.isAssignableFrom(Boolean.class)) {
				return valueClass.cast(this.getInteger(Long.class) != 0);
			} else if (valueClass.isAssignableFrom(String.class)) {
				return valueClass.cast((this.getInteger(Long.class) != 0) ? this.trueString : this.falseString);
			}
			return this.getInteger(valueClass);
		}

		@Override
		public <T> void setWith(T value)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			this.checkValueClass(value.getClass(), Boolean.class, Byte.class, Short.class, Integer.class, Long.class,
					String.class);
			if (value instanceof Boolean) {
				this.setInteger(((Boolean) value).booleanValue() ? 1 : 0);
			} else if (value instanceof String) {
				String tmpStringValue = ((String) value).trim();
				if (tmpStringValue.compareToIgnoreCase(this.trueString) == 0) {
					this.setInteger(1);
				} else if (tmpStringValue.compareToIgnoreCase(this.falseString) == 0) {
					this.setInteger(0);
				} else {
					throw new BitStoreDataException(
							"String '%s' não pode ser atribuída ao campo '%s' do tipo '%s' ( somente '%s' ou '%s' )",
							tmpStringValue, this.viewFieldMap.getName(), this.viewFieldMap.getType().name(),
							this.trueString, this.falseString);
				}
			} else {
				this.setInteger(value);
			}
		}
	}

	public static class BitStoreRUFFieldData extends BitStoreFieldData {

		protected BitStoreRUFFieldData(BitStoreViewFieldMap bitStoreViewFieldMap, BitStoreRecordData parentRecordData) {
			super(bitStoreViewFieldMap, parentRecordData);
		}

		@Override
		public <T> T getAs(Class<T> valueClass)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			this.checkValueClass(valueClass, byte[].class, String.class);
			byte[] tmpBytes = this.getBytes();
			if (valueClass.isAssignableFrom(String.class)) {
				return valueClass.cast(ByteHelper.hexify(tmpBytes));
			}
			return valueClass.cast(tmpBytes);
		}

		@Override
		public <T> void setWith(T value)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			this.checkValueClass(value.getClass(), byte[].class, String.class);
			if (value instanceof byte[]) {
				this.setBytes((byte[]) value);
			} else if (value instanceof String) {
				this.setBytes(ByteHelper.parseHexString((String) value));
			}
		}
	}

	public static class BitStoreIntegerFieldData extends BitStoreFieldData {

		protected static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###,###,###,###,###,###");

		protected BitStoreIntegerFieldData(BitStoreViewFieldMap bitStoreViewFieldMap,
				BitStoreRecordData parentRecordData) {
			super(bitStoreViewFieldMap, parentRecordData);
		}

		@Override
		public <T> T getAs(Class<T> valueClass)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			this.checkValueClass(valueClass, Byte.class, Short.class, Integer.class, Long.class, String.class);
			if (valueClass.isAssignableFrom(String.class)) {
				long tmpValue = this.getInteger(Long.class);
				Integer tmpDefaultBase = ((BitStoreIntegerFieldMap) this.viewFieldMap.getFieldMap()).defaultBase;
				String tmpValueString = null;
				if ((tmpDefaultBase != null) && (tmpDefaultBase == 16)) {
					int tmpHexaDigits = (BitHelper
							.getBitArrayBufferLength(this.viewFieldMap.getFieldMap().getBitLength()) * 2);
					String tmpFormat = String.format("0x%%0%dX", tmpHexaDigits);
					tmpValueString = String.format(tmpFormat, tmpValue);
				} else {
					tmpValueString = DECIMAL_FORMAT.format(tmpValue);
				}
				return valueClass.cast(tmpValueString);
			}
			return this.getInteger(valueClass);
		}

		@Override
		public <T> void setWith(T value)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			this.checkValueClass(value.getClass(), Byte.class, Short.class, Integer.class, Long.class, String.class);
			if (value instanceof String) {
				String tmpStringValue = ((String) value).trim();
				Integer tmpDefaultBase = ((BitStoreIntegerFieldMap) this.viewFieldMap.getFieldMap()).defaultBase;
				long tmpLongValue = 0;
				if ((tmpDefaultBase != null) && (tmpDefaultBase == 16)) {
					if (!tmpStringValue.matches("0x[0-9A-Fa-f]+")) {
						throw new BitStoreDataException(
								"String '%s' não pode ser atribuída ao campo '%s' do tipo '%s' ( inteiro de %d bits em base 16 )",
								StringHelper.wrap(tmpStringValue, 20), this.viewFieldMap.getName(),
								this.viewFieldMap.getType().name(), this.viewFieldMap.getBitLength());
					}
					tmpLongValue = Long.parseLong(tmpStringValue.replaceAll("0x", ""), 16);
				} else {
					try {
						tmpLongValue = DECIMAL_FORMAT.parse(tmpStringValue).longValue();
					} catch (ParseException e) {
						throw new BitStoreDataException("String '%s' não pode ser convertida para inteiro",
								((String) value));
					}
				}
				this.setInteger(tmpLongValue);
			} else {
				this.setInteger(value);
			}
		}
	}

	public static class BitStoreDateFieldData extends BitStoreFieldData {

		public static final Date REFERENCE_DATE = new GregorianCalendar(2000, 0, 1).getTime();

		public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

		protected BitStoreDateFieldData(BitStoreViewFieldMap bitStoreViewFieldMap,
				BitStoreRecordData parentRecordData) {
			super(bitStoreViewFieldMap, parentRecordData);
		}

		public static int computeDayOffset(Date date) {
			return DateTimeHelper.computeElapsedDays(date, BitStoreDateFieldData.REFERENCE_DATE);
		}

		public static Date computeDatetime(int relativeDays, int relativeMinutes) {
			if ((relativeDays == 0) && (relativeMinutes == 0)) {
				return new Date(0);
			}
			return DateTimeHelper.computeElapsedDate(BitStoreDateFieldData.REFERENCE_DATE, relativeDays, 0,
					relativeMinutes, 0);
		}

		@Override
		public <T> T getAs(Class<T> valueClass)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			this.checkValueClass(valueClass, Date.class, Integer.class, Long.class, String.class);
			if (valueClass.isAssignableFrom(Date.class) || valueClass.isAssignableFrom(String.class)) {
				GregorianCalendar tmpDate = new GregorianCalendar();
				tmpDate.setTime(BitStoreDateFieldData.REFERENCE_DATE);
				tmpDate.add(Calendar.DAY_OF_MONTH, this.getInteger(Integer.class).intValue());
				return valueClass.cast((valueClass.isAssignableFrom(Date.class)) ? tmpDate.getTime()
						: BitStoreDateFieldData.DATE_FORMAT.format(tmpDate.getTime()));
			}
			return this.getInteger(valueClass);
		}

		@Override
		public <T> void setWith(T value)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			this.checkValueClass(value.getClass(), Date.class, Integer.class, Long.class, String.class);
			if (value instanceof Date) {
				this.setInteger(computeDayOffset((Date) value));
			} else if (value instanceof String) {
				String tmpString = ((String) value).trim();
				try {
					Date tmpDate = BitStoreDateFieldData.DATE_FORMAT.parse(tmpString);
					this.setInteger(computeDayOffset(tmpDate));
				} catch (ParseException e) {
					throw new BitStoreDataException("String '%s' não pode ser convertida para data no formato '%s'",
							tmpString, BitStoreDateFieldData.DATE_FORMAT.toLocalizedPattern());
				}
			} else {
				this.setInteger(value);
			}
		}
	}

	public static class BitStoreTimeFieldData extends BitStoreFieldData {

		public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

		protected BitStoreTimeFieldData(BitStoreViewFieldMap bitStoreViewFieldMap,
				BitStoreRecordData parentRecordData) {
			super(bitStoreViewFieldMap, parentRecordData);
		}

		@Override
		public <T> T getAs(Class<T> valueClass)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			this.checkValueClass(valueClass, Short.class, Integer.class, Long.class, String.class);
			if (valueClass.isAssignableFrom(String.class)) {
				int tmpMinuteCount = this.getInteger(Integer.class);
				GregorianCalendar tmpCalendar = new GregorianCalendar();
				tmpCalendar.set(Calendar.HOUR_OF_DAY, 0);
				tmpCalendar.set(Calendar.MINUTE, 0);
				tmpCalendar.add(Calendar.MINUTE, tmpMinuteCount);
				return valueClass.cast(BitStoreTimeFieldData.TIME_FORMAT.format(tmpCalendar.getTime()));
			}
			return this.getInteger(valueClass);
		}

		@Override
		public <T> void setWith(T value)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			this.checkValueClass(value.getClass(), Date.class, Short.class, Integer.class, Long.class, String.class);
			if ((value instanceof Date) || (value instanceof String)) {
				Calendar tmpCalendar = Calendar.getInstance();
				if (value instanceof Date) {
					tmpCalendar.setTime((Date) value);
				} else {
					String tmpString = ((String) value).trim();
					try {
						tmpCalendar.setTime(BitStoreTimeFieldData.TIME_FORMAT.parse(tmpString));
					} catch (ParseException e) {
						throw new BitStoreDataException(
								"String '%s' não pode ser convertida para horário no formato '%s'", tmpString,
								BitStoreTimeFieldData.TIME_FORMAT.toLocalizedPattern());
					}
				}
				this.setInteger((tmpCalendar.get(Calendar.HOUR_OF_DAY) * 60) + tmpCalendar.get(Calendar.MINUTE));
			} else {
				this.setInteger(value);
			}
		}
	}

	public static class BitStoreCurrencyFieldData extends BitStoreFieldData {

		public static final String CURRENCY_CODE = Currency.getInstance(Locale.getDefault()).getSymbol();

		public static final NumberFormat NUMBER_FORMAT = NumberFormat.getCurrencyInstance(Locale.getDefault());

		protected BitStoreCurrencyFieldData(BitStoreViewFieldMap bitStoreViewFieldMap,
				BitStoreRecordData parentRecordData) {
			super(bitStoreViewFieldMap, parentRecordData);
		}

		@Override
		public <T> T getAs(Class<T> valueClass)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			this.checkValueClass(valueClass, Byte.class, Short.class, Integer.class, Long.class, String.class);
			if (valueClass.isAssignableFrom(String.class)) {
				float tmpValue = this.getInteger(Long.class);
				return valueClass.cast(NUMBER_FORMAT.format(tmpValue / 100));
			}
			return this.getInteger(valueClass);
		}

		@Override
		public <T> void setWith(T value)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			this.checkValueClass(value.getClass(), Byte.class, Short.class, Integer.class, Long.class, String.class);
			if (value instanceof String) {
				try {
					this.setInteger((long) (NUMBER_FORMAT.parse((String) value).floatValue() * 100));
				} catch (Throwable e) {
					throw new BitStoreDataException("String '%s' não pode ser convertida para 'moeda' (%s)",
							((String) value), e.getClass().getSimpleName());
				}
			} else {
				this.setInteger(value);
			}
		}
	}

	public static class BitStoreEnumerationData extends BitStoreFieldData {

		protected BitStoreEnumerationData(BitStoreViewFieldMap bitStoreViewFieldMap,
				BitStoreRecordData parentRecordData) {
			super(bitStoreViewFieldMap, parentRecordData);
		}

		protected static final BitStoreEnumerationFieldItemMap invalidItemMap = new BitStoreEnumerationFieldItemMap(-1,
				"Inválido");

		@Override
		public <T> T getAs(Class<T> valueClass)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			this.checkValueClass(valueClass, Byte.class, Short.class, Integer.class, Long.class, String.class,
					BitStoreEnumerationFieldItemMap.class);
			byte[] tmpValue = this.getBytes();
			Boolean tmpIsInverted = ((BitStoreEnumerationFieldMap) this.viewFieldMap.getFieldMap()).inverted;
			if ((tmpIsInverted != null) && tmpIsInverted.booleanValue()) {
				int tmpBitLength = this.viewFieldMap.getBitLength();
				BitHelper.bitArrayInvert(tmpValue, BitHelper.getBitArrayPadLength(tmpBitLength), tmpBitLength);
			}
			long tmpIntegerValue = this.getInteger(Integer.class,
					(this.isLittleEndianBytes ? ByteHelper.fromLittleEndian(tmpValue)
							: ByteHelper.fromBigEndian(tmpValue)));
			BitStoreEnumerationFieldItemMap tmpEnumerationValue = invalidItemMap;
			for (BitStoreEnumerationFieldItemMap tmpEnumerationItem : ((BitStoreEnumerationFieldMap) this.viewFieldMap
					.getFieldMap()).getItems()) {
				if (tmpEnumerationItem.value == tmpIntegerValue) {
					tmpEnumerationValue = tmpEnumerationItem;
					break;
				}
			}
			if (valueClass.isAssignableFrom(BitStoreEnumerationFieldItemMap.class)) {
				return valueClass.cast(tmpEnumerationValue);
			} else if (valueClass.isAssignableFrom(String.class)) {
				return valueClass.cast(String.format("%d - %s", tmpIntegerValue, tmpEnumerationValue.name));
			}
			return this.getInteger(valueClass, (this.isLittleEndianBytes ? ByteHelper.fromLittleEndian(tmpValue)
					: ByteHelper.fromBigEndian(tmpValue)));
		}

		@Override
		@SuppressWarnings("unused")
		public <T> void setWith(T value)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			this.checkValueClass(value.getClass(), Byte.class, Short.class, Integer.class, Long.class, String.class);
			if (value instanceof String) {
				String tmpValueString = ((String) value).trim();
				String tmpPattern = "\\d+ - (.*)";
				if (tmpValueString.matches(tmpPattern)) {
					tmpValueString = tmpValueString.replaceAll(tmpPattern, "$1");
				}
				for (BitStoreEnumerationFieldItemMap tmpEnumerationItem : ((BitStoreEnumerationFieldMap) this.viewFieldMap
						.getFieldMap()).getItems()) {
					if (tmpEnumerationItem.name.compareTo(tmpValueString) == 0) {
						int tmpByteLength = this.viewFieldMap.getByteLength();
						byte[] tmpValue = (this.isLittleEndianBytes
								? ByteHelper.toLittleEndian(tmpEnumerationItem.value, tmpByteLength)
								: ByteHelper.toBigEndian(tmpEnumerationItem.value, tmpByteLength));
						Boolean tmpIsInverted = ((BitStoreEnumerationFieldMap) this.viewFieldMap
								.getFieldMap()).inverted;
						if ((tmpIsInverted != null) && tmpIsInverted.booleanValue()) {
							int tmpBitLength = this.viewFieldMap.getBitLength();
							BitHelper.bitArrayInvert(tmpValue, BitHelper.getBitArrayPadLength(tmpBitLength),
									tmpBitLength);
						}
						super.setBytes(tmpValue);
						return;
					}
				}
				throw new BitStoreDataException("Nenhum ítem com o nome '%s' no campo enumerado '%s'", ((String) value),
						this.viewFieldMap.getName());
			} else {
				long tmpLongValue = this.convertToLong(value);
				//
				byte[] tmpBytes = (this.isLittleEndianBytes
						? ByteHelper.toLittleEndian(tmpLongValue, this.viewFieldMap.getByteLength())
						: ByteHelper.toBigEndian(tmpLongValue, this.viewFieldMap.getByteLength()));
				super.setBytes(tmpBytes);
				if (false) {
					for (BitStoreEnumerationFieldItemMap tmpEnumerationItemMap : ((BitStoreEnumerationFieldMap) this.viewFieldMap
							.getFieldMap()).getItems()) {
						if (tmpEnumerationItemMap.value == tmpLongValue) {
							super.setBytes(tmpBytes);
							return;
						}
					}
					throw new BitStoreDataException("Nenhum ítem com o valor '%d' no campo enumerado '%s'",
							tmpLongValue, this.viewFieldMap.getName());
				}
			}
		}

		@Override
		public void setBytes(byte[] bytes)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			this.setWith(ByteHelper.fromBigEndian(bytes));
		}
	}

	public static class BitStoreBitArrayFieldData extends BitStoreFieldData {

		protected BitStoreBitArrayFieldData(BitStoreViewFieldMap bitStoreViewFieldMap,
				BitStoreRecordData parentRecordData) {
			super(bitStoreViewFieldMap, parentRecordData);
		}

		@Override
		public <T> T getAs(Class<T> valueClass)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			this.checkValueClass(valueClass, byte[].class, String.class);
			if (valueClass.isAssignableFrom(String.class)) {
				int tmpBitLength = this.viewFieldMap.getBitLength();
				return valueClass.cast(BitHelper.bitArrayToString(this.getBytes(),
						BitHelper.getBitArrayPadLength(tmpBitLength), tmpBitLength));
			}
			return valueClass.cast(this.getBytes());
		}

		@Override
		public <T> void setWith(T value)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			this.checkValueClass(value.getClass(), byte[].class);
			this.setBytes((byte[]) value);
		}
	}

	public static class BitStoreByteArrayFieldData extends BitStoreFieldData {

		protected BitStoreByteArrayFieldData(BitStoreViewFieldMap bitStoreViewFieldMap,
				BitStoreRecordData parentRecordData) {
			super(bitStoreViewFieldMap, parentRecordData);
		}

		@Override
		public <T> T getAs(Class<T> valueClass)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			this.checkValueClass(valueClass, byte[].class, String.class);
			if (valueClass.isAssignableFrom(String.class)) {
				return valueClass.cast(ByteHelper.hexify(this.getBytes()));
			}
			return valueClass.cast(this.getBytes());
		}

		@Override
		public <T> void setWith(T value)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			this.checkValueClass(value.getClass(), byte[].class, String.class);
			if (value instanceof byte[]) {
				this.setBytes((byte[]) value);
			} else if (value instanceof String) {
				this.setBytes(ByteHelper.parseHexString((String) value));
			}
		}
	}

	public static class BitStoreIntegerArrayFieldData extends BitStoreFieldData {

		protected final int integerLength;

		protected final int arrayLength;

		protected BitStoreIntegerArrayFieldData(BitStoreIntegerArrayFieldMap bitStoreIntegerArrayFieldMap,
				BitStoreViewFieldMap bitStoreViewFieldMap, BitStoreRecordData parentRecordData) {
			super(bitStoreViewFieldMap, parentRecordData);
			this.integerLength = bitStoreIntegerArrayFieldMap.integerLength;
			this.arrayLength = bitStoreIntegerArrayFieldMap.arrayLength;
		}

		protected <T> void checkIntegerArray(Class<T> valueClass) throws BitStoreDataException {
			int tmpTypeSize;
			if (byte[].class.isAssignableFrom(valueClass)) {
				tmpTypeSize = Byte.SIZE;
			} else if (short[].class.isAssignableFrom(valueClass)) {
				tmpTypeSize = Short.SIZE;
			} else if (int[].class.isAssignableFrom(valueClass)) {
				tmpTypeSize = Integer.SIZE;
			} else if (long[].class.isAssignableFrom(valueClass)) {
				tmpTypeSize = Long.SIZE;
			} else {
				throw new BitStoreDataException("Campo '%s' do tipo '%s' não pode ser convertido para '%s'",
						this.viewFieldMap.getName(), this.viewFieldMap.getType().name(), valueClass.getSimpleName());
			}
			if (tmpTypeSize < this.integerLength) {
				throw new BitStoreDataException(
						"Tamanho do tipo '%s' ( %d bits ) não é suficiente para o campo '%s' do tipo '%s' ( %d bits )",
						valueClass.getSimpleName(), tmpTypeSize, this.viewFieldMap.getName(),
						this.viewFieldMap.getType().name(), this.integerLength);
			}
		}

		@Override
		public <T> T getAs(Class<T> valueClass)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			T tmpResult = null;
			if (valueClass.isAssignableFrom(String.class)) {
				try {
					StringBuilder tmpValueString = new StringBuilder();
					tmpValueString.append("[ ");
					boolean tmpFirstItem = true;
					long[] tmpValueArray = this.getAs(long[].class);
					for (long tmpValueElement : tmpValueArray) {
						if (tmpFirstItem) {
							tmpFirstItem = false;
						} else {
							tmpValueString.append(", ");
						}
						tmpValueString.append(Long.toString(tmpValueElement));
					}
					tmpValueString.append(" ]");
					tmpResult = valueClass.cast(tmpValueString.toString());
				} catch (BitStoreDataException e) {
					tmpResult = valueClass.cast(e.getLocalizedMessage());
				}
			} else {
				this.checkIntegerArray(valueClass);
				int tmpBitOffset = BitHelper.getBitArrayPadLength(this.viewFieldMap.getBitLength());
				long[] tmpLongArray = new long[this.arrayLength];
				for (int tmpLongIndex = 0; tmpLongIndex < tmpLongArray.length; tmpLongIndex++) {
					byte[] tmpBytes = BitHelper.subBitArray(this.getBytes(), tmpBitOffset, this.integerLength);
					tmpLongArray[tmpLongIndex] = ByteHelper.fromBigEndian(tmpBytes);
					tmpBitOffset += this.integerLength;
				}
				if (valueClass.isAssignableFrom(byte[].class)) {
					byte[] tmpByteArray = new byte[this.arrayLength];
					for (int tmpByteIndex = 0; tmpByteIndex < tmpByteArray.length; tmpByteIndex++) {
						tmpByteArray[tmpByteIndex] = (byte) tmpLongArray[tmpByteIndex];
					}
					tmpResult = valueClass.cast(tmpByteArray);
				} else if (valueClass.isAssignableFrom(short[].class)) {
					short[] tmpShortArray = new short[this.arrayLength];
					for (int tmpShortIndex = 0; tmpShortIndex < tmpShortArray.length; tmpShortIndex++) {
						tmpShortArray[tmpShortIndex] = (short) tmpLongArray[tmpShortIndex];
					}
					tmpResult = valueClass.cast(tmpShortArray);
				} else if (valueClass.isAssignableFrom(int[].class)) {
					int[] tmpIntegerArray = new int[this.arrayLength];
					for (int tmpIntegerIndex = 0; tmpIntegerIndex < tmpIntegerArray.length; tmpIntegerIndex++) {
						tmpIntegerArray[tmpIntegerIndex] = (int) tmpLongArray[tmpIntegerIndex];
					}
					tmpResult = valueClass.cast(tmpIntegerArray);
				} else if (valueClass.isAssignableFrom(long[].class)) {
					tmpResult = valueClass.cast(tmpLongArray);
				}
			}
			return tmpResult;
		}

		@Override
		public <T> void setWith(T value)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			long[] tmpElementArray = new long[this.arrayLength];
			if (value instanceof String) {
				String tmpValue = ((String) value).trim();
				if (!tmpValue.matches("\\[( *\\d+,)* *\\d+ *\\]")) {
					throw new BitStoreDataException(
							"String '%s' não pode ser atribuída ao campo '%s' do tipo '%s' ( array de %d inteiros de %d bits )",
							StringHelper.wrap(tmpValue, 20), this.viewFieldMap.getName(),
							this.viewFieldMap.getType().name(), this.arrayLength, this.integerLength);
				}
				String[] tmpArray = tmpValue.replaceAll("[\\[\\] ]", "").split(",");
				if (tmpArray.length != this.arrayLength) {
					throw new BitStoreDataException(
							"Array '%s' ( %d elementos ) não pode ser atribuída ao campo '%s' do tipo '%s' ( array de %d inteiros de %d bits )",
							StringHelper.wrap(tmpValue, 20), tmpArray.length, this.viewFieldMap.getName(),
							this.viewFieldMap.getType().name(), this.arrayLength, this.integerLength);
				}
				for (int tmpIntegerIndex = 0; tmpIntegerIndex < this.arrayLength; tmpIntegerIndex++) {
					try {
						tmpElementArray[tmpIntegerIndex] = (Long) BitStoreIntegerFieldData.DECIMAL_FORMAT
								.parse(tmpArray[tmpIntegerIndex]);
					} catch (ParseException e) {
						throw new BitStoreDataException(e.getLocalizedMessage());
					}
				}
			} else {
				this.checkIntegerArray(value.getClass());
				if (Array.getLength(value) != this.arrayLength) {
					throw new BitStoreDataException(
							"Array do tipo '%s' ( %d elementos ) não pode ser atribuída ao campo '%s' do tipo '%s' ( array de %d inteiros de %d bits )",
							value.getClass().getSimpleName(), Array.getLength(value), this.viewFieldMap.getName(),
							this.viewFieldMap.getType().name(), this.arrayLength, this.integerLength);
				}
				if (value instanceof byte[]) {
					for (int tmpByteIndex = 0; tmpByteIndex < this.arrayLength; tmpByteIndex++) {
						tmpElementArray[tmpByteIndex] = Array.getByte(value, tmpByteIndex);
					}
				} else if (value instanceof short[]) {
					for (int tmpShortIndex = 0; tmpShortIndex < this.arrayLength; tmpShortIndex++) {
						tmpElementArray[tmpShortIndex] = Array.getShort(value, tmpShortIndex);
					}
				} else if (value instanceof int[]) {
					for (int tmpIntegerIndex = 0; tmpIntegerIndex < this.arrayLength; tmpIntegerIndex++) {
						tmpElementArray[tmpIntegerIndex] = Array.getInt(value, tmpIntegerIndex);
					}
				} else if (value instanceof long[]) {
					for (int tmpLongIndex = 0; tmpLongIndex < this.arrayLength; tmpLongIndex++) {
						tmpElementArray[tmpLongIndex] = Array.getLong(value, tmpLongIndex);
					}
				}
			}
			int tmpBitLength = this.viewFieldMap.getBitLength();
			byte[] tmpBuffer = BitHelper.createBitArrayBuffer(tmpBitLength);
			int tmpBufferOffset = BitHelper.getBitArrayPadLength(tmpBitLength);
			int tmpElementOffset = BitHelper.getBitArrayPadLength(this.integerLength);
			for (int tmpElementIndex = 0; tmpElementIndex < this.arrayLength; tmpElementIndex++) {
				byte[] tmpElementBytes = ByteHelper.toBigEndian(tmpElementArray[tmpElementIndex]);
				BitHelper.bitArrayCopy(tmpElementBytes, tmpElementOffset, tmpBuffer, tmpBufferOffset,
						this.integerLength);
				tmpBufferOffset += this.integerLength;
			}
			this.setBytes(tmpBuffer);
		}
	}

	public static class BitStoreRecordData {

		protected final BitStoreViewRecordMap viewRecordMap;

		protected final BitStoreRecordSource recordSource;

		protected final BitStoreViewData parentView;

		protected final BitStoreItemData[] items;

		protected final BitStoreFieldData[] fields;

		protected boolean isLittleEndianBytes;

		protected boolean isLittleEndianBits;

		public boolean hiden;

		protected BitStoreRecordData(BitStoreViewRecordMap viewRecordMap, BitStoreRecordSource bitStoreRecordSource,
				BitStoreViewData parentView) {
			this.viewRecordMap = viewRecordMap;
			this.recordSource = bitStoreRecordSource;
			this.parentView = parentView;
			this.isLittleEndianBytes = this.viewRecordMap.isLittleEndianBytes();
			this.isLittleEndianBits = this.viewRecordMap.isLittleEndianBits();
			int tmpFieldCount = 0;
			this.items = new BitStoreItemData[viewRecordMap.getItems().length];
			for (int tmpIndex = 0; tmpIndex < this.items.length; tmpIndex++) {
				this.items[tmpIndex] = viewRecordMap.getItems()[tmpIndex].createDataObject(this);
				if (this.items[tmpIndex].getItemType() == BitStoreViewItemMap.ItemType.Field) {
					tmpFieldCount++;
				}
			}
			int tmpFieldIndex = 0;
			this.fields = new BitStoreFieldData[tmpFieldCount];
			for (int tmpIndex = 0; tmpIndex < this.items.length; tmpIndex++) {
				if (this.items[tmpIndex].getItemType() == BitStoreViewItemMap.ItemType.Field) {
					this.fields[tmpFieldIndex] = (BitStoreFieldData) this.items[tmpIndex];
					++tmpFieldIndex;
				}
			}
			this.hiden = this.viewRecordMap.getName().startsWith("_");
		}

		public String getName() {
			return this.viewRecordMap.name;
		}

		public String getPath() {
			return String.format("%s.%s", this.parentView.getName(), this.getName());
		}

		public String getDescription() {
			return this.viewRecordMap.getDescription();
		}

		public int getIndex() {
			return this.viewRecordMap.getIndex();
		}

		public int getDataType() {
			return this.viewRecordMap.dataType;
		}

		public int getBitLength() {
			return this.viewRecordMap.bitLength;
		}

		public int getByteLength() {
			return this.viewRecordMap.getByteLength();
		}

		public boolean isLittleEndianBytes() {
			return this.isLittleEndianBytes;
		}

		public boolean isLittleEndianBits() {
			return this.isLittleEndianBits;
		}

		public BitStoreRecordData getMainView() {
			return this.parentView.getParentStore().getMainView().searchRecord(this.viewRecordMap.name);
		}

		public BitStoreRecordData searchView(String viewName) {
			BitStoreRecordData tmpResult = null;
			BitStoreViewData tmpView = this.parentView.getParentStore().searchView(viewName);
			if (tmpView != null) {
				tmpResult = tmpView.searchRecord(this.viewRecordMap.name);
			}
			return tmpResult;
		}

		public BitStoreRecordData[] getViews() throws BitStoreMapException {
			String tmpName = this.viewRecordMap.name;
			BitStoreViewData[] tmpStoreViews = this.parentView.getParentStore().getViews();
			List<BitStoreRecordData> tmpRecordViews = new ArrayList<BitStoreRecordData>();
			for (BitStoreViewData tmpStoreView : tmpStoreViews) {
				BitStoreRecordData tmpRecordView = tmpStoreView.searchRecord(tmpName);
				if (tmpRecordView != null) {
					tmpRecordViews.add(tmpRecordView);
				}
			}
			return tmpRecordViews.toArray(new BitStoreRecordData[0]);
		}

		public BitStoreRecordData getView(String viewName) throws BitStoreMapException {
			return this.parentView.getParentStore().getView(viewName).getRecord(this.viewRecordMap.name);
		}

		public BitStoreViewData getParentView() {
			return this.parentView;
		}

		public BitStoreViewRecordMap getViewRecordMap() {
			return this.viewRecordMap;
		}

		public BitStoreRecordSource getRecordSource() {
			return this.recordSource;
		}

		public BitStoreItemData[] getItems() {
			return this.getItems(false);
		}

		public BitStoreItemData[] getItems(boolean includeHiden) {
			if (includeHiden) {
				return Arrays.copyOf(this.items, this.items.length);
			}
			List<BitStoreItemData> tmpResult = new ArrayList<BitStoreItemData>();
			for (BitStoreItemData tmpItem : this.items) {
				if (!tmpItem.hiden) {
					tmpResult.add(tmpItem);
				}
			}
			return tmpResult.toArray(new BitStoreItemData[0]);
		}

		public BitStoreItemData searchItem(String itemName) {
			for (BitStoreItemData tmpItem : this.items) {
				if (tmpItem.getName().compareTo(itemName) == 0) {
					return tmpItem;
				}
			}
			return null;
		}

		public BitStoreItemData getItem(String fieldName) throws BitStoreMapException {
			BitStoreItemData tmpResult = this.searchItem(fieldName);
			if (tmpResult == null) {
				throw new BitStoreMapException("Nenhum ítem com o nome '%s' na visão '%s' do registro '%s'", fieldName,
						this.viewRecordMap.getParentView().name, this.viewRecordMap.name);
			}
			return tmpResult;
		}

		public BitStoreFieldData[] getFields() {
			return this.getFields(false);
		}

		public BitStoreFieldData[] getFields(boolean includeHiden) {
			if (includeHiden) {
				return Arrays.copyOf(this.fields, this.fields.length);
			}
			List<BitStoreFieldData> tmpResult = new ArrayList<BitStoreFieldData>();
			for (BitStoreFieldData tmpItem : this.fields) {
				if (!tmpItem.hiden) {
					tmpResult.add(tmpItem);
				}
			}
			return tmpResult.toArray(new BitStoreFieldData[0]);
		}

		public String[] getFieldNames() {
			return this.getFieldNames(false);
		}

		public String[] getFieldNames(boolean includeHiden) {
			List<String> tmpResult = new ArrayList<String>();
			//
			for (BitStoreFieldData tmpField : this.getFields(includeHiden)) {
				tmpResult.add(tmpField.getName());
			}
			return tmpResult.toArray(new String[0]);
		}

		public BitStoreFieldData searchField(String fieldName) {
			for (BitStoreItemData tmpField : this.items) {
				if (tmpField.getName().compareTo(fieldName) == 0) {
					return (BitStoreFieldData) tmpField;
				}
			}
			return null;
		}

		public BitStoreFieldData getField(String fieldName) throws BitStoreMapException {
			BitStoreFieldData tmpResult = this.searchField(fieldName);
			if (tmpResult == null) {
				throw new BitStoreMapException("Nenhum campo com o nome '%s' na visão '%s' do registro '%s'", fieldName,
						this.viewRecordMap.getParentView().name, this.viewRecordMap.name);
			}
			return tmpResult;
		}

		public byte[] getBytes()
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			byte[] tmpResult = new byte[this.viewRecordMap.getByteLength()];
			for (BitStoreFieldData tmpRecordItemData : this.fields) {
				byte[] tmpBytes = tmpRecordItemData.getBytes();
				int tmpFieldViewOffset = this.isLittleEndianBytes ? 0
						: BitHelper.getBitArrayPadLength(tmpRecordItemData.getViewFieldMap().getBitLength());
				for (BitStoreViewPieceMap tmpViewPiece : tmpRecordItemData.getViewFieldMap().getPieces()) {
					BitHelper.bitArrayCopy(this.isLittleEndianBytes, tmpBytes, tmpFieldViewOffset, tmpResult,
							tmpViewPiece.offset, tmpViewPiece.length);
					tmpFieldViewOffset += tmpViewPiece.length;
				}
			}
			return tmpResult;
		}

		public void setBytes(byte[] bytes)
				throws BitStoreMapException, BitStoreDataException, BitStoreSetupException, BitStoreDeviceException,
				BitStoreSessionException, BitStoreAuthenticationException, BitStoreAccessException {
			if (bytes.length != this.viewRecordMap.getByteLength()) {
				throw new BitStoreDataException("Bytes inválidos para o registro '%s'", this.viewRecordMap.name);
			}
			for (BitStoreItemData tmpViewItemData : this.items) {
				byte[] tmpBytes = new byte[tmpViewItemData.getByteLength()];
				int tmpFieldOffset = (this.isLittleEndianBytes ? 0
						: BitHelper.getBitArrayPadLength(tmpViewItemData.getBitLength()));
				for (BitStoreViewPieceMap tmpViewPiece : tmpViewItemData.viewItemMap.getPieces()) {
					BitHelper.bitArrayCopy(this.isLittleEndianBytes, bytes, tmpViewPiece.offset, tmpBytes,
							tmpFieldOffset, tmpViewPiece.length);
					tmpFieldOffset += tmpViewPiece.length;
				}
				if (tmpViewItemData.getItemType() == BitStoreViewItemMap.ItemType.RUF) {
					for (byte tmpByte : tmpBytes) {
						if (tmpByte != 0) {
							throw new BitStoreDataException("Valor diferente de zero para campo RUF '%s'",
									tmpViewItemData.getName());
						}
					}
				} else {
					tmpViewItemData.setBytes(tmpBytes);
				}
			}
		}

		@Override
		public String toString() {
			return BitStoreHelper.dumpStoreRecordData(this);
		}
	}

	public class BitStoreViewData {

		protected final BitStoreViewMap viewMap;

		protected final BitStoreRecordData[] records;

		protected final BitStoreData parentStore;

		public boolean hiden;

		protected BitStoreViewData(BitStoreViewMap viewMap, BitStoreData parentStore) throws BitStoreMapException {
			this.viewMap = viewMap;
			this.parentStore = parentStore;
			this.records = new BitStoreRecordData[viewMap.getRecords().length];
			int tmpIndex = 0;
			BitStoreSource tmpStoreSource = this.parentStore.getSource();
			for (BitStoreViewRecordMap tmpViewRecord : viewMap.getRecords()) {
				this.records[tmpIndex++] = new BitStoreRecordData(tmpViewRecord,
						tmpStoreSource.getRecord(tmpViewRecord.getName()), this);
			}
			this.hiden = this.viewMap.getName().startsWith("_");
		}

		public String getName() {
			return this.viewMap.name;
		}

		public String getDescription() {
			return this.viewMap.getDescription();
		}

		public int getIndex() {
			return this.viewMap.getIndex();
		}

		public int getBitLength() {
			return this.viewMap.getBitLength();
		}

		public int getByteLength() {
			return this.viewMap.getByteLength();
		}

		public BitStoreData getParentStore() {
			return this.parentStore;
		}

		public BitStoreViewMap getViewMap() {
			return this.viewMap;
		}

		public BitStoreRecordData searchRecord(String recordName) {
			for (BitStoreRecordData tmpRecordData : this.records) {
				if (tmpRecordData.getName().compareTo(recordName) == 0) {
					return tmpRecordData;
				}
			}
			return null;
		}

		public BitStoreRecordData getRecord(int recordIndex) throws BitStoreMapException {
			if ((recordIndex < 0) || (recordIndex >= this.records.length)) {
				throw new BitStoreMapException("Índice de registro '%d' é inválido na visão '%s' do mapa '%s'",
						this.viewMap.name, this.viewMap.getStoreMap().name);
			}
			return this.records[recordIndex];
		}

		public BitStoreRecordData getRecord(String recordName) throws BitStoreMapException {
			BitStoreRecordData tmpResult = this.searchRecord(recordName);
			if (tmpResult == null) {
				throw new BitStoreMapException("Não há registro '%s' na visão '%s' do mapa '%s'", recordName,
						this.viewMap.name, this.viewMap.getStoreMap().name);
			}
			return tmpResult;
		}

		public BitStoreRecordData[] getRecords(boolean includeHiden) {
			if (includeHiden) {
				return Arrays.copyOf(this.records, this.records.length);
			}
			List<BitStoreRecordData> tmpResult = new ArrayList<BitStoreRecordData>();
			for (BitStoreRecordData tmpItem : this.records) {
				if (!tmpItem.hiden) {
					tmpResult.add(tmpItem);
				}
			}
			return tmpResult.toArray(new BitStoreRecordData[0]);
		}

		public BitStoreRecordData[] getRecords() {
			return this.getRecords(false);
		}

		public String[] getRecordNames(boolean includeHiden) {
			List<String> tmpResult = new ArrayList<String>();
			for (BitStoreRecordData tmpRecord : this.getRecords(includeHiden)) {
				tmpResult.add(tmpRecord.getName());
			}
			return tmpResult.toArray(new String[0]);
		}

		public String[] getRecordNames() {
			return this.getRecordNames(false);
		}

		public BitStoreItemData searchItem(String itemPath) throws BitStoreMapException {
			String[] tmpItemPath = itemPath.split("\\.");
			if (tmpItemPath.length != 2) {
				throw new BitStoreMapException("Caminho '%s' inválido para ítem da visão '%s'", itemPath,
						this.getViewMap().getName());
			}
			BitStoreRecordData tmpRecord = this.searchRecord(tmpItemPath[0]);
			if (tmpRecord != null) {
				return tmpRecord.searchItem(tmpItemPath[1]);
			}
			return null;
		}

		public BitStoreItemData getItem(String itemPath) throws BitStoreMapException {
			String[] tmpItemPath = itemPath.split("\\.");
			if (tmpItemPath.length != 2) {
				throw new BitStoreMapException("Caminho '%s' inválido para ítem da view '%s'", itemPath,
						this.viewMap.getName());
			}
			return this.getRecord(tmpItemPath[0]).getField(tmpItemPath[1]);
		}

		@Override
		public String toString() {
			return BitStoreHelper.dumpStoreViewData(this);
		}
	}

	protected final BitStoreMap storeMap;

	protected final BitStoreSource storeSource;

	protected final BitStoreViewData mainView;

	protected final BitStoreViewData[] alternativeViews;

	protected final BitStoreViewData[] views;

	public BitStoreData(BitStoreMap storeMap, BitStoreSource storeSource) throws BitStoreMapException {
		this.storeMap = storeMap;
		this.storeSource = storeSource;
		this.mainView = new BitStoreViewData(this.storeMap.getMainView(), this);
		this.alternativeViews = new BitStoreViewData[this.storeMap.alternativeViews.length];
		for (int tmpIndex = 0; tmpIndex < this.alternativeViews.length; tmpIndex++) {
			this.alternativeViews[tmpIndex] = new BitStoreViewData(this.storeMap.alternativeViews[tmpIndex], this);
		}
		this.views = new BitStoreViewData[1 + this.alternativeViews.length];
		this.views[0] = this.mainView;
		System.arraycopy(this.alternativeViews, 0, this.views, 1, this.alternativeViews.length);
	}

	public BitStoreData(BitStoreMap bitStoreMap) throws BitStoreMapException {
		this(bitStoreMap, new BitStoreMemorySource(bitStoreMap));
	}

	public BitStoreData(BitStoreData sourceBitStoreData)
			throws BitStoreSetupException, BitStoreDeviceException, BitStoreSessionException,
			BitStoreAuthenticationException, BitStoreAccessException, BitStoreMapException, BitStoreDataException {
		this(sourceBitStoreData.storeMap);
		for (BitStoreRecordData tmpSourceRecord : sourceBitStoreData.mainView.records) {
			this.mainView.getRecord(tmpSourceRecord.getIndex()).setBytes(tmpSourceRecord.getBytes());
		}
	}

	public BitStoreMap getMap() {
		return this.storeMap;
	}

	public BitStoreSource getSource() {
		return this.storeSource;
	}

	public BitStoreViewData[] getViews() {
		return Arrays.copyOf(this.views, this.views.length);
	}

	public BitStoreViewData[] getViews(boolean includeHiden) {
		if (includeHiden) {
			return Arrays.copyOf(this.views, this.views.length);
		}
		List<BitStoreViewData> tmpResult = new ArrayList<BitStoreViewData>();
		for (BitStoreViewData tmpItem : this.views) {
			if (!tmpItem.hiden) {
				tmpResult.add(tmpItem);
			}
		}
		return tmpResult.toArray(new BitStoreViewData[0]);
	}

	public BitStoreViewData getMainView() {
		return this.mainView;
	}

	public BitStoreViewData[] getAlternativeViews() {
		return this.getAlternativeViews(false);
	}

	public BitStoreViewData[] getAlternativeViews(boolean includeHiden) {
		if (includeHiden) {
			return Arrays.copyOf(this.alternativeViews, this.alternativeViews.length);
		}
		List<BitStoreViewData> tmpResult = new ArrayList<BitStoreViewData>();
		for (BitStoreViewData tmpItem : this.alternativeViews) {
			if (!tmpItem.hiden) {
				tmpResult.add(tmpItem);
			}
		}
		return tmpResult.toArray(new BitStoreViewData[0]);
	}

	public BitStoreViewData searchView(String viewName) {
		for (BitStoreViewData tmpView : this.views) {
			if (tmpView.getName().compareTo(viewName) == 0) {
				return tmpView;
			}
		}
		return null;
	}

	public BitStoreViewData getView(String viewName) throws BitStoreMapException {
		BitStoreViewData tmpResult = this.searchView(viewName);
		if (tmpResult == null) {
			throw new BitStoreMapException("Nenhuma visão '%s' no mapa '%s'", viewName, this.getMap().name);
		}
		return tmpResult;
	}

	public BitStoreItemData getItem(String itemPath) throws BitStoreMapException {
		String[] tmpItemPath = itemPath.split("\\.");
		if (tmpItemPath.length != 3) {
			throw new BitStoreMapException("Caminho '%s' inválido para ítem do store '%s'", itemPath,
					this.getMap().getName());
		}
		return this.getView(tmpItemPath[0]).getRecord(tmpItemPath[1]).getField(tmpItemPath[2]);
	}

	public BitStoreRecordData getRecord(String recordPath) throws BitStoreMapException {
		String[] tmpItemPath = recordPath.split("\\.");
		if (tmpItemPath.length != 2) {
			throw new BitStoreMapException("Caminho '%s' inválido para registro do store '%s'", recordPath,
					this.getMap().getName());
		}
		return this.getView(tmpItemPath[0]).getRecord(tmpItemPath[1]);
	}

	@Override
	public String toString() {
		return BitStoreHelper.dumpStoreData(this);
	}
}
