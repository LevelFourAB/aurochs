package se.l4.aurochs.config.internal.streaming;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;

import javax.xml.bind.DatatypeConverter;

import se.l4.aurochs.serialization.format.JsonInput;
import se.l4.aurochs.serialization.format.StreamingInput;

/**
 * Input for JSON. Based of {@link JsonInput} but supports things as skipping
 * quotes for keys and values.
 * 
 * @author Andreas Holstenson
 *
 */
public class ConfigJsonInput
	implements StreamingInput
{
	private static final char NULL = 0;

	private final Reader in;
	
	private final char[] buffer;
	private int position;
	private int limit;
	
	private final boolean[] lists;
	private int level;
	
	private Token token;
	private Object value;
	
	public ConfigJsonInput(Reader in)
	{
		this.in = in;
		
		lists = new boolean[10];
		buffer = new char[1024];
	}
	
	private void readWhitespace()
		throws IOException
	{
		while(true)
		{
			if(limit - position < 1)
			{
				if(! read(1))
				{
					return;
				}
			}
			
			char c = buffer[position];
			if(Character.isWhitespace(c) || c == ',')
			{
				position++;
			}
			else if(c == '#')
			{
				// Comment
				readUntilEndOfLine();
			}
			else
			{
				return;
			}
		}
	}
	
	private void readUntilEndOfLine()
		throws IOException
	{
		while(true)
		{
			if(limit - position < 1)
			{
				if(! read(1))
				{
					return;
				}
			}
			
			char c = buffer[position];
			if(c == '\n' || c == '\r')
			{
				return;
			}
			else
			{
				position++;
			}
		}
	}
	
	private char readNext()
		throws IOException
	{
		readWhitespace();
		
		return read();
	}
	
	private char read()
		throws IOException
	{
		if(limit - position < 1)
		{
			if(! read(1))
			{
				throw new EOFException();
			}
		}
		
		return buffer[position++];
	}
	
	private boolean read(int minChars)
		throws IOException
	{
		if(limit < 0)
		{
			return false;
		}
		else if(position + minChars < limit)
		{
			return true;
		}
		else if(limit >= position)
		{
			// If we have characters left we need to keep them in the buffer
			int stop = limit - position;
			System.arraycopy(buffer, position, buffer, 0, stop);
			limit = stop;
		}
		
		int read = in.read(buffer, limit, buffer.length - limit);
		position = 0;
		limit = read;
		
		if(read == -1)
		{
			return false;
		}
		
		if(read < minChars)
		{
			throw new IOException("Needed " + minChars + " but got " + read);
		}
		
		return true;
	}
	
	private Token toToken(char c)
	{
		switch(c)
		{
			case '{':
				return Token.OBJECT_START;
			case '}':
				return Token.OBJECT_END;
			case '[':
				return Token.LIST_START;
			case ']':
				return Token.LIST_END;
		}
		
		if(token != Token.KEY && ! lists[level])
		{
			return Token.KEY;
		}
		
		return Token.VALUE;
	}
	
	private Object readNextValue()
		throws IOException
	{
		char c = readNext();
		if(c == '"')
		{
			// This is a string
			return readString(false);
		}
		else
		{
			StringBuilder value = new StringBuilder();
			_outer:
			while(true)
			{
				value.append(c);
				
				c = peekChar(false);
				switch(c)
				{
					case '}':
					case ']':
					case ',':
					case ':':
					case '=':
					case '\n':
					case '\r':
					case NULL: // EOF
						break _outer;
//					default:
//						if(Character.isWhitespace(c)) break _outer;
				}
				
				read();
			}
			
			return toObject(value.toString().trim());
		}
	}
	
	private Object toObject(String in)
	{
		if(in.equals("false"))
		{
			return false;
		}
		else if(in.equals("true"))
		{
			return true;
		}
		
		try
		{
			return Long.parseLong(in);
		}
		catch(NumberFormatException e)
		{
			try
			{
				return Double.parseDouble(in);
			}
			catch(NumberFormatException e2)
			{
			}
		}
		
		return in;
	}
	
	private String readString(boolean readStart)
		throws IOException
	{
		StringBuilder key = new StringBuilder();
		char c = read();
		if(readStart)
		{
			if(c != '"') throw new IOException("Expected \", but got " + c);
			c = read();
		}
		
		while(c != '"')
		{
			if(c == '\\')
			{
				readEscaped(key);
			}
			else
			{
				key.append(c);
			}
			
			c = read();
		}
		
		return key.toString();
	}
	
	private String readKey()
		throws IOException
	{
		StringBuilder key = new StringBuilder();
		char c = read();
		while(c != ':' && c != '=')
		{
			if(c == '\\')
			{
				readEscaped(key);
			}
			else if(! Character.isWhitespace(c))
			{
				key.append(c);
			}
			
			c = read();
		}
		
		return key.toString();
	}

	private void readEscaped(StringBuilder result)
		throws IOException
	{
		char c = read();
		switch(c)
		{
			case '\'':
				result.append('\'');
				break;
			case '"':
				result.append('"');
				break;
			case '\\':
				result.append('\\');
				break;
			case '/':
				result.append('/');
				break;
			case 'r':
				result.append('\r');
				break;
			case 'n':
				result.append('\n');
				break;
			case 't':
				result.append('\t');
				break;
			case 'b':
				result.append('\b');
				break;
			case 'f':
				result.append('\f');
				break;
			case 'u':
				// Unicode, read 4 chars and treat as hex
				read(4);
				String s = new String(buffer, position, 4);
				result.append((char) Integer.parseInt(s, 16));
				position += 4;
				break;
		}
	}

	@Override
	public Token next(Token expected)
		throws IOException
	{
		Token t = next();
		if(t != expected)
		{
			throw new IOException("Expected "+ expected + " but got " + t);
		}
		return t;
	}
	
	@Override
	public Token next()
		throws IOException
	{
		char peeked = peekChar();
		
		Token token = toToken(peeked);
		switch(token)
		{
			case OBJECT_END:
			case LIST_END:
				readNext();
				level--;
				return this.token = token; 
			case OBJECT_START:
			case LIST_START:
				readNext();
				level++;
				lists[level] = token == Token.LIST_START;
				return this.token = token;
			case KEY:
			{
				readWhitespace();
				String key;
				if(peeked == '"')
				{
					key = readString(true);
					char next = readNext();
					if(next != ':' && next != '=')
					{
						throw new IOException("Expected :, got " + next);
					}
				}
				else
				{
					// Case where keys do not include quotes
					key = readKey();
				}
				
				value = key;
				return this.token = token;
			}
			case VALUE:
			{
				value = readNextValue();
				
				// Check for trailing commas
				readWhitespace();
				char c = peekChar();
				if(c == ',') read();
				
				return this.token = token;
			}
		}
		
		return null;
	}
	
	private char peekChar()
		throws IOException
	{
		return peekChar(true);
	}
	
	private char peekChar(boolean ws)
		throws IOException
	{
		if(ws) readWhitespace();
		
		if(limit - position < 1)
		{
			if(false == read(1))
			{
				return NULL;
			}
		}
		
		if(limit - position > 0)
		{
			return buffer[position];
		}
		
		return NULL;
	}
	
	@Override
	public Token peek()
		throws IOException
	{
		readWhitespace();
		
		if(limit - position < 1)
		{
			if(false == read(1)) return null;
		}
		
		if(limit - position > 0)
		{
			return toToken(buffer[position]);
		}
		
		return null;
	}
	
	@Override
	public void skipValue()
		throws IOException
	{
		if(token != Token.KEY)
		{
			throw new IOException("Value skipping can only be used with when token is " + Token.KEY);
		}
		
		switch(peek())
		{
			case LIST_START:
			case LIST_END:
			case OBJECT_START:
			case OBJECT_END:
				next();
				skip();
				break;
			default:
				next();
		}
	}
	
	@Override
	public void skip()
		throws IOException
	{
		Token stop;
		switch(token)
		{
			case LIST_START:
				stop = Token.LIST_END;
				break;
			case OBJECT_START:
				stop = Token.OBJECT_END;
				break;
			default:
				throw new IOException("Can only skip when start of object or list, token is now " + token);
		}
		
		int currentLevel = level;
		
		Token next = peek();
		while(true)
		{
			// Loop until no more tokens or if we stopped and the level has been reset
			if(next == null)
			{
				throw new IOException("No more tokens, but end of skipped value not found");
			}
			else if(next == stop && level == currentLevel)
			{
				// Consume this last token
				next();
				break;
			}

			// Read peeked value and peek for next one
			next();
			next = peek(); 
		}
	}
	
	@Override
	public Token current()
	{
		return token;
	}
	
	@Override
	public Object getValue()
	{
		return value;
	}
	
	@Override
	public String getString()
	{
		return String.valueOf(value);
	}

	@Override
	public boolean getBoolean()
	{
		return (Boolean) value;
	}
	
	@Override
	public double getDouble()
	{
		return ((Number) value).doubleValue();
	}
	
	@Override
	public float getFloat()
	{
		return ((Number) value).floatValue();
	}
	
	@Override
	public long getLong()
	{
		return ((Number) value).longValue();
	}
	
	@Override
	public int getInt()
	{
		return ((Number) value).intValue();
	}
	
	@Override
	public short getShort()
	{
		return ((Number) value).shortValue();
	}
	
	@Override
	public byte[] getByteArray()
	{
		/*
		 * JSON uses Base64 strings, so we need to decode on demand.
		 */
		String value = getString();
		return DatatypeConverter.parseBase64Binary(value);
	}
}