package se.l4.aurochs.net.internal;

import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;

import com.google.common.io.Closeables;
import com.google.inject.Provider;

import se.l4.commons.config.ConfigException;

/**
 * Helper functions for SSL/TLS support.
 * 
 * @author Andreas Holstenson
 *
 */
public class SslHelper
{
	public static final X509TrustManager TRUSTING = new X509TrustManager()
		{
			@Override
			public X509Certificate[] getAcceptedIssuers()
			{
				return null;
			}
	
			@Override
			public void checkClientTrusted(X509Certificate[] certs, String authType)
			{
			}
	
			@Override
			public void checkServerTrusted(X509Certificate[] certs, String authType)
			{
			}
		};

	static
	{
		if(Security.getProvider("BC") == null)
		{
			Security.addProvider(new BouncyCastleProvider());
		}
	}
	
	public static SSLEngine createClientEngine(TrustManager trustManager)
		throws GeneralSecurityException
	{
		SSLContext ctx = SSLContext.getInstance("TLS");
		
		ctx.init(new KeyManager[0], new TrustManager[] { trustManager }, new SecureRandom());
		
		SSLEngine engine = ctx.createSSLEngine();
		engine.setUseClientMode(true);
		return engine;
	}
	
	public static Provider<SSLEngine> createEngine(ServerConfig.TLS config) 
		throws GeneralSecurityException, IOException
	{
		final SSLContext ctx = SSLContext.getInstance("TLS");
		
		KeyManager[] keys = createKeyManagers(config);

		ctx.init(keys, new TrustManager[] { TRUSTING }, new SecureRandom());
		
		return new Provider<SSLEngine>()
		{
			@Override
			public SSLEngine get()
			{
				SSLEngine engine = ctx.createSSLEngine();
				engine.setUseClientMode(false);
				return engine;
			}
		};
	}
	
	public static KeyManager[] createKeyManagers(ServerConfig.TLS config)
		throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException, UnrecoverableKeyException
	{
		PrivateKey pk = readPrivateKey(config);
		X509Certificate cert = readCertificate(config);
		
		char[] empty = new char[0];
		
		KeyStore store = KeyStore.getInstance("JKS");
		store.load(null, empty);
		
		store.setCertificateEntry("", cert);
		store.setKeyEntry("", pk, empty, new Certificate[] { cert });
		
		KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		factory.init(store, empty);
		
		return factory.getKeyManagers();
	}
	
	public static PrivateKey readPrivateKey(final ServerConfig.TLS config)
		throws IOException
	{
		FileReader file = new FileReader(config.getPrivateKey());
		try
		{
			PEMReader reader = new PEMReader(file, new PasswordFinder()
			{
				@Override
				public char[] getPassword()
				{
					return config.getPassword().toCharArray();
				}
			});
			
			Object obj;
			while((obj = reader.readObject()) != null)
			{
				if(obj instanceof PrivateKey)
				{
					return (PrivateKey) obj;
				}
				else if(obj instanceof KeyPair)
				{
					return ((KeyPair) obj).getPrivate();
				}
			}
			
			throw new ConfigException("No private key found in " + config.getPrivateKey());
		}
		finally
		{
			Closeables.closeQuietly(file);
		}
	}
	
	public static X509Certificate readCertificate(final ServerConfig.TLS config)
		throws IOException
	{
		FileReader file = new FileReader(config.getCertificate());
		try
		{
			PEMReader reader = new PEMReader(file, new PasswordFinder()
			{
				@Override
				public char[] getPassword()
				{
					return config.getPassword().toCharArray();
				}
			});
			
			Object obj;
			while((obj = reader.readObject()) != null)
			{
				if(obj instanceof X509Certificate)
				{
					return (X509Certificate) obj;
				}
			}
			
			throw new ConfigException("No certificate found in " + config.getCertificate());
		}
		finally
		{
			Closeables.closeQuietly(file);
		}
	}
}
