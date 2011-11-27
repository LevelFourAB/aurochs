package se.l4.aurochs.serialization;

/**
 * Name for a {@link Serializer}.
 * 
 * @author Andreas Holstenson
 *
 */
public class QualifiedName
{
	private final String namespace;
	private final String name;

	public QualifiedName(String namespace, String name)
	{
		this.namespace = namespace;
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getNamespace()
	{
		return namespace;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null)
			? 0
			: name.hashCode());
		result = prime * result + ((namespace == null)
			? 0
			: namespace.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		QualifiedName other = (QualifiedName) obj;
		if(name == null)
		{
			if(other.name != null)
				return false;
		}
		else if(!name.equals(other.name))
			return false;
		if(namespace == null)
		{
			if(other.namespace != null)
				return false;
		}
		else if(!namespace.equals(other.namespace))
			return false;
		return true;
	}
}