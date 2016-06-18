package org.baldurproject;

import java.util.LinkedList;

/**
 * Used for hardware synthesis of Nets.
 * 
 * @author luke
 *
 * @param <T>
 */
@Module
public class Net<T> {
	private T value;

	public Net(T value) {
		this.value = value;
	}

	@Inline
	public T get() {
		return value;
	}

	@Inline
	public void drive(
		final Object src, 
		final T value,
		final long delay
	) {
		this.value = value;
	}

	@Inline
	public void drive(
		final T value,
		final long delay
	) {
		this.value = value;
	}

	@Inline
	public void drive(
		final T value
	) {
		this.value = value;
	}
}