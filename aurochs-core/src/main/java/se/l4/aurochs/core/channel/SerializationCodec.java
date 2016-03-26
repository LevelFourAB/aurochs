package se.l4.aurochs.core.channel;

import java.io.IOException;
import java.io.InputStream;

import com.google.common.base.Throwables;

import se.l4.aurochs.core.io.Bytes;
import se.l4.commons.serialization.Serializer;
import se.l4.commons.serialization.SerializerCollection;
import se.l4.commons.serialization.format.BinaryInput;
import se.l4.commons.serialization.standard.CompactDynamicSerializer;

public class SerializationCodec<T>
	implements ChannelCodec<Bytes, T>
{
	private final Serializer<T> serializer;

	public SerializationCodec(Serializer<T> serializer)
	{
		this.serializer = serializer;
	}
	
	public static SerializationCodec<Object> newDynamicCodec(SerializerCollection serializers)
	{
		return new SerializationCodec<>(new CompactDynamicSerializer(serializers));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> SerializationCodec<T> newDynamicCodec(SerializerCollection serializers, Class<T> type)
	{
		return new SerializationCodec(new CompactDynamicSerializer(serializers));
	}
	
	@Override
	public boolean accepts(Bytes in)
	{
		return true;
	}
	
	@Override
	public T fromSource(Bytes object)
	{
		try(InputStream in = object.asInputStream())
		{
			BinaryInput streaming = new BinaryInput(in);
			return serializer.read(streaming);
		}
		catch(IOException e)
		{
			throw Throwables.propagate(e);
		}
	}
	
	@Override
	public Bytes toSource(T object)
	{
		return Bytes.create(serializer.toBytes(object));
	}
}
