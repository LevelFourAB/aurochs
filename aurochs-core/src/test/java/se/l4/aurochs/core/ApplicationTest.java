package se.l4.aurochs.core;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.google.inject.AbstractModule;

/**
 * Tests for {@link Application}.
 * 
 * @author Andreas Holstenson
 *
 */
public class ApplicationTest
{
	@Test
	public void testNoModule()
	{
		SystemSession session = new Application()
			.start();
		
		assertThat(session, notNullValue());
	}
	
	@Test
	public void testSingleModule()
	{
		SystemSession session = new Application()
			.add(new ModuleImpl())
			.start();
		
		assertThat(session, notNullValue());
	}
	
	private static class ModuleImpl
		extends AbstractModule
	{
		@Override
		protected void configure()
		{
		}
	}
}
