package de.codesourcery.booleanalgebra.ast;

import java.util.ArrayList;
import java.util.List;

public class TreeMatcher implements INodeMatcher
{
    private boolean unwrapParent;
    private boolean unwrapLeftChild;
    private boolean unwrapRightChild;

    private INodeMatcher thisNodeMatcher;    
    private INodeMatcher leftChildMatcher;
    private INodeMatcher rightChildMatcher;
    private INodeMatcher extraMatcher;    
    
    private boolean requiresNodeToHaveParent = true;
    
    private ASTNode matchedParent;
    private ASTNode matchedLeftChild;
    private ASTNode matchedRightChild;
    
    private boolean ignoreChildOrder = false;
    
    public TreeMatcher() {
    }
    
    public TreeMatcher(INodeMatcher matcher) {
        if (matcher == null) {
            throw new IllegalArgumentException("matcher must not be NULL.");
        }
        this.thisNodeMatcher = matcher;
    }
    
    public TreeMatcher unwrapAll() {
        unwrapParent = true;
        unwrapLeftChild = true;
        unwrapRightChild = true;
        return this;
    }
    
    public TreeMatcher ignoreChildOrder(boolean yesNo)
    {
        this.ignoreChildOrder = yesNo;
        return this;
    }
    
    public TreeMatcher requireNodeToHaveParent(boolean yesNo) {
        this.requiresNodeToHaveParent = yesNo;
        return this;
    }
    
    public TreeMatcher unwrapParent() {
        unwrapParent = true;
        return this;
    }
    
    public TreeMatcher unwrapLeftChild() {
        unwrapLeftChild = true;
        return this;
    }   
    
    public TreeMatcher unwrapRightChild() {
        unwrapRightChild = true;
        return this;
    }     
    
    protected abstract class NodeMatchBuilder<T extends NodeMatchBuilder<?>> {
        
        private final List<INodeMatcher> matchers=new ArrayList<>();
        
        public final TreeMatcher buildOR() 
        {
            if ( matchers.isEmpty() ) {
                throw new IllegalStateException("Need at least one matcher");
            }
            final INodeMatcher m;
            if ( matchers.size() == 1 ) {
                m = matchers.get(0);
            } else {
                m = CombinedNodeMatcher.matchOR( this.matchers );
            }
            return assignMatcher( m );
        }
        
        protected abstract TreeMatcher assignMatcher( INodeMatcher matcher);
        
        @SuppressWarnings("unchecked")
        public final T addMatcher(INodeMatcher matcher) 
        {
            if (matcher == null) {
                throw new IllegalArgumentException("matcher must not be NULL.");
            }
            
            this.matchers.add( matcher );
            return (T) this;
        }        
        
        public final T matchAND() {
            return addMatcher( OperatorTypeMatcher.matchAND() );
        }
        
        public final T matchOR() {
            return addMatcher( OperatorTypeMatcher.matchOR() );
        }    
        
        public final T matchNOT() {
            return addMatcher( OperatorTypeMatcher.matchNOT() );
        }         
    }    
    
    public class ThisNodeMatchBuilder extends NodeMatchBuilder<ThisNodeMatchBuilder> {

        @Override
        protected TreeMatcher assignMatcher(INodeMatcher matcher)
        {
            if (matcher == null) {
                throw new IllegalArgumentException("matcher must not be NULL.");
            }
            TreeMatcher.this.thisNodeMatcher = matcher;
            return TreeMatcher.this;
        }
    }
    
    public class LeftChildMatchBuilder extends NodeMatchBuilder<LeftChildMatchBuilder> {

        @Override
        protected TreeMatcher assignMatcher(INodeMatcher matcher)
        {
            if (matcher == null) {
                throw new IllegalArgumentException("matcher must not be NULL.");
            }
            TreeMatcher.this.leftChildMatcher = matcher;
            return TreeMatcher.this;
        }
    }    
    
    public class RightChildMatchBuilder extends NodeMatchBuilder<RightChildMatchBuilder> {

        @Override
        protected TreeMatcher assignMatcher(INodeMatcher matcher)
        {
            if (matcher == null) {
                throw new IllegalArgumentException("matcher must not be NULL.");
            }
            TreeMatcher.this.rightChildMatcher = matcher;
            return TreeMatcher.this;
        }
    }        
    
    public class ExtraMatchBuilder extends NodeMatchBuilder<ExtraMatchBuilder> {

        @Override
        protected TreeMatcher assignMatcher(INodeMatcher matcher)
        {
            if (matcher == null) {
                throw new IllegalArgumentException("matcher must not be NULL.");
            }
            TreeMatcher.this.extraMatcher = matcher;
            return TreeMatcher.this;
        }
    } 
    
    public ExtraMatchBuilder matchExtra() {
        return new ExtraMatchBuilder();
    }    
    
    public ThisNodeMatchBuilder matchParent() {
        return new ThisNodeMatchBuilder();
    }
    
    public LeftChildMatchBuilder matchLeftChild() {
        return new LeftChildMatchBuilder();
    }    
    
    public RightChildMatchBuilder matchRightChild() {
        return new RightChildMatchBuilder();
    }      
    
    public TreeMatcher leftChildMatcher(INodeMatcher matcher) {
        if (matcher == null) {
            throw new IllegalArgumentException("matcher must not be NULL.");
        }
        
        if ( leftChildMatcher != null ) {
            throw new IllegalStateException("Left-child matcher already set to "+leftChildMatcher);
        }        
        leftChildMatcher = matcher;
        return this;
    }
    
    public TreeMatcher rightChildMatcher(INodeMatcher matcher) {
        if (matcher == null) {
            throw new IllegalArgumentException("matcher must not be NULL.");
        }
        
        if ( rightChildMatcher != null ) {
            throw new IllegalStateException("Right-child matcher already set to "+rightChildMatcher);
        }        
        rightChildMatcher = matcher;
        return this;
    }    
    
    private boolean isMatchingLeftChild(ASTNode leftChild) 
    {
        if ( leftChildMatcher == null ) {
            return true;
        }
        
        if ( leftChild == null ) {
            return false;
        }
        
        return leftChildMatcher.matches( this, leftChild );
    }    
    
    private boolean isMatchingRightChild(ASTNode rightChild) 
    {
        if ( rightChildMatcher == null ) {
            return true;
        }
        
        if ( rightChild == null ) {
            return false;
        }
        
        return rightChildMatcher.matches( this, rightChild );
    }   
    
    private boolean hasMatchingChildren(ASTNode parent) {
        
        boolean matches = matchesChildren(  
                unwrap( leftChild( parent ) , unwrapLeftChild ),
                unwrap( rightChild( parent ) , unwrapRightChild ) 
        );
        
        if ( matches ) {
            matchedLeftChild = leftChild(parent);
            matchedRightChild = rightChild(parent);
            return true;
        } 
        else if ( ignoreChildOrder ) 
        {
            matches = matchesChildren(   
                    unwrap( rightChild( parent ) , unwrapLeftChild ),
                    unwrap( leftChild( parent ) , unwrapRightChild ) 
            );
            
            if ( matches ) {
                matchedLeftChild = rightChild(parent);
                matchedRightChild = leftChild(parent);
                return true;                
            }
        }
        
        return false;
    }
    
    private boolean matchesChildren(ASTNode leftChild,ASTNode rightChild)
    {
        boolean matches = isMatchingLeftChild( leftChild );
        if ( matches ) {
            matches &= isMatchingRightChild(rightChild );
        }
        return matches;
    }
    
    private ASTNode unwrap(ASTNode n,boolean doUnwrap) 
    {
        if ( ! doUnwrap || n == null ) {
            return n;
        }
        ASTNode result=n;
        while( result instanceof TermNode) {
            result = result.child(0);
        }
        return result;
    }
    
    public ASTNode parentMatch() {
        return matchedParent;
    }
    
    private ASTNode leftChild(ASTNode node) {
        return node.getChildCount() > 0 ? node.leftChild() : null;
    }   
    
    private ASTNode rightChild(ASTNode node) {
        return node.getChildCount() > 1 ? node.rightChild() : null;
    }     
    
    public ASTNode rightMatch() {
        return matchedRightChild;
    }  
    
    public ASTNode leftMatch() {
        return matchedLeftChild;
    }        
    
    @Override
    public boolean matches(TreeMatcher matcher, ASTNode n)
    {
        if ( thisNodeMatcher == null ) {
            throw new IllegalStateException("At least the INodeMatcher for the main node needs to be set");
        }
        
        matchedLeftChild = matchedRightChild = matchedParent = null;
        
        ASTNode matchedNode = unwrap(n,unwrapParent); 
        if ( requiresNodeToHaveParent && ! matchedNode.hasParent() ) {
            return false;
        }
        boolean matches = thisNodeMatcher.matches( matcher, matchedNode );
        if ( matches ) {
            matchedParent = n;
        }
        
        if ( matches ) {
            matches &= hasMatchingChildren( matchedNode );
        }
        
        if ( matches && extraMatcher != null ) {
            matches &= extraMatcher.matches( this , matchedParent );
            if ( ! matches ) {
                matchedParent = null;
            }
        }
        return matches;
    }

    @Override
    public String toString()
    {
        return "matchedParent="+parentMatch()+" / leftMatch="+leftMatch()+" / rightMatch="+rightMatch();
    }
}
