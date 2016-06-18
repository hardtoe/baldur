package org.baldurproject.compile;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedList;

import jkit.jil.tree.JilClass;
import jkit.jil.tree.JilField;
import jkit.jil.tree.Type;

public abstract class InstanceVisitor {
	protected final BaldurCompiler compiler;

	public InstanceVisitor(final BaldurCompiler compiler) {
		this.compiler = compiler;
	}


	protected JilField getField(
		final String fieldName, 
		final JilClass parentJilClass
	) {
		if (parentJilClass == null) {
			return null;
			
		} else if (parentJilClass.field(fieldName) == null) {
			return getField(fieldName, compiler.get(parentJilClass.superClass()));
			
		} else {
			return (JilField) parentJilClass.field(fieldName);
		}
	}
	
	public void visitObject(
		final Object o
	) throws 
		IllegalArgumentException,
		IllegalAccessException, 
		SecurityException, 
		NoSuchFieldException 
	{
		Class javaClass = 
			o.getClass();
		 
		JilClass jilClass = 
			compiler.get(javaClass);
		
		while (jilClass != null) {
			visitObject(o, javaClass, jilClass);
			jilClass = compiler.get(jilClass.superClass());
		}
	}


	public void visitObject(
		final Object o, 
		Class javaClass, 
		JilClass jilClass
	) throws 
		IllegalAccessException, 
		NoSuchFieldException 
	{
		HashSet<String> fields = 
			compiler.getAllFields(jilClass);
		
		for (String fieldName : fields) {
			Field javaField =
				compiler.getField(javaClass, fieldName);
			
			javaField.setAccessible(true);
	
			if (
				javaField.getType() == Byte.class ||
				javaField.getType() == Short.class ||
				javaField.getType() == Integer.class ||
				javaField.getType() == Long.class ||
				javaField.getType() == Float.class ||
				javaField.getType() == Double.class ||
				javaField.getType() == Boolean.class ||
				javaField.getType() == Character.class ||
				javaField.getType() == byte.class ||
				javaField.getType() == short.class ||
				javaField.getType() == int.class ||
				javaField.getType() == long.class ||
				javaField.getType() == float.class ||
				javaField.getType() == double.class ||
				javaField.getType() == boolean.class ||
				javaField.getType() == char.class
			) {
				visitFieldPrimitive(o, javaField);
	
			} else if (
				javaField.getType().getName().startsWith("[")
			) {	
				Object array = 
					javaField.get(o);
	
				Class componentType =
					javaField.getType().getComponentType();
				
				if (
					componentType == Byte.class ||
					componentType == Short.class ||
					componentType == Integer.class ||
					componentType == Long.class ||
					componentType == Float.class ||
					componentType == Double.class ||
					componentType == Boolean.class ||
					componentType == Character.class ||
					componentType == byte.class ||
					componentType == short.class ||
					componentType == int.class ||
					componentType == long.class ||
					componentType == float.class ||
					componentType == double.class ||
					componentType == boolean.class ||
					componentType == char.class
				) {
					visitFieldPrimitiveArray(o, javaField, array);
					
				} else {
					for (int i = 0; i < Array.getLength(array); i++) {
						visitArrayEntryObject(javaField, array, i);
					}
				}
				
			} else {
				visitFieldObject(o, javaField);
			}
		}
	}

	public abstract void visitFieldPrimitive(
		final Object o, 
		final Field javaField
	) throws 
		IllegalAccessException;

	public abstract void visitFieldPrimitiveArray(
		final Object parent,
		final Field javaField, 
		final Object array);

	public abstract void visitArrayEntryObject(
		final Field javaField,
		final Object array, 
		final int i
	) throws 
		IllegalAccessException,
		NoSuchFieldException;

	public abstract void visitFieldObject(
		final Object o, 
		final Field javaField
	) throws 
		IllegalAccessException,
		NoSuchFieldException;
}