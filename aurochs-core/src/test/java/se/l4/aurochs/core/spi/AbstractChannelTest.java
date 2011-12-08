package se.l4.aurochs.core.spi;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import se.l4.aurochs.core.channel.ChannelListener;
import se.l4.aurochs.core.channel.MessageEvent;

/**
 * Tests for {@link AbstractChannel}.
 * 
 * @author Andreas Holstenson
 *
 */
public class AbstractChannelTest
{
	@Test
	public void addListener()
	{
		TestChannel<Object> channel = new TestChannel<Object>();
		
		ChannelListener<Object> listener = mock(ChannelListener.class);
		channel.addListener(listener);
		
		String message = "message";
		channel.send(message);
		
		verify(listener).messageReceived(new MessageEvent<Object>(channel, message));
	}
	
	@Test
	public void removeSingleListener()
	{
		TestChannel<Object> channel = new TestChannel<Object>();
		
		ChannelListener<Object> listener = mock(ChannelListener.class);
		channel.addListener(listener);
		channel.removeListener(listener);
		
		String message = "message";
		channel.send(message);
		
		verify(listener, never()).messageReceived(new MessageEvent<Object>(channel, message));
	}
	
	@Test
	public void removeFirstListener()
	{
		TestChannel<Object> channel = new TestChannel<Object>();
		
		ChannelListener<Object> listener1 = mock(ChannelListener.class);
		channel.addListener(listener1);
		
		ChannelListener<Object> listener2 = mock(ChannelListener.class);
		channel.addListener(listener2);
		
		channel.removeListener(listener1);
		
		String message = "message";
		channel.send(message);
		
		verify(listener1, never()).messageReceived(new MessageEvent<Object>(channel, message));
		verify(listener2).messageReceived(new MessageEvent<Object>(channel, message));
	}
	
	@Test
	public void removeLastListener()
	{
		TestChannel<Object> channel = new TestChannel<Object>();
		
		ChannelListener<Object> listener1 = mock(ChannelListener.class);
		channel.addListener(listener1);
		
		ChannelListener<Object> listener2 = mock(ChannelListener.class);
		channel.addListener(listener2);
		
		channel.removeListener(listener2);
		
		String message = "message";
		channel.send(message);
		
		verify(listener1).messageReceived(new MessageEvent<Object>(channel, message));
		verify(listener2, never()).messageReceived(new MessageEvent<Object>(channel, message));
	}
	
	
	private static class TestChannel<T>
		extends AbstractChannel<T>
	{
		public void send(T message)
		{
			fireMessageReceived(new MessageEvent<T>(this, message));
		}
	}
}
