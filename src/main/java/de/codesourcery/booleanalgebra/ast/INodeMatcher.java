package de.codesourcery.booleanalgebra.ast;

public interface INodeMatcher
{
    public static final INodeMatcher ANY_NODE_MATCHER = new INodeMatcher() {

        @Override
        public boolean matches(ASTNode n)
        {
            return true;
        }
    };
    
    public boolean matches(ASTNode n);
}
