package se.l4.aurochs.serialization.spi;

/**
 * Implementation of {@link TypeViaClass} that wraps a {@link Class}.
 * 
 * @author Andreas Holstenson
 *
 */
public class TypeViaClass
	implements Type
{
	private final Class<?> type;

	public TypeViaClass(Class<?> type)
	{
		this.type = type;
	}
	
	@Override
	public Class<?> getErasedType()
	{
		return type;
	}
	
	@Override
	public Type[] getParameters()
	{
		return new Type[0];
	}
}
