package se.l4.aurochs.serialization.enums;

import se.l4.aurochs.serialization.format.ValueType;

/**
 * {@link ValueTranslator} that uses the {@link Enum#name() name} of the enum.
 * 
 * @author Andreas Holstenson
 *
 */
public class NameTranslator
	implements ValueTranslator<String>
{
	private final Enum<?>[] values;

	public NameTranslator(Class<? extends Enum<?>> type)
	{
		values = type.getEnumConstants();
	}
	
	@Override
	public ValueType getType()
	{
		return ValueType.STRING;
	}
	
	@Override
	public String fromEnum(Enum<?> value)
	{
		return value.name();
	}
	
	@Override
	public Enum<?> toEnum(String value)
	{
		for(Enum<?> e : values)
		{
			if(e.name().equals(value))
			{
				return e;
			}
		}
		
		return null;
	}

}
