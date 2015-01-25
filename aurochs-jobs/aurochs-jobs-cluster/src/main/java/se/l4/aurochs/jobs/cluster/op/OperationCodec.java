package se.l4.aurochs.jobs.cluster.op;

import se.l4.aurochs.core.channel.ChannelCodec;
import se.l4.aurochs.core.io.Bytes;

public class OperationCodec
	implements ChannelCodec<Bytes, JobOperation>
{

	@Override
	public boolean accepts(Bytes in)
	{
		return true;
	}

	@Override
	public JobOperation fromSource(Bytes object)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bytes toSource(JobOperation object)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
