package de.codesourcery.booleanalgebra.ast;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.codesourcery.booleanalgebra.IExpressionContext;
import de.codesourcery.booleanalgebra.exceptions.ParseException;
import de.codesourcery.booleanalgebra.lexer.ILexer;

public abstract class ASTNode
{
	private static long nodeCounter = 0;

	private final long nodeId = nodeCounter++;

	private ASTNode parent;
	private final List<ASTNode> children = new ArrayList<ASTNode>();

	public ASTNode() {
	}

	protected ASTNode(ASTNode n1,ASTNode... nodes) {
		addChild( n1 );
		if ( nodes != null ) {
			for ( ASTNode n : nodes ) {
				addChild( n );
			}
		}
	}

	public void toDOT(OutputStream out) 
	{
		final PrintWriter pWriter = new PrintWriter(out,true);
		try {
			pWriter.println("diGraph G { ");
			toDOT( pWriter );
			pWriter.println("}");
			pWriter.flush();
		} finally {
			pWriter.close();
		}
	}

	public void toDOT(PrintWriter writer) {

		if ( getParent() != null ) 
		{
			writer.println( ""+getParent().nodeId+" -> "+nodeId+";");
		}

		String label;
		if ( isBooleanOperator() ) {
			label = ((OperatorNode) this).getType().toString();
		} else if ( this instanceof IdentifierNode) {
			label = ((IdentifierNode) this).getIdentifier().toString();
		} else {
			label = toString();
		}

		writer.println( ""+nodeId+" [label=\""+label+"\"];" );
		for ( ASTNode child : children ) 
		{
			child.toDOT( writer );
		}
	}

	public abstract boolean isEquals(ASTNode other);

	private static final List<ASTNode> reverse(List<ASTNode> list) {
		final List<ASTNode> copy = new ArrayList<ASTNode>( list );
		Collections.reverse( copy );
		return copy;
	}

	public Iterator<ASTNode> createInOrderIterator() 
	{
		final Stack<ASTNode> nodesToVisit = new Stack<ASTNode>();

		return new Iterator<ASTNode>() {

			private boolean isNew = true;

			private void addToStack(List<ASTNode> nodes) 
			{
				for ( ASTNode n : nodes ) {
					nodesToVisit.add( n );
					nodesToVisit.addAll( reverse( n.children ) );
				}
				isNew = false;
			}

			@Override
			public boolean hasNext() {
				if ( isNew ) {
					addToStack( Collections.singletonList( ASTNode.this ) );
				}
				return ! nodesToVisit.isEmpty();
			}

			@Override
			public ASTNode next() 
			{
				if ( ! hasNext() ) {
					throw new NoSuchElementException();
				}
				ASTNode node = nodesToVisit.pop();
				addToStack( node.children );
				return node;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public boolean isEquivalent(ASTNode other, IExpressionContext context) 
	{
		if ( this == other ) {
			return true;
		}
		ASTNode thisValue = evaluate( context );    	
		ASTNode otherValue = other.evaluate( context );
		if ( otherValue != null && thisValue != null ) {
			if ( thisValue == otherValue ) {
				return true;
			}
			return thisValue.isEquals( otherValue );
		}
		return isEquals( other );
	}

	public final boolean isLeafNode() {
		return getMaxSupportedChildCount() == 0;
	}

	public boolean getLiteralValue(IExpressionContext context) {
		return false;
	}

	public boolean isSimpleTerm() {
		if ( getChildCount() == 0 ) {
			return true;
		}
		if ( getChildCount() == 1 ) {
			return child(0).isSimpleTerm();
		}
		return false;
	}

	public boolean isLiteralValue() {
		return false;
	}

	public boolean hasLiteralValue(IExpressionContext context) {
		return false;
	}

	public final ASTNode createCopy(boolean copyChildren) {
		ASTNode result = copyThisNode();
		if ( copyChildren ) {
			for ( ASTNode child : children() ) {
				result.addChild( child.createCopy( true ) );
			}
		}
		return result;
	}

	protected abstract ASTNode copyThisNode();

	public abstract ASTNode evaluate(IExpressionContext context);

	protected boolean hasChild(int index) {
		return index >= 0 && index < children.size();
	}

	public int getChildCount() {
		return children.size();
	}

	public boolean hasChildren() {
		return ! children.isEmpty();
	}

	public List<ASTNode> children() {
		return children;
	}

	public ASTNode getParent()
	{
		return parent;
	}

	public boolean visitInOrder(INodeVisitor visitor) 
	{
		return visitInOrder(visitor,0);
	}

	protected boolean visitInOrder(INodeVisitor visitor,int currentDepth) 
	{
		// 1. Traverse the left subtree.
		// 2. Visit the root.
		// 3. Traverse the right subtree.
		switch( getChildCount() ) {
			case 0:
				return visitor.visit( this, currentDepth );
			case 1:
				if ( ! children.get(0).visitInOrder( visitor , currentDepth+1) ) {
					return false;
				}
				return visitor.visit( this, currentDepth );
			case 2:
				if ( ! children.get(0).visitInOrder( visitor , currentDepth+1) ) {
					return false;
				}			
				if ( ! visitor.visit( this, currentDepth ) ) {
					return false;
				}			
				return children.get(1).visitInOrder( visitor );
			default:
				throw new RuntimeException("Internal error, node with more than 2 children ?");
		}
	}    

	public int getTreeDepth() {
		return getTreeDepth(0);
	}

	protected int getTreeDepth(int current) 
	{
		int result = current;
		for ( ASTNode child : children) {
			int tmp = child.getTreeDepth(current+1);
			if ( tmp > result ) {
				result = tmp;
			}
		}
		return result;
	}

	public boolean visitPreOrder(INodeVisitor visitor) 
	{
		return visitPreOrder(visitor,0);
	}

	protected boolean visitPreOrder(INodeVisitor visitor,int currentDepth) 
	{
		// 1. Visit the root.
		// 2. Traverse the left subtree.
		// 3. Traverse the right subtree.

		if ( ! visitor.visit( this, currentDepth ) ) {
			return false;
		}

		switch( getChildCount() ) {
			case 0:
				return true;
			case 1:
				if ( ! children.get(0).visitPreOrder( visitor ,currentDepth+1) ) {
					return false;
				}
				return true;
			case 2:
				if ( ! children.get(0).visitPreOrder( visitor ,currentDepth+1) ) {
					return false;
				}			
				return children.get(1).visitPreOrder( visitor , currentDepth+1 );
			default:
				throw new RuntimeException("Internal error, node with more than 2 children ?");
		}
	}

	public boolean visitPostOrder(INodeVisitor visitor) 
	{
		return visitPostOrder(visitor,0);
	}

	protected boolean visitPostOrder(INodeVisitor visitor,int currentDepth) 
	{
		// Traverse the left subtree.
		// Traverse the right subtree.
		// Visit the root.
		switch( getChildCount() ) 
		{
			case 0:
				return visitor.visit( this, currentDepth );
			case 1:
				if ( ! children.get(0).visitPostOrder( visitor , currentDepth +1 ) ) {
					return false;
				}
				return visitor.visit( this, currentDepth );
			case 2:
				if ( ! children.get(0).visitPostOrder( visitor , currentDepth +1 ) ) {
					return false;
				}			
				if ( children.get(1).visitPostOrder( visitor , currentDepth +1 ) ) {
					return false;
				}
				return visitor.visit( this, currentDepth );
			default:
				throw new RuntimeException("Internal error, node with more than 2 children ?");
		}
	}


	public void replaceWith(ASTNode other) {
		getParent().replaceChild( this , other );
	}

	public void replaceChild(ASTNode childToReplace, ASTNode newChild) 
	{
		if ( childToReplace == null ) {
			throw new IllegalArgumentException("childToReplace must not be null");
		}
		if ( newChild == null ) {
			throw new IllegalArgumentException("newChild must not be null");
		}
		int index = -1;
		int i = 0;
		for ( ASTNode child : children ) {
			if ( child == childToReplace ) {
				index = i;
				break;
			}
			i++;
		}
		if ( index == -1 ) {
			throw new NoSuchElementException("can't find child "+childToReplace+" on "+this);
		}

		children.remove( index );
		childToReplace.setParent(null);
		children.add( index , newChild );
		newChild.setParent( this );
	}

	public final void print(final  PrintWriter writer) {

		final INodeVisitor v = new INodeVisitor() {

			@Override
			public boolean visit(ASTNode node, int currentDepth)
			{
				writer.println( node.toString() );
				return true;
			}
		};
		visitInOrder( v );
	}


	public boolean hasParent() {
		return this.parent != null;
	}

	public void setParent(ASTNode parent)
	{
		this.parent = parent;
	}

	public ASTNode addChild(ASTNode child) {
		if (child== null) {
			throw new IllegalArgumentException("child must not be NULL.");
		}
		if ( getMaxSupportedChildCount() != -1 && children.size() >= getMaxSupportedChildCount() ) {
			throw new IllegalArgumentException("Node "+this+" supports at most "+getMaxSupportedChildCount()+" child nodes.");
		}
		children.add( child );
		child.setParent( this );
		return child;
	}

	public void parse() {

	}

	public final boolean isBooleanOperator() {
		return isAND() || isNOT() || isOR();
	}

	public boolean isAND() {
		return false;
	}

	public boolean isNOT() {
		return false;
	}    

	public boolean isOR() {
		return false;
	}    

	public ASTNode child(int index) {
		return children.get(index);
	}

	protected abstract int getMaxSupportedChildCount();

	public abstract ASTNode parse(ILexer lexer) throws ParseException;

	@Override
	public final String toString() {
		return toString(false);
	}

	protected final String childToString(int index,boolean prettyPrint) {
		if ( index < 0 || index >= getChildCount() ) {
			return "<no child>";
		}
		if ( child(index).isSimpleTerm()) {
			return child(index).toString(prettyPrint);
		}
		return "("+child(index).toString(prettyPrint)+")";
	}    

	public abstract String toString(boolean prettyPrint);
}
