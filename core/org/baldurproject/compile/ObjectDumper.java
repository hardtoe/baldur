package org.baldurproject.compile;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.LinkedList;


public class ObjectDumper extends InstanceVisitor {
	private String value;
	private String indent;
	
	public ObjectDumper(
		final BaldurCompiler compiler
	) {
		super(compiler);
		this.indent = "";
	}
	
	private void indent() {
		indent = indent + "  ";
	}
	
	private void dedent() {
		indent = indent.substring(2);
	}
	
	public String dump(
		final Object o
	) throws 
		IllegalArgumentException, 
		SecurityException, 
		IllegalAccessException, 
		NoSuchFieldException 
	{
		value = "";
		visitObject(o);
		return value;
	}
	
	public void visitFieldNullObject(
		final Field javaField
	) {
		indent();
		
		value += indent + javaField.getGenericType() + " " + javaField.getName() + " = null\n";
		
		dedent();
	}

	public void visitFieldPrimitive(
		final Object o, 
		final Field javaField
	) throws 
		IllegalAccessException 
	{
		indent();
		
		value += indent + javaField.getType().getName() + " " + javaField.getName() + " = " + javaField.get(o) + "\n";
		
		dedent();
	}

	public void visitFieldPrimitiveArray(
		final Object parent,
		final Field javaField, 
		final Object array
	) {					
		indent();
		
		LinkedList values =
			new LinkedList();
		
		for (int i = 0; i < Array.getLength(array); i++) {
			values.add(Array.get(array, i));
		}
		
		value += indent + javaField.getType().getComponentType() + "[] " + javaField.getName() + " = " + values + "\n";
		
		dedent();
	}

	public void visitArrayEntryObject(
		final Field javaField,
		final Object array, 
		final int i
	) throws 
		IllegalAccessException,
		NoSuchFieldException 
	{
		indent();
		
		value += 
			indent + javaField.getType().getComponentType() + " " + javaField.getName() + "[" + i + "] : " + Array.get(array, i).hashCode() + "\n";
		
		visitObject(Array.get(array, i));
		
		dedent();
	}

	public void visitFieldObject(
		final Object parent, 
		final Field javaField
	) throws 
		IllegalAccessException,
		NoSuchFieldException 
	{
		indent();
		
		Object o = 
			javaField.get(parent);
		
		if (o != null) {
			value += 
				indent + 
				javaField.getGenericType() + " " + 
				javaField.getName() + " : " + 
				o.hashCode() + "\n";
			
			visitObject(o);
			
		} else {
			value += 
				indent + 
				javaField.getGenericType() + " " + 
				javaField.getName() + "\n";
		}
		
		dedent();
	}
}
