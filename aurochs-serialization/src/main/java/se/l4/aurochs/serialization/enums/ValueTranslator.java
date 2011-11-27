package se.l4.aurochs.serialization.enums;

import se.l4.aurochs.serialization.format.ValueType;

/**
 * Translator between {@link Enum} and a serialized value.
 * 
 * @author Andreas Holstenson
 *
 * @param <Type>
 */
public interface ValueTranslator<Type>
{
	/**
	 * Get the type to read and write.
	 * 
	 * @return
	 */
	ValueType getType();
	
	/**
	 * Translate an enum to its serialized value.
	 * 
	 * @param value
	 * @return
	 */
	Type fromEnum(Enum<?> value);
	
	/**
	 * Translate a serialized value to an enum value.
	 * 
	 * @param value
	 * @return
	 */
	Enum<?> toEnum(Type value);
}
