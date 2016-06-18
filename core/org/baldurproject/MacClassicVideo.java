package org.baldurproject;

/**
 * Currently requires a 100MHz clock to function.  Output is
 * 512x384 black and while video driven at 640x480@60Hz VGA.
 * 
 * VRAM takes up 24KB.  If you use both read() and write()
 * functions, VRAM utilization will double in order to support
 * dual ported access.  There should be a way around this, but
 * I don't know of an easy one.
 * 
 * @author luke
 */
@Module
public class MacClassicVideo implements MemoryInterface {
	private final OutputWire  HSYNC;
	private final OutputWire  VSYNC;
	private final OutputWire  RED;
	private final OutputWire  GREEN;
	private final OutputWire  BLUE;

	private final short[] vram = 
		new short[(512 * 384) / 16];
	
	private int pixelCount = 0;
	private int lineCount = 0;
	
	public MacClassicVideo (
		final OutputWire HSYNC,
		final OutputWire VSYNC,
		final OutputWire RED,
		final OutputWire GREEN,
		final OutputWire BLUE
	) {
		this.HSYNC = HSYNC;
		this.VSYNC = VSYNC;
		this.RED = RED;
		this.GREEN = GREEN;
		this.BLUE = BLUE;
	}
	
	@Blocking
	public long read(int address) {
		return vram[address >> 1];
	}
	
	@Blocking
	public void write(int address, long data) {
		vram[address >> 1] = (short) data;
	}
	
	@Process
	public void generateVideo() {
		while (true) {
			// clk1
			VSYNC.drive(lineCount < 10 || lineCount > 12);
			HSYNC.drive(pixelCount < 16 || pixelCount > 112);

			short vramWord = 
				vram[
				     ((pixelCount - 224) / 16) | 
				     ((lineCount - 93) * 32)];
			
			int pixelCountQ = 
				15 - ((pixelCount - 224) % 16);
			
			// clk2
			if (lineCount >= 525) {
				lineCount = 0;
			} else {
				lineCount = lineCount;
			}
				
			// clk3
			if (pixelCount >= 800) {
				pixelCount = 0;
				lineCount++;

			} else {
				pixelCount++;
			}
			
			// clk4
			if (
				lineCount > 93 &&
				lineCount < 477 &&
				pixelCount > 224 && 
				pixelCount < 736
			) {
				boolean videoOut = 
					((vramWord >> pixelCountQ) & 1) == 1;

				RED.drive(videoOut);
				GREEN.drive(videoOut);
				BLUE.drive(videoOut);

			} else {
				RED.drive(false);
				GREEN.drive(false);
				BLUE.drive(false);
			}
			
		}
	}
}
