package de.codesourcery.booleanalgebra.ast;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

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
    
    public abstract boolean evaluate(IExpressionContext context);
    
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
