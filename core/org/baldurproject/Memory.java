package org.baldurproject;

@Module
public class Memory implements MemoryInterface {
	private final long[] data;
	private final int size;
	
	public Memory(
		final int size
	) {
		this.data = new long[size];
		this.size = size;
	}
	
	@Blocking
	public long read(final int address) {
		return data[address];
	}

	@Blocking
	public void write(
		final int address, 
		final long value
	) {
		data[address] = value;
	}
	
	// FIXME: method names can't be the same as field names
	@Blocking
	@Inline
	public int getSize() {
		return size;
	}
}
