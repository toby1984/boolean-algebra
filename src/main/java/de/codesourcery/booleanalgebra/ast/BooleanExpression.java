package de.codesourcery.booleanalgebra.ast;

import de.codesourcery.booleanalgebra.IExpressionContext;
import de.codesourcery.booleanalgebra.exceptions.ParseException;
import de.codesourcery.booleanalgebra.lexer.ILexer;
import de.codesourcery.booleanalgebra.lexer.TokenType;

public class BooleanExpression extends ASTNode
{
	public BooleanExpression() {
	}
	
	public BooleanExpression(ASTNode lhs,ASTNode rhs) {
		addChild( lhs );
		addChild( rhs );
	}

	@Override
	protected int getMaxSupportedChildCount()
	{
		return 2;
	}

	@Override
	public ASTNode parse(ILexer lexer) throws ParseException
	{
		addChild( parseTerm( lexer ) );
		lexer.read(TokenType.EQUALS);
		addChild( parseTerm( lexer ) );        
		return this;
	}
	
    protected static ASTNode unwrap(ASTNode n) {
        
        ASTNode current = n;
        while ( current instanceof TermNode && current.hasChildren() && current.child(0) instanceof TermNode) {
            current = current.child(0);
        }
        return current;
    }	

	private ASTNode parseTerm(ILexer lexer ) throws ParseException 
	{
	    final ASTNode result = new TermNode().parse( lexer );
        return unwrap( result );
	}
	
	public ASTNode getLHS() {
		return child(0);
	}

	public ASTNode getRHS() {
		return child(1);
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
		return new BooleanExpression(child(0),child(1));
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
