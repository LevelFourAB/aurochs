package se.l4.aurochs.events;

public class FakeEventHandle
	implements EventHandle
{
	public static final EventHandle NOOP = new FakeEventHandle();
	
	private FakeEventHandle()
	{
	}
	
	@Override
	public void stop()
	{
	}
}
