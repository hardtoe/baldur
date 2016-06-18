package org.baldurproject;

@Module
public class M68000 {
	private final MemoryInterface memory;
	
	public M68000(
		final MemoryInterface memory
	) {
		this.memory = memory;
	}
	
	// MC68000UM Section 2.1.1
	/**
	 * 8 data registers
	 */
	private int[] dataRegs = new int[8];
	
	/**
	 * 7 address registers
	 */
	private int[] addrRegs = new int[7];
	
	/**
	 * user stack pointer
	 */
	public int usp = 0;
	
	/**
	 * program counter
	 */
	public int pc = 0;
	
	/**
	 * user condition code register
	 * 
	 * Section 2.1.3
	 * 
	 * bit 4: extend
	 * bit 3: negative
	 * bit 2: zero
	 * bit 1: overflow
	 * bit 0: carry
	 */
	public byte ccr = 0;
	
	// MC68000UM Section 2.1.2
	/**
	 * supervisor stack pointer
	 */
	public int ssp = 0;
	
	/**
	 * upper 8 bits of supervisor status register
	 * 
	 * Section 2.1.3
	 * 
	 * bit 7:   trace mode
	 * bit 5:   supervisor state
	 * bit 2-0: interrupt mask
	 */
	public byte sr = 0; 
	
	/**
	 * vector base register
	 */
	public int vbr = 0;
	
	/**
	 * alternate function code register
	 */
	public byte sfc = 0;

	/**
	 * alternate function code register
	 */
	public byte dfc = 0;
	
	@Blocking
	public void reset() {
		addrRegs[0] = 0;
		addrRegs[1] = 0;
		addrRegs[2] = 0;
		addrRegs[3] = 0;
		addrRegs[4] = 0;
		addrRegs[5] = 0;
		addrRegs[6] = 0;
		
		dataRegs[0] = 0;	
		dataRegs[1] = 0;	
		dataRegs[2] = 0;	
		dataRegs[3] = 0;	
		dataRegs[4] = 0;	
		dataRegs[5] = 0;	
		dataRegs[6] = 0;	
		dataRegs[7] = 0;

		usp = 0;
		pc = 0;
		ccr = 0;
		ssp = 0;
		sr = 0; 
		vbr = 0;
		sfc = 0;
		dfc = 0;
	}
	
	@Blocking
	public int getDataRegister(int addr) {
		return dataRegs[addr];
	}
	
	@Blocking
	public void writeDataRegister(int addr, int value) {
		dataRegs[addr] = value;
	}
	
	@Blocking
	public int getAddrRegister(int addr) {
		if (addr == 7) {
			return usp;
			
		} else {
			return addrRegs[addr];
		}
	}
	
	@Blocking
	public void writeAddrRegister(int addr, int value) {
		if (addr == 7) {
			usp = value;
			
		} else {
			addrRegs[addr] = value;
		}
	}

	@Blocking
	public void run() {
		while (true) {
			executeInstruction();
		}
	}

	@Blocking
	@Inline
	public void executeInstruction() {
		// fetch next instruction
		short instruction =
			(short) memory.read(pc);

		
		// decode instruction fields
		byte opcode =
			(byte) ((instruction >> 12) & 0xF);
		
		byte register =
			(byte) ((instruction >> 9) & 0x7);
		
		byte opmode =
			(byte) ((instruction >> 6) & 0x7);
		
		byte effectiveAddressMode =
			(byte) ((instruction >> 3) & 0x7);
		
		byte effectiveAddressRegister =
			(byte) ((instruction >> 0) & 0x7);
	
		byte opmodeSize =
			(byte) (1 << (opmode & 0x3));
		
		
		// retrieve operand from register
		int regOperandData =
			dataRegs[register];
		
		
		// retrieve operand from effective address
		int eaOperandData = 0; 
		
		int effectiveAddress = 0;
		
		if (effectiveAddressMode == 0) {
			eaOperandData =
				dataRegs[effectiveAddressRegister];
		
		} else if (effectiveAddressMode == 1) {
			eaOperandData =
				getAddrRegister(effectiveAddressRegister);
		
		} else  {
			if (effectiveAddressMode == 2) {
			
				effectiveAddress = 
					getAddrRegister(effectiveAddressRegister);
				
				eaOperandData =
					(int) memory.read(effectiveAddress);
			
			
			} else if (effectiveAddressMode == 3) {
				effectiveAddress = 
					getAddrRegister(effectiveAddressRegister);
				
				eaOperandData =
					(int) memory.read(effectiveAddress);
			
				writeAddrRegister(
					effectiveAddressRegister,  
					getAddrRegister(effectiveAddressRegister) + opmodeSize);
					
			} else if (effectiveAddressMode == 4) {
				writeAddrRegister(
					effectiveAddressRegister,  
					getAddrRegister(effectiveAddressRegister) - opmodeSize);
				
				effectiveAddress =
					getAddrRegister(effectiveAddressRegister);
				
				eaOperandData =
					(int) memory.read(effectiveAddress);
				
			} else if (effectiveAddressMode == 5) {
				pc = pc + 2;
				
				effectiveAddress =
					(int) (getAddrRegister(effectiveAddressRegister) +
					memory.read(pc));
					
				eaOperandData =
					(int) memory.read(effectiveAddress);
				
			} else if (effectiveAddressMode == 6) {
				pc = pc + 2;
				
				short briefExtensionWord =
					(short) memory.read(pc);
				
				boolean idxIsAddrRegister = 
					((briefExtensionWord >> 15) & 1) == 1;
				
				byte idxRegister =
					(byte) ((briefExtensionWord >> 12) & 0x7);
				
				boolean idxIsLongWord = 
					((briefExtensionWord >> 11) & 1) == 1;
				
				byte scaleFactor =
					(byte) ((briefExtensionWord >> 9) & 0x3);
				
				byte displacement =
					(byte) ((briefExtensionWord >> 0) & 0xFF);
				
				int idxRegValue;
				
				if (idxIsAddrRegister) {
					idxRegValue = getAddrRegister(idxRegister);
					
				} else {
					idxRegValue = dataRegs[idxRegister];
				}
				
				effectiveAddress =
					(int) (getAddrRegister(effectiveAddressRegister) +
					displacement +
					(idxRegValue << scaleFactor));
				
				eaOperandData =
					(int) memory.read(effectiveAddress); 
				
			} else if (effectiveAddressMode == 7 && effectiveAddressRegister == 0) {
				pc = pc + 2;
				
				effectiveAddress = 
					(int) memory.read(pc);
				
				eaOperandData =
					(int) memory.read(effectiveAddress);
						
			} else if (
				effectiveAddressMode == 7 && 
				effectiveAddressRegister == 1
			) {
				pc = pc + 2;
				
				effectiveAddress = 
					(int) (memory.read(pc) << 16);
				
				pc = pc + 2;
				
				effectiveAddress = 
					(int) (effectiveAddress | memory.read(pc));
				
				eaOperandData =
					(int) memory.read(effectiveAddress);
				
			} else if (
				effectiveAddressMode == 7 && 
				effectiveAddressRegister == 4 && 
				(opmode & 0x3) == 1
			) {
				pc = pc + 2;
				
				eaOperandData = 
					(int) (memory.read(pc));
				
			} else if (
				effectiveAddressMode == 7 && 
				effectiveAddressRegister == 1 && 
				(opmode & 0x3) == 2
			) {
				pc = pc + 2;
				
				eaOperandData = 
					(int) (memory.read(pc) << 16);
				
				pc = pc + 2;
				
				eaOperandData = 
					(int) (eaOperandData | memory.read(pc));
				
			} 
		}
		
		
		// calculate result
		// FIXME: need to change the ordering of these operands depending on mode
		boolean destinationIsEffectiveAddress =
				((opmode >> 2) & 1) == 1;
			
		int aluResult = 0;
		
		if (opcode == 13) {
			aluResult =
				regOperandData + 
				eaOperandData;
			
		} else if (opcode == 12) {
			aluResult =
				regOperandData & 
				eaOperandData;
			
		} else if (opcode == 11) {
			aluResult =
				regOperandData ^ 
				eaOperandData;
			
		} else if (opcode == 9 && !destinationIsEffectiveAddress) {
			aluResult =
				regOperandData - 
				eaOperandData;
			
		} else if (opcode == 9 && destinationIsEffectiveAddress) {
			aluResult =
				eaOperandData -
				regOperandData;
			
		} else if (opcode == 8) {
			aluResult =
				regOperandData |
				eaOperandData;
			
		}
		
		// write back result to the destination operand
		if (!destinationIsEffectiveAddress) {
			dataRegs[register] = aluResult;
			
		} else if (destinationIsEffectiveAddress && effectiveAddressMode == 0) {
			dataRegs[effectiveAddressRegister] = aluResult;

		} else if (destinationIsEffectiveAddress && effectiveAddressMode == 1) {
			writeAddrRegister(effectiveAddressRegister, aluResult);
				
		} else {
			memory.write(effectiveAddress, aluResult);	
		}
		
		
		// next instruction
		pc = pc + 2;
	}
}
