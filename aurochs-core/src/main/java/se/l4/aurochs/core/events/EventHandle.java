package se.l4.aurochs.core.events;

public interface EventHandle
{
	void stop();
	
	static EventHandle noop()
	{
		return FakeEventHandle.NOOP;
	}
}
