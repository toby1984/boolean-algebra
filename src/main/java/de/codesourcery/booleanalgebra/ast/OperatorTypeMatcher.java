package de.codesourcery.booleanalgebra.ast;

public class OperatorTypeMatcher implements INodeMatcher {

    private final OperatorType expectedType;
    
    private OperatorTypeMatcher(OperatorType expectedType)
    {
        if (expectedType == null) {
            throw new IllegalArgumentException("expectedType must not be NULL.");
        }
        this.expectedType = expectedType;
    }
    
    public static INodeMatcher matchAND() {
        return new OperatorTypeMatcher( OperatorType.AND );
    }
    
    public static INodeMatcher matchOR() {
        return new OperatorTypeMatcher( OperatorType.OR );
    }
    
    public static INodeMatcher matchNOT() {
        return new OperatorTypeMatcher( OperatorType.NOT );
    }    

    @Override
    public boolean matches(TreeMatcher matcher, ASTNode n)
    {
        return n instanceof OperatorNode && expectedType.equals( ((OperatorNode) n).getType() );
    }
}