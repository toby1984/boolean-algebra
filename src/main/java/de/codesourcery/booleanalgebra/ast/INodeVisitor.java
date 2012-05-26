package de.codesourcery.booleanalgebra.ast;

public interface INodeVisitor
{
    public boolean visit(ASTNode node, int currentDepth);
}
