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

package jkit.jil.tree;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.*;

import javax.management.RuntimeErrorException;

import org.baldurproject.compile.BaldurCompiler;

import jkit.compiler.Clazz.Field;
import jkit.compiler.SyntacticAttribute;
import jkit.compiler.SyntacticElement;
import jkit.compiler.SyntacticElementImpl;
import jkit.jil.tree.JilMethod.JilParameter;
import jkit.jil.tree.Type.Clazz;
import jkit.util.Pair;

public interface JilExpr extends SyntacticElement, Cloneable, JilNode {
	
	public Type type();
	
	public static abstract class AbstractExpr extends JilNodeImpl {
		private final ArrayList<SyntacticAttribute> attributes;
		
		public AbstractExpr(
			final LinkedList<JilNode> children,
			final SyntacticAttribute... attributes
		) {
			super(children);
			this.attributes = new ArrayList();
			for(SyntacticAttribute a : attributes) {
				this.attributes.add(a);
			}
		}
		
		public AbstractExpr(
			final LinkedList<JilNode> children,
			final List<SyntacticAttribute> attributes
		) {
			super(children);
			this.attributes = new ArrayList(attributes);			
		}
				
		public <T extends SyntacticAttribute> T attribute(java.lang.Class<T> ac) {
			for(SyntacticAttribute a : attributes) {
				if(a.getClass().equals(ac)) {
					return (T) a;
				}
			}
			return null;
		}
		
		public <T extends SyntacticAttribute> List<T> attributes(java.lang.Class<T> ac) {
			ArrayList<T> r = new ArrayList();
			for(SyntacticAttribute a : attributes) {
				if(a.getClass().equals(ac)) {
					r.add((T) a);
				}
			}
			return r;
		}
		
		public List<SyntacticAttribute> attributes() {
			// this is to prevent any kind of aliasing issues.
			return attributes;
		}
	}
	
	/**
	 * A Variable object represents a local variable access. A variable
	 * access is considered local if the variable is declared within the current
	 * method. Non-local variable accesses correspond to variables which are
	 * declared outside the current method.
	 * 
	 * @author djp
	 * 
	 */
	public static final class Variable extends AbstractExpr implements JilExpr {
		private final String value;
		private final Type type;

		public Variable(
			final String value, 
			final Type type, 
			final SyntacticAttribute... attributes
		) {
			super(JilNodeImpl.list(), attributes);
			
			if(value == null) {
				throw new IllegalArgumentException("supplied variable cannot be null");
			} 
			
			if(type == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.value = value;
			this.type = type;
		}
		
		public Variable(
			final String value, 
			final Type type, 
			final List<SyntacticAttribute> attributes
		) {
			super(JilNodeImpl.list(), attributes);
			
			if(value == null) {
				throw new IllegalArgumentException("supplied variable cannot be null");
			} 
			
			if(type == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.value = value;
			this.type = type;
		}

		public String value() {
			return value;
		}		
		
		public Type type() {
			return type;
		}	
		
		public String toString() {
			return value;
		}

		// FIXME: this doesn't handle super class fields accessed through this
		public String toVerilog(
			final JilClass jilClass, 
			final JilMethod method
		) {
			//throw new UnsupportedOperationException("This method should not be called");
			return method.getVerilogName() + "_" + value;
			
			//return "variable(" + value + ")";
			/*
			if (value.equals("this")) {
				return jilClass.type().getVerilogName();
				
			} else if (value.equals("super")) {
				Clazz superType = 
					jilClass.superClass();
				
				return superType.getVerilogName();
			
			} else {
				return method.getVerilogName() + "_" + value;
			}
			*/
		}

		public void replaceWith(JilNode child, JilNode replacement) {
			throw new UnsupportedOperationException("This JilNode doesn't have children");
		}
	}
	
	/**
	 * An static Class Access
	 * 
	 * @author djp
	 * 
	 */
	public static final class ClassVariable extends JilNodeImpl implements JilExpr {		
		private final Type.Clazz type;

		public ClassVariable(
			final Type.Clazz type, 
			final SyntacticAttribute... attributes
		) {
			super(JilNodeImpl.list(), attributes);	
			
			if(type == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.type = type;
		}

		public ClassVariable(
			final Type.Clazz type, 
			final List<SyntacticAttribute> attributes
		) {
			super(JilNodeImpl.list(), attributes);
			
			if(type == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.type = type;
		}

		public Type.Clazz type() {
			return type;
		}
		
		public String toString() {
			return type.toString();
		}

		public String toVerilog(
			final JilClass jilClass, 
			final JilMethod method
		) {
			return type.toString();
		}

		public void replaceWith(JilNode child, JilNode replacement) {
			throw new UnsupportedOperationException("This JilNode doesn't have children");
		}
	}

	
	/**
	 * Represents an explicit cast.
	 * 
	 * @author djp
	 *
	 */
	public static final class Cast extends JilNodeImpl implements JilExpr {
		private JilExpr expr;
		private final Type type;

		public Cast(
			final JilExpr expr, 
			final Type type, 
			final SyntacticAttribute... attributes
		) {
			super(JilNodeImpl.list(expr), attributes);
			
			if(expr == null) {
				throw new IllegalArgumentException("supplied expression cannot be null");
			}
			
			if(type == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.expr = expr;
			this.type = type;
		}

		public Cast(
			final JilExpr expr, 
			final Type type, 
			final List<SyntacticAttribute> attributes
		) {
			super(JilNodeImpl.list(expr), attributes);
			
			if(expr == null) {
				throw new IllegalArgumentException("supplied expression cannot be null");
			}
			
			if(type == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.expr = expr;
			this.type = type;
		}
		
		public JilExpr expr() {
			return expr;
		}

		public Type type() {
			return type;
		}
		
		public String toString() {
			return "(" + type.toString() + ")" + expr;
		}

		public String toVerilog(
			final JilClass jilClass, 
			final JilMethod method
		) {
			return expr.toVerilog(jilClass, method);
		}

		public void replaceWith(
			final JilNode child, 
			final JilNode replacement
		) {
			expr = (JilExpr) replacement;
			children().remove(child);
			children().add(replacement);
		}
	}

	/**
	 * Represents an implicit type conversion between primitive types.
	 * 
	 * @author djp
	 *
	 */
	public static final class Convert extends JilNodeImpl implements JilExpr {
		private JilExpr expr;
		private final Type.Primitive type;

		public Convert(
			final Type.Primitive type, 
			final JilExpr expr, 
			final SyntacticAttribute... attributes
		) {
			super(JilNodeImpl.list(expr), attributes);
			
			if(expr == null) {
				throw new IllegalArgumentException("supplied expression cannot be null");
			}
			
			if(type == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.expr = expr;
			this.type = type;
		}

		public Convert(
			final Type.Primitive type, 
			final JilExpr expr, 
			final List<SyntacticAttribute> attributes
		) {
			super(JilNodeImpl.list(expr), attributes);
			
			if(expr == null) {
				throw new IllegalArgumentException("supplied expression cannot be null");
			}
			
			if(type == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.expr = expr;
			this.type = type;
		}
		
		public JilExpr expr() {
			return expr;
		}

		public Type.Primitive type() {
			return type;
		}		
		
		public String toString() {
			return "[" + type.toString() + "]" + expr;
		}

		public String toVerilog(
			final JilClass jilClass, 
			final JilMethod method
		) {
			// FIXME: this is broken for floating point, otherwise OK
			return expr.toVerilog(jilClass, method);
		}

		public void replaceWith(
			final JilNode child, 
			final JilNode replacement
		) {
			expr = (JilExpr) replacement;
			children().remove(child);
			children().add(replacement);
		}
	}
	
	/**
	 * Represents an InstanceOf binary operation.
	 * 
	 * @author djp
	 * 
	 */
	public static final class InstanceOf extends JilNodeImpl implements JilExpr {
		private JilExpr lhs;
		private final Type.Reference rhs;
		private final Type type;

		public InstanceOf(
			final JilExpr lhs, 
			final Type.Reference rhs, 
			final Type type, 
			final SyntacticAttribute... attributes
		) {
			super(JilNodeImpl.list(lhs), attributes);
			
			if(lhs == null) {
				throw new IllegalArgumentException("supplied expression cannot be null");
			}			
			
			if(type == null || rhs == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.lhs = lhs;
			this.rhs = rhs;
			this.type = type;
		}

		public InstanceOf(
			final JilExpr lhs, 
			final Type.Reference rhs, 
			final Type type, 
			final List<SyntacticAttribute> attributes
		) {
			super(JilNodeImpl.list(lhs), attributes);
			
			if(lhs == null) {
				throw new IllegalArgumentException("supplied expression cannot be null");
			}
			
			if(type == null || rhs == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.lhs = lhs;
			this.rhs = rhs;
			this.type = type;
		}
		
		public JilExpr lhs() {
			return lhs;
		}

		public Type.Reference rhs() {
			return rhs;
		}
		
		public Type type() {
			return type;
		}
		
		public String toString() {
			return "(" + lhs.toString() + " instanceof " + rhs + ")";
		}

		public String toVerilog(
			final JilClass jilClass, 
			final JilMethod method
		) {
			throw new UnsupportedOperationException("instanceof is not yet supported");
		}

		public void replaceWith(
			final JilNode child, 
			final JilNode replacement
		) {
			lhs = (JilExpr) replacement;
			children().remove(child);
			children().add(replacement);
		}
	}

	/**
	 * Represents Unary Arithmetic Operators
	 * 
	 * @author djp
	 * 
	 */
	public static final class UnOp extends JilNodeImpl implements JilExpr {
		public static final int NOT = 0;
		public static final int INV = 1;
		public static final int NEG = 2;

		private JilExpr expr;
		private final int op;
		private final Type.Primitive type;
		
		public UnOp(
			final JilExpr expr, 
			final int op, 
			final Type.Primitive type, 
			final SyntacticAttribute... attributes
		) {
			super(JilNodeImpl.list(expr), attributes);
			
			if(expr == null) {
				throw new IllegalArgumentException("supplied expression cannot be null");
			}
			
			if(type == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.expr = expr;
			this.op = op;
			this.type = type;
		}

		public UnOp(
			final JilExpr expr, 
			final int op, 
			final Type.Primitive type, 
			final List<SyntacticAttribute> attributes
		) {
			super(JilNodeImpl.list(expr), attributes);
			
			if(expr == null) {
				throw new IllegalArgumentException("supplied expression cannot be null");
			}
			
			if(type == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.expr = expr;
			this.op = op;
			this.type = type;
		}
		
		public int op() {
			return op;
		}

		public JilExpr expr() {
			return expr;
		}
		
		public Type.Primitive type() {
			return type;
		}
		
		public static final String[] unopstr={"!","~","-","++","--","++","--"};	
		
		public String toString() {
			return unopstr[op] + "(" + expr + ")";
		}

		public String toVerilog(
			final JilClass jilClass, 
			final JilMethod method
		) {
			if (op < 3) {
				return unopstr[op] + "(" + expr.toVerilog(jilClass, method) + ")";
			
			} else if (unopstr[op].equals("++") || unopstr[op].equals("--")) {
				return "(" + unopstr[op] + " = (" + expr.toVerilog(jilClass, method) + ") + 1)";
			
			} else {
				throw new InvalidParameterException("op = " + op);
			}
		}

		public void replaceWith(
			final JilNode child, 
			final JilNode replacement
		) {
			expr = (JilExpr) replacement;
			children().remove(child);
			children().add(replacement);
		}
	}

	/**
	 * A Binary Operator.  E.g. +.-,*,/,<,<=,>,?=,==,!=, etc.
	 * 
	 * @author djp
	 * 
	 */
	public static final class BinOp extends JilNodeImpl implements JilExpr {
		// BinOp Constants
		public static final int ADD = 0;
		public static final int SUB = 1;
		public static final int MUL = 2;
		public static final int DIV = 3;
		public static final int MOD = 4;
		public static final int SHL = 5;
		public static final int SHR = 6;
		public static final int USHR = 7;
		public static final int AND = 8;
		public static final int OR = 9;
		public static final int XOR = 10;

		public static final int LT = 11;
		public static final int LTEQ = 12;
		public static final int GT = 13;
		public static final int GTEQ = 14;
		public static final int EQ = 15;
		public static final int NEQ = 16;
		public static final int LAND = 17;
		public static final int LOR = 18;

		private JilExpr lhs;
		private JilExpr rhs;
		private final int op;
		private final Type.Primitive type;

		public BinOp(
			final JilExpr lhs, 
			final JilExpr rhs, 
			final int op, 
			final Type.Primitive type, 
			final SyntacticAttribute... attributes
		) {
			super(JilNodeImpl.list(lhs, rhs), attributes);
			
			if(lhs == null || rhs == null) {
				throw new IllegalArgumentException("supplied expression(s) cannot be null");
			}
			
			if(type == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.lhs = lhs;
			this.rhs = rhs;
			this.op = op;
			this.type = type;
		}

		public BinOp(
			final JilExpr lhs, 
			final JilExpr rhs, 
			final int op, 
			final Type.Primitive type, 
			final List<SyntacticAttribute> attributes
		) {
			super(JilNodeImpl.list(lhs, rhs), attributes);
			
			if(lhs == null || rhs == null) {
				throw new IllegalArgumentException("supplied expression(s) cannot be null");
			}
			
			if(type == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.lhs = lhs;
			this.rhs = rhs;
			this.op = op;
			this.type = type;
		}
		
		public int op() {
			return op;
		}

		public JilExpr lhs() {
			return lhs;
		}
		
		public JilExpr rhs() {
			return rhs;
		}
		
		public Type.Primitive type() {
			return type;
		}		
		
		protected static final String[] binopstr = {"+", "-", "*", "/", "%", "<<",
			">>", ">>>", "&", "|", "^", "<", "<=", ">", ">=", "==", "!=", "&&",
			"||", "+"};
		
		public String toString() {
			return "(" + lhs + binopstr[op] + rhs + ")";
		}

		public String toVerilog(
			final JilClass jilClass, 
			final JilMethod method
		) {
			return "(" + lhs.toVerilog(jilClass, method) + binopstr[op] + rhs.toVerilog(jilClass, method) + ")";
		}

		public void replaceWith(
			final JilNode child, 
			final JilNode replacement
		) {
			if (child == lhs) {	
				lhs = (JilExpr) replacement;
			}

			if (child == rhs) {	
				rhs = (JilExpr) replacement;
			}
			
			children().remove(child);
			children().add(replacement);
		}
	}

	/**
	 * Represents a method call. The method call be either "polymorphic", or
	 * "non-polymorphic". The former means the method will be called on the
	 * dynamic type of the received, whilst the latter means that the method
	 * will be called directly on the static type of the receiver.
	 * 
	 * @author djp
	 * 
	 */
	public static class Invoke extends JilStmt.AbstractStmt implements JilExpr {
		protected JilExpr target;
		protected final String name;
		protected final ArrayList<JilExpr> parameters;
		protected final Type type; 		
		protected final Type.Function funType;		
				
		/**
		 * Construct a method which may, or may not be polymorphic.
		 * 
		 * @param target
		 *            The expression from which the receiver is determined
		 * @param name
		 *            The name of the method
		 * @param parameters
		 *            The parameters of the method
		 * @param funType
		 *            The function type of the method being called.
		 * @param type
		 *            the return type from this expression. Note that this can
		 *            differ from the return type of funType, especially in the
		 *            case of generics.
		 */
		public Invoke(
			final JilExpr target, 
			final String name, 
			final List<JilExpr> parameters,
			final Type.Function funType, 
			final Type type, 
			final SyntacticAttribute... attributes
		) {
			super(JilNodeImpl.list(parameters, target), attributes);
			
			if(target == null) {
				throw new IllegalArgumentException("supplied expression(s) cannot be null");
			}
			
			if(parameters == null || parameters.contains(null)) {
				throw new IllegalArgumentException("supplied parameter(s) cannot be null");
			}
			
			if(type == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.target = target;
			this.name = name;
			this.parameters = new ArrayList(parameters);
			this.type = type;
			this.funType = funType;
		}
		
		/**
		 * Construct a method which may, or may not be polymorphic.
		 * 
		 * @param target
		 *            The expression from which the receiver is determined
		 * @param name
		 *            The name of the method
		 * @param parameters
		 *            The parameters of the method
		 * @param funType
		 *            The function type of the method being called.
		 * @param type
		 *            the return type from this expression. Note that this can
		 *            differ from the return type of funType, especially in the
		 *            case of generics.
		 */
		public Invoke(
			final JilExpr target, 
			final String name, 
			final List<JilExpr> parameters,
			final Type.Function funType, 
			final Type type, 
			final List<SyntacticAttribute> attributes
		) {
			super(JilNodeImpl.list(parameters, target), attributes);
			
			if(target == null) {
				throw new IllegalArgumentException("supplied expression(s) cannot be null");
			}
			
			if(parameters == null || parameters.contains(null)) {
				throw new IllegalArgumentException("supplied parameter(s) cannot be null");
			}
			
			if(type == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.target = target;
			this.name = name;
			this.parameters = new ArrayList(parameters);			
			this.type = type;
			this.funType = funType;
		}
		
		public Invoke(
			final JilExpr target, 
			final String name, 
			final List<JilExpr> parameters,
			final Type.Function funType, 
			final Type type,
			final List<Pair<Type.Clazz, String>> exceptions,
			final List<SyntacticAttribute> attributes
		) {
			super(JilNodeImpl.list(parameters, target), exceptions, attributes);
			
			if(target == null) {
				throw new IllegalArgumentException("supplied expression(s) cannot be null");
			}
			
			if(parameters == null || parameters.contains(null)) {
				throw new IllegalArgumentException("supplied parameter(s) cannot be null");
			}
			
			if(type == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.target = target;
			this.name = name;
			this.parameters = new ArrayList(parameters);			
			this.type = type;
			this.funType = funType;
		}
		
		public JilExpr target() {
			return target;
		}
		
		public String name() {
			return name;
		}

		public Type type() {
			return type;
		}
		
		public Type.Function funType() {
			return funType;
		}
		
		public List<? extends JilExpr> parameters() {
			return parameters;
		}
		
		public Invoke clone() {
			return 
				new Invoke(
					target, 
					name, 
					parameters, 
					funType, 
					type,
					(List<Pair<Type.Clazz, String>>) exceptions(),
					attributes());
		}
		
		public String toString() {
			String r = target.toString() + "." + name + "(";
			boolean firstTime = true;
			for(JilExpr p : parameters) {
				if(!firstTime) {
					r += ", ";
				}
				firstTime=false;
				r += p.toString();
			}
			return r + ")";
		}

		public Pair<JilClass, JilMethod> getMethodToInvoke(
			final JilClass jilClass
		) {
			if (target.toString().equals("this")) {
				return findMethod(jilClass);

			} else if (target.toString().equals("super")) {
				return findMethod(BaldurCompiler.get().get(jilClass.superClass()));
			
			} else {
				JilExpr.Deref deref =
					(JilExpr.Deref) target;

				Field field =
					deref.getMemberField(jilClass);
				
				JilClass fieldClass = 
					BaldurCompiler.get().get((Type.Clazz) field.type());

				return findMethod(fieldClass);
			}
		}
		
		public String toVerilog(
			final JilClass jilClass, 
			final JilMethod method
		) {	
			throw new UnsupportedOperationException();
			//return method.returnName();
			
			/*
			if (target.toString().equals("this")) {
				JilMethod methodToInvoke = 
					findMethod(jilClass).second();
				
				String params = 
					verilogToSetParameters(jilClass, method, methodToInvoke);
				
				return params + methodToInvoke.startSignal() + " = 1";
				
			} else if (target.toString().equals("super")) {
				JilMethod methodToInvoke =
					findMethod(BaldurCompiler.get().get(jilClass.superClass())).second();
				
				String params = 
					verilogToSetParameters(jilClass, method, methodToInvoke);
				
				return params + methodToInvoke.getVerilogName() + " = 1";
			
			} else {
				JilExpr.Deref deref =
					(JilExpr.Deref) target;

				Field field =
					deref.getMemberField(jilClass);
				
				JilClass fieldClass = 
					BaldurCompiler.get().get((Type.Clazz) field.type());

				JilMethod methodToInvoke =
					findMethod(fieldClass).second();
				
				String params = 
						verilogToSetParameters(jilClass, method, methodToInvoke);
				
				return params + methodToInvoke.getVerilogName() + " = 1";
			}
			*/
		}

		// FIXME: need to assign the leafs of objects
		public String verilogToSetParameters(
			final JilClass jilClass, 
			final JilMethod method,
			final JilMethod methodToInvoke
		) {
			String params = "";
			
			for (int i = 0; i < parameters.size(); i++) {
				JilExpr givenParam = 
					parameters.get(i);
				
				JilParameter methodParam =
					methodToInvoke.parameters().get(i);
				
				Type paramType =
					methodToInvoke.type().parameterTypes().get(i);
				
				if (
					paramType instanceof Type.Primitive ||
					paramType instanceof Type.Array
				) {
					params += methodToInvoke.getVerilogName() + "_" + methodParam.name() + " = " + givenParam.toVerilog(jilClass, method) + ";\n";
					
				} else {
					Type.Clazz paramClassType =
						(Type.Clazz) paramType; 
					
					JilClass paramClass =
						BaldurCompiler.get().get(paramClassType);
					
					for (JilField f : paramClass.fields()) {
						//allocateParameterSpace(module, param, f.type(), "_" + f.name());
						// FIXME: make this recursive
						// FIXME: dereference the right-hand side and retrieve those leaves
						params += 
							methodToInvoke.getVerilogName() + "_" + methodParam.name() + "_" + f.name() + " = " + 
							givenParam.toVerilog(jilClass, method) + ";\n";
					}
					
				}
				
			}
			
			return params;
		}

		public Pair<JilClass, JilMethod> findMethod(
			final JilClass fieldClass
		) {
			if (fieldClass != null) {
				for (JilMethod m : fieldClass.methods(name)) {
					if (argsMatch(m)) {
						return new Pair<JilClass, JilMethod>(fieldClass, m);
					}
				}
				
				return findMethod(BaldurCompiler.get().get(fieldClass.superClass()));
						
			} else {	
				throw new Error("Can't find method for this invocation.");
			}
		}
		

		

		private boolean argsMatch(
			final JilMethod m
		) {
			if (parameters.size() == m.parameters().size()) {
				for (int i = 0; i < parameters.size(); i++) {
					JilExpr givenParam = 
						parameters.get(i);
					
					Type paramType = 
						m.type().parameterTypes().get(i);
					
					if (!instanceOf(givenParam.type(), paramType)) {
						return false;
					}
				}
				
				return true;
				
			} else {
				return false;
			}
		}

		private boolean instanceOf(
			final Type givenType, 
			final Type requiredType
		) {
			// TODO: i sure hope this does what i think it does....
			return 
				givenType.equals(requiredType) ||
				givenType.compareTo(requiredType) <= 0;
		}

		public Type.Clazz getMemberClass(
			final JilClass jilClass
		) {
			if (jilClass.field(name) != null) {
				return jilClass.type();
			
			} else if (jilClass.methods(name).size() > 0) {
				return jilClass.type(); // FIXME: need to pick the right method
				
			} else {
				return getMemberClass(BaldurCompiler.get().get(jilClass.superClass()));
			}
		}

		public void replaceWith(
			final JilNode child, 
			final JilNode replacement
		) {
			if (child == target) {	
				target = (JilExpr) replacement;
			}

			if (parameters.contains(child)) {	
				int i = parameters.indexOf(child);
				parameters.set(i, (JilExpr) replacement);
			}
			
			children().remove(child);
			children().add(replacement);
		}
	}

	/**
	 * A special invoke represents a method call that will be translated into an
	 * invokespecial bytecode. It may seem unnecessary to have this explicit in
	 * the JIL intermediate representation. However, in some cases, the user
	 * needs to be able to state this explicitly. For example, consider explicit
	 * super-class method calls. E.g. "super.add()" from a class extending
	 * ArrayList. In this case, the method call is "non-polymorphic". That is,
	 * it does not adhere to the normal dynamic dispatch rules, and will not
	 * dynamically dispatch based on the actual type of the receiver. This must
	 * be so, since otherwise any such super call would likely end causing an
	 * infinte loop!
	 * 
	 * @author djp
	 * 
	 */
	public static final class SpecialInvoke extends Invoke {
		/**
		 * Construct a method which may, or may not be polymorphic.
		 * 
		 * @param target
		 *            The expression from which the receiver is determined
		 * @param name
		 *            The name of the method
		 * @param parameters
		 *            The parameters of the method
		 * @param funType
		 *            The function type of the method being called.
		 * @param type
		 *            the return type from this expression. Note that this can
		 *            differ from the return type of funType, especially in the
		 *            case of generics.
		 */
		public SpecialInvoke(JilExpr target, String name, List<JilExpr> parameters,
				Type.Function funType, Type type, SyntacticAttribute... attributes) {
			super(target,name,parameters,funType,type,attributes);
		}
		
		/**
		 * Construct a method which may, or may not be polymorphic.
		 * 
		 * @param target
		 *            The expression from which the receiver is determined
		 * @param name
		 *            The name of the method
		 * @param parameters
		 *            The parameters of the method
		 * @param funType
		 *            The function type of the method being called.
		 * @param type
		 *            the return type from this expression. Note that this can
		 *            differ from the return type of funType, especially in the
		 *            case of generics.
		 */
		public SpecialInvoke(JilExpr target, String name, List<JilExpr> parameters,
				Type.Function funType, Type type, List<SyntacticAttribute> attributes) {
			super(target,name,parameters,funType,type,attributes);			
		}
		
		public SpecialInvoke(JilExpr target, String name,
				List<JilExpr> parameters, Type.Function funType, Type type,
				List<Pair<Type.Clazz, String>> exceptions,
				List<SyntacticAttribute> attributes) {
			super(target,name,parameters,funType,type,exceptions,attributes);			
		}
		
		public Invoke clone() {
			// Note, the unsafe cast below is actually safe!
			return new SpecialInvoke(target(), name(),
					(List<JilExpr>) parameters(), funType(), type(),
					(List<Pair<Type.Clazz, String>>) exceptions(),attributes());
		}
		
		public String toString() {
			String r = target.toString() + "." + name + "!(";
			boolean firstTime = true;
			for(JilExpr p : parameters) {
				if(!firstTime) {
					r += ", ";
				}
				firstTime=false;
				r += p.toString();
			}
			return r + ")";
		}
	}
		
	
	/**
	 * Represents the new operator. The parameters provided are either passed to
	 * that object's constructor, or are used to determine the necessary array
	 * dimensions (e.g. in new array[x+1]). Observe that, if this new operator
	 * declares an anonymous class, then this can include various declarations.
	 * 
	 * @author djp
	 * 
	 */
	public static final class New extends JilStmt.AbstractStmt implements JilExpr {
		private final Type.Reference type;		
		private final ArrayList<JilExpr> parameters;
		private final Type.Function funType;
		
		/**
		 * Create an AST node represent a new statement or expression.
		 * 
		 * @param type -
		 *            the type being constructed e.g. java.lang.String or
		 *            Integer[]
		 * @param parameters -
		 *            The parameters (if any) supplied to the constructor.
		 *            Should be an empty (i.e. non-null) list.
		 * @param funType
		 *            The static type of the constructor to be called. Note, if
		 *            this new call is for an array, then you can pass null
		 *            here.
		 */
		public New(
			final Type.Reference type, 
			final List<JilExpr> parameters,
			final Type.Function funType, 
			final SyntacticAttribute... attributes
		) {
			super(JilNodeImpl.list(parameters), attributes);	
			
			if(parameters == null || parameters.contains(null)) {
				throw new IllegalArgumentException("supplied parameter(s) cannot be null");
			}
			
			if(type == null || funType == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.type = type;			
			this.parameters = new ArrayList(parameters);			
			this.funType = funType;
		}

		/**
		 * Create an AST node represent a new statement or expression.
		 * 
		 * @param type -
		 *            the type being constructed e.g. java.lang.String or
		 *            Integer[]
		 * @param parameters -
		 *            The parameters (if any) supplied to the constructor.
		 *            Should be an empty (i.e. non-null) list .
		 */
		public New(
			final Type.Reference type, 
			final List<JilExpr> parameters,
			final Type.Function funType, 
			final List<SyntacticAttribute> attributes
		) {
			super(JilNodeImpl.list(parameters), attributes);
			
			if(parameters == null || parameters.contains(null)) {
				throw new IllegalArgumentException("supplied parameter(s) cannot be null");
			}
			
			if(type == null || funType == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.type = type;			
			this.parameters = new ArrayList(parameters);			
			this.funType = funType;
		}
		
		public New(
			final Type.Reference type, 
			final List<JilExpr> parameters,
			final Type.Function funType,
			final List<Pair<Type.Clazz, String>> exceptions,
			final List<SyntacticAttribute> attributes
		) {
			super(JilNodeImpl.list(parameters), exceptions, attributes);
			
			if(parameters == null || parameters.contains(null)) {
				throw new IllegalArgumentException("supplied parameter(s) cannot be null");
			}
			
			if(type == null || funType == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.type = type;			
			this.parameters = new ArrayList(parameters);			
			this.funType = funType;
		}
		
		public Type.Reference type() {
			return type;
		}

		public Type.Function funType() {
			return funType;
		}
		
		public List<? extends JilExpr> parameters() {
			return parameters;
		}
		
		public New clone() {
			return 
				new New(
					type, 
					parameters, 
					funType, 
					(List<Pair<Type.Clazz, String>>) exceptions(),
					attributes());
		}
		
		public String toString() {
			String r = "new " + type + "(";
			boolean firstTime = true;
			for(JilExpr p : parameters) {
				if(!firstTime) {
					r += ", ";
				}
				firstTime=false;
				r += p.toString();
			}
			return r + ")";
		}

		public String toVerilog(
			final JilClass jilClass, 
			final JilMethod method
		) {
			String r = "new " + type + "(";
			boolean firstTime = true;
			for(JilExpr p : parameters) {
				if(!firstTime) {
					r += ", ";
				}
				firstTime=false;
				r += p.toVerilog(jilClass, method);
			}
			return r + ")";
		}

		public void replaceWith(
			final JilNode child, 
			final JilNode replacement
		) {
			if (parameters.contains(child)) {	
				int i = parameters.indexOf(child);
				parameters.set(i, (JilExpr) replacement);
			}
			
			children().remove(child);
			children().add(replacement);
		}
	}

	/**
	 * Represents the act of derefencing a field.
	 * 
	 * @author djp
	 * 
	 */
	public static final class Deref extends AbstractExpr implements JilExpr {
		private JilExpr target;
		private final String name;
		private final Type type;
		private final boolean isStatic;
		
		public Deref(
			JilExpr lhs, 
			String rhs, 
			boolean isStatic, 
			Type type,
			SyntacticAttribute... attributes
		) {
			super(JilNodeImpl.list(lhs), attributes);
			
			if(lhs == null) {
				throw new IllegalArgumentException("supplied expression cannot be null");
			}
			
			if(rhs == null) {
				throw new IllegalArgumentException("supplied string cannot be null");
			}
			
			if(type == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.target = lhs;
			this.name = rhs;
			this.type = type;
			this.isStatic = isStatic;
		}

		public Deref(
			JilExpr lhs, 
			String rhs, 
			boolean isStatic, 
			Type type, 
			List<SyntacticAttribute> attributes
		) {
			super(JilNodeImpl.list(lhs), attributes);
			
			if(lhs == null) {
				throw new IllegalArgumentException("supplied expression cannot be null");
			}
			
			if(rhs == null) {
				throw new IllegalArgumentException("supplied string cannot be null");
			}
			
			if(type == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.target = lhs;
			this.name = rhs;
			this.type = type;
			this.isStatic = isStatic;
		}
		
		public JilExpr target() {
			return target;
		}
		
		public String name() {
			return name;
		}
				
		public Type type() {
			return type;
		}
		
		public boolean isStatic() {
			return isStatic;
		}		
		
		public String toString() {
			return target + "." + name;
		}

		// FIXME: this doesn't currently handle boxing/unboxing...
		public String toVerilog(
			final JilClass jilClass, 
			final JilMethod method
		) {
			if (target.toString().equals("this")) {
				return getMemberClass(jilClass).getVerilogName() + "_" + name;
				
			} else if (target.toString().equals("super")) {
				return getMemberClass(BaldurCompiler.get().get(jilClass.superClass())).getVerilogName() + "_" + name;
			
			} else {
				return method.getVerilogName() + "_" + name;
			}
			
			
			//return "deref(" + target.type() + ", " + name + ")";
			//return target.toVerilog(jilClass, method) + "_" + name;
		}
		

		public Type.Clazz getMemberClass(
			final JilClass jilClass
		) {
			if (jilClass.field(name) != null) {
				return jilClass.type();
			
			} else if (jilClass.methods(name).size() > 0) {
				return jilClass.type(); // FIXME: need to pick the right method
				
			} else {
				return getMemberClass(BaldurCompiler.get().get(jilClass.superClass()));
			}
		}
		
		public Field getMemberField(
			final JilClass jilClass
		) {
			if (jilClass.field(name) != null) {
				return jilClass.field(name);

			} else {
				return getMemberField(BaldurCompiler.get().get(jilClass.superClass()));
			}
		}

		public void replaceWith(
			final JilNode child, 
			final JilNode replacement
		) {
			target = (JilExpr) replacement;
			
			children().remove(child);
			children().add(replacement);
		}
	}

	/**
	 * Represents an index into an array. E.g. A[i] is an index into array A.
	 * 
	 * @author djp
	 * 
	 */
	public static final class ArrayIndex extends AbstractExpr implements JilExpr {
		private JilExpr array;
		private JilExpr idx;
		private final Type type;

		public ArrayIndex(
			final JilExpr array, 
			final JilExpr idx, 
			final Type type, 
			final SyntacticAttribute... attributes
		) {
			super(JilNodeImpl.list(array, idx), attributes);
			
			if(array == null || idx == null) {
				throw new IllegalArgumentException("supplied expression(s) cannot be null");
			}			
			
			if(type == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.array = array;
			this.idx = idx;
			this.type = type;
		}

		public ArrayIndex(
			final JilExpr array, 
			final JilExpr idx, 
			final Type type, 
			final List<SyntacticAttribute> attributes
		) {
			super(JilNodeImpl.list(array, idx), attributes);
			
			if(array == null || idx == null) {
				throw new IllegalArgumentException("supplied expression(s) cannot be null");
			}			
			
			if(type == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.array = array;
			this.idx = idx;
			this.type = type;
		}
		
		public JilExpr target() {
			return array;
		}

		public JilExpr index() {
			return idx;
		}
		
		public Type type() {
			return type;
		}
		
		public String toString() {
			return array + "[" + idx + "]";
		}

		public String toVerilog(
			final JilClass jilClass, 
			final JilMethod method
		) {
			return array.toVerilog(jilClass, method) + "[" + idx.toVerilog(jilClass, method) + "]";
		}

		public void replaceWith(
			final JilNode child, 
			final JilNode replacement
		) {
			if (child == array) {	
				array = (JilExpr) replacement;
			}
			
			if (child == idx) {
				idx = (JilExpr) replacement;
			}
			
			children().remove(child);
			children().add(replacement);
		}
	}
	
	public static interface Value extends JilExpr {}
	
	/**
	 * Represents a numerical constant
	 * 
	 * @author djp
	 *
	 */
	public static class Number extends AbstractExpr implements JilExpr,Value {
		protected final int value;
		private final Type.Primitive type;
		
		public Number(
			final int value, 
			final Type.Primitive type, 
			final SyntacticAttribute... attributes
		) {
			super(JilNodeImpl.list(), attributes);	
			
			if(type == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.value = value;
			this.type = type;
		}
		
		public Number(
			final int value, 
			final Type.Primitive type, 
			final List<SyntacticAttribute> attributes
		) {
			super(JilNodeImpl.list(), attributes);
			
			if(type == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.value = value;
			this.type = type;
		}
		
		public int intValue() {
			return value;
		}
		
		public Type.Primitive type() {
			return type;
		}

		public String toVerilog(
			final JilClass jilClass, 
			final JilMethod method
		) {
			return value + "";
		}		

		public void replaceWith(JilNode child, JilNode replacement) {
			throw new UnsupportedOperationException("This JilNode doesn't have children");
		}
	}
	
	/**
	 * A boolean constant.
	 * 
	 * @author djp
	 *
	 */
	public static final class Bool extends Number {
		public Bool(boolean value, SyntacticAttribute... attributes) {
			super(value?1:0, new Type.Bool(), attributes);
		}
		
		public Bool(boolean value, List<SyntacticAttribute> attributes) {
			super(value?1:0, new Type.Bool(), attributes);
		}
		
		public boolean value() {
			return value==1;
		}
		
		public String toString() {
			if(value==1) { return "true"; }
			else { return "false"; }
		}
	}
	
	/**
	 * Represents a character constant.
	 * 
	 * @author djp
	 *
	 */
	public static final class Char extends Number {
		public Char(char value, SyntacticAttribute... attributes) {
			super(value,new Type.Char(),attributes);
		}
		
		public Char(char value, List<SyntacticAttribute> attributes) {
			super(value,new Type.Char(),attributes);
		}
		
		public char value() {
			return (char)value;
		}
		
		public String toString() {
			return "'" + (char)value + "'"; 
		}
	}
	
	/**
	 * Represents a byte constant.
	 * 
	 * @author djp
	 *
	 */
	public static final class Byte extends Number {
		public Byte(byte value, SyntacticAttribute... attributes) {
			super(value,new Type.Byte(), attributes);
		}
		
		public Byte(byte value, List<SyntacticAttribute> attributes) {
			super(value,new Type.Byte(), attributes);
		}
		
		public byte value() {
			return (byte)value;
		}
		
		public String toString() {
			return value + "b"; 
		}
	}
	
	/**
	 * Represents a short constant.
	 * @author djp
	 *
	 */
	public static final class Short extends Number {
		public Short(short value, SyntacticAttribute... attributes) {
			super(value, new Type.Short(), attributes);
		}
		
		public Short(short value, List<SyntacticAttribute> attributes) {
			super(value, new Type.Short(), attributes);
		}
		
		public short value() {
			return (short)value;
		}
		
		public String toString() {
			return value + "s"; 
		}
	}

	/**
     * Represents an int constant.
     * 
     * @author djp
     * 
     */	
	public static final class Int extends Number {
		public Int(int value, SyntacticAttribute... attributes) {
			super(value,new Type.Int(), attributes);
		}
		
		public Int(int value, List<SyntacticAttribute> attributes) {
			super(value,new Type.Int(), attributes);
		}
		
		public int value() {
			return value;
		}
		public String toString() {
			return java.lang.Integer.toString(value);			
		}
	}

	/**
     * Represents a long Constant.
     * 
     * @author djp
     * 
     */
	public static final class Long extends AbstractExpr implements JilExpr,Value {
		private final long value;
		
		public Long(
			final long value, 
			final SyntacticAttribute... attributes
		) {
			super(JilNodeImpl.list(), attributes);			
			this.value=value;
		}
		
		public Long(
			final long value, 
			final List<SyntacticAttribute> attributes
		) {
			super(JilNodeImpl.list(), attributes);
			this.value=value;
		}
		
		public long value() {
			return value;
		}
		
		public Type.Long type() {
			return new Type.Long();
		}
		public String toString() {
			return value + "l"; 
		}

		public String toVerilog(
			final JilClass jilClass, 
			final JilMethod method
		) {
			return value + "";
		}

		public void replaceWith(JilNode child, JilNode replacement) {
			throw new UnsupportedOperationException("This JilNode doesn't have children");
		}
	}
	
	/**
     * A Float Constant.
     * 
     * @author djp
     * 
     */
	public static final class Float extends AbstractExpr implements JilExpr,Value {
		private final float value;
		
		public Float(
			final float value, 
			final SyntacticAttribute... attributes
		) {
			super(JilNodeImpl.list(), attributes);
			this.value=value;
		}
		
		public Float(
			final float value, 
			final List<SyntacticAttribute> attributes
		) {
			super(JilNodeImpl.list(), attributes);
			this.value=value;
		}
		
		public float value() {
			return value;
		}
		
		public Type.Float type() {
			return new Type.Float();
		}
		public String toString() {
			return value + "f"; 
		}

		public String toVerilog(
			final JilClass jilClass, 
			final JilMethod method
		) {
			return toString();
		}

		public void replaceWith(JilNode child, JilNode replacement) {
			throw new UnsupportedOperationException("This JilNode doesn't have children");
		}
	}

	/**
     * A Double Constant.
     * 
     * @author djp
     * 
     */
	public static final class Double extends AbstractExpr implements JilExpr,Value {
		private final double value;
		
		public Double(
			final double value, 
			final SyntacticAttribute... attributes
		) {
			super(JilNodeImpl.list(), attributes);
			this.value=value;
		}
		
		public Double(
			final double value, 
			final List<SyntacticAttribute> attributes
		) {
			super(JilNodeImpl.list(), attributes);
			this.value=value;
		}
		
		public double value() {
			return value;
		}
		
		public Type.Double type() {
			return new Type.Double();
		}
		public String toString() {
			return java.lang.Double.toString(value); 
		}

		public String toVerilog(
			final JilClass jilClass, 
			final JilMethod method
		) {
			return toString();
		}

		public void replaceWith(JilNode child, JilNode replacement) {
			throw new UnsupportedOperationException("This JilNode doesn't have children");
		}
	}
	
	/**
     * A String Constant.
     * 
     * @author djp
     * 
     */
	public static final class StringVal extends AbstractExpr implements JilExpr,Value {
		private final String value;
		
		public StringVal(
			final String value, 
			final SyntacticAttribute... attributes
		) {
			super(JilNodeImpl.list(), attributes);
			this.value=value;
		}
		
		public StringVal(
			final String value, 
			final List<SyntacticAttribute> attributes
		) {
			super(JilNodeImpl.list(), attributes);
			this.value = value;
		}
		
		public java.lang.String value() {
			return value;
		}
		
		public Type.Clazz type() {
			return jkit.jil.util.Types.JAVA_LANG_STRING;			
		}
		public String toString() {
			return "\"" + value + "\""; 
		}

		public String toVerilog(
			final JilClass jilClass, 
			final JilMethod method
		) {
			return toString();
		}

		public void replaceWith(JilNode child, JilNode replacement) {
			throw new UnsupportedOperationException("This JilNode doesn't have children");
		}
	}		
	
	/**
     * The null Constant.
     * 
     * @author djp
     * 
     */
	public static final class Null extends AbstractExpr implements JilExpr,Value {
		public Null(
			final SyntacticAttribute... attributes
		) {
			super(JilNodeImpl.list(), attributes);
		}
		
		public Type.Null type() {
			return jkit.jil.util.Types.T_NULL;
		}
		
		public String toString() {
			return "null"; 
		}

		public String toVerilog(
			final JilClass jilClass, 
			final JilMethod method
		) {
			return "0";
		}

		public void replaceWith(JilNode child, JilNode replacement) {
			throw new UnsupportedOperationException("This JilNode doesn't have children");
		}
	}
			
	/**
     * An array constant (used for array initialisers only).
     * 
     * @author djp
     * 
     */
	public static final class Array extends AbstractExpr implements JilExpr,Value {
		private final ArrayList<JilExpr> values;
		private final Type.Array type;
		
		public Array(
			final List<JilExpr> values, 
			final Type.Array type, 
			final SyntacticAttribute... attributes
		) {
			super(JilNodeImpl.list(values), attributes);
			
			if(values == null || values.contains(null)) {
				throw new IllegalArgumentException("supplied expression(s) cannot be null");
			}
			
			if(type == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.values = new ArrayList(values);
			this.type = type;
		}
		
		public Array(
			final List<JilExpr> values, 
			final Type.Array type, 
			final List<SyntacticAttribute> attributes
		) {
			super(JilNodeImpl.list(values), attributes);
			
			if(values == null || values.contains(null)) {
				throw new IllegalArgumentException("supplied expression(s) cannot be null");
			}
			
			if(type == null) {
				throw new IllegalArgumentException("supplied type cannot be null");
			}
			
			this.values = new ArrayList(values);
			this.type = type;
		}
		
		public List<JilExpr> values() {
			return values;
		}
		
		public Type.Array type() {
			return type;
		}
		
		public String toString() {
			String r = type + "{";
			boolean firstTime=true;
			for(JilExpr v : values) {
				if(!firstTime) {
					r += ", ";
				}
				firstTime = false;
				r += v.toString();
			}
			return r + "}";
		}

		public String toVerilog(
			final JilClass jilClass, 
			final JilMethod method
		) {
			String r = type + "{";
			boolean firstTime=true;
			for(JilExpr v : values) {
				if(!firstTime) {
					r += ", ";
				}
				firstTime = false;
				r += v.toVerilog(jilClass, method);
			}
			return r + "}";
		}

		public void replaceWith(
			final JilNode child, 
			final JilNode replacement
		) {
			if (values.contains(child)) {	
				int i = values.indexOf(child);
				values.set(i, (JilExpr) replacement);
			}
			
			children().remove(child);
			children().add(replacement);
		}
	}	
	
	/**
	 * Represents a Class Constant
	 * 
	 */
	public static final class Class extends AbstractExpr implements JilExpr,Value {
		private final Type classType;
		private final Type.Clazz type;

		public Class(
			final Type classType, 
			final Type.Clazz type, 
			final SyntacticAttribute... attributes
		) {
			super(JilNodeImpl.list(), attributes);
			
			if(classType == null || type == null) {
				throw new IllegalArgumentException("supplied type(s) cannot be null");
			}
			
			this.type = type;
			this.classType = classType;
		}

		public Class(
			final Type classType, 
			final Type.Clazz type, 
			final List<SyntacticAttribute> attributes
		) {
			super(JilNodeImpl.list(), attributes);
			
			if(classType == null || type == null) {
				throw new IllegalArgumentException("supplied type(s) cannot be null");
			}
			
			this.type = type;
			this.classType = classType;
		}

		public Type.Clazz type() {
			return type;
		}
		
		public Type classType() {
			return classType;
		}
		
		public String toString() {
			return classType + ".class";
		}

		public String toVerilog(
			final JilClass jilClass, 
			final JilMethod method
		) {
			return toString();
		}

		public void replaceWith(JilNode child, JilNode replacement) {
			throw new UnsupportedOperationException("This JilNode doesn't have children");
		}
	}

	public String toVerilog(JilClass jilClass, JilMethod method);
}
