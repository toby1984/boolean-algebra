package de.codesourcery.booleanalgebra.ast;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import de.codesourcery.booleanalgebra.IExpressionContext;
import de.codesourcery.booleanalgebra.exceptions.ParseException;
import de.codesourcery.booleanalgebra.lexer.ILexer;

public abstract class ASTNode
{
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
    
    public boolean getLiteralValue(IExpressionContext context) {
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
    
    public List<ASTNode> children() {
        return children;
    }
    
    public ASTNode getParent()
    {
        return parent;
    }
    
    public boolean visitPreOrder(INodeVisitor visitor) {
        
        if ( ! visitor.visit( this ) ) {
            return false;
        }
        
        for ( ASTNode child : this.children ) {
            if ( ! child.visitInOrder( visitor ) ) {
                return false;
            }
        }
        return true;
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

	public boolean visitInOrder(INodeVisitor visitor) {
        
        for ( ASTNode child : this.children ) {
            if ( ! child.visitInOrder( visitor ) ) {
                return false;
            }
        }
        
        if ( ! visitor.visit( this ) ) {
            return false;
        }        
        return true;
    }
    
    public final void print(final  PrintWriter writer) {
        
        final INodeVisitor v = new INodeVisitor() {

            @Override
            public boolean visit(ASTNode node)
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
    
    public abstract ASTNode parse(ILexer lexer, ASTNode previousNode) throws ParseException;
    
    protected final String childToString(int index) {
        if ( index < 0 || index >= getChildCount() ) {
            return "<no child>";
        }
        return child(index).toString();
    }    
}
