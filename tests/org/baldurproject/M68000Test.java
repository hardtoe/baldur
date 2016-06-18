package org.baldurproject;

import junit.framework.Assert;

import org.baldurproject.compile.BaldurCompilerMain;
import org.junit.Before;
import org.junit.Test;

public class M68000Test {
	private Memory mem;
	private M68000 cpu;

	@Before public void setup() {
		mem = new Memory(1 << 20);
		cpu = new M68000(mem);
	}
	
	@Test public void test_ADD_opmode000_eaMode000() {
		cpu.writeDataRegister(0, 1);
		cpu.writeDataRegister(1, 2);
		
		mem.write(
			0, 
			(13 << 12) | // opcode
			(0 << 9) |   // register
			(0 << 6) |   // opmode
			(0 << 3) |   // effective address mode
			(1 << 0)     // effective address register
		);
		
		cpu.executeInstruction();
		
		Assert.assertEquals("reg0 value", 3, cpu.getDataRegister(0));
	}
	
	
	@Test public void test_ADD_opmode100_eaMode000() {
		cpu.writeDataRegister(0, 1);
		cpu.writeDataRegister(1, 2);
		
		mem.write(
			0, 
			(13 << 12) | // opcode
			(0 << 9) |   // register
			(4 << 6) |   // opmode
			(0 << 3) |   // effective address mode
			(1 << 0)     // effective address register
		);
		
		cpu.executeInstruction();

		Assert.assertEquals("reg0 value", 3, cpu.getDataRegister(1));
	}
	
	@Test public void test_ADD_opmode000_eaMode001() {
		cpu.writeDataRegister(0, 1);
		cpu.writeAddrRegister(1, 2);
		
		mem.write(
			0, 
			(13 << 12) | // opcode
			(0 << 9) |   // register
			(0 << 6) |   // opmode
			(1 << 3) |   // effective address mode
			(1 << 0)     // effective address register
		);
		
		cpu.executeInstruction();

		Assert.assertEquals("reg0 value", 3, cpu.getDataRegister(0));
	}
	
	@Test public void test_ADD_opmode100_eaMode001() {
		cpu.writeDataRegister(0, 1);
		cpu.writeAddrRegister(1, 2);
		
		mem.write(
			0, 
			(13 << 12) | // opcode
			(0 << 9) |   // register
			(4 << 6) |   // opmode
			(1 << 3) |   // effective address mode
			(1 << 0)     // effective address register
		);
		
		cpu.executeInstruction();

		Assert.assertEquals("reg0 value", 3, cpu.getAddrRegister(1));
	}
	
	@Test public void test_ADD_opmode000_eaMode001_usp() {
		cpu.writeDataRegister(0, 1);
		cpu.writeAddrRegister(7, 2);
		
		mem.write(
			0, 
			(13 << 12) | // opcode
			(0 << 9) |   // register
			(0 << 6) |   // opmode
			(1 << 3) |   // effective address mode
			(7 << 0)     // effective address register
		);
		
		cpu.executeInstruction();

		Assert.assertEquals("reg0 value", 3, cpu.getDataRegister(0));
	}
	
	@Test public void test_ADD_opmode000_eaMode010() {
		cpu.writeDataRegister(0, 1);
		cpu.writeAddrRegister(1, 1000);
		
		mem.write(1000, 4);
		
		mem.write(
			0, 
			(13 << 12) | // opcode
			(0 << 9) |   // register
			(0 << 6) |   // opmode
			(2 << 3) |   // effective address mode
			(1 << 0)     // effective address register
		);
		
		cpu.executeInstruction();

		Assert.assertEquals("reg0 value", 5, cpu.getDataRegister(0));
	}
	
	@Test public void test_ADD_opmode100_eaMode010() {
		cpu.writeDataRegister(0, 1);
		cpu.writeAddrRegister(1, 1000);
		
		mem.write(1000, 4);
		
		mem.write(
			0, 
			(13 << 12) | // opcode
			(0 << 9) |   // register
			(4 << 6) |   // opmode
			(2 << 3) |   // effective address mode
			(1 << 0)     // effective address register
		);
		
		cpu.executeInstruction();

		Assert.assertEquals("reg0 value", 5, mem.read(1000));
	}
	
	@Test public void test_ADD_opmode000_eaMode010_usp() {
		cpu.writeDataRegister(0, 1);
		cpu.writeAddrRegister(7, 1000);
		
		mem.write(1000, 4);
		
		mem.write(
			0, 
			(13 << 12) | // opcode
			(0 << 9) |   // register
			(0 << 6) |   // opmode
			(2 << 3) |   // effective address mode
			(7 << 0)     // effective address register
		);
		
		cpu.executeInstruction();

		Assert.assertEquals("reg0 value", 5, cpu.getDataRegister(0));
	}
	
	@Test public void test_ADD_opmode000_eaMode011() {
		cpu.writeDataRegister(0, 1);
		cpu.writeAddrRegister(1, 1000);
		
		mem.write(1000, 4);
		
		mem.write(
			0, 
			(13 << 12) | // opcode
			(0 << 9) |   // register
			(0 << 6) |   // opmode
			(3 << 3) |   // effective address mode
			(1 << 0)     // effective address register
		);
		
		cpu.executeInstruction();

		Assert.assertEquals("reg0 value", 5, cpu.getDataRegister(0));
		Assert.assertEquals("addr1 value", 1001, cpu.getAddrRegister(1));
	}
	
	@Test public void test_ADD_opmode001_eaMode011() {
		cpu.writeDataRegister(0, 1);
		cpu.writeAddrRegister(1, 1000);
		
		mem.write(1000, 4);
		
		mem.write(
			0, 
			(13 << 12) | // opcode
			(0 << 9) |   // register
			(1 << 6) |   // opmode
			(3 << 3) |   // effective address mode
			(1 << 0)     // effective address register
		);
		
		cpu.executeInstruction();

		Assert.assertEquals("reg0 value", 5, cpu.getDataRegister(0));
		Assert.assertEquals("addr1 value", 1002, cpu.getAddrRegister(1));
	}
	
	@Test public void test_ADD_opmode010_eaMode011() {
		cpu.writeDataRegister(0, 1);
		cpu.writeAddrRegister(1, 1000);
		
		mem.write(1000, 4);
		
		mem.write(
			0, 
			(13 << 12) | // opcode
			(0 << 9) |   // register
			(2 << 6) |   // opmode
			(3 << 3) |   // effective address mode
			(1 << 0)     // effective address register
		);
		
		cpu.executeInstruction();

		Assert.assertEquals("reg0 value", 5, cpu.getDataRegister(0));
		Assert.assertEquals("addr1 value", 1004, cpu.getAddrRegister(1));
	}
	
	@Test public void test_ADD_opmode000_eaMode100() {
		cpu.writeDataRegister(0, 1);
		cpu.writeAddrRegister(1, 1000);
		
		mem.write(999, 4);
		
		mem.write(
			0, 
			(13 << 12) | // opcode
			(0 << 9) |   // register
			(0 << 6) |   // opmode
			(4 << 3) |   // effective address mode
			(1 << 0)     // effective address register
		);
		
		cpu.executeInstruction();

		Assert.assertEquals("reg0 value", 5, cpu.getDataRegister(0));
		Assert.assertEquals("addr1 value", 999, cpu.getAddrRegister(1));
	}
	
	@Test public void test_ADD_opmode001_eaMode100() {
		cpu.writeDataRegister(0, 1);
		cpu.writeAddrRegister(1, 1000);
		
		mem.write(998, 4);
		
		mem.write(
			0, 
			(13 << 12) | // opcode
			(0 << 9) |   // register
			(1 << 6) |   // opmode
			(4 << 3) |   // effective address mode
			(1 << 0)     // effective address register
		);
		
		cpu.executeInstruction();

		Assert.assertEquals("reg0 value", 5, cpu.getDataRegister(0));
		Assert.assertEquals("addr1 value", 998, cpu.getAddrRegister(1));
	}
	
	@Test public void test_ADD_opmode010_eaMode100() {
		cpu.writeDataRegister(0, 1);
		cpu.writeAddrRegister(1, 1000);
		
		mem.write(996, 4);
		
		mem.write(
			0, 
			(13 << 12) | // opcode
			(0 << 9) |   // register
			(2 << 6) |   // opmode
			(4 << 3) |   // effective address mode
			(1 << 0)     // effective address register
		);
		
		cpu.executeInstruction();

		Assert.assertEquals("reg0 value", 5, cpu.getDataRegister(0));
		Assert.assertEquals("addr1 value", 996, cpu.getAddrRegister(1));
	}
	
	@Test public void test_ADD_opmode000_eaMode101() {
		cpu.writeDataRegister(0, 1);
		cpu.writeAddrRegister(1, 1000);
		
		mem.write(1010, 4);
		
		mem.write(
			0, 
			(13 << 12) | // opcode
			(0 << 9) |   // register
			(0 << 6) |   // opmode
			(5 << 3) |   // effective address mode
			(1 << 0)     // effective address register
		);

		mem.write(2, 10);
		
		cpu.executeInstruction();

		Assert.assertEquals("reg0 value", 5, cpu.getDataRegister(0));
		Assert.assertEquals("addr1 value", 1000, cpu.getAddrRegister(1));
	}
	
	@Test public void test_ADD_opmode000_eaMode110() {
		cpu.writeDataRegister(0, 1);
		cpu.writeAddrRegister(1, 1000);
		
		mem.write(1040, 4);
		
		mem.write(
			0, 
			(13 << 12) | // opcode
			(0 << 9) |   // register
			(0 << 6) |   // opmode
			(6 << 3) |   // effective address mode
			(1 << 0)     // effective address register
		);

		cpu.writeDataRegister(1, 32);
		mem.write(
			2, 
			(0 << 15) | // 1 = address index reg, 0 = data index reg
			(1 << 12) | // index register
			(0 << 11) | // 1 = long word, 0 = sign-extended word
			(0 << 9)  | // scale, 0 = 1, 1 = 2, 2 = 4, 3 = 8
			(0 << 8)  | // 0 = brief extension word format
			(8 << 0)    // displacement
		);
		
		cpu.executeInstruction();

		Assert.assertEquals("reg0 value", 5, cpu.getDataRegister(0));
		Assert.assertEquals("addr1 value", 1000, cpu.getAddrRegister(1));
	}
	
	@Test public void test_ADD_opmode000_eaMode110_addrRegIdx() {
		cpu.writeDataRegister(0, 1);
		cpu.writeAddrRegister(1, 1000);
		
		mem.write(1040, 4);
		
		mem.write(
			0, 
			(13 << 12) | // opcode
			(0 << 9) |   // register
			(0 << 6) |   // opmode
			(6 << 3) |   // effective address mode
			(1 << 0)     // effective address register
		);

		cpu.writeAddrRegister(2, 32);
		mem.write(
			2, 
			(1 << 15) | // 1 = address index reg, 0 = data index reg
			(2 << 12) | // index register
			(0 << 11) | // 1 = long word, 0 = sign-extended word
			(0 << 9)  | // scale, 0 = 1, 1 = 2, 2 = 4, 3 = 8
			(0 << 8)  | // 0 = brief extension word format
			(8 << 0)    // displacement
		);
		
		cpu.executeInstruction();

		Assert.assertEquals("reg0 value", 5, cpu.getDataRegister(0));
		Assert.assertEquals("addr1 value", 1000, cpu.getAddrRegister(1));
	}
	
	@Test public void test_ADD_opmode000_eaMode111_eaReg000() {
		cpu.writeDataRegister(0, 3);
		
		
		mem.write(
			0, 
			(13 << 12) | // opcode
			(0 << 9) |   // register
			(0 << 6) |   // opmode
			(7 << 3) |   // effective address mode
			(0 << 0)     // effective address register
		);

		mem.write(
			2, 
			2000
		);

		mem.write(
			2000, 
			3
		);
		
		cpu.executeInstruction();

		Assert.assertEquals("reg0 value", 6, cpu.getDataRegister(0));
	}

	
	@Test public void test_ADD_opmode000_eaMode111_eaReg001() {
		cpu.writeDataRegister(0, 3);
		
		
		mem.write(
			0, 
			(13 << 12) | // opcode
			(0 << 9) |   // register
			(0 << 6) |   // opmode
			(7 << 3) |   // effective address mode
			(1 << 0)     // effective address register
		);

		mem.write(
			2, 
			1
		);

		mem.write(
			4, 
			2000
		);

		mem.write(
			(1 << 16) + 2000, 
			3
		);
		
		cpu.executeInstruction();

		Assert.assertEquals("reg0 value", 6, cpu.getDataRegister(0));
	}

	
	@Test public void test_ADD_opmode001_eaMode111_eaReg100() {
		cpu.writeDataRegister(0, 3);
		
		mem.write(
			0, 
			(13 << 12) | // opcode
			(0 << 9) |   // register
			(1 << 6) |   // opmode
			(7 << 3) |   // effective address mode
			(4 << 0)     // effective address register
		);

		mem.write(
			2, 
			3
		);

		mem.write(
			(1 << 16) + 2000, 
			3
		);
		
		cpu.executeInstruction();

		Assert.assertEquals("reg0 value", 6, cpu.getDataRegister(0));
	}

	
	@Test public void compileMacClassic() {
		new BaldurCompilerMain().compile(new String[] {
			"-cp", "core",
			"-bootclasspath", "synthesis",
			"-verbose",
			"-sourcepath", "core;synthesis",
			"-d", "output",
			"-elab", "org.baldurproject.examples.MacClassic",
			
			"synthesis/java/lang/Boolean.java",
			"synthesis/org/baldurproject/InputWire.java", 
			"synthesis/org/baldurproject/OutputWire.java", 
			"core/org/baldurproject/Timer.java", 
			"core/org/baldurproject/Serial.java", 
			"core/org/baldurproject/examples/MacClassic.java"
		});
	}
}
