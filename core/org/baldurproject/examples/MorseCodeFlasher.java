package org.baldurproject.examples;

import org.baldurproject.Blocking;
import org.baldurproject.Clock;
import org.baldurproject.Input;
import org.baldurproject.InputWire;
import org.baldurproject.Module;
import org.baldurproject.Output;
import org.baldurproject.Process;
import org.baldurproject.OutputWire;
import org.baldurproject.Serial;
import org.baldurproject.Timer;

@Module 
public class MorseCodeFlasher {
	@Input  @Clock public final InputWire  CLK = new InputWire();
	@Output        public final OutputWire LED = new OutputWire(false);

	@Output        public final OutputWire TX  = new OutputWire(false);
	@Input         public final InputWire  RX  = new InputWire();
	
	// modules
	public final Serial serial = new Serial(TX, RX, 115200, 64);
	public final Timer timer = new Timer();
	
	// dot = 1
	// dash = 2
	short[] morseTable = new short[] {
		1 | (2 << 2) | (0 << 4) | (0 << 6) | (0 << 8), // a .-
		2 | (1 << 2) | (1 << 4) | (1 << 6) | (0 << 8), // b -...
		2 | (1 << 2) | (2 << 4) | (1 << 6) | (0 << 8), // c -.-.
		2 | (1 << 2) | (1 << 4) | (0 << 6) | (0 << 8), // d -..
		1 | (0 << 2) | (0 << 4) | (0 << 6) | (0 << 8), // e .
		1 | (1 << 2) | (2 << 4) | (1 << 6) | (0 << 8), // f ..-.
		2 | (2 << 2) | (1 << 4) | (0 << 6) | (0 << 8), // g --.
		1 | (1 << 2) | (1 << 4) | (1 << 6) | (0 << 8), // h ....
		1 | (1 << 2) | (0 << 4) | (0 << 6) | (0 << 8), // i ..
		1 | (2 << 2) | (2 << 4) | (2 << 6) | (0 << 8), // j .---
		2 | (1 << 2) | (2 << 4) | (0 << 6) | (0 << 8), // k -.-
		1 | (2 << 2) | (1 << 4) | (1 << 6) | (0 << 8), // l .-..
		2 | (2 << 2) | (0 << 4) | (0 << 6) | (0 << 8), // m --
		2 | (1 << 2) | (0 << 4) | (0 << 6) | (0 << 8), // n -.
		2 | (2 << 2) | (2 << 4) | (0 << 6) | (0 << 8), // o ---
		1 | (2 << 2) | (2 << 4) | (1 << 6) | (0 << 8), // p .--.
		2 | (2 << 2) | (1 << 4) | (2 << 6) | (0 << 8), // q --.-
		1 | (2 << 2) | (1 << 4) | (0 << 6) | (0 << 8), // r .-.
		1 | (1 << 2) | (1 << 4) | (0 << 6) | (0 << 8), // s ...
		2 | (0 << 2) | (0 << 4) | (0 << 6) | (0 << 8), // t -
		1 | (1 << 2) | (2 << 4) | (0 << 6) | (0 << 8), // u ..-
		1 | (1 << 2) | (1 << 4) | (2 << 6) | (0 << 8), // v ...-
		1 | (2 << 2) | (2 << 4) | (0 << 6) | (0 << 8), // w .--
		2 | (1 << 2) | (1 << 4) | (2 << 6) | (0 << 8), // x -..-
		2 | (1 << 2) | (2 << 4) | (2 << 6) | (0 << 8), // y -.--
		2 | (2 << 2) | (1 << 4) | (1 << 6) | (0 << 8), // z --..
	};

	// processes
	@Process 
	public void run() {
		while (true) {
			byte letterToSend = 
				serial.read();
			
			short morseToSend;
			
			if (letterToSend == 32) {
				morseToSend = 0xf;
				
			} else if (letterToSend >= 97) {
				morseToSend = morseTable[letterToSend - 97];
				
			} else if (letterToSend >= 65) {
				morseToSend = morseTable[letterToSend - 65];
				
			} else {
				morseToSend = morseTable[letterToSend - 48];
			}
			
			morse(morseToSend);	
			letterPause();
			
			serial.write(letterToSend);
		}	
	}
	
	@Blocking
	public void morse(int code) {
		while ((code & 3) != 0) {
			if ((code & 3) == 1) {
				dit();
				
			} else if ((code & 3) == 2) {
				dah();
				
			} else if ((code & 3) == 3) {
				letterPause(); letterPause();
			}
			
			code = (code >> 2);
		}
	}

	@Blocking
	private void dit() {
		LED.drive(true);
		timer.delayMillis(100);

		LED.drive(false);
		timer.delayMillis(100);
	}
	
	@Blocking
	private void dah() {
		LED.drive(true);
		timer.delayMillis(300);

		LED.drive(false);
		timer.delayMillis(100);
	}
	
	@Blocking
	private void letterPause() {
		timer.delayMillis(300);
	}
}
