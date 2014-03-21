package se.l4.aurochs.serialization.internal;

import java.util.List;

import se.l4.aurochs.serialization.spi.Type;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeBindings;

/**
 * Implementation of {@link Type} that uses {@link ResolvedType}.
 * 
 * @author Andreas Holstenson
 *
 */
public class TypeViaResolvedType
	implements Type
{
	private final ResolvedType rt;
	private final TypeBindings bindings;

	public TypeViaResolvedType(ResolvedType rt)
	{
		this.rt = rt;
		bindings = rt.getTypeBindings();
	}
	
	public ResolvedType getResolvedType()
	{
		return rt;
	}

	@Override
	public Class<?> getErasedType()
	{
		return rt.getErasedType();
	}

	@Override
	public Type[] getParameters()
	{
		List<ResolvedType> types = bindings.getTypeParameters();
		Type[] result = new Type[types.size()];
		for(int i=0, n=types.size(); i<n; i++)
		{
			result[i] = new TypeViaResolvedType(types.get(i));
		}
		
		return result;
	}
	
	@Override
	public String toString()
	{
		return rt.getBriefDescription();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rt == null) ? 0 : rt.hashCode());
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
		TypeViaResolvedType other = (TypeViaResolvedType) obj;
		if(rt == null)
		{
			if(other.rt != null)
				return false;
		}
		else if(!rt.equals(other.rt))
			return false;
		return true;
	}
}
