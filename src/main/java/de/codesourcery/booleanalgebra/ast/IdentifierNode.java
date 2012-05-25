package de.codesourcery.booleanalgebra.ast;

import de.codesourcery.booleanalgebra.IExpressionContext;
import de.codesourcery.booleanalgebra.exceptions.ParseException;
import de.codesourcery.booleanalgebra.lexer.ILexer;
import de.codesourcery.booleanalgebra.lexer.TokenType;

public class IdentifierNode extends ASTNode
{
    private Identifier identifier;
    
    @Override
    protected int getMaxSupportedChildCount()
    {
        return 0;
    }

    @Override
    public ASTNode parse(ILexer lexer, ASTNode previousNode) throws ParseException
    {
        final int start = lexer.currentParseOffset();
        final String tmp = lexer.read(TokenType.IDENTIFIER).getContents();
        if ( ! Identifier.isValidIdentifier( tmp ) ) {
            throw new ParseException( "Not a valid identifier: '"+tmp+"'" , start );
        }
        identifier = new Identifier( tmp );
        return this;
    }

    public Identifier getIdentifier()
    {
        return identifier;
    }
    
    @Override
    public String toString()
    {
        return identifier.toString();
    }
    
    @Override
    public boolean evaluate(IExpressionContext context)
    {
        return context.lookup( identifier );
    }  
}
