package org.baldurproject.examples;

import org.baldurproject.Clock;
import org.baldurproject.Input;
import org.baldurproject.InputWire;
import org.baldurproject.Memory;
import org.baldurproject.Module;
import org.baldurproject.Output;
import org.baldurproject.OutputWire;
import org.baldurproject.Queue;
import org.baldurproject.Serial;

@Module
public class SingleBrainFrak {
	@Input  @Clock public final InputWire  CLK = new InputWire();

	@Output        public final OutputWire TX  = new OutputWire(false);
	@Input         public final InputWire  RX  = new InputWire();

	public final Queue txQueue = new Queue(16);
	public final Queue rxQueue = new Queue(16);

	public final Serial serial = new Serial(TX, RX, 115200, txQueue, rxQueue);
	
	// standard distribution of brainfuck has 30,000 byte-sized cells for data
	private final Memory data = new Memory(1 << 15);
	
	// 1024 program words should be good for now
	private final Memory program = new Memory(1 << 17);
	
	public final BrainFrak cpu = new BrainFrak(rxQueue, txQueue, program, data);
}
