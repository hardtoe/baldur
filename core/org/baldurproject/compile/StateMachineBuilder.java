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
import java.util.Map.Entry;

import org.baldurproject.compile.StateMachine.Arc;
import org.baldurproject.compile.StateMachine.State;

import com.google.common.collect.LinkedListMultimap;

import jkit.bytecode.*;
import jkit.bytecode.Bytecode.*;
import jkit.bytecode.attributes.*;
import jkit.compiler.Clazz;
import jkit.compiler.ClassLoader;
import jkit.compiler.FieldNotFoundException;
import jkit.compiler.MethodNotFoundException;
import jkit.jil.util.Types;
import jkit.jil.tree.*;
import jkit.jil.tree.JilExpr.Invoke;
import jkit.jil.tree.JilMethod.JilParameter;
import jkit.jil.tree.Type.Primitive;
import jkit.jil.util.Exprs;
import jkit.util.Pair;
import jkit.util.Triple;
import static jkit.compiler.SyntaxError.*;

public final class StateMachineBuilder {
	protected final BaldurCompiler compiler;
	
	public StateMachineBuilder(
		BaldurCompiler compiler
	) {
		this.compiler = compiler;
	}

	public StateMachine build(
		final JilClass jilClass, 
		final JilMethod jilMethod, 
		final VerilogModule module
	) {
		StateMachine sm;

		if (hasAnnotation("org.baldurproject.Process", jilMethod.modifiers())) {
			IState resetState =
				new StateMachine.State();
			
			sm = new StateMachine(resetState);
			sm.addState(resetState);
			
			IState methodBody = 
				buildBody(jilClass, jilMethod, jilMethod.body());
			
			sm.addState(methodBody);
			resetState.arc(methodBody);
			
		} else {
			IState waitState =
				new StateMachine.State();

			sm = new StateMachine(waitState);
			sm.addState(waitState);

			IState methodBody = 
				buildBody(jilClass, jilMethod, jilMethod.body());
			
			sm.addState(methodBody);

			waitState.addTree(jilMethod.doneSignal() + " = 0;");

			waitState.arc(
				"(" + jilMethod.startSignal() + ")", 
				methodBody);

			methodBody.arc(waitState);
			
			waitState.addPreAction(jilMethod.doneSignal() + " = 1;");   
		}
		
		sm.setName(jilMethod.getVerilogName());
		module.addWire("reg " + jilMethod.doneSignal() + " = 0;");
		module.addWire("reg " + jilMethod.startSignal() + " = 0;");
		
		if (!(jilMethod.type().returnType() instanceof Type.Void)) {
			module.addWire("reg " + getVerilogType(jilMethod.type().returnType()) + " " + jilMethod.returnName() + " = 0;");
		}
		
		for (int i = 0; i < jilMethod.parameters().size(); i++) {
			JilParameter param = 
				jilMethod.parameters().get(i);
			
			param.setVerilogName(jilMethod.getVerilogName() + "_" + param.name());
			
			Type type =
				jilMethod.type().parameterTypes().get(i);
			
			allocateLocalSpace(module, param.getVerilogName(), type, "");
		}
		
		for (Entry<String, Type> local : jilMethod.localVarTypes().entrySet()) {
			allocateLocalSpace(module, jilMethod.getVerilogName() + "_" + local.getKey(), local.getValue(), "");
		}
		
		return sm;
	}

	public void allocateLocalSpace(
		final VerilogModule module,
		final String param, 
		final Type type,
		final String suffix
	) {
		if (
			type instanceof Type.Primitive ||
			type instanceof Type.Array // FIXME: passing the value of the array around is not the best thing to do...
		) {
			module.addWire("reg " + getVerilogType(type) + " " + param + suffix + " = 0;");
			
		} else {
			Type.Clazz jilClassType =
				(Type.Clazz) type; 
			
			JilClass jilClass =
				compiler.get(jilClassType);
			
			for (JilField f : jilClass.fields()) {
				allocateLocalSpace(module, param, f.type(), "_" + f.name());
			}
			
		}
	}

	public String getVerilogType(Type type) {
		Type t;
		
		if (type instanceof Type.Array) {
			t = ((Type.Array) type).element();
			
		} else {
			t = type;
		}
		
		if (t instanceof Type.Bool) {
			return "[0:0]";
			
		} else if (t instanceof Type.Byte) {
			return "[7:0]";
			
		} else if (t instanceof Type.Char) {
			return "[7:0]";
			
		} else if (t instanceof Type.Short) {
			return "[15:0]";
			
		} else if (t instanceof Type.Int) {
			return "[31:0]";
			
		} else if (t instanceof Type.Long) {
			return "[63:0]";
			
		} else {
			return "[0:0]"; // FIXME: this is a hack for Net!
			//throw new Error("Unknown type: " + type);
		}
	}


	private boolean lastLabel;
	private boolean lastIfGoto;
	private boolean lastGoto;

	private State currentState; 
	private String currentLabel; 
	
	// FIXME: add end states
	private IState buildBody(
		final JilClass jilClass, 
		final JilMethod jilMethod, 
		final List<JilStmt> body
	) {
		// for each basic block, these are the possible states
		HashMap<String, State> states =
			new HashMap<String, State>();
		
		// for each label, these are the possible exit arcs
		// (src, (predicate, dst))
		LinkedListMultimap<String, Pair<String, String>> arcs =
			LinkedListMultimap.create();
		
		currentState =
			new State();
		
		currentLabel = 
			"START";
		
		states.put(currentLabel, currentState);
		

		StateMachine stateMachine = 
			new StateMachine(states.get("START"));

		lastLabel = true;
		lastIfGoto = false;
		lastGoto = false;

		processMethodBody(
			jilClass, 
			jilMethod, 
			body, 
			states, 
			arcs,
			stateMachine,
			false);
		
		// pass 2: connect gotos to target state
		for (Entry<String, Pair<String, String>> entry : arcs.entries()) {
			State srcState =
				states.get(entry.getKey());
			
			srcState.arc(entry.getValue().first(), states.get(entry.getValue().second()));

		}
		
		// pass 3: add all states to state machine
		IdentityHashMap<State, State> uniqueStates =
			new IdentityHashMap<State, State>();
		
		for (State s : states.values()) {
			uniqueStates.put(s, s);
		}
		
		for (State s : uniqueStates.values()) {
			stateMachine.addState(s);
		}
		
		return stateMachine;
	}
	
	public void processMethodBody(
		final JilClass jilClass,
		final JilMethod jilMethod, 
		final List<JilStmt> body,
		HashMap<String, State> states,
		LinkedListMultimap<String, Pair<String, String>> arcs,
		StateMachine stateMachine,
		final boolean inline
	) {
		
		// pass 1: create states out of basic blocks
		for (JilStmt s : body) {
			if (s instanceof JilStmt.Label) {
				String nextLabel = 
					((JilStmt.Label) s).label();
				
				if (lastLabel) {
					// do nothing special

				} else if (lastGoto) {
					currentState = new State();
					
				} else {
					arcs.put(currentLabel, new Pair<String, String>(null, nextLabel));
					currentState = new State();
				}
				
				currentLabel = nextLabel;
				states.put(currentLabel, currentState);

				lastLabel = true;
				lastIfGoto = false;
				lastGoto = false;
				
			} else if (s instanceof JilStmt.Goto) {
				arcs.put(currentLabel, new Pair<String, String>(null, ((JilStmt.Goto) s).label()));
				
				lastLabel = false;
				lastIfGoto = false;
				lastGoto = true;
				
			} else if (s instanceof JilStmt.IfGoto) {	
				arcs.put(
					currentLabel, 
					new Pair<String, String>(
						((JilStmt.IfGoto) s).condition().toVerilog(jilClass, jilMethod), 
						((JilStmt.IfGoto) s).label()));
				
				lastLabel = false;
				lastIfGoto = true;
				lastGoto = false;
				
			} else if (s instanceof JilStmt.Return) {
				if (!inline) {
					stateMachine.addEndState(currentState);
				}
				
				if (((JilStmt.Return) s).expr() != null) {
					currentState.addTree(jilMethod.returnName() + " = " + ((JilStmt.Return) s).expr().toVerilog(jilClass, jilMethod) + ";");
				}
				
				lastLabel = false;
				lastIfGoto = false;
				lastGoto = false;

			} else {
				if (lastIfGoto) {
					State nextState = new State();
					currentState.arc(nextState);
					currentState = nextState;
					
					currentLabel = currentLabel + "_";
					states.put(currentLabel, currentState);
					
				} else if (lastGoto) {
					currentState = new State();
					currentLabel = currentLabel + "_";
					states.put(currentLabel, currentState);
				}


				if (
					s instanceof JilExpr.Invoke || (
						s instanceof JilStmt.Assign &&
						((JilStmt.Assign) s).rhs() instanceof JilExpr.Invoke)
				) {

					
					JilExpr.Invoke invocation;
					String variableToAssign;
					
					if (s instanceof JilExpr.Invoke){
						invocation = (JilExpr.Invoke) s;
						variableToAssign = null;
						
					} else {
						JilStmt.Assign assignment =
							((JilStmt.Assign) s);
						
						invocation =
							(JilExpr.Invoke) assignment.rhs();
						
						variableToAssign = 
							assignment.lhs().toVerilog(jilClass, jilMethod);
					}
						
					
					Pair<JilClass, JilMethod> classMethodPair =
						invocation.getMethodToInvoke(jilClass);
					
					JilMethod methodToInvoke = 
						classMethodPair.second();
					
					JilClass classOfMethodToInvoke = 
						classMethodPair.first();
					
					String params = 
						invocation.verilogToSetParameters(jilClass, jilMethod, methodToInvoke);
					
					if (hasAnnotation("org.baldurproject.Inline", methodToInvoke.modifiers())) {
						// we already took care of these flags
						lastLabel = false;
						lastIfGoto = false;
						lastGoto = false;
						
						currentState.addTree(params); 
						
						processMethodBody(
							classOfMethodToInvoke, 
							methodToInvoke, 
							methodToInvoke.body(), 
							states, 
							arcs,
							stateMachine,
							true);

					} else {
						if (!lastIfGoto && !lastGoto && !lastLabel) {
							State nextState = new State();
							currentState.arc(nextState);
							currentState = nextState;
							
							currentLabel = currentLabel + "_";
							states.put(currentLabel, currentState);
						}
				
						currentState.addPreAction(params);
						currentState.addPreAction(methodToInvoke.startSignal() + " = 1;");
						currentState.addTree(methodToInvoke.startSignal() + " = 0;");

						State nextState = new State();
						currentState.arc(methodToInvoke.doneSignal(), nextState);
						currentState = nextState;
						
						currentLabel = currentLabel + "_";
						states.put(currentLabel, currentState);
					}

					if (variableToAssign != null) {
						currentState.addTree(variableToAssign + " = " + methodToInvoke.returnName() + ";");
					}
					
		
					
				} else {			
					String verilog = 
						s.toVerilog(jilClass, jilMethod) + ";";
				
					// FIXME: not sure why, but the Jil has some tmp variables with a $ sign in them...they are broken
					if (!verilog.contains("$")) {
						currentState.addTree(verilog);
					}
				}
				
	
				
				lastLabel = false;
				lastIfGoto = false;
				lastGoto = false;
			}
			
		}
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
}