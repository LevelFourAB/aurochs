package se.l4.aurochs.events;

public interface EventHandle
{
	void stop();
	
	static EventHandle noop()
	{
		return FakeEventHandle.NOOP;
	}
}
