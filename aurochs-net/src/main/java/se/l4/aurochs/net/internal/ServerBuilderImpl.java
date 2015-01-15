package se.l4.aurochs.net.internal;

import java.util.function.Consumer;

import se.l4.aurochs.config.Config;
import se.l4.aurochs.core.Session;
import se.l4.aurochs.core.spi.Sessions;
import se.l4.aurochs.net.Server;
import se.l4.aurochs.net.ServerBuilder;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ServerBuilderImpl
	implements ServerBuilder
{
	private final Provider<DefaultTransportFunctions> transportFunctions;
	private final Config config;
	
	private ServerConfig serverConfig;
	private Sessions.Listener listener;

	@Inject
	public ServerBuilderImpl(Config config, Provider<DefaultTransportFunctions> transportFunctions)
	{
		this.config = config;
		this.transportFunctions = transportFunctions;
	}

	@Override
	public ServerBuilder withConfig(String key)
	{
		return withConfig(config.get(key, ServerConfig.class).getOrDefault(new ServerConfig()));
	}

	@Override
	public ServerBuilder withConfig(ServerConfig config)
	{
		this.serverConfig = config;
		return this;
	}
	
	@Override
	public ServerBuilder withSessionListener(Sessions.Listener listener)
	{
		this.listener = listener;
		return this;
	}
	
	@Override
	public ServerBuilder withSessionListener(Consumer<Session> onCreate, Consumer<Session> onDestroy)
	{
		return withSessionListener(new Sessions.Listener()
		{
			@Override
			public void sessionCreated(Session session)
			{
				onCreate.accept(session);
			}
			
			@Override
			public void sessionDestroyed(Session session)
			{
				onDestroy.accept(session);
			}
		});
	}
	
	@Override
	public Server build()
	{
		return new ServerImpl(serverConfig, listener, transportFunctions.get());
	}

	@Override
	public Server start()
		throws Exception
	{
		Server server = build();
		server.start();
		return server;
	}

}
