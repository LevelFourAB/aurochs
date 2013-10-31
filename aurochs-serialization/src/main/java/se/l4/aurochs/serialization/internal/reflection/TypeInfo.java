package se.l4.aurochs.serialization.internal.reflection;

import java.util.Map;

import se.l4.aurochs.serialization.ReflectionSerializer;
import se.l4.aurochs.serialization.SerializerFormatDefinition;

/**
 * Information about a type used with {@link ReflectionSerializer}.
 * 
 * @author Andreas Holstenson
 *
 */
public class TypeInfo<T>
{
	private final FieldDefinition[] fields;
	private final Class<T> type;
	private final Map<String, FieldDefinition> fieldMap;
	private final FactoryDefinition<T>[] factories;
	private final SerializerFormatDefinition formatDefinition;

	public TypeInfo(Class<T> type,
			FactoryDefinition<T>[] factories,
			Map<String, FieldDefinition> fieldMap, 
			FieldDefinition[] fields)
	{
		this.type = type;
		this.factories = factories;
		this.fieldMap = fieldMap;
		this.fields = fields;
		
		SerializerFormatDefinition.Builder builder = SerializerFormatDefinition.builder();
		for(FieldDefinition fdef : fields)
		{
			builder.field(fdef.getName()).withHints(fdef.getHints()).using(fdef.getSerializer());
		}
		formatDefinition = builder.build();
	}
	
	public Class<T> getType()
	{
		return type;
	}
	
	public FieldDefinition[] getAllFields()
	{
		return fields;
	}
	
	public FieldDefinition getField(String name)
	{
		return fieldMap.get(name);
	}
	
	/**
	 * Create a new instance.
	 * 
	 * @param fields
	 * @return
	 */
	public T newInstance(Map<String, Object> fields)
	{
		try
		{
			FactoryDefinition<T> bestDef = factories[0];
			int bestScore = bestDef.getScore(fields);
			
			for(int i=1, n=factories.length; i<n; i++)
			{
				int score = factories[i].getScore(fields);
				if(score > bestScore)
				{
					bestDef = factories[i];
					bestScore = score;
				}
			}
			
			return bestDef.create(fields);
		}
		catch(RuntimeException e)
		{
			throw new RuntimeException("Could not create " + type + "; " + e.getMessage(), e);
		}
	}
	
	public SerializerFormatDefinition getFormatDefinition()
	{
		return formatDefinition;
	}
}
