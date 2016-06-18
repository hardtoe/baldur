package org.baldurproject;

public interface MemoryInterface {
	public long read(int address);
	public void write(int address, long data);
}
