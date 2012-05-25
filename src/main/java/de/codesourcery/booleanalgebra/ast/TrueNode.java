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
    public boolean evaluate(IExpressionContext context)
    {
        return true;
    }    

}
