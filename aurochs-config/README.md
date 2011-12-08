Simplistic configuration for Java applications.

## Basic usage

```java
Config config = ConfigBuilder.with(serializerCollection)
	.addFile("/etc/app/normal.conf")
	.build();
	
Value<Thumbnails> thumbs = config.get("thumbs", Thumbnails.class);

Value<Size> mediumSize = config.get("thumbs.medium", Size.class);
```

## Configuration format

The format is similar to JSON but is not as strict. For example it does not
require quotes around keys or string values and the initial braces can be
skipped.

Example:

```
thumbs: {
	medium: { width: 400, height: 400 }
	small: {
		width: 100
		height: 100
	}
}

# Override the width
thumbs.small.width: 150
``` 