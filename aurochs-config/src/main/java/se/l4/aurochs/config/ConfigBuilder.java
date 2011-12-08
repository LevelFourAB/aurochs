package se.l4.aurochs.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.ValidatorFactory;

import org.apache.bval.jsr303.ApacheValidationProvider;
import org.apache.bval.jsr303.ConfigurationImpl;
import org.apache.bval.jsr303.DefaultConstraintValidatorFactory;
import org.apache.bval.jsr303.DefaultMessageInterpolator;
import org.apache.bval.jsr303.resolver.SimpleTraversableResolver;

import se.l4.aurochs.config.internal.ConfigResolver;
import se.l4.aurochs.config.internal.DefaultConfig;
import se.l4.aurochs.config.internal.RawFormatReader;
import se.l4.aurochs.serialization.SerializerCollection;

import com.google.common.io.Closeables;
import com.google.common.io.InputSupplier;

/**
 * Builder for configuration instances.
 * 
 * @author Andreas Holstenson
 *
 */
public class ConfigBuilder
{
	private final SerializerCollection collection;
	private final List<InputSupplier<InputStream>> suppliers;
	
	private ConfigBuilder(SerializerCollection collection)
	{
		this.collection = collection;
		
		suppliers = new ArrayList<InputSupplier<InputStream>>();
	}
	
	/**
	 * Create a new builder.
	 * 
	 * @return
	 */
	public static ConfigBuilder with(SerializerCollection collection)
	{
		return new ConfigBuilder(collection);
	}
	
	/**
	 * Add a file that should be loaded.
	 * 
	 * @param path
	 * @return
	 */
	public ConfigBuilder addFile(String path)
	{
		return addFile(new File(path));
	}

	/**
	 * Add a file that should be loaded.
	 * 
	 * @param file
	 * @return
	 */
	public ConfigBuilder addFile(final File file)
	{
		suppliers.add(new InputSupplier<InputStream>()
		{
			@Override
			public InputStream getInput()
				throws IOException
			{
				if(! file.exists())
				{
					throw new ConfigException("The file " + file + " does not exist");
				}
				else if(! file.isFile())
				{
					throw new ConfigException(file + " is not a file");
				}
				else if(! file.canRead())
				{
					throw new ConfigException("Can not read " + file + ", check permissions");
				}
				
				return new FileInputStream(file);
			}
			
			@Override
			public String toString()
			{
				return file.getPath();
			}
		});
		
		return this;
	}
	
	/**
	 * Add a stream that should be read.
	 * 
	 * @param stream
	 * @return
	 */
	public ConfigBuilder addStream(final InputStream stream)
	{
		suppliers.add(new InputSupplier<InputStream>()
		{
			@Override
			public InputStream getInput()
				throws IOException
			{
				return stream;
			}
			
			@Override
			public String toString()
			{
				return "custom stream";
			}
		});
		
		return this;
	}
	
	private ValidatorFactory createValidator()
	{
		ApacheValidationProvider provider = new ApacheValidationProvider();
		
		ConfigurationImpl config = new ConfigurationImpl(null, provider);
		config.messageInterpolator(new DefaultMessageInterpolator());
		config.traversableResolver(new SimpleTraversableResolver());
		config.constraintValidatorFactory(new DefaultConstraintValidatorFactory());
		
		return provider.buildValidatorFactory(config);
	}
	
	/**
	 * Create the configuration object. This will load any declared input
	 * files.
	 * 
	 * @return
	 */
	public Config build()
	{
		Map<String, Object> data = new HashMap<String, Object>();
		
		for(InputSupplier<InputStream> supplier : suppliers)
		{
			InputStream in = null;
			try
			{
				in = supplier.getInput();
				Map<String, Object> readConfig = RawFormatReader.read(in);
				ConfigResolver.resolveTo(readConfig, data);
			}
			catch(IOException e)
			{
				throw new ConfigException("Unable to read " + supplier.toString() + "; " + e.getMessage(), e);
			}
			finally
			{
				Closeables.closeQuietly(in);
			}
		}
		
		ValidatorFactory validatorFactory = createValidator();
		return new DefaultConfig(collection, validatorFactory, data);
	}
}
