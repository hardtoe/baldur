package org.baldurproject;

import java.util.LinkedList;

import org.baldurproject.simulate.Scheduler;

/**
 * Used for simulation of simple wires.
 * 
 * @author luke
 */
@Module
public class OutputWire {
	private LinkedList<Runnable> callbacks;
	
	private boolean value;
	
	public OutputWire(boolean value) {
		this.callbacks = new LinkedList<Runnable>();
		this.value = value;
	}

	@Inline
	public boolean get() {
		return value;
	}
	
	@Inline
	public void drive(
		final Object src, 
		final boolean value,
		final long delay
	) {
		Scheduler.get().schedule(delay, new Runnable() {		
			public void run() {
				if (OutputWire.this.value != value) {
					OutputWire.this.value = value;
					
					for (Runnable block : callbacks) {
						block.run();
					}
				}
			}
		});
	}

	@Inline
	public void drive(
		final boolean value,
		final long delay
	) {
		drive(null, value, delay);
	}

	@Inline
	public void drive(
		final boolean value
	) {
		drive(null, value, 0);
	}

	public void addCallback(Runnable block) {
		callbacks.add(block);
	}
}
