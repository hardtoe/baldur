package org.baldurproject.examples;

import org.baldurproject.Blocking;
import org.baldurproject.Memory;
import org.baldurproject.Module;
import org.baldurproject.Process;
import org.baldurproject.Queue;

/**
 * A simple CPU based on the Brainfuck esoteric language.
 * 
 * http://en.wikipedia.org/wiki/Brainfuck
 * 
 * @author luke
 *
 */
@Module
public class BrainFrak {
	public final Queue outputQueue;
	public final Queue inputQueue;
	
	private final Memory data;
	private final Memory program;
	
	// data pointer
	private short ptr = 0;
	
	// program counter
	private int pc = 0;
	private int lastInstruction = 0;
	
	// loop counter
	private short loopCounter = 0; 
	
	// internal instructions
	private final byte INC_PTR = 0;
	private final byte DEC_PTR = 1;
	private final byte INC = 2;
	private final byte DEC = 3;
	private final byte OUT = 4;
	private final byte IN = 5;
	private final byte BEGIN_WHILE = 6;
	private final byte END_WHILE = 7;
	
	// external instruction ascii codes
	private final byte INC_PTR_CHAR = '>';
	private final byte DEC_PTR_CHAR = '<';
	private final byte INC_CHAR = '+';
	private final byte DEC_CHAR = '-';
	private final byte OUT_CHAR = '.';
	private final byte IN_CHAR = ',';
	private final byte BEGIN_WHILE_CHAR = '[';
	private final byte END_WHILE_CHAR = ']';
	
	private final byte END_OF_PROGRAM = '@';
	private final byte START_EXECUTION = '!';
	
	public BrainFrak(
		final Queue input,
		final Queue output,
		final Memory program,
		final Memory data
	) {
		this.inputQueue = input;
		this.outputQueue = output;
		this.program = program;
		this.data = data;
	}
	
	// processes
	@Process 
	public void run() {
		while (true) {
			// load program until '@' character is read
			loadProgramFromSerial();
			
			// pass through until '!' character is read
			// this allows us to daisy-chain and program multiple BrainFrak CPUs 
			passThroughUntilExclamationMark();
			
			while (pc < lastInstruction) {
				byte instruction = 
					(byte) program.read(pc);
				
				byte currentData = 
					(byte) data.read(ptr);
				
				if (instruction == INC_PTR) {
					ptr = (short) (ptr + 1);
					pc++;
					
				} else if (instruction == DEC_PTR) {
					ptr = (short) (ptr - 1);
					pc++;
					
				} else if (instruction == INC) {
					data.write(ptr, currentData + 1);
					pc++;
					
				} else if (instruction == DEC) {
					data.write(ptr, currentData - 1);
					pc++;
					
				} else if (instruction == OUT) {
					outputQueue.put(currentData);
					pc++;
					
				} else if (instruction == IN) {
					data.write(ptr, outputQueue.get());
					pc++;
					
				} else if (instruction == BEGIN_WHILE) {
					if (currentData == 0) {
						fastforward();
						
					} else {
						pc++;
					}
					
					
				} else if (instruction == END_WHILE) {
					if (currentData == 0) {
						pc++;
						
					} else {
						rewind();
					}
					
				} else {
					pc++;
				}
			}	
			
			initializeMemory();
		}
	}

	@Blocking
	private void passThroughUntilExclamationMark() {
		byte input =
			(byte) inputQueue.get();
		
		while (input != START_EXECUTION) {
			outputQueue.put(input);
			
			input = 
				(byte) inputQueue.get();
		}
	}

	@Blocking
	private void initializeMemory() {
		for (int i = 0; i < data.getSize(); i++) {
			data.write(i, 0);
		}
	}
	
	@Blocking
	private void loadProgramFromSerial() {
		byte input =
			(byte) inputQueue.get();
		
		pc = 0;
		
		while (input != END_OF_PROGRAM) {
			if (
				input == INC_PTR_CHAR ||
				input == DEC_PTR_CHAR ||
				input == INC_CHAR ||
				input == DEC_CHAR ||
				input == OUT_CHAR ||
				input == IN_CHAR ||
				input == BEGIN_WHILE_CHAR ||
				input == END_WHILE_CHAR
			) {

				byte translatedValue = 0;
				
				if (input == INC_PTR_CHAR) {
					translatedValue = INC_PTR;
					
				} else if (input == DEC_PTR_CHAR) {
					translatedValue = DEC_PTR;
					
				} else if (input == INC_CHAR) {
					translatedValue = INC;
					
				} else if (input == DEC_CHAR) {
					translatedValue = DEC;
					
				} else if (input == OUT_CHAR) {
					translatedValue = OUT;
					
				} else if (input == IN_CHAR) {
					translatedValue = IN;
					
				} else if (input == BEGIN_WHILE_CHAR) {
					translatedValue = BEGIN_WHILE;
					
				} else if (input == END_WHILE_CHAR) {
					translatedValue = END_WHILE;
				}
				
				program.write(pc, translatedValue);
				pc++;
			}

			input = (byte) inputQueue.get();
		}
		
		lastInstruction = pc;
		pc = 0;
	}

	@Blocking
	private void fastforward() {
		loopCounter = 1;
		
		while (loopCounter != 0) {
			pc++;
			
			byte instruction = (byte) program.read(pc);
			
			if (instruction == BEGIN_WHILE) {
				loopCounter++;
				
			} else if (instruction == END_WHILE) {
				loopCounter--;
			} 
		}
		
		pc++;
	}

	@Blocking
	private void rewind() {
		loopCounter = 1;
		
		while (loopCounter != 0) {
			pc--;
			
			long instruction = program.read(pc);
			
			if (instruction == BEGIN_WHILE) {
				loopCounter--;
				
			} else if (instruction == END_WHILE) {
				loopCounter++;
			} 
		}
		
		pc++;
	}

}
