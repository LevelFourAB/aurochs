package se.l4.aurochs.jobs.cluster.messages;

import java.util.concurrent.CompletableFuture;

import se.l4.aurochs.jobs.SubmittedJob;

public class SubmittedJobImpl<T>
	implements SubmittedJob<T>
{
	private final CompletableFuture<T> result;

	public SubmittedJobImpl(CompletableFuture<T> result)
	{
		this.result = result;
	}

	@Override
	public CompletableFuture<T> result()
	{
		return result;
	}

}
