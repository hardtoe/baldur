package org.baldurproject;

import java.util.LinkedList;

import org.baldurproject.simulate.Scheduler;

/**
 * Used for simulation of simple wires.
 * 
 * @author luke
 */
@Module
public class InputWire {
	private LinkedList<Runnable> callbacks;
	
	private boolean value;
	
	public InputWire() {
		this.callbacks = new LinkedList<Runnable>();
	}

	@Inline
	public boolean get() {
		return value;
	}

	public void addCallback(Runnable block) {
		callbacks.add(block);
	}
}
