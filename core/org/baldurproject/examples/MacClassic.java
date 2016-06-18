package org.baldurproject.examples;

import org.baldurproject.Clock;
import org.baldurproject.Input;
import org.baldurproject.InputWire;
import org.baldurproject.M68000;
import org.baldurproject.MacClassicVideo;
import org.baldurproject.MemoryInterface;
import org.baldurproject.Module;
import org.baldurproject.Process;
import org.baldurproject.Output;
import org.baldurproject.OutputWire;
import org.baldurproject.Timer;

@Module
public class MacClassic {
	@Input  @Clock public final InputWire  CLK = new InputWire();
	
	@Output        public final OutputWire  HSYNC = new OutputWire(false);
	@Output        public final OutputWire  VSYNC = new OutputWire(false);
	@Output        public final OutputWire  RED   = new OutputWire(false);
	@Output        public final OutputWire  GREEN = new OutputWire(false);
	@Output        public final OutputWire  BLUE  = new OutputWire(false);

	private MemoryInterface video = 
		new MacClassicVideo(HSYNC, VSYNC, RED, GREEN, BLUE);

	private M68000 cpu =
		new M68000(video);
	
	int rand = 1;
	
	Timer timer = new Timer();
	
	@Process
	public void run() {
		while (true) {
			for (int i = 0; i < (512 * 384); i++) {
				video.write(i, rand);
				
				for (int j = 0; j < 32; j++) {
					rand = 
						(rand << 1) |
						(((rand >> 31) & 1) ^ ((rand >> 28) & 1));
				}
			}
			
			for (int i = 0; i < 100000000; i++) {
				cpu.executeInstruction();
			}
			
			cpu.reset();
		}
	}
}
