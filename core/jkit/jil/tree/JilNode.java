package jkit.jil.tree;

import java.util.List;

import jkit.jil.tree.JilExpr.Variable;

public interface JilNode {
	public List<JilNode> children();
	public JilNode parent();
	public void setParent(final JilNode parent);
	public void replaceWith(JilNode child, JilNode replacement);
}
