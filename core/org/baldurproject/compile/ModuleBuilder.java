// This file is part of the Java Compiler Kit (JKit)
//
// The Java Compiler Kit is free software; you can 
// redistribute it and/or modify it under the terms of the 
// GNU General Public License as published by the Free Software 
// Foundation; either version 2 of the License, or (at your 
// option) any later version.
//
// The Java Compiler Kit is distributed in the hope
// that it will be useful, but WITHOUT ANY WARRANTY; without 
// even the implied warranty of MERCHANTABILITY or FITNESS FOR 
// A PARTICULAR PURPOSE.  See the GNU General Public License 
// for more details.
//
// You should have received a copy of the GNU General Public 
// License along with the Java Compiler Kit; if not, 
// write to the Free Software Foundation, Inc., 59 Temple Place, 
// Suite 330, Boston, MA  02111-1307  USA
//
// (C) David James Pearce, 2009. 

package org.baldurproject.compile;

import java.lang.reflect.Array;
import java.util.*;

import jkit.bytecode.*;
import jkit.bytecode.Bytecode.*;
import jkit.bytecode.attributes.*;
import jkit.compiler.Clazz;
import jkit.compiler.ClassLoader;
import jkit.compiler.FieldNotFoundException;
import jkit.compiler.MethodNotFoundException;
import jkit.jil.util.Types;
import jkit.jil.tree.*;
import jkit.jil.tree.Type.Primitive;
import jkit.jil.util.Exprs;
import jkit.util.Pair;
import jkit.util.Triple;
import static jkit.compiler.SyntaxError.*;

public final class ModuleBuilder {
	protected final ClassLoader loader;
	protected final BaldurCompiler compiler;
	protected final int version = 49;
	protected final StateMachineBuilder smBuilder;
	
	protected JilField clock;
	
	protected List<StateMachine> stateMachines;
	
	protected HashSet<String> visited = new HashSet<String>();
	
	
	public ModuleBuilder(
		ClassLoader loader,
		BaldurCompiler compiler
	) {
		this.loader = loader;
		this.compiler = compiler;
		this.smBuilder = new StateMachineBuilder(compiler);
		this.stateMachines = new LinkedList<StateMachine>();
	}
	
	public VerilogModule build(
		jkit.jil.tree.JilClass top
	) {				
		String moduleName = 
			top.type().lastComponent().first().toString();
		
		VerilogModule m =
			new VerilogModule(moduleName);
		

		buildFields(top, m);
	
		
		if (clock == null) {
			throw new Error("Could not find clock.");
		}
		
		buildMethods(top, m);		

		// this is a little bit of a hack
		m.addBody("parameter false = 0;");
		m.addBody("parameter true = 1;");

		
		for (StateMachine sm : stateMachines) {
			m.addBody(sm.toVerilogHeader());
		}
		
		m.addBody("initial begin");
		for (StateMachine sm : stateMachines) {
			m.addBody(sm.toVerilogInit());
		}
		m.addBody("end\n");	

		m.addBody("always @(posedge " + clock.getVerilogName() + ") begin");
		for (StateMachine sm : stateMachines) {
			m.addBody(sm.toVerilogBody());
		}
		m.addBody("end");
		
		// build module dot diagram
		System.out.println("digraph {");
		for (StateMachine sm : stateMachines) {
			System.out.println(sm.toDot());
		}
		System.out.println("}");
		
		
		return m;
	}
		
	private boolean hasAnnotation(
		final String name, 
		final List<Modifier> modifiers
	) {
		for (Modifier m : modifiers) {
			if (
				m instanceof Modifier.Annotation &&
				((Modifier.Annotation) m).type().toString().contains(name)
			) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isClock(List<Modifier> modifiers) {
		return hasAnnotation("org.baldurproject.Clock", modifiers);
	}
	
	private boolean isInput(List<Modifier> modifiers) {
		return hasAnnotation("org.baldurproject.Input", modifiers);
	}
	
	private boolean isOutput(List<Modifier> modifiers) {
		return hasAnnotation("org.baldurproject.Output", modifiers);
	}

	private boolean isModule(List<Modifier> modifiers) {
		return hasAnnotation("org.baldurproject.Module", modifiers);
	}
	
	protected void buildOutPorts(
		final JilClass clazz, 
		final VerilogModule m
	) {
		for (JilField f : clazz.fields()) {
			if (visited.contains(f.getVerilogName())) {
				// do nothing
			} else {
				visited.add(f.getVerilogName());
					
				if (f.type() instanceof Type.Primitive) {
					m.addPort("output reg " + f.getVerilogType() + " " + f.getVerilogName() + ",");
					
				} else if (f.type() instanceof Type.Array) {	
					m.addPort("output reg " + f.getVerilogType() + " " + f.getVerilogName() + " " + f.getVerilogArrayDimensions() + ",");
	
					// TODO: only going to init arrays less than 1024 elements
					if (Array.getLength(f.getElabValue()) < 1024) {
						for (int i = 0; i < Array.getLength(f.getElabValue()); i++) {
							m.addInitialization(f.getVerilogName() + "[" + i + "] = " + Array.get(f.getElabValue(), i) + ";");
						}
					}
					
					
				} else if (f.type() instanceof Type.Variable) {
				
				} else if (f.type() instanceof Type.Clazz) {
					buildOutPorts(compiler.get((Type.Clazz) f.type()), m);
				}
			}
		}
	}
	
	protected void buildInPorts(
		final JilClass clazz, 
		final VerilogModule m,
		final boolean clock
	) {
		for (JilField f : clazz.fields()) {
			if (visited.contains(f.getVerilogName())) {
				// do nothing
			} else {
				visited.add(f.getVerilogName());
					
				if (f.type() instanceof Type.Primitive) {
					m.addPort("input " + f.getVerilogType() + " " + f.getVerilogName() + ",");
					
					if (clock) {
						setClock(f);
					}
					
				} else if (f.type() instanceof Type.Array) {	
					m.addPort("input " + f.getVerilogType() + " " + f.getVerilogName() + " " + f.getVerilogArrayDimensions() + ",");
	
					if (clock) {
						setClock(f);
					}
					
				} else if (f.type() instanceof Type.Variable) {
				
				} else if (f.type() instanceof Type.Clazz) {
					buildInPorts(compiler.get((Type.Clazz) f.type()), m, clock);
				}
			}
		}
	}
	
	private void setClock(JilField f) {
		clock = f;
	}

	// FIXME: this doesn't yet support arrays of objects
	protected void buildFields(
		final JilClass jilClass, 
		final VerilogModule m
	) {
		if (jilClass != null) {
			buildFields(compiler.get(jilClass.superClass()), m);

			for (JilField f : jilClass.fields()) {
				if (visited.contains(f.getVerilogName())) {
					// do nothing
				} else {
					visited.add(f.getVerilogName());
					
					if (f.type() instanceof Type.Primitive) {
						if (isInput(f.modifiers())) {
							m.addPort("input " + f.getVerilogType() + " " + f.getVerilogName() + ",");
	
							if (isClock(f.modifiers())) {
								setClock(f);
							}
							
						} else if (isOutput(f.modifiers())) {
							m.addPort("output reg " + f.getVerilogType() + " " + f.getVerilogName() + ",");
							
						} else {
							m.addWire("reg " + f.getVerilogType() + " " + f.getVerilogName() + " = " + f.getElabValue() + ";");
						}
						
						
					} else if (f.type() instanceof Type.Array) {
						if (isInput(f.modifiers())) {
							m.addPort("input " + f.getVerilogType() + " " + f.getVerilogName() + " " + f.getVerilogArrayDimensions() + ",");
							
						} else if (isOutput(f.modifiers())) {
							m.addPort("output reg " + f.getVerilogType() + " " + f.getVerilogName() + " " + f.getVerilogArrayDimensions() + ",");
							
						} else {
							m.addWire("reg " + f.getVerilogType() + " " + f.getVerilogName() + " " + f.getVerilogArrayDimensions() + ";");
						}		
						
						if (!isInput(f.modifiers())) {
							// TODO: only going to init arrays less than 1024 elements
							if (Array.getLength(f.getElabValue()) < 1024) {
								for (int i = 0; i < Array.getLength(f.getElabValue()); i++) {
									m.addInitialization(f.getVerilogName() + "[" + i + "] = " + Array.get(f.getElabValue(), i) + ";");
								}
							}
						}
						
					} else if (f.type() instanceof Type.Variable) {
					
					} else if (f.type() instanceof Type.Clazz) {
						if (isInput(f.modifiers())) {
							buildInPorts(compiler.get((Type.Clazz) f.type()), m, isClock(f.modifiers()));
							
						} else if (isOutput(f.modifiers())) {
							buildOutPorts(compiler.get((Type.Clazz) f.type()), m);
							
						} else {
							buildFields(compiler.get((Type.Clazz) f.type()), m);
						}
						
						buildMethods(compiler.get((Type.Clazz) f.type()), m);
					}
				}
			}
		}
	}

	protected void buildMethods(
		final JilClass jilClass, 
		final VerilogModule m
	) {
		if (jilClass != null) {
			buildMethods(compiler.get(jilClass.superClass()), m);
			
			for (JilMethod method : jilClass.methods()) {
				if (visited.contains(method.getVerilogName())) {
					// do nothing
				} else {
					visited.add(method.getVerilogName());
						
					// modules are constructed at elaboration time
					if (!moduleConstructor(jilClass, method)) {
						StateMachine sm =
							smBuilder.build(jilClass, method, m);
						
							while (
								(
									sm.removeUnreachableStates() +
									sm.removeSimpleTransitiveStates() +
									sm.removeComplexTransitiveStates()
								) > 0);
					
						
						
						stateMachines.add(sm);
					}
				}
			}
		}
	}

	private boolean moduleConstructor(
		final JilClass jilClass, 
		final JilMethod method
	) {
		return
			isModule(jilClass.modifiers()) &&
			method.name().equals(jilClass.name().split("_", 2)[0]);
	}
}
