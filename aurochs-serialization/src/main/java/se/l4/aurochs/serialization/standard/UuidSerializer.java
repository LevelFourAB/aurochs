package se.l4.aurochs.serialization.standard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import se.l4.aurochs.serialization.DefaultSerializerCollection;
import se.l4.aurochs.serialization.Expose;
import se.l4.aurochs.serialization.ReflectionSerializer;
import se.l4.aurochs.serialization.Serializer;
import se.l4.aurochs.serialization.SerializerCollection;
import se.l4.aurochs.serialization.Use;
import se.l4.aurochs.serialization.format.BinaryInput;
import se.l4.aurochs.serialization.format.BinaryOutput;
import se.l4.aurochs.serialization.format.StreamingInput;
import se.l4.aurochs.serialization.format.StreamingInput.Token;
import se.l4.aurochs.serialization.format.StreamingOutput;

/**
 * Serializer for {@link UUID} that transforms into a byte array.
 * 
 * @author Andreas Holstenson
 *
 */
public class UuidSerializer
	implements Serializer<UUID>
{

	@Override
	public UUID read(StreamingInput in) throws IOException
	{
		in.next(Token.VALUE);
		byte[] bytes = in.getByteArray();
		
		long msb = 0;
		long lsb = 0;
		for(int i=0; i<8; i++)
		{
			msb = (msb << 8) | (bytes[i] & 0xff);
			lsb = (lsb << 8) | (bytes[8 + i] & 0xff);
		}
		
		return new UUID(msb, lsb);
	}

	@Override
	public void write(UUID object, String name, StreamingOutput stream)
		throws IOException
	{
		long msb = object.getMostSignificantBits();
		long lsb = object.getLeastSignificantBits();
		
		byte[] buffer = new byte[16];
		for(int i=0; i<8; i++)
		{
			buffer[i] = (byte) (msb >>> 8 * (7 - i));
			buffer[8+i] = (byte) (lsb >>> 8 * (7 - i));
		}
		
		stream.write(name, buffer);
	}
	
	@Use(ReflectionSerializer.class)
	public static class UuidClass
	{
		@Expose
		@Use(UuidSerializer.class)
		private UUID id;
	}

	public static void main(String[] args)
		throws Exception
	{
		SerializerCollection col = new DefaultSerializerCollection();
		
		UUID uuid = UUID.randomUUID();
		System.out.println(uuid);
		
		UuidClass uc = new UuidClass();
		uc.id = uuid;
		
		Serializer<UuidClass> serializer = col.find(UuidClass.class);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		StreamingOutput output = new BinaryOutput(out);
		serializer.write(uc, "", output);
		output.flush();
		byte[] data = out.toByteArray();
		
		UuidClass read = serializer.read(new BinaryInput(new ByteArrayInputStream(data)));
		System.out.println(read.id);
	}
}
