package se.l4.aurochs.config.internal.streaming;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import se.l4.aurochs.serialization.format.AbstractStreamingInput;
import se.l4.aurochs.serialization.format.StreamingInput;

/**
 * Implementation of {@link StreamingInput} that works on a objects.
 * 
 * @author Andreas Holstenson
 *
 */
public class MapInput
	extends AbstractStreamingInput
{
	private enum State
	{
		START,
		KEY,
		VALUE,
		END,
		DONE
	}
	
	private State state;
	private State previousState;
	private Token token;
	
	private Map.Entry<String, Object> entry;
	private Iterator<Map.Entry<String, Object>> iterator;
	
	private StreamingInput subInput;
	private String key;

	public MapInput(String key, Map<String, Object> root)
	{
		this.key = key;
		state = State.START;
		
		iterator = root.entrySet().iterator();
	}
	
	public static StreamingInput resolveInput(String key, Object data)
	{
		if(data instanceof Map)
		{
			return new MapInput(key, (Map) data);
		}
		else if(data instanceof List)
		{
			return new ListInput(key, (List) data);
		}
		else if(data == null)
		{
			return new NullInput(key);
		}
		else
		{
			return new ValueInput(key, data);
		}
	}
	
	private StreamingInput resolveInput()
	{
		String newKey = key.isEmpty() ? entry.getKey() : key + '.' + entry.getKey();
		return resolveInput(newKey, entry.getValue());
	}
	
	@Override
	protected IOException raiseException(String message)
	{
		return new IOException(key + ": " + message);
	}

	@Override
	public Token peek()
		throws IOException
	{
		switch(state)
		{
			case START:
				return Token.OBJECT_START;
			case KEY:
				return Token.KEY;
			case VALUE:
				Token peeked = subInput.peek();
				if(peeked != null)
				{
					return peeked;
				}
				else
				{
					advancePosition();
					return peek();
				}
			case END:
				return Token.OBJECT_END;
		}
		
		return null;
	}

	@Override
	public Token next0()
		throws IOException
	{
		switch(state)
		{
			case START:
				// Check what the next state should be
				advancePosition();
				return token = Token.OBJECT_START;
			case KEY:
				setState(State.VALUE);
				subInput = resolveInput();
				return token = Token.KEY;
			case VALUE:
				/*
				 * Value state, check the sub input until it returns null
				 */
				Token t = subInput.next();
				if(t == null)
				{
					// Nothing left in the value, advance and check again
					advancePosition();
					return next();
				}
				
				setState(State.VALUE);
				return token = t;
			case END:
				setState(State.DONE);
				return token = Token.OBJECT_END;
		}
		
		return token = null;
	}
	
	private void setState(State state)
	{
		previousState = this.state;
		this.state = state;
	}

	private void advancePosition()
	{
		if(iterator.hasNext())
		{
			entry = iterator.next();
			setState(State.KEY);
		}
		else
		{
			setState(State.END);
		}
	}

	@Override
	public Token current()
	{
		return subInput != null ? subInput.current() : token;
	}

	@Override
	public Object getValue()
	{
		switch(previousState)
		{
			case KEY:
				return entry.getKey();
			case VALUE:
				return subInput.getValue();
		}
		
		return null;
	}

	@Override
	public String getString()
	{
		switch(previousState)
		{
			case KEY:
				return entry.getKey();
			case VALUE:
				return subInput.getString();
		}
		
		return null;
	}

	@Override
	public boolean getBoolean()
	{
		switch(previousState)
		{
			case VALUE:
				return subInput.getBoolean();
		}
		
		return false;
	}

	@Override
	public double getDouble()
	{
		switch(previousState)
		{
			case VALUE:
				return subInput.getDouble();
		}
		
		return 0;
	}

	@Override
	public float getFloat()
	{
		switch(previousState)
		{
			case VALUE:
				return subInput.getFloat();
		}
		
		return 0;
	}

	@Override
	public long getLong()
	{
		switch(previousState)
		{
			case VALUE:
				return subInput.getLong();
		}
		
		return 0;
	}

	@Override
	public int getInt()
	{
		switch(previousState)
		{
			case VALUE:
				return subInput.getInt();
		}
		
		return 0;
	}

	@Override
	public short getShort()
	{
		switch(previousState)
		{
			case VALUE:
				return subInput.getShort();
		}
		
		return 0;
	}

}
