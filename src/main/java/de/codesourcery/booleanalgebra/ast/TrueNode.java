package de.codesourcery.booleanalgebra.ast;

import de.codesourcery.booleanalgebra.IExpressionContext;
import de.codesourcery.booleanalgebra.exceptions.ParseException;
import de.codesourcery.booleanalgebra.lexer.ILexer;
import de.codesourcery.booleanalgebra.lexer.TokenType;

public class TrueNode extends ASTNode
{
    @Override
    protected int getMaxSupportedChildCount()
    {
        return 0;
    }

    @Override
    public boolean isLiteralValue() {
    	return true;
    }
    
    @Override
    public ASTNode parse(ILexer lexer) throws ParseException
    {
        lexer.read(TokenType.TRUE);
        return this;
    }
    
    @Override
    public String toString(boolean prettyPrint)
    {
        return "true";
    }

    @Override
	public ASTNode evaluate(IExpressionContext context)
    {
        return this;
    }

    @Override
    public boolean getLiteralValue(IExpressionContext context) {
    	return true;
    }    
    
    @Override
    public boolean isEquivalent(ASTNode other, IExpressionContext context) 
    {
    	if ( other.evaluate( context ) instanceof TrueNode) {
    		return true;
    	}
    	return super.isEquivalent(other, context);
    }

	@Override
	protected TrueNode copyThisNode() {
		return new TrueNode();
	}    

	@Override
    public boolean hasLiteralValue(IExpressionContext context) {
    	return true;
    }

	@Override
	public boolean isEquals(ASTNode other) {
		return other instanceof TrueNode;
	}

    @Override
    protected int thisHashCode()
    {
        return 0xdeadbeef;
    }	
}
