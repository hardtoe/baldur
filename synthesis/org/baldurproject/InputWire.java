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
	private boolean value;

	@Inline
	public boolean get() {
		return value;
	}
}
