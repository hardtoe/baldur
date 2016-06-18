package org.baldurproject.compile;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jkit.compiler.ClassLoader;
import jkit.compiler.SyntacticAttribute;
import jkit.jil.tree.JilClass;
import jkit.jil.tree.JilField;
import jkit.jil.tree.JilMethod;
import jkit.jil.tree.JilMethod.JilParameter;
import jkit.jil.tree.Type;
import jkit.jil.tree.Type.Clazz;
import jkit.jil.tree.Type.Reference;
import jkit.util.Pair;
import jkit.util.Triple;

import com.rits.cloning.Cloner;

/**
 * TODO: propogate generics...
 * 
 * FIXME: keep super class fields stay in the superclass. right now they get pulled into the subclass.
 *        once this is done, make sure ModuleBuilder.buildFields is correct as well
 *        
 * TODO: parse objects contained within arrays.
 */

public class ElabBuilder extends InstanceVisitor {
	private final LinkedList<Triple<Class<?>, JilClass, String>> stack;
	private final IdentityHashMap<Object, JilClass> elaboratedObjects;
	private final Cloner cloner;
	private Field parentJavaField;
	
	public ElabBuilder(
		final BaldurCompiler compiler
	) {
		super(compiler);
		
		stack = 
			new LinkedList<Triple<Class<?>,JilClass,String>>();
		
		elaboratedObjects =
			new IdentityHashMap<Object, JilClass>();
		
		cloner = 
			new Cloner();
		
		cloner.dontCloneInstanceOf(
			ClassLoader.class, 
			SyntacticAttribute.class, 
			Type.class);
	}

	@Override
	public void visitFieldPrimitive(
		final Object parent, 
		final Field javaField
	) throws 
		IllegalAccessException 
	{
		Object o =
			javaField.get(parent);
		
		String fieldName =
			javaField.getName();

		JilClass parentJilClass = 
			currentJilClass();
		
		JilField parentJilField = 
			getField(fieldName, parentJilClass);

		parentJilField.setElabValue(o);
		parentJilField.setVerilogName(parentJilClass.type().toString() + "_" + fieldName);
	}

	@Override
	public void visitFieldPrimitiveArray(
		final Object parent,
		final Field javaField, 
		final Object o
	) {		
		String fieldName =
			javaField.getName();

		JilClass parentJilClass = 
			currentJilClass();
		
		JilField parentJilField = 
			getField(fieldName, parentJilClass);

		parentJilField.setElabValue(o);
		parentJilField.setVerilogName(parentJilClass.type().toString() + "_" + fieldName);
	}

	@Override
	public void visitArrayEntryObject(
		final Field javaField, 
		final Object array, 
		final int i
	) throws 
		IllegalAccessException, 
		NoSuchFieldException
	{
		Object o =
			Array.get(array, i);
		
		push(
			o,
			javaField.getName() + "_" + i, 
			o.getClass(), 
			compiler.get(o.getClass()));

		visitObject(o);
		
		pop();
	}

	@Override
	public void visitFieldObject(
		final Object parent, 
		final Field javaField
	) throws 
		IllegalAccessException, 
		NoSuchFieldException
	{
		Object o =
			javaField.get(parent);
		
		String fieldName =
			javaField.getName();

		JilClass parentJilClass = 
			currentJilClass();
		
		JilField parentJilField = 
			getField(fieldName, parentJilClass);
		
		if (o != null) {

			parentJavaField = javaField;
			
			push(
				o,
				javaField.getName(), 
				o.getClass(), 
				compiler.get(o.getClass()));
	
			JilClass fieldJilClass =
				currentJilClass();
			
			/*
			parentJilClass.fields().remove(parentJilField);
			
			JilField elabParentJilField =
				new JilField(
					parentJilField.name(),
					fieldJilClass.type(),
					parentJilField.modifiers(),
					parentJilField.attributes());
			
			parentJilClass.fields().add(elabParentJilField);
			
			elabParentJilField.setVerilogName(parentJilClass.type().toString() + "_" + fieldName);
			*/

			parentJilField.setVerilogName(parentJilClass.type().toString() + "_" + fieldName);
			parentJilField.setType(fieldJilClass.type());
			
			parentJavaField = javaField;
			visitObject(o);
	
			pop();
			
		} else {
			parentJilField.setElabValue(o);
			parentJilField.setVerilogName(parentJilClass.type().toString() + "_" + fieldName);
		}
	
	}
	
	@Override
	public void visitObject(
		final Object o
	) throws 
		IllegalArgumentException,
		IllegalAccessException, 
		SecurityException, 
		NoSuchFieldException 
	{

			
		super.visitObject(o);
	}
	
	public JilClass build(
		final Object topLive
	) throws 
		IllegalArgumentException, 
		SecurityException, 
		IllegalAccessException, 
		NoSuchFieldException 
	{
		push(
			topLive,
			"top", 
			topLive.getClass(), 
			compiler.get(topLive.getClass()));
		
		visitObject(topLive);
		
		return pop().second();
	}

	private boolean isPrimitive(Object o) {
		return 
			o.getClass() == Byte.class ||
			o.getClass() == Short.class ||
			o.getClass() == Integer.class ||
			o.getClass() == Long.class ||
			o.getClass() == Float.class ||
			o.getClass() == Double.class ||
			o.getClass() == Boolean.class ||
			o.getClass() == Character.class ||
			o.getClass() == byte.class ||
			o.getClass() == short.class ||
			o.getClass() == int.class ||
			o.getClass() == long.class ||
			o.getClass() == float.class ||
			o.getClass() == double.class ||
			o.getClass() == boolean.class ||
			o.getClass() == char.class;
	}
	
	private void push(
		final Object o,
		final String name, 
		final Class<? extends Object> javaClass, 
		final JilClass jilClass
	) {
		JilClass elabJilClass;
		
		if (
			elaboratedObjects.containsKey(o) &&
			!isPrimitive(o)
		) {
			elabJilClass =
				elaboratedObjects.get(o);
			
			stack.push(
				new Triple<Class<?>, JilClass, String>(
					javaClass, 
					elabJilClass,
					name));
			
		} else {
			elabJilClass = 
				cloner.deepClone(jilClass);
			
			System.out.println(elabJilClass.type());
			
			if (parentJavaField != null) {
				System.out.println("not null");
				
				String genericTypeName = 
					parentJavaField.getGenericType().toString().replaceAll("^.+<", "").replaceAll(">", "");
			
				if (
					!genericTypeName.equals(o.getClass().getCanonicalName()) &&
					parentJavaField.getGenericType().toString().contains("<") 
				) {
					System.out.println("replacing generics for " + elabJilClass.name());
					replaceGenerics(elabJilClass, genericTypeName);
				}	
			}
			
			Clazz jilClassType =
				elabJilClass.type();
			
			stack.push(
				new Triple<Class<?>, JilClass, String>(
					javaClass, 
					elabJilClass,
					name));
			

			Clazz elabJilClassType = 
				elabJilClassType(jilClassType); 

			elabJilClass.setType(elabJilClassType);
			compiler.addCompiledClass(elabJilClass);
			
			allocateMethodNames(elabJilClass);
			
			elaborateInheritanceHierarchy(elabJilClass);
			
			elaboratedObjects.put(o, elabJilClass);
		}
	}
	
	// FIXME: this doesn't handle replacing generics in the super class
	private void elaborateInheritanceHierarchy(
		JilClass subClass
	) {
		JilClass superClass =
			compiler.get(subClass.superClass());
		
		if (superClass != null) {
			JilClass elabSuperClass =
				cloner.deepClone(superClass);
			
			Clazz elabSuperClassType =
				elabJilClassType(elabSuperClass.type());
			
			elabSuperClass.setType(elabSuperClassType);
			subClass.setSuperClass(elabSuperClassType);
			
			compiler.addCompiledClass(elabSuperClass);
			
			allocateMethodNames(elabSuperClass);
			
			elaborateInheritanceHierarchy(elabSuperClass);
			
		}
	}
	
	
	private void allocateMethodNames(
		final JilClass jilClass
	) {
		String baseName =
			jilClass.type().getVerilogName();
		
		for (JilMethod m : jilClass.methods()) {
			String signature = "";
			
			for (Type p : m.type().parameterTypes()) {
				signature += p.toString().replaceFirst("^.+\\.", "");
			}
			
			if (signature.length() == 0) {
				m.setVerilogName(baseName + "_" + m.name());
				
			} else {
				m.setVerilogName(baseName + "_" + m.name() + "_" + signature);
			}
		}
	}

	// FIXME: this needs to replace generics everywhere in the class.  right now it only takes care of fields.
	// FIXME: this needs to search for specific generic variables and replace them. right now it replaces all variables with the generic type.
	private void replaceGenerics(
		final JilClass jilClass, 
		final String genericTypeName
	) {
		Clazz genericType = 
			compiler.get(genericTypeName).type();
		
		for (JilField f : jilClass.fields()) {
			if (f.type() instanceof Type.Variable) {
				System.out.println("replaced field!");
				f.setType(genericType);		
			}
		}
		
		for (JilMethod m : jilClass.methods()) {
			for (int i = 0; i < m.type().parameterTypes().size(); i++) {
				Type t = 
					m.type().parameterTypes().get(i);
				
				if (t instanceof Type.Variable) {
					System.out.println("replaced parameter!");
					m.type().parameterTypes().set(i, genericType);	
				}
			}
			
			Type t = 
				m.type().returnType();
			
			if (t instanceof Type.Variable) {
				System.out.println("replaced return!");
				m.type().setReturnType(genericType);	
			}
		}
		
	}

	public Clazz elabJilClassType(Clazz jilClassType) {
		List<Pair<String, List<Type.Reference>>> components =
			new LinkedList<Pair<String, List<Type.Reference>>>();
		
		Iterator<Pair<String, List<Reference>>> i = 
			jilClassType.components().iterator();
		
		while (i.hasNext()) {
			Pair<String, List<Type.Reference>> component = i.next();
			
			if (i.hasNext()) {
				components.add(component);
				
			} else {
				components.add(
					new Pair<String, List<Reference>>(
						jilClassType.lastComponent().first() + hierarchyString(), 
						component.second()));
			}
		}
		
		Clazz elabJilClassType =
			new Clazz(
				jilClassType.pkg(), 
				components);
		
		return elabJilClassType;
	}
	
	private Triple<Class<?>, JilClass, String> pop() {
		return stack.pop();
	}
	
	private Triple<Class<?>, JilClass, String> peek() {
		return stack.peek();
	}
	
	private String hierarchyString() {
		String hierarchy = "";
		
		for (Triple<Class<?>, JilClass, String> t : stack) {
			hierarchy = "_" + t.third() + hierarchy;
		}
		
		return hierarchy;
	}
	
	private JilClass currentJilClass() {
		return peek().second();
	}
	
	private Class<?> currentJavaClass() {
		return peek().first();
	}
}
