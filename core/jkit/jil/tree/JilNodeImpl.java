package jkit.jil.tree;

import java.util.LinkedList;
import java.util.List;

import jkit.compiler.SyntacticAttribute;
import jkit.compiler.SyntacticElementImpl;

public abstract class JilNodeImpl extends SyntacticElementImpl implements JilNode {
	private final LinkedList<JilNode> children;
	private JilNode parent;
	
	public JilNodeImpl(
		final LinkedList<JilNode> children
	) {
		this.children = children;
	}
	
	public JilNodeImpl(
		final LinkedList<JilNode> children,
		final List<SyntacticAttribute> attributes
	) {
		super(attributes);
		this.children = children;
	}
	
	public JilNodeImpl(
		final LinkedList<JilNode> children,
		final SyntacticAttribute[] attributes
	) {
		super(attributes);
		this.children = children;
		
		for (JilNode child : children) {
			child.setParent(this);
		}
	}
	
	public void setParent(final JilNode parent) {
		this.parent = parent;
	}
	
	public List<JilNode> children() {
		return children;
	}

	public JilNode parent() {
		return parent;
	}

	public static LinkedList<JilNode> list(	
		final JilNode... nodes
	) {
		LinkedList<JilNode> nodeList =
			new LinkedList<JilNode>();
		
		for (JilNode n : nodes) {
			nodeList.add(n);
		}
		
		return nodeList;
	}

	public static LinkedList<JilNode> list(
		final List<? extends JilNode> additionalList, 
		final JilNode... nodes
	) {
		LinkedList<JilNode> nodeList =
			new LinkedList<JilNode>();
		
		nodeList.addAll(additionalList);
		
		for (JilNode n : nodes) {
			nodeList.add(n);
		}
		
		return nodeList;
	}
}
