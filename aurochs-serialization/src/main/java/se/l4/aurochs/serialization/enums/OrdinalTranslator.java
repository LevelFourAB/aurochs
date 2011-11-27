package se.l4.aurochs.serialization.enums;

import se.l4.aurochs.serialization.format.ValueType;

/**
 * Translator that will use the {@link Enum#ordinal()} of an enum value in.
 * 
 * @author Andreas Holstenson
 *
 */
public class OrdinalTranslator
	implements ValueTranslator<Integer>
{
	private final Enum<?>[] values;

	public OrdinalTranslator(Class<? extends Enum<?>> type)
	{
		values = type.getEnumConstants();
	}
	
	@Override
	public ValueType getType()
	{
		return ValueType.INTEGER;
	}
	
	@Override
	public Integer fromEnum(Enum<?> value)
	{
		return value.ordinal();
	}
	
	@Override
	public Enum<?> toEnum(Integer value)
	{
		int v = value.intValue();
		
		if(v >= 0 && v < values.length)
		{
			return values[v];
		}
		
		return null;
	}
}
