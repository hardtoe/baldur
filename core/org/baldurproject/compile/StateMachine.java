package org.baldurproject.compile;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jkit.util.Pair;


import org.antlr.runtime.RecognitionException;

public class StateMachine implements IState {
	private String name;
	
	private final IState startState;
	
	private final Set<IState> topLevelStates =
		new LinkedHashSet<IState>();
	
	private final Set<IState> endStates =
		new LinkedHashSet<IState>();

	private String clkName;
	
	public void setClkName(final String clkName) {
		this.clkName = clkName;
	}

	public String toDot() {
		String dot = 
			"subgraph " + name + " {\n" +
			"label=\"" + name + "\";\n";
		
		dot += toDot(startState, "box");
		for (IState src : getStates()) {
			if (src != startState) {
				dot += toDot(src, endStates.contains(src) ? "doublecircle" : "oval");
			}
		}
		
		dot += "}";
		
		return dot;
	}

	public String toDot(
		IState state, 
		String shape
	) {
		String dot = "";
		
		for (Arc arc : state.getArcs()) {
			dot += toDot(state, arc);
		}
		
		dot += (state.getName() + " [label=\""+listToString(state.trees())+"\", shape="+shape+"];").replaceAll("\n", "") + "\n";
		
		return dot;
	}

	public String toDot(
		IState src, 
		Arc arc
	) {
		return ("  " + src.getName() + " -> " + arc.getDestination().getName() + " [label=\""+arc.getPredicate()+"\"];").replaceAll("\n", "") + "\n";
	}
	
	private String listToString(List<String> list) {
		String value = "";
				
		
		for (String line : list) {
			value += line + "\\n";
		}
		
		/*
		if (list.size() > 0) {
			return ":)";
		} else {
			return "";
		}
		*/
		
		return value;
	}
	
	public static class State implements IState {
		private final List<Arc> arcs =
			new LinkedList<Arc>();
		
		private Arc defaultArc;
		
		private final List<String> preActions =
			new LinkedList<String>();
		
		private final List<String> trees =
			new LinkedList<String>();
		
		private final List<String> postActions =
			new LinkedList<String>();

		private String exitCondition = null;
		
		// FIXME: change this to a better name generation system
		public String getLineNumber() {
			/*
			if (trees.isEmpty()) {
				return 0 + "";
			} else {
				return Math.abs(trees.hashCode()) + "";
			}
			*/
			return "";
		}
		
		@Override
		public String toString() {
			String string = "state: ";
			
			if (trees.isEmpty()) {
				string += getName() + " is empty\n";
				
			} else {
				string += getName() + "\n";
				
				for (String t : trees) {
					string += indent(statementToVerilog(t)) + "\n";
	
				}
				
				for (Arc arc : getArcs()) {
					string += indent(arc.toString()) + "\n";
				}
				
				if (defaultArc != null) {
					string += indent(defaultArc.toString()) + "\n";
				}
			}
			
			return string;
		}
		
		public Arc arc(
			final String predicate, 
			final IState dst
		) {
			Arc arc = 
				new Arc(predicate, dst);
			
			if (predicate == null) {
				if (defaultArc != null) {
					//throw new Error("someone is trying to add a default arc to a state that already has one!");
					System.out.println("someone is trying to add a default arc to a state that already has one!");
					defaultArc = arc;
					
				} else {
					defaultArc = arc;
				}
				
			} else {
				arcs.add(arc);
			}
			
			return arc;
		}

		public Set<IState> getStates() {
			Set<IState> states =
				new LinkedHashSet<IState>();
			
			states.add(this);
			
			return states;
		}

		public List<Arc> getArcs() {
			LinkedList<Arc> allArcs = 
				new LinkedList<StateMachine.Arc>();
			
			allArcs.addAll(arcs);
			
			if (defaultArc != null) {
				allArcs.add(defaultArc);
			}
			
			return allArcs;
		}

		public void addArc(int index, Arc arc) {
			arcs.add(index, arc);
		}
		
		public void removeArc(Arc arc) {
			if (defaultArc == arc) {
				defaultArc = null;
				
			} else {
				arcs.remove(arc);
			}
		}
		
		public void addTree(final String t) {
			trees.add(t);
		}

		public List<String> trees() {
			return trees;
		}

		public Arc arc(IState dst) {
			return this.defaultArc = new Arc(null, dst);
		}

		public IState getStartState() {
			return this;
		}

		public Set<IState> getEndStates() {
			return getStates();
		}

		public String getName() {
			return "line_" + getLineNumber() + "_" + hashCode();
		}
		
		public String toVerilog(String stateName) {
			String verilog = 
				getName() + " : begin\n";
			
			verilog += "    // state actions\n";
			for (String t : trees) {
				verilog += 
					indent(
						indent(
							statementToVerilog(t))) + "\n";
			}
			
			if (exitCondition == null) {
				verilog += indent(arcsToVerilog(stateName));
				
			} else {
				verilog += "// exit condition\n";
				verilog += "if (" + exitCondition + ") begin";
				verilog += indent(arcsToVerilog(stateName));
				verilog += "end\n";
			}
			
			verilog +=
				"  end\n\n";
			
			return verilog;
		}

		public String arcsToVerilog(String stateName) {
			String verilog = 
				"\n" +
				"    // state arcs\n";
			
			Iterator<Arc> i = 
				arcs.iterator();
			
			while (i.hasNext()) {
				Arc arc =
					i.next();
							
				try {
					verilog += 
						indent(indent(toVerilog(stateName, arc)));
					
				} catch (RecognitionException e) {
					System.err.println(e);
				}
				
				if (i.hasNext()) {
					// not last
					verilog += " else ";
				} else {
					if (defaultArc != null) {
						// not last
						verilog += " else ";
					} else {
						// last
						verilog += "\n";
					}
				}
			}
			
			if (defaultArc != null) {
				try {
					verilog += 
						indent(indent(toVerilog(stateName, defaultArc))) + "\n";
					
				} catch (RecognitionException e) {
					System.err.println(e);
				}				
			}
			return verilog;
		}

		protected String toVerilog(
			final String stateName, 
			final Arc arc
		) throws 
			RecognitionException 
		{
			if (arc.predicate != null) {
				return 
					"if " + predicateToVerilog(arc.predicate) + " begin\n" +
					"  " + stateName + " <= " + arc.getDestination().getName() + ";\n" +
					indent(actionsToVerilog(arc)) +
					"end";
			} else {
				return
					"begin\n" +
					"  " + stateName + " <= " + arc.getDestination().getName() + ";\n" +
					indent(actionsToVerilog(arc)) +
					"end";
			}
		}

		private String actionsToVerilog(
			final Arc arc
		) {
			String verilog = "";
			
			for (String t : this.getPostActions()) {
				verilog += 
					statementToVerilog(t) + "\n";	
			}
			
			for (String t : ((State) arc.getDestination()).getPreActions()) {
				verilog += 
					statementToVerilog(t) + "\n";

			}
			
			return verilog;
		}

		public List<String> getPreActions() {
			return preActions;
		}
 
		public List<String> getPostActions() {
			return postActions;
		}

		public void addPreAction(String t) {
			preActions.add(t);
		}

		public void addPostAction(String t) {
			postActions.add(t);
		}

		public void setExitCondition(String t) {
			exitCondition  = t;
		}

		public void removeState(IState s) {
			// do nothing
		}
		
	}
	
	public static class Arc {
		private String predicate;
		private IState dst;
		
		public Arc(
			final String predicate,
			final IState dst
		) {
			this.predicate = predicate;
			this.dst = (dst != null ? dst.getStartState() : null);
		}

		
		
		public String getPredicate() {
			return predicate;
		}
		
		public IState getDestination() {
			return dst;
		}
		
		@Override
		public String toString() {
			if (predicate == null) {
				return "arc: -> " + ((StateMachine.State) dst.getStartState()).getName();
			} else {
				return "arc: " + predicateToVerilog(predicate) + " -> " + ((StateMachine.State) dst.getStartState()).getName();
			}
		}



		public void setDestination(final IState newDst) {
			this.dst = newDst;
		}



		public void setPredicate(final String newPredicate) {
			this.predicate = newPredicate;
		}
	}

	public StateMachine(final IState startState) {
		this.startState = startState.getStartState();
	}
	
	public void setName(final String name) {
		this.name = name;
	}
	
	public Arc arc(
		final String predicate, 
		final IState dst
	) {
		final LinkedList<Arc> arcs =
			new LinkedList<Arc>();
		
		for (IState endState : endStates) {
			arcs.add(endState.arc(predicate, dst));
		}
		
		return new Arc(predicate, dst);
	}

	public Set<IState> getStates() {
		Set<IState> allStates =
			new LinkedHashSet<IState>();
		
		for (IState state : topLevelStates) {
			allStates.addAll(state.getStates());
		}
		
		return allStates;
	}

	public List<Arc> getArcs() {
		LinkedList<Arc> allArcs =
			new LinkedList<Arc>();
		
		for (IState state : endStates) {
			allArcs.addAll(state.getArcs());
		}
		
		return allArcs;
	}

	// TODO: add conditional end state
	public void addEndState(IState state) {
		if (state != null) {
			endStates.addAll(state.getEndStates());
		}
	}
	
	public void addTree(String t) {
		for (IState state : getEndStates()) {
			state.addTree(t);
		}
	}

	public List<String> trees() {
		throw new Error("should not be called on this type");
	}

	public Arc arc(final IState dst) {
		return arc(null, dst.getStartState());
	}

	public void addState(final IState state) {
		topLevelStates.add(state);
	}

	public IState getStartState() {
		return startState.getStartState();
	}

	public Set<IState> getEndStates() {
		return endStates;
	}

	public String toVerilogHeader() {
		String verilog = "";

		// declare state variable
		verilog +=
			"reg [31:0] " + stateVarName() + ";\n";
		
		// define states
		int i = 0;
		for (IState state : getStates()) {
			verilog +=
				"parameter " + state.getName() + " = " + (i++) + ";\n";
		}
		
		return verilog;
	}

	public String toVerilogInit() {
		return indent(stateVarName() + " = " + startState.getName() + ";\n");
	}

	public String toVerilogBody() {
		String verilog = "";
		
		verilog += 
			"  case(" + stateVarName() + ")\n";
		
		for (IState state : getStates()) {
			verilog += indent(indent(((State) state).toVerilog(stateVarName())));
		}
		
		verilog +=
			"    default : begin end\n" +
			"  endcase\n";
	
		return verilog;
	}
	
	public String toVerilog() {
		String verilog = "";
		
		// declare state variable
		verilog += toVerilogHeader();
		
		// initialize to start state (take advantage of the fact we are in an FPGA with built-in reset)
		verilog += 
			"initial begin\n" +
			toVerilogInit() +
			"end\n\n";
		
		// output state-machine always block
		verilog +=
			"always @(posedge " + clkName + ") begin : " + getName() + "\n" +
			toVerilogBody() +
			"end\n";

		return verilog;
	}

	private String stateVarName() {
		return name + "_state";
	}
	
	public String toString() {
		String value = "";
		
		for (IState state : getStates()) {
			value += state + "\n";
		}

		
		return value;
	}
	


	public String getName() {
		return name;
	}

	public void addPreAction(String t) {
		startState.addPreAction(t);
	}

	public void addPostAction(String t) {
		for (IState state : getEndStates()) {
			state.addPostAction(t);
		}
	}


	private static String indent(final String input) {
        if (input != null) {
            return "  " + input.replaceAll("\n", "\n  ").replaceAll("\n  $", "\n");
        } else {
            return "null";
        }
    }

	private static String statementToVerilog(
		final String t
	) {
		return t;
	}

	private static String predicateToVerilog(
		String t
	) {
		if (t == null) {
			return "(1'b1)";
		}
		
		return "(" + t + ")";
	}

	public int removeUnreachableStates() {
		LinkedList<IState> statesToRemove =
			new LinkedList<IState>();
		
		for (IState s : getStates()) {
			if (
				s != getStartState() &&
				unreachableState(s)
			) {
				statesToRemove.add(s);
			}
		}
		
		for (IState s : statesToRemove) {
			removeState(s);
		}
		
		return statesToRemove.size();
	}

	public void removeState(IState stateToRemove) {
		endStates.remove(stateToRemove);
		topLevelStates.remove(stateToRemove);
		
		for (IState s : topLevelStates) {
			s.removeState(stateToRemove);
		}
	}

	private boolean unreachableState(IState dstState) {
		return getIncomingArcs(dstState).size() == 0;
	}

	/**
	 * Remove states that have only a default exit arc and no action
	 */
	public int removeSimpleTransitiveStates() {
		LinkedList<IState> statesToRemove =
			new LinkedList<IState>();
		
		for (IState transitiveState : getStates()) {
			if (
				transitiveState.getArcs().size() == 1 &&
				transitiveState.getArcs().get(0).getPredicate() == null &&
				transitiveState.trees().size() == 0 &&
				transitiveState.getPostActions().size() == 0 &&
				transitiveState.getPreActions().size() == 0 &&
				transitiveState != getStartState()
			) {
				IState dst = 
					transitiveState.getArcs().get(0).getDestination();	
				
				if (dst != transitiveState) {
					for (Arc incomingArc : getIncomingArcs(transitiveState)) {
						incomingArc.setDestination(dst);
					}
	
					statesToRemove.add(transitiveState);
				}
			}
		}
		
		for (IState stateToRemove : statesToRemove) {
			removeState(stateToRemove);
		}
		
		return statesToRemove.size();
	}


	/**
	 * Remove states that may have multiple exit arcs and no action.
	 * This is intended to optimize loops.
	 */
	public int removeComplexTransitiveStates() {
		LinkedList<IState> statesToRemove =
			new LinkedList<IState>();
		
		for (IState transitiveState : getStates()) {
			if (
				transitiveState.getArcs().size() >= 1 &&
				transitiveState.trees().size() == 0 &&
				transitiveState.getPostActions().size() == 0 &&
				transitiveState.getPreActions().size() == 0 &&
				transitiveState != getStartState() &&
				!loopsOnItself(transitiveState)
			) {
				for (IState srcState : getStates()) {
					LinkedList<Arc> arcsToRemove =
						new LinkedList<StateMachine.Arc>();
					
					
					List<Arc> incomingArcs = 
						new LinkedList<Arc>(srcState.getArcs());
					
					for (int arcIndex = 0; arcIndex < incomingArcs.size(); arcIndex++) {
						Arc incomingArc = 
							incomingArcs.get(arcIndex);
						
						int insertionIndex = arcIndex;
						
						if (incomingArc.getDestination() == transitiveState) {
							
							
							for (Arc outgoingArc : transitiveState.getArcs()) {
								IState dst = 
									outgoingArc.getDestination();
									
								String lhs = 
									incomingArc.getPredicate() == null ? "1" : incomingArc.getPredicate();
								
								String rhs = 
									outgoingArc.getPredicate() == null ? "1" : outgoingArc.getPredicate();
								
								Arc newIncomingArc =
									new Arc("(" + lhs + " && " + rhs + ")", dst);
								
								System.out.println("TRANSITIVE ARC: " + "(" + lhs + " && " + rhs + ")");
								
								((State) srcState).addArc(insertionIndex, newIncomingArc);
								insertionIndex++; // next arc needs to come after this one
							}
							
							arcsToRemove.add(incomingArc);
						}
					}
					
					for (Arc arcToRemove : arcsToRemove) {
						((State) srcState).removeArc(arcToRemove);
					}
				}

				statesToRemove.add(transitiveState);
				
				break;
			}
		}
		
		for (IState stateToRemove : statesToRemove) {
			removeState(stateToRemove);
		}
		
		return statesToRemove.size();
	}
	
	private boolean loopsOnItself(IState transitiveState) {
		for (Arc arc : transitiveState.getArcs()) {
			if (arc.getDestination() == transitiveState) {
				return true;
			}
		}

		return false;
	}

	private List<Arc> getIncomingArcs(final IState dst) {
		LinkedList<Arc> incomingArcs = 
			new LinkedList<Arc>();
		
		for (IState s : getStates()) {
			for (Arc a : s.getArcs()) {
				if (a.getDestination() == dst) {
					incomingArcs.add(a);
				}
			}
		}
		
		return incomingArcs;
	}

	public List<String> getPostActions() {
		throw new UnsupportedOperationException();
	}

	public List<String> getPreActions() {
		throw new UnsupportedOperationException();
	}
}
