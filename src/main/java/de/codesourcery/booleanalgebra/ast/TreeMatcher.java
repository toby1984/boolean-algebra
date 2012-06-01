package de.codesourcery.booleanalgebra.ast;

public class TreeMatcher implements INodeMatcher
{
    private INodeMatcher thisNodeMatcher;
    
    private boolean unwrapParent = false;
    private boolean unwrapLeftChild = false;
    private boolean unwrapRightChild = false;
    
    private INodeMatcher leftChildMatcher=null;
    private INodeMatcher rightChildMatcher=null;
    
    private boolean requiresNodeToHaveParent = true;
    
    private ASTNode matchedParent= null;
    private ASTNode matchedLeftChild = null;
    private ASTNode matchedRightChild = null;
    
    private boolean ignoreChildOrder = true;
    
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
    
    public TreeMatcher setIgnoreChildOrder(boolean yesNo)
    {
        this.ignoreChildOrder = yesNo;
        return this;
    }
    
    public TreeMatcher setRequiresNodeToHaveParent(boolean yesNo) {
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
    
    protected static final class OperatorTypeMatcher implements INodeMatcher {

        private final OperatorType expectedType;
        
        public OperatorTypeMatcher(OperatorType expectedType)
        {
            if (expectedType == null) {
                throw new IllegalArgumentException("expectedType must not be NULL.");
            }
            this.expectedType = expectedType;
        }

        @Override
        public boolean matches(ASTNode n)
        {
            return n instanceof OperatorNode && expectedType.equals( ((OperatorNode) n).getType() );
        }
    }
    
    public TreeMatcher match(INodeMatcher matcher) 
    {
        if (matcher == null) {
            throw new IllegalArgumentException("matcher must not be NULL.");
        }
        
        if ( thisNodeMatcher != null ) {
            throw new IllegalStateException("Matcher already set to "+thisNodeMatcher);
        }        
        thisNodeMatcher = matcher;
        return this;
    }
    
    public TreeMatcher matchNOT() {
        return match( new OperatorTypeMatcher( OperatorType.NOT ) );
    }
    
    public TreeMatcher matchOR() {
        return match( new OperatorTypeMatcher( OperatorType.OR ) );
    }    
    
    public TreeMatcher matchAND() {
        return match( new OperatorTypeMatcher( OperatorType.OR ) );
    }       
    
    private boolean isMatchingLeftChild(ASTNode leftChild) 
    {
        if ( leftChildMatcher == null ) {
            return true;
        }
        
        if ( leftChild == null ) {
            return false;
        }
        if ( leftChildMatcher.matches( leftChild ) ) {
            matchedLeftChild = leftChild;
            return true;
        }
        return false;
    }    
    
    private boolean isMatchingRightChild(ASTNode rightChild) 
    {
        if ( rightChildMatcher == null ) {
            return true;
        }
        if ( rightChild == null ) {
            return false;
        }
        if ( rightChildMatcher.matches( rightChild ) ) {
            matchedRightChild = rightChild;
            return true;
        }
        return false;
    }   
    
    private boolean hasMatchingChildren(ASTNode parent) {
        
        boolean matches = matchesChildren(  
                unwrap( leftChild( parent ) , unwrapLeftChild ),
                unwrap( rightChild( parent ) , unwrapRightChild ) );
        
        if ( ! matches && ignoreChildOrder ) 
        {
            matchesChildren(  
                    unwrap( rightChild( parent ) , unwrapLeftChild ),
                    unwrap( leftChild( parent ) , unwrapRightChild ) );            
        }
        
        return matches;
    }
    
    private boolean matchesChildren(ASTNode leftChild,ASTNode rightChild)
    {
        boolean matches = isMatchingLeftChild( leftChild );
        matches |= isMatchingRightChild(rightChild );
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
    public boolean matches(ASTNode n)
    {
        if ( thisNodeMatcher == null ) {
            throw new IllegalStateException("Matcher not properly initialized");
        }
        
        matchedLeftChild = matchedRightChild = matchedParent = null;
        
        ASTNode matchedNode = unwrap(n,unwrapParent); 
        if ( requiresNodeToHaveParent && ! matchedNode.hasParent() ) {
            return false;
        }
        final boolean matches = thisNodeMatcher.matches( matchedNode ) && hasMatchingChildren( matchedNode );
        if ( matches ) {
            matchedParent = matchedNode;
        }
        return matches;
    }

}
