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
	private static final Type[] EMPTY_TYPE_ARRAY = new Type[0];
	
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
		return EMPTY_TYPE_ARRAY;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		TypeViaClass other = (TypeViaClass) obj;
		if(type == null)
		{
			if(other.type != null)
				return false;
		}
		else if(!type.equals(other.type))
			return false;
		return true;
	}
	
	@Override
	public String toString()
	{
		return type.toString();
	}
}
