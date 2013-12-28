package se.l4.aurochs.config;

public interface ValueListener<T>
{
	void valueChanged(String key, T oldValue, T newValue);
}
