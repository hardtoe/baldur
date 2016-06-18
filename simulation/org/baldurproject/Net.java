package org.baldurproject;

import java.util.LinkedList;

import org.baldurproject.simulate.Scheduler;

/**
 * Used for simulation of Nets.
 * 
 * @author luke
 *
 * @param <T>
 */
@Module
public class Net<T> {
	private LinkedList<Runnable> callbacks;
	
	private T value;
	
	public Net(T value) {
		this.callbacks = new LinkedList<Runnable>();
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
		Scheduler.get().schedule(delay, new Runnable() {		
			public void run() {
				if (Net.this.value != value) {
					Net.this.value = value;
					
					for (Runnable block : callbacks) {
						block.run();
					}
				}
			}
		});
	}

	@Inline
	public void drive(
		final T value,
		final long delay
	) {
		drive(null, value, delay);
	}

	@Inline
	public void drive(
		final T value
	) {
		drive(null, value, 0);
	}

	public void addCallback(Runnable block) {
		callbacks.add(block);
	}
}
