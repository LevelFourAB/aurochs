package se.l4.aurochs.channels;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import se.l4.aurochs.channels.MessageListener;
import se.l4.aurochs.channels.MessageEvent;

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
		
		MessageListener<Object> listener = mock(MessageListener.class);
		channel.addMessageListener(listener);
		
		String message = "message";
		channel.send(message);
		
		verify(listener).messageReceived(new MessageEvent<Object>(channel, channel, message));
	}
	
	@Test
	public void removeSingleListener()
	{
		TestChannel<Object> channel = new TestChannel<Object>();
		
		MessageListener<Object> listener = mock(MessageListener.class);
		channel.addMessageListener(listener);
		channel.removeMessageListener(listener);
		
		String message = "message";
		channel.send(message);
		
		verify(listener, never()).messageReceived(new MessageEvent<Object>(channel, channel, message));
	}
	
	@Test
	public void removeFirstListener()
	{
		TestChannel<Object> channel = new TestChannel<Object>();
		
		MessageListener<Object> listener1 = mock(MessageListener.class);
		channel.addMessageListener(listener1);
		
		MessageListener<Object> listener2 = mock(MessageListener.class);
		channel.addMessageListener(listener2);
		
		channel.removeMessageListener(listener1);
		
		String message = "message";
		channel.send(message);
		
		verify(listener1, never()).messageReceived(new MessageEvent<Object>(channel, channel, message));
		verify(listener2).messageReceived(new MessageEvent<Object>(channel, channel, message));
	}
	
	@Test
	public void removeLastListener()
	{
		TestChannel<Object> channel = new TestChannel<Object>();
		
		MessageListener<Object> listener1 = mock(MessageListener.class);
		channel.addMessageListener(listener1);
		
		MessageListener<Object> listener2 = mock(MessageListener.class);
		channel.addMessageListener(listener2);
		
		channel.removeMessageListener(listener2);
		
		String message = "message";
		channel.send(message);
		
		verify(listener1).messageReceived(new MessageEvent<Object>(channel, channel, message));
		verify(listener2, never()).messageReceived(new MessageEvent<Object>(channel, channel, message));
	}
	
	
	private static class TestChannel<T>
		extends AbstractChannel<T>
	{
		@Override
		public void send(T message)
		{
			fireMessageReceived(new MessageEvent<T>(this, this, message));
		}
		
		@Override
		public void close()
		{
		}
	}
}
