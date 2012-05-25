package de.codesourcery.booleanalgebra.ast;

import de.codesourcery.booleanalgebra.IExpressionContext;
import de.codesourcery.booleanalgebra.exceptions.ParseException;
import de.codesourcery.booleanalgebra.lexer.ILexer;
import de.codesourcery.booleanalgebra.lexer.TokenType;

public class BooleanExpression extends ASTNode
{
    public BooleanExpression() {
    }
    
    @Override
    protected int getMaxSupportedChildCount()
    {
        return 2;
    }

    @Override
    public ASTNode parse(ILexer lexer, ASTNode previous) throws ParseException
    {
        addChild( new TermNode().parse( lexer, null ) );
        lexer.read(TokenType.EQUALS);
        addChild( new TermNode().parse( lexer, null ) );        
        return this;
    }
    
    public TermNode getLHS() {
        return (TermNode) child(0);
    }
    
    public TermNode getRHS() {
        return (TermNode) child(1);
    }    
    
    @Override
    public String toString()
    {
        return childToString(0)+" = "+childToString(1);
    }

    @Override
    public ASTNode evaluate(IExpressionContext context)
    {
        throw new UnsupportedOperationException("Cannot evaluate an expression,only terms");
    }

	@Override
	public boolean hasLiteralValue(IExpressionContext context) {
        return false;
	}

	@Override
	protected BooleanExpression copyThisNode() {
		return new BooleanExpression();
	}

}
