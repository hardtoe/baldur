package org.baldurproject.compile;

import java.util.List;
import java.util.Set;

import org.baldurproject.compile.StateMachine.Arc;

public interface IState {
	public Arc arc(String predicate, IState dst);
	public Arc arc(IState dst);
	public Set<IState> getStates();
	public List<StateMachine.Arc> getArcs(); 
	public void addTree(final String t);
	public List<String> trees();
	public IState getStartState();
	public Set<IState> getEndStates();
	public String getName();
	
	public void addPreAction(String t);
	public void addPostAction(String t);
	
 
	public void removeState(IState s);
	public List<String> getPostActions();
	public List<String> getPreActions();
}
