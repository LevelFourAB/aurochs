package se.l4.aurochs.config;

import java.io.IOException;

import se.l4.aurochs.serialization.Expose;
import se.l4.aurochs.serialization.ReflectionSerializer;
import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.SerializerFormatDefinition;
import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.format.StreamingOutput;
import se.l4.aurochs.serialization.format.ValueType;
import se.l4.aurochs.serialization.format.StreamingInput.Token;

/**
 * A configuration key, represents the path of the config object that has been
 * deserialized. Used to resolve further configuration values.
 * 
 * <p>
 * Use this to deserialize any field named {@code config:key} via {@link Config}.
 * 
 * <p>
 * Example use with {@link Expose} and {@link ReflectionSerializer}:
 * <pre>
 * @Expose(ConfigKey.NAME)
 * private ConfigKey configKey;
 * </pre>
 * 
 * @author Andreas Holstenson
 *
 */
public class ConfigKey
{
	public static final String NAME = "config:key";
	
	private final Config config;
	private final String key;
	
	private ConfigKey(Config config, String key)
	{
		this.config = config;
		this.key = key;
	}
	
	/**
	 * Get the value of a sub path to this key.
	 * 
	 * @param subPath
	 * @param type
	 * @return
	 */
	public <T> Value<T> get(String subPath, Class<T> type)
	{
		return config.get(key + '.' + subPath, type);
	}
	
	/**
	 * Get the value of a sub path to this key.
	 * 
	 * @param subPath
	 * @param type
	 * @return
	 */
	public <T> T asObject(String subPath, Class<T> type)
	{
		return config.asObject(key + '.' + subPath, type);
	}
	
	/**
	 * Get this object as another type.
	 * 
	 * @param type
	 * @return
	 */
	public <T> T asObject(Class<T> type)
	{
		return config.asObject(key, type);
	}
	

	public static class ConfigKeySerializer
		implements Serializer<ConfigKey>
	{
		private final Config config;

		public ConfigKeySerializer(Config config)
		{
			this.config = config;
		}
		
		@Override
		public ConfigKey read(StreamingInput in)
			throws IOException
		{
			in.next(Token.VALUE);
			return new ConfigKey(config, in.getString());
		}
		
		@Override
		public void write(ConfigKey object, String name, StreamingOutput stream)
			throws IOException
		{
			// Ignore this, keys should never be written
		}
		
		@Override
		public SerializerFormatDefinition getFormatDefinition()
		{
			return SerializerFormatDefinition.forValue(ValueType.STRING);
		}
	}
}
