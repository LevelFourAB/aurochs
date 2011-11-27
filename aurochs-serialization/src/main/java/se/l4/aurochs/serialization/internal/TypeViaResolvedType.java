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
}
