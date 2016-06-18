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
	private boolean value;
	
	public OutputWire(boolean value) {
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
		this.value = value;
	}

	@Inline
	public void drive(
		final boolean value,
		final long delay
	) {
		this.value = value;
	}

	@Inline
	public void drive(
		final boolean value
	) {
		this.value = value;
	}
}
