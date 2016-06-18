package org.baldurproject;

@Module
public class Queue  {
	private final long[] data;

	private byte readPointer;
	private byte writePointer;
	
	private final int bigMask;
	private final int littleMask;
	private final int size;
	
	public Queue(
		final long[] data
	) {
		this.data = data;
		bigMask = (data.length * 2) - 1;
		littleMask = data.length - 1;
		size = data.length;		
		
		readPointer = 0;
		writePointer = 0;
	}
	
	public Queue(
		final int size
	) {
		this(new long[size]);
	}
	
	@Blocking
	public void put(final long item) {
		while (full());
		
		data[writePointer & littleMask] = item;
		writePointer = (byte) ((writePointer + 1) & bigMask);
	}

	@Blocking
	public long get() {
		while (empty());
		
		long item = data[readPointer & littleMask];
		readPointer = (byte) ((readPointer + 1) & bigMask);
		return item;
	}

	@Blocking
	@Inline
	private boolean pointerWrap() {
		return ((readPointer / size) != (writePointer / size));
	}

	@Blocking
	@Inline
	private boolean sameEntry() {
		return ((readPointer & littleMask) == (writePointer & littleMask));
	}

	@Blocking
	@Inline
	public boolean full() {
		return 
			((readPointer / size) != (writePointer / size)) &&
			((readPointer & littleMask) == (writePointer & littleMask));
	}

	@Blocking
	@Inline
	public boolean free() {
		return 
			!(((readPointer / size) != (writePointer / size)) &&
			((readPointer & littleMask) == (writePointer & littleMask)));
	}

	@Blocking
	@Inline
	public boolean avail() {
		return 
			!(!((readPointer / size) != (writePointer / size)) &&
			((readPointer & littleMask) == (writePointer & littleMask)));
	}

	@Blocking
	@Inline
	public boolean empty() {
		return 
			!((readPointer / size) != (writePointer / size)) &&
			((readPointer & littleMask) == (writePointer & littleMask));
	}
}
