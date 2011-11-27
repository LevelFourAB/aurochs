package se.l4.aurochs.serialization.spi;

import se.l4.aurochs.serialization.SerializationException;

/**
 * Default implementation of {@link InstanceFactory}.
 * 
 * @author Andreas Holstenson
 *
 */
public class DefaultInstanceFactory
	implements InstanceFactory
{

	@Override
	public <T> T create(Class<T> type)
	{
		try
		{
			return type.newInstance();
		}
		catch(InstantiationException e)
		{
			throw new SerializationException("Unable to create; " + e.getCause().getMessage(), e.getCause());
		}
		catch(IllegalAccessException e)
		{
			throw new SerializationException("Unable to create; " + e.getMessage(), e);
		}
	}

}
