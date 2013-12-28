package se.l4.aurochs.config.internal.streaming;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import se.l4.aurochs.serialization.format.AbstractStreamingInput;
import se.l4.aurochs.serialization.format.StreamingInput;

/**
 * Input that works on lists.
 * 
 * @author Andreas Holstenson
 *
 */
public class ListInput
	extends AbstractStreamingInput
{
	private enum State
	{
		START,
		VALUE,
		END,
		DONE
	}
	
	private State state;
	private State previousState;
	
	private Iterator<Object> iterator;
	private Object object;
	
	private StreamingInput subInput;
	private String key;

	public ListInput(String key, List<Object> root)
	{
		this.key = key;
		state = State.START;
		
		iterator = root.iterator();
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
				return Token.LIST_START;
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
				return Token.LIST_END;
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
				return Token.LIST_START;
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
				return t;
			case END:
				setState(State.DONE);
				return Token.LIST_END;
		}
		
		return null;
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
			object = iterator.next();
			subInput = MapInput.resolveInput(key, object);
			setState(State.VALUE);
		}
		else
		{
			setState(State.END);
		}
	}

	@Override
	public Token current()
	{
		return subInput != null ? subInput.current() : super.current();
	}

	@Override
	public Object getValue()
	{
		switch(previousState)
		{
			case VALUE:
				return subInput.getValue();
		}
		
		return null;
	}

	@Override
	public String getString()
	{
		switch(state)
		{
			case VALUE:
				return subInput.getString();
		}
		
		return null;
	}

	@Override
	public boolean getBoolean()
	{
		switch(state)
		{
			case VALUE:
				return subInput.getBoolean();
		}
		
		return false;
	}

	@Override
	public double getDouble()
	{
		switch(state)
		{
			case VALUE:
				return subInput.getDouble();
		}
		
		return 0;
	}

	@Override
	public float getFloat()
	{
		switch(state)
		{
			case VALUE:
				return subInput.getFloat();
		}
		
		return 0;
	}

	@Override
	public long getLong()
	{
		switch(state)
		{
			case VALUE:
				return subInput.getLong();
		}
		
		return 0;
	}

	@Override
	public int getInt()
	{
		switch(state)
		{
			case VALUE:
				return subInput.getInt();
		}
		
		return 0;
	}

	@Override
	public short getShort()
	{
		switch(state)
		{
			case VALUE:
				return subInput.getShort();
		}
		
		return 0;
	}

}
