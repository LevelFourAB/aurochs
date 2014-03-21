package se.l4.aurochs.serialization.spi;

import se.l4.aurochs.serialization.Serializer;

/**
 * A callback for {@link SerializerResolver} that is used when a new resolver
 * is discovered.
 * 
 * @author Andreas Holstenson
 *
 */
public interface NamingCallback
{
	void registerIfNamed(Class<?> from, Serializer<?> serializer);
}
