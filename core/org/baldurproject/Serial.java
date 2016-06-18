package org.baldurproject;

@Module
public class Serial {
	private final OutputWire tx;
	private final InputWire rx;
	
	private final Queue txQueue;
	private final Queue rxQueue;
	
	private final Timer timer = new Timer();
	
	private final int numClocksPerBit;

	public Serial(
		final OutputWire tx,
		final InputWire rx, 
		final int baud
	) {
		this(tx, rx, baud, 16);
	}
	
	public Serial(
		final OutputWire tx,
		final InputWire rx, 
		final int baud,
		final int bufferSize
	) {
		this(tx, rx, baud, new Queue(bufferSize), new Queue(bufferSize));
	}
	
	public Serial(
		final OutputWire tx,
		final InputWire rx, 
		final int baud,
		final Queue txQueue,
		final Queue rxQueue
	) {
		this.tx = tx;
		this.rx = rx;
		this.numClocksPerBit = (int) (Configuration.clockFrequency / baud) - 7;

		this.txQueue = txQueue;
		this.rxQueue = rxQueue;
	}
	
	@Blocking
	@Inline
	public byte read() {
		return (byte) rxQueue.get();
	}
	
	@Blocking
	@Inline
	public void write(byte data) {
		txQueue.put(data);
	}
	
	@Process
	public void transmit() {
		tx.drive(true);
		
		while(true) {
			long data = txQueue.get();
			
			// start
			tx.drive(false);
			timer.delayClocks(numClocksPerBit);

			for (int bitCount = 0; bitCount < 8; bitCount++) {
				tx.drive((data & 1) == 1);
				timer.delayClocks(numClocksPerBit);
				data = (byte) (data >> 1);
			}
			
			// stop
			tx.drive(true);
			timer.delayClocks(numClocksPerBit);
		}
	}
	
	
	@Process
	public void receive() {
		while(true) {
			long data = 0;
			
			// wait for start bit
			while (rx.get());
			
			// wait until start bit is over plus a couple bits
			timer.delayClocks(numClocksPerBit);
			timer.delayClocks(numClocksPerBit/2);
			
			// sample each bit and shift the result into our data byte
			for (int bitCount = 0; bitCount < 8; bitCount++) {
				if (rx.get()) {
					data = (data >> 1) | 0x80;
				} else {
					data = (data >> 1) & 0x7F;
				}
				
				timer.delayClocks(numClocksPerBit);
			}
			
			// put the data byte into the rxQueue
			rxQueue.put(data);
		}
	}
	
}
