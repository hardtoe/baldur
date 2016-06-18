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
public class DoubleBrainFrak {
	@Input  @Clock public final InputWire  CLK = new InputWire();

	@Output        public final OutputWire TX  = new OutputWire(false);
	@Input         public final InputWire  RX  = new InputWire();

	public final Queue txQueue = new Queue(16);
	public final Queue pipeQueue = new Queue(16);
	public final Queue rxQueue = new Queue(16);

	public final Serial serial = new Serial(TX, RX, 115200, txQueue, rxQueue);

	private final Memory program0 = new Memory(1 << 15);
	private final Memory data0 = new Memory(1 << 15);
	public final BrainFrak cpu0 = new BrainFrak(rxQueue, pipeQueue, program0, data0);

	private final Memory program1 = new Memory(1 << 15);
	private final Memory data1 = new Memory(1 << 15);
	public final BrainFrak cpu1 = new BrainFrak(pipeQueue, txQueue, program1, data1);
}
