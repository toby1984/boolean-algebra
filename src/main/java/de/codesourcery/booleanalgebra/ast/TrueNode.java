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
    public ASTNode parse(ILexer lexer, ASTNode previousNode) throws ParseException
    {
        lexer.read(TokenType.TRUE);
        return this;
    }
    
    @Override
    public String toString()
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
	protected TrueNode copyThisNode() {
		return new TrueNode();
	}    

	@Override
    public boolean hasLiteralValue(IExpressionContext context) {
    	return true;
    }	
}
