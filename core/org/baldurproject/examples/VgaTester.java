package org.baldurproject.examples;

import org.baldurproject.Clock;
import org.baldurproject.Input;
import org.baldurproject.InputWire;
import org.baldurproject.MacClassicVideo;
import org.baldurproject.MemoryInterface;
import org.baldurproject.Module;
import org.baldurproject.Process;
import org.baldurproject.Output;
import org.baldurproject.OutputWire;

@Module
public class VgaTester {
	@Input  @Clock public final InputWire  CLK = new InputWire();
	
	@Output        public final OutputWire  HSYNC = new OutputWire(false);
	@Output        public final OutputWire  VSYNC = new OutputWire(false);
	@Output        public final OutputWire  RED   = new OutputWire(false);
	@Output        public final OutputWire  GREEN = new OutputWire(false);
	@Output        public final OutputWire  BLUE  = new OutputWire(false);

	private MemoryInterface video = 
		new MacClassicVideo(HSYNC, VSYNC, RED, GREEN, BLUE);
	
	@Process
	public void letsHaveFun() {
		int i = 0;
		
		while (true) {
			video.write(i, i);
			i++;
		}
	}
}
