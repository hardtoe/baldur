package org.baldurproject.examples;

import org.baldurproject.compile.BaldurCompilerMain;
import org.junit.Test;

public class CompileExamplesTest {
	@Test public void compileMorseCodeFlasher() {
		new BaldurCompilerMain().compile(new String[] {
			"-cp", "core",
			"-bootclasspath", "synthesis",
			"-verbose",
			"-sourcepath", "core;synthesis",
			"-d", "output",
			"-elab", "org.baldurproject.examples.MorseCodeFlasher",
			
			"synthesis/java/lang/Boolean.java",
			"synthesis/org/baldurproject/InputWire.java", 
			"synthesis/org/baldurproject/OutputWire.java", 
			"core/org/baldurproject/Timer.java", 
			"core/org/baldurproject/Serial.java", 
			"core/org/baldurproject/examples/MorseCodeFlasher.java",
			"tests/org/baldurproject/compile/ConstantFoldingTest.java"
		});
	}
	
	@Test public void compileBrainFrakCpu() {
		new BaldurCompilerMain().compile(new String[] {
			"-cp", "core",
			"-bootclasspath", "synthesis",
			"-verbose",
			"-sourcepath", "core;synthesis",
			"-d", "output",
			"-elab", "org.baldurproject.examples.SingleBrainFrak",
			
			"synthesis/java/lang/Boolean.java",
			"synthesis/org/baldurproject/InputWire.java", 
			"synthesis/org/baldurproject/OutputWire.java", 
			"core/org/baldurproject/Timer.java", 
			"core/org/baldurproject/Serial.java", 
			"core/org/baldurproject/examples/SingleBrainFrak.java",
			"tests/org/baldurproject/compile/ConstantFoldingTest.java"
		});
	}
	
	@Test public void compileDoubleBrainFrakCpu() {
		new BaldurCompilerMain().compile(new String[] {
			"-cp", "core",
			"-bootclasspath", "synthesis",
			"-verbose",
			"-sourcepath", "core;synthesis",
			"-d", "output",
			"-elab", "org.baldurproject.examples.DoubleBrainFrak",
			
			"synthesis/java/lang/Boolean.java",
			"synthesis/org/baldurproject/InputWire.java", 
			"synthesis/org/baldurproject/OutputWire.java", 
			"core/org/baldurproject/Timer.java", 
			"core/org/baldurproject/Serial.java", 
			"core/org/baldurproject/examples/DoubleBrainFrak.java",
			"tests/org/baldurproject/compile/ConstantFoldingTest.java"
		});
	}
	
	@Test public void compileVgaTester() {
		new BaldurCompilerMain().compile(new String[] {
			"-cp", "core",
			"-bootclasspath", "synthesis",
			"-verbose",
			"-sourcepath", "core;synthesis",
			"-d", "output",
			"-elab", "org.baldurproject.examples.VgaTester",
			
			"synthesis/java/lang/Boolean.java",
			"synthesis/org/baldurproject/InputWire.java", 
			"synthesis/org/baldurproject/OutputWire.java", 
			"core/org/baldurproject/Timer.java", 
			"core/org/baldurproject/Serial.java", 
			"core/org/baldurproject/examples/VgaTester.java"
		});
	}
}
