package se.l4.aurochs.serialization.format;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;

/**
 * Streamer that outputs JSON.
 * 
 * @author andreas
 *
 */
public class JsonOutput
	implements StreamingOutput
{
	private static final int HEX_MASK = (1 << 4) - 1;

	private static final char[] DIGITS = {
		'0', '1', '2', '3', '4', '5', '6', '7',
		'8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
	};
	
	private final static char[] BASE64 = {
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
		'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
		'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
	};
	
	private static final int LEVELS = 20;
	
	protected final Writer writer;
	private final boolean beautify;
	
	private boolean[] lists;
	private boolean[] hasData;
	
	private int level;

	private final char[] encoded;
	
	/**
	 * Create a JSON streamer that will write to the given output.
	 * 
	 * @param out
	 */
	public JsonOutput(OutputStream out)
	{
		this(out, false);
	}
	
	/**
	 * Create a JSON streamer that will write to the given output, optionally
	 * with beautification of the generated JSON.
	 * 
	 * @param out
	 * @param beautify
	 */
	public JsonOutput(OutputStream out, boolean beautify)
	{
		this(new OutputStreamWriter(out), beautify);
	}
	
	/**
	 * Create a JSON streamer that will write to the given output.
	 * 
	 * @param out
	 */
	public JsonOutput(Writer writer)
	{
		this(writer, false);
	}
	
	/**
	 * Create a JSON streamer that will write to the given output, optionally
	 * with beautification of the generated JSON.
	 * 
	 * @param out
	 * @param beautify
	 */
	public JsonOutput(Writer writer, boolean beautify)
	{
		this.writer = writer;
		this.beautify = beautify;
		
		lists = new boolean[LEVELS];
		hasData = new boolean[LEVELS];
		
		encoded = new char[4];
	}
	
	/**
	 * Escape and write the given string.
	 * 
	 * @param in
	 * @throws IOException
	 */
	private void writeEscaped(String in)
		throws IOException
	{
		for(int i=0, n=in.length(); i<n; i++)
		{
			char c = in.charAt(i);
			if(c == '"' || c == '\\')
			{
				writer.write('\\');
				writer.write(c);
			}
			else if(c == '\r')
			{
				writer.write('\\');
				writer.write('r');
			}
			else if(c == '\n')
			{
				writer.write('\\');
				writer.write('n');
			}
			else if(c == '\t')
			{
				writer.write('\\');
				writer.write('t');
			}
			else if(c == '\b')
			{
				writer.write('\\');
				writer.write('b');
			}
			else if(c == '\f')
			{
				writer.write('\\');
				writer.write('f');
			}
			else if(c <= 0x1F)
			{
				writer.write('\\');
				writer.write('u');
				
				int v = c;
				int pos = 4;
				do
				{
					encoded[--pos] = DIGITS[v & HEX_MASK];
					v >>>= 4;
				}
				while (v != 0);
				
				for(int j=0; j<pos; j++) writer.write('0');
				writer.write(encoded, pos, 4 - pos);
			}
			else
			{
				writer.write(c);
			}
		}
	}

	/**
	 * Increase the level by one.
	 * 
	 * @param list
	 */
	private void increaseLevel(boolean list)
	{
		level++;
		
		if(hasData.length == level)
		{
			// Grow lists when needed
			hasData = Arrays.copyOf(hasData, hasData.length * 2);
			lists = Arrays.copyOf(lists, hasData.length * 2);
		}
		
		hasData[level] = false;
		lists[level] = list;
	}
	
	/**
	 * Decrease the level by one.
	 * 
	 * @throws IOException
	 */
	private void decreaseLevel()
		throws IOException
	{
		level--;
		
		if(beautify && hasData[level])
		{
			writer.write('\n');
			
			for(int i=0; i<level; i++)
			{
				writer.write("\t");
			}
		}
	}

	/**
	 * Start a write, will output commas and beautification if needed.
	 * 
	 * @throws IOException
	 */
	private void startWrite()
		throws IOException
	{
		if(hasData[level]) writer.write(',');
		
		hasData[level] = true;
		
		if(beautify && level > 0)
		{
			writer.write('\n');
			
			for(int i=0; i<level; i++)
			{
				writer.write('\t');
			}
		}
	}
	
	/**
	 * Check if the name should be written or not.
	 * 
	 * @return
	 */
	private boolean shouldOutputName()
	{
		return level != 0 && ! lists[level];
	}
	
	@Override
	public void writeObjectStart(String name)
		throws IOException
	{
		startWrite();
		
		if(shouldOutputName())
		{
			writer.write('"');
			writeEscaped(name);
//			writer.write("\": {");
			writer.write('"');
			writer.write(':');
		}
		
		writer.write('{');
		
		increaseLevel(false);
	}
	
	@Override
	public void writeObjectEnd(String name)
		throws IOException
	{
		decreaseLevel();
		
		writer.write('}');
	}
	
	@Override
	public void writeListStart(String name)
		throws IOException
	{
		startWrite();
		
		if(shouldOutputName())
		{
			writer.write('"');
			writeEscaped(name);
//			writer.write("\": [");
			writer.write('"');
			writer.write(':');
		}
		
		writer.write('[');
		
		increaseLevel(true);
	}
	
	@Override
	public void writeListEnd(String name)
		throws IOException
	{
		decreaseLevel();
		writer.write(']');
	}
	
	@Override
	public void write(String name, String value)
		throws IOException
	{
		startWrite();
		
		if(shouldOutputName())
		{
			writer.write('"');
			writeEscaped(name);
//			writer.write("\": ");
			writer.write('"');
			writer.write(':');
		}
		
		if(value == null)
		{
			writer.write("null");
		}
		else
		{
			writer.write('"');
			writeEscaped(value);
			writer.write('"');
		}
	}
	
	private void writeUnescaped(String name, String value)
		throws IOException
	{
		startWrite();
		
		if(shouldOutputName())
		{
			writer.write('"');
			writeEscaped(name);
//			writer.write("\": ");
			writer.write('"');
			writer.write(':');
		}
		
		if(value == null)
		{
			writer.write("null");
		}
		else
		{
			writer.write(value);
		}
	}
	
	@Override
	public void write(String name, int number)
		throws IOException
	{
		writeUnescaped(name, Integer.toString(number));	
	}
	
	@Override
	public void write(String name, long number)
		throws IOException
	{
		writeUnescaped(name, Long.toString(number));	
	}
	
	@Override
	public void write(String name, float number)
		throws IOException
	{
		writeUnescaped(name, Float.toString(number));
	}
	
	@Override
	public void write(String name, double number)
		throws IOException
	{
		writeUnescaped(name, Double.toString(number));
	}
	
	@Override
	public void write(String name, boolean bool)
		throws IOException
	{
		writeUnescaped(name, Boolean.toString(bool));
	}
	
	@Override
	public void write(String name, byte[] data)
		throws IOException
	{
		startWrite();
		
		if(shouldOutputName())
		{
			writer.write('"');
			writeEscaped(name);
			writer.write('"');
			writer.write(':');
		}
		
		if(data == null)
		{
			writer.write("null");
			return;
		}
		
		writer.write('"');

		int i = 0;
		for(int n=data.length - 2; i<n; i+=3)
		{
			write(data, i, 3);
		}

		if(i < data.length)
		{
			write(data, i, data.length - i);
		}
		
		writer.write('"');
	}
	
	/**
	 * Write some BASE64 encoded bytes.
	 * 
	 * @param data
	 * @param pos
	 * @param chars
	 * @param len
	 * @throws IOException
	 */
	private void write(byte[] data, int pos, int len)
		throws IOException
	{
		char[] chars = BASE64;
		
		int loc = (len > 0 ? (data[pos] << 24) >>> 8 : 0) |
			(len > 1 ? (data[pos+1] << 24) >>> 16 : 0) |
			(len > 2 ? (data[pos+2] << 24) >>> 24 : 0);
		
		switch(len)
		{
			case 3:
				writer.write(chars[loc >>> 18]);
				writer.write(chars[(loc >>> 12) & 0x3f]);
				writer.write(chars[(loc >>> 6) & 0x3f]);
				writer.write(chars[loc & 0x3f]);
				break;
			case 2:
				writer.write(chars[loc >>> 18]);
				writer.write(chars[(loc >>> 12) & 0x3f]);
				writer.write(chars[(loc >>> 6) & 0x3f]);
				writer.write('=');
				break;
			case 1:
				writer.write(chars[loc >>> 18]);
				writer.write(chars[(loc >>> 12) & 0x3f]);
				writer.write('=');
				writer.write('=');
		}
	}
	
	@Override
	public void writeNull(String name)
		throws IOException
	{
		write(name, (String) null);
	}
	
	@Override
	public void flush() throws IOException
	{
		writer.flush();
	}
}