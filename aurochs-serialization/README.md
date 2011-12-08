Serialization for Java objects with different formats. The reflection has full
support for generics and uses annotations to control the format of a class.

## Basic usage

```java
SerializerCollection collection = new DefaultSerializerCollection();
Serializer<Person> serializer = collection.find(Person.class);

Person instance = serializer.read(streamingInput);

serializer.write(instance, "", streamingOutput);
```

## Annotating classes

Serializers are dynamically resolved by looking for `@Use` on classes. For
reflection and annotation based serialization `@Use(ReflectionSerializer.class)`
should be used.

Any fields that should be part of the serialized form need to be annotated
with `@Expose`.

Example:

```java
@Use(ReflectionSerializer.class)
public class Person {
	@Expose
	private String name;
	
	@Expose
	private int age;
	
	@Expose
	private List<String> address;
}
```

## Immutability and constructors

Fields in classes may be `final`, in which case the library will try to use
constructors when deserializing.

Example:

```java
@Use(ReflectionSerializer.class)
public class Person {
	@Expose
	private final String name;
	
	@Expose
	private final int age;

	public Person(@Expose("name") String name, @Expose("age") int age) {
		this.name = name;
		this.age = age;
	}	
}
```