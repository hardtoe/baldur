package jkit.bytecode.attributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

import jkit.bytecode.BytecodeAttribute;
import jkit.bytecode.BinaryOutputStream;
import jkit.bytecode.Constant;
import jkit.bytecode.Constant.Info;
import jkit.bytecode.Constant.Utf8;
import jkit.compiler.ClassLoader;

public class SourceFile implements BytecodeAttribute {
	private String filename;
	
	public SourceFile(String filename) {
		this.filename = filename;
	}
	
	public String name() {
		return "SourceFile";
	}
	
	public String filename() {
		return filename;
	}
	
	public void write(BinaryOutputStream writer,
			Map<Constant.Info, Integer> constantPool, ClassLoader loader)
			throws IOException {		
		writer.write_u2(constantPool.get(new Constant.Utf8("SourceFile")));
		writer.write_u4(2);
		writer.write_u2(constantPool.get(new Constant.Utf8(filename)));
	}
	
	public void addPoolItems(Set<Constant.Info> constantPool, ClassLoader loader) {		
		Constant.addPoolItem(new Constant.Utf8("SourceFile"), constantPool);
		Constant.addPoolItem(new Constant.Utf8(filename), constantPool);
	}
	
	public void print(PrintWriter output,
			Map<Constant.Info, Integer> constantPool, ClassLoader loader)
			throws IOException {
		output.println("  SourceFile: \"" + filename + "\"");
	}
}
