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

package jkit.jil.stages;

import java.util.*;

import jkit.util.Pair;
import jkit.jil.tree.*;
import jkit.jil.tree.JilExpr.Invoke;
import jkit.jil.tree.JilStmt.Assign;

/**
 * The purpose of this pass is to isolate any method calls buried
 * in an expression.  
 * 
 * @author luke
 * 
 */
public final class MethodCallIsolation {
	static int uniqueMethodNumber = 0;
	
	public void apply(
		final JilClass owner
	) {
		for (JilMethod m : owner.methods()) {
			isolateMethodCalls(m);
		}
	}
	
	protected void isolateMethodCalls(
		final JilMethod method
	) {
		LinkedList<JilStmt> newBody =
			new LinkedList<JilStmt>();
		
		for (JilStmt s : method.body()) {
			// depth-first search
			// for each method, replace it with a local variable,
			// add an Assignment to a new local variable to newBody
			if (
				s instanceof JilExpr.Invoke || (
					s instanceof JilStmt.Assign &&
					((JilStmt.Assign) s).rhs() instanceof JilExpr.Invoke)
			) {
				JilExpr.Invoke invocation;
				
				if (s instanceof JilExpr.Invoke){
					invocation = (JilExpr.Invoke) s;
					
				} else {
					JilStmt.Assign assignment =
						((JilStmt.Assign) s);
					
					invocation =
						(JilExpr.Invoke) assignment.rhs();
				}
				
				newBody.addAll(searchAndReplace(invocation));
				
				
			} else {
				newBody.addAll(searchAndReplace(s));
			}
			
			newBody.add(s);
		}
		
		method.body().clear();
		method.body().addAll(newBody);
	}

	public boolean isMatch(
		final JilNode node
	) {
		return node instanceof JilExpr.Invoke;
	}
	
	
	public List<JilStmt> searchAndReplace(JilNode parent) {
		LinkedList<JilStmt> replacements =
			new LinkedList<JilStmt>();
		
		if (parent != null) {		
			LinkedList<JilNode> children =
				new LinkedList<JilNode>(parent.children());
			
			for (JilNode child : children) {
				replacements.addAll(searchAndReplace(child));
				
				if (isMatch(child)) {
					JilExpr.Invoke methodCall =
						(JilExpr.Invoke) child;
					
					JilExpr.Variable replacement = 
						new JilExpr.Variable("INLINED_METHOD_RETURN_VALUE_" + uniqueMethodNumber, methodCall.type());
					
					uniqueMethodNumber++;
					
					parent.replaceWith(child, replacement);
					
					replacements.add(new JilStmt.Assign(replacement, methodCall));
				}
			}
		}
		
		return replacements;
	}
}
