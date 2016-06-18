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

package jkit.java.tree;

import java.util.List;

import jkit.compiler.SyntacticAttribute;
import jkit.compiler.SyntacticElement;
import jkit.compiler.SyntacticElementImpl;
import jkit.java.tree.Expr.UnOp;
import jkit.java.tree.Type.Clazz;
import jkit.jil.*;
import jkit.jil.tree.Modifier;
import jkit.util.Triple;

public interface Stmt extends SyntacticElement {

	/**
	 * A simple statement represents one which is not composed of other
	 * statements. Examples include return, throw and assignment statements;
	 * example of non-simple statements include if, while and for statements.
	 */
	public interface Simple extends Stmt {
	}

	/**
	 * A block represents a list of statements contained between curly braces.
	 * For example:
	 * 
	 * <pre>
	 * public void f() {
	 * 	int x;
	 * 	x = 2;
	 * 	{
	 * 		int y = 2;
	 * 	}
	 * }
	 * </pre>
	 * 
	 * This is made up of two blocks (and other statements: one for the body of
	 * the method, and the other for the internal block.
	 * 
	 * @author djp
	 * 
	 */
	public static class Block extends SyntacticElementImpl implements Stmt {
		private List<Stmt> statements;

		public Block(List<Stmt> statements, SyntacticAttribute... attributes) {
			super(attributes);
			this.statements = statements;
		}

		public List<Stmt> statements() {
			return statements;
		}
	}

	/**
	 * Represents a synchronised block of code.  For example:
	 * <pre>
	 * public void f(List<String> x) {
	 *  synchronized(x) {
	 *   ...
	 *  }
	 * }
	 * </pre>
	 * @author djp
	 *
	 */
	public static class SynchronisedBlock extends Block {
		private Expr expr;

		public SynchronisedBlock(Expr expr, List<Stmt> statements,
				SyntacticAttribute... attributes) {
			super(statements, attributes);
			this.expr = expr;
		}

		public Expr expr() {
			return expr;
		}
		
		public void setExpr(Expr expr) {
			this.expr = expr;
		}
	}

	/**
	 * This represents a catch block, which is contained within a try-catch
	 * block.
	 * 
	 * @author djp
	 */
	public static class CatchBlock extends Block {
		private Type.Clazz type;
		private String variable;

		public CatchBlock(Type.Clazz type, String variable,
				List<Stmt> statements, SyntacticAttribute... attributes) {
			super(statements, attributes);
			this.type = type;
			this.variable = variable;
		}

		public Type.Clazz type() {
			return type;
		}

		public String variable() {
			return variable;
		}
	}

	/**
	 * This represents a try-catch block.  For example:
	 * <pre>
	 * public void f(int x) {
	 *  try {
	 *   x = x / 0;
	 *  } catch(ArithmeticException e) {
	 *   ...
	 *  }
	 * }
	 * </pre>
	 * 
	 * @author djp	 
	 */
	public static class TryCatchBlock extends Block {
		private List<CatchBlock> handlers;
		private Block finallyBlk;

		public TryCatchBlock(List<CatchBlock> handlers, Block finallyBlk,
				List<Stmt> statements, SyntacticAttribute... attributes) {
			super(statements, attributes);
			this.handlers = handlers;
			this.finallyBlk = finallyBlk;
		}

		public List<CatchBlock> handlers() {
			return handlers;
		}

		public Block finaly() {
			return finallyBlk;
		}
	}

	public static class Label extends SyntacticElementImpl implements Stmt {
		private String label;
		private Stmt statement;

		public Label(String label, Stmt statement, SyntacticAttribute... attributes) {
			super(attributes);
			this.label = label;
			this.statement = statement;
		}

		public String label() {
			return label;
		}

		public Stmt statement() {
			return statement;
		}

		public void setStatement(Stmt stmt) {
			this.statement = stmt;
		}
	}

	public static class Assignment extends SyntacticElementImpl implements Simple, Expr {
		private Expr lhs, rhs;

		public Assignment(Expr lhs, Expr rhs,
				SyntacticAttribute... attributes) {
			super(attributes);
			this.lhs = lhs;
			this.rhs = rhs;
		}

		public Expr lhs() {
			return lhs;
		}

		public Expr rhs() {
			return rhs;
		}
		
		public void setLhs(Expr lhs) {
			this.lhs = lhs;
		}

		public void setRhs(Expr rhs) {
			this.rhs = rhs;
		}
	}

	public static class AssignmentOp extends Assignment {
		private int op;		

		public AssignmentOp(int op, Expr lhs, Expr rhs,
				SyntacticAttribute... attributes) {
			super(lhs,rhs,attributes);
			this.op = op;			
		}

		public int op() {
			return op;
		}
		
		public void setOp(int op) {
			this.op = op;
		}
	}
	
	public static class Return extends SyntacticElementImpl implements Simple {
		private Expr expr;

		public Return(Expr expr, SyntacticAttribute... attributes) {
			super(attributes);
			this.expr = expr;
		}

		public Expr expr() {
			return expr;
		}
		
		public void setExpr(Expr expr) {
			this.expr = expr;
		}
	}

	public static class Throw extends SyntacticElementImpl implements Simple {
		private Expr expr;

		public Throw(Expr expr, SyntacticAttribute... attributes) {
			super(attributes);
			this.expr = expr;
		}

		public Expr expr() {
			return expr;
		}
		
		public void setExpr(Expr expr) {
			this.expr = expr;
		}
	}

	public static class Assert extends SyntacticElementImpl implements Simple {
		private Expr expr;

		public Assert(Expr expr, SyntacticAttribute... attributes) {
			super(attributes);
			this.expr = expr;
		}

		public Expr expr() {
			return expr;
		}
		
		public void setExpr(Expr expr) {
			this.expr = expr;
		}
	}

	public static class Break extends SyntacticElementImpl implements Simple {
		private String label;

		public Break(String label, SyntacticAttribute... attributes) {
			super(attributes);
			this.label = label;
		}

		public String label() {
			return label;
		}
		
		public void setLabel(String label) {
			this.label = label;
		}
	}

	public static class Continue extends SyntacticElementImpl implements Simple {
		private String label;

		public Continue(String label, SyntacticAttribute... attributes) {
			super(attributes);
			this.label = label;
		}

		public String label() {
			return label;
		}
		
		public void setLabel(String label) {
			this.label = label;
		}
	}

	public static class If extends SyntacticElementImpl implements Stmt {
		private Expr condition;
		private Stmt trueStatement;
		private Stmt falseStatement;

		public If(Expr condition, Stmt trueStatement,
				Stmt falseStatement, SyntacticAttribute... attributes) {
			super(attributes);
			this.condition = condition;
			this.trueStatement = trueStatement;
			this.falseStatement = falseStatement;
		}

		public Expr condition() {
			return condition;
		}

		public void setCondition(Expr condition) {
			this.condition = condition;
		}
		
		public Stmt trueStatement() {
			return trueStatement;
		}

		public void setTrueStatement(Stmt trueStatement) {
			this.trueStatement = trueStatement;
		}
		
		public Stmt falseStatement() {
			return falseStatement;
		}
		
		public void setFalseStatement(Stmt falseStatement) {
			this.falseStatement = falseStatement;
		}
	}

	public static class While extends SyntacticElementImpl implements Stmt {
		private Expr condition;
		private Stmt body;

		public While(Expr condition, Stmt body, SyntacticAttribute... attributes) {
			super(attributes);
			this.condition = condition;
			this.body = body;
		}

		public Expr condition() {
			return condition;
		}

		public void setCondition(Expr condition) {
			this.condition = condition;
		}
				
		public Stmt body() {
			return body;
		}
		
		public void setBody(Stmt body) {
			this.body = body;
		}
	}

	public static class DoWhile extends SyntacticElementImpl implements Stmt {
		private Expr condition;
		private Stmt body;

		public DoWhile(Expr condition, Stmt body, SyntacticAttribute... attributes) {
			super(attributes);
			this.condition = condition;
			this.body = body;
		}

		public Expr condition() {
			return condition;
		}

		public Stmt body() {
			return body;
		}

		public void setCondition(Expr condition) {
			this.condition = condition;
		}
						
		public void setBody(Stmt body) {
			this.body = body;
		}
	}

	public static class For extends SyntacticElementImpl implements Stmt {
		private Stmt initialiser;
		private Expr condition;
		private Stmt increment;
		private Stmt body;

		public For(Stmt initialiser, Expr condition, Stmt increment,
				Stmt body, SyntacticAttribute... attributes) {
			super(attributes);
			this.initialiser = initialiser;
			this.condition = condition;
			this.increment = increment;
			this.body = body;
		}

		public Stmt initialiser() {
			return initialiser;
		}
	
		public void setInitialiser(Stmt initialiser) {
			this.initialiser = initialiser;
		}
		
		public Expr condition() {
			return condition;
		}

		public Stmt body() {
			return body;
		}

		public void setCondition(Expr condition) {
			this.condition = condition;
		}
						
		public void setBody(Stmt body) {
			this.body = body;
		}
		
		public Stmt increment() {
			return increment;
		}
		
		public void setIncrement(Stmt increment) {
			this.increment = increment;
		}
	}

	public static class ForEach extends SyntacticElementImpl implements Stmt {
		private String var;
		private List<Modifier> modifiers; // for variable
		private Type type; // for variable
		private Expr source;
		private Stmt body;

		public ForEach(List<Modifier> modifiers, String var, Type type,
				Expr source, Stmt body, SyntacticAttribute... attributes) {
			super(attributes);
			this.modifiers = modifiers;
			this.var = var;
			this.type = type;
			this.source = source;
			this.body = body;
		}

		/**
		 * Set the modifiers of the variable declared in the for-each statement.
		 * Use java.lang.reflect.Modifier for this.
		 * 
		 * @param type
		 */
		public void setModifiers(List<Modifier> modifiers) {
			this.modifiers = modifiers;
		}

		/**
		 * Get modifiers of this local variable
		 * 
		 * @return
		 */
		public List<Modifier> modifiers() {
			return modifiers;
		}

		/**
		 * Get type of variable declared in for-each statement.
		 * 
		 * @return
		 */
		public Type type() {
			return type;
		}

		public void setType(Type t) {
			type = t;
		}
		
		/**
		 * Get name of variable declared in for-each statement.
		 * 
		 * @return
		 */
		public String var() {
			return var;
		}

		public void setVar(String v) {
			var = v;
		}
		
		/**
		 * Get the source expression which corresponds to an array or collection
		 * which the for-each statement is going to iterate over.
		 * 
		 * @return
		 */
		public Expr source() {
			return source;
		}

		public void setSource(Expr source) {
			this.source = source;
		}
		
		/**
		 * Get the body of the for-each statement. Maybe null if there is no
		 * body!
		 * 
		 * @return
		 */
		public Stmt body() {
			return body;
		}
		
		public void setBody(Stmt b) {
			body = b;
		}
	}

	/**
	 * A VarDef is a symbol table entry for a local variable. It can be thought
	 * of as a declaration for that variable, including its type, modifiers,
	 * name and whether or not it is a parameter to the method.
	 * 
	 * @author djp
	 */
	public static class VarDef extends SyntacticElementImpl implements Simple {
		private List<Modifier> modifiers;
		private Type type;
		private List<Triple<String, Integer, Expr>> definitions;

		public VarDef(List<Modifier> modifiers, Type type,
				List<Triple<String, Integer, Expr>> definitions,
				SyntacticAttribute... attributes) {
			super(attributes);
			this.modifiers = modifiers;
			this.definitions = definitions;
			this.type = type;
		}

		/**
		 * Set the modifiers of this local variable. Use
		 * java.lang.reflect.Modifier for this.
		 * 
		 * @param type
		 */
		public void setModifiers(List<Modifier> modifiers) {
			this.modifiers = modifiers;
		}

		/**
		 * Get modifiers of this local variable
		 * 
		 * @return
		 */
		public List<Modifier> modifiers() {
			return modifiers;
		}

		/**
         * The list of variable definitions declared. Each is a triple of the
         * form (s,i,e), where s is the variable name, i is the number of array
         * braces provided and e is a (possibly null) initialiser.
         * 
         * @return
         */
		public List<Triple<String, Integer, Expr>> definitions() {
			return definitions;
		}

		public Type type() {
			return type;
		}
	}

	public static class Case extends SyntacticElementImpl {
		private Expr condition;
		private List<Stmt> statements;

		public Case(Expr condition, List<Stmt> statements,
				SyntacticAttribute... attributes) {
			super(attributes);
			this.condition = condition;
			this.statements = statements;
		}

		public Expr condition() {
			return condition;
		}

		public void setCondition(Expr e) {
			condition = e;
		}
		
		public List<Stmt> statements() {
			return statements;
		}
	}

	public static class DefaultCase extends Case {
		public DefaultCase(List<Stmt> statements, SyntacticAttribute... attributes) {
			super(null, statements, attributes);
		}
	}

	public static class Switch extends SyntacticElementImpl implements Stmt {
		private Expr condition;
		private List<Case> cases;

		public Switch(Expr condition, List<Case> cases,
				SyntacticAttribute... attributes) {
			super(attributes);
			this.condition = condition;
			this.cases = cases;
		}

		public Expr condition() {
			return condition;
		}

		public void setCondition(Expr condition) {
			this.condition = condition;
		}
						
		public List<Case> cases() {
			return cases;
		}
	}
	

	/**
	 * Represents pre/post increment/decrement. The whole point of this class is
	 * to enable these expressions to be statements.
	 * 
	 * @author djp
	 * 
	 */
	public static class PrePostIncDec extends UnOp implements Expr,Simple {
		public PrePostIncDec(int op, Expr expr, SyntacticAttribute... attributes) {
			super(op,expr,attributes);
		}
		public PrePostIncDec(int op, Expr expr, List<SyntacticAttribute> attributes) {
			super(op,expr,attributes);
		}
	}
}
