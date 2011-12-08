package se.l4.aurochs.config;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.junit.Test;

import se.l4.aurochs.serialization.DefaultSerializerCollection;
import se.l4.aurochs.serialization.Expose;
import se.l4.aurochs.serialization.ReflectionSerializer;
import se.l4.aurochs.serialization.Use;

import com.google.common.base.Charsets;

public class DefaultConfigTest
{
	@Test
	public void testBasic()
	{
		
	}
	
	@Test
	public void testSizeObject()
	{
		Config config = ConfigBuilder.with(new DefaultSerializerCollection())
			.addStream(stream("medium: { width: 100, height: 100 }"))
			.build();
		
		Value<Size> size = config.get("medium", Size.class);
		assertThat(size, notNullValue());
		
		Size actual = size.get();
		assertThat(actual, notNullValue());
		
		assertThat(actual.width, is(100));
		assertThat(actual.height, is(100));
	}
	
	@Test
	public void testThumbnailsObject()
	{
		Config config = ConfigBuilder.with(new DefaultSerializerCollection())
			.addStream(stream("thumbnails: { \n medium: { width: 100, height: 100 }\n }"))
			.build();
		
		Value<Thumbnails> value = config.get("thumbnails", Thumbnails.class);
		assertThat(value, notNullValue());
		
		Thumbnails thumbs = value.get();
		assertThat(thumbs, notNullValue());
		
		assertThat(thumbs.medium, notNullValue());
	}
	
	@Test
	public void testInvalidSize()
	{
		Config config = ConfigBuilder.with(new DefaultSerializerCollection())
			.addStream(stream("medium: { width: 100 }\n }"))
			.build();
		
		try
		{
			config.get("medium", Size.class);
			
			fail("validation should have failed");
		}
		catch(ConfigException e)
		{
		}
	}
	
	@Test
	public void testInvalidThumbnailsSize()
	{
		Config config = ConfigBuilder.with(new DefaultSerializerCollection())
			.addStream(stream("thumbnails: { \n medium: { width: 100, height: 4000 }\n }"))
			.build();
		
		try
		{
			config.get("thumbnails", Thumbnails.class);
			
			fail("validation should have failed");
		}
		catch(ConfigException e)
		{
		}
	}
	
	private InputStream stream(String in)
	{
		return new ByteArrayInputStream(in.getBytes(Charsets.UTF_8));
	}
	
	@Use(ReflectionSerializer.class)
	public static class Thumbnails
	{
		@Expose @Valid
		private Size medium;
		@Expose @Valid
		private Size large;
	}
	
	@Use(ReflectionSerializer.class)
	public static class Size
	{
		@Min(1) @Max(1000)
		@Expose
		private int width;
		
		@Min(1) @Max(1000)
		@Expose
		private int height;
	}
}
