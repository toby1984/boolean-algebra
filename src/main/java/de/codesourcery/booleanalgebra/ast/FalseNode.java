package de.codesourcery.booleanalgebra.ast;

import de.codesourcery.booleanalgebra.IExpressionContext;
import de.codesourcery.booleanalgebra.exceptions.ParseException;
import de.codesourcery.booleanalgebra.lexer.ILexer;
import de.codesourcery.booleanalgebra.lexer.TokenType;

public class FalseNode extends ASTNode
{
    @Override
    protected int getMaxSupportedChildCount()
    {
        return 0;
    }

    @Override
    public ASTNode parse(ILexer lexer, ASTNode previousNode) throws ParseException
    {
        lexer.read(TokenType.FALSE);
        return this;
    }
    
    @Override
    public boolean isLiteralValue() {
    	return true;
    }
    
    @Override
    public String toString()
    {
        return "false";
    }
    
    @Override
    public ASTNode evaluate(IExpressionContext context)
    {
        return this;
    }  
    
    @Override
    public boolean getLiteralValue(IExpressionContext context) {
    	return false;
    }   
    
	@Override
	protected FalseNode copyThisNode() {
		return new FalseNode();
	} 	
	
	@Override
    public boolean hasLiteralValue(IExpressionContext context) {
    	return true;
    }	
}
