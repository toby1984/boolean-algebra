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
	public ASTNode parse(ILexer lexer) throws ParseException
	{
		addChild( new TermNode().parse( lexer ) );
		lexer.read(TokenType.EQUALS);
		addChild( new TermNode().parse( lexer ) );        
		return this;
	}

	public TermNode getLHS() {
		return (TermNode) child(0);
	}

	public TermNode getRHS() {
		return (TermNode) child(1);
	}    

	@Override
	public String toString(boolean prettyPrint)
	{
		return childToString(0,prettyPrint)+" = "+childToString(1,prettyPrint);
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

	@Override
	public boolean isEquals(ASTNode other) 
	{
		if ( other instanceof BooleanExpression ) {
			BooleanExpression otherExpr = (BooleanExpression ) other;
			return getLHS().isEquals( otherExpr.getLHS() ) &&
					getRHS().isEquals( otherExpr.getRHS() );
		}
		return false;
	}

	@Override
	public boolean isEquivalent(ASTNode other, IExpressionContext context) 
	{
		if ( !(other instanceof BooleanExpression) ) 
		{
			return false;
		}

		final BooleanExpression otherExpr = (BooleanExpression ) other;

		if ( getLHS().isEquivalent( otherExpr.getLHS() , context ) &&
		     getRHS().isEquivalent( otherExpr.getRHS() , context ) ) 
		{
			return true;
		}

		if ( getLHS().isEquivalent( otherExpr.getRHS() , context ) &&
			 getRHS().isEquivalent( otherExpr.getLHS() , context ) ) 
		{
			return true;
		}		
		return false;
	}
}
