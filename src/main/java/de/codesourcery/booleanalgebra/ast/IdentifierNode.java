package de.codesourcery.booleanalgebra.ast;

import org.apache.commons.lang.ObjectUtils;

import de.codesourcery.booleanalgebra.IExpressionContext;
import de.codesourcery.booleanalgebra.exceptions.ParseException;
import de.codesourcery.booleanalgebra.lexer.ILexer;
import de.codesourcery.booleanalgebra.lexer.TokenType;

public class IdentifierNode extends ASTNode
{
    private Identifier identifier;
    
    public IdentifierNode() {
    }
    
    public IdentifierNode(Identifier identifier) {
    	if (identifier == null) {
			throw new IllegalArgumentException("identifier must not be null");
		}
    	this.identifier = identifier;
    }
    
    @Override
    protected int getMaxSupportedChildCount()
    {
        return 0;
    }

    @Override
    public ASTNode parse(ILexer lexer) throws ParseException
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
    public String toString(boolean prettyPrint)
    {
        return identifier.toString();
    }
    
    @Override
	public ASTNode evaluate(IExpressionContext context)
    {
        return context.tryLookup( identifier );
    }

	@Override
	protected IdentifierNode copyThisNode() 
	{
		if ( this.identifier == null ) {
			return new IdentifierNode();
		}
		return new IdentifierNode( this.identifier );
	}  
	
	@Override
	public boolean isEquivalent(ASTNode other, IExpressionContext context) 
	{
		if ( other instanceof IdentifierNode) {
			return ObjectUtils.equals( this.identifier , ((IdentifierNode) other).getIdentifier() );
		}
		return false;
	}
	
	@Override
    public boolean hasLiteralValue(IExpressionContext context) 
	{
    	ASTNode value = context.tryLookup( identifier );
    	if ( value != null && value.isLiteralValue() ) {
    		return true;
    	}
    	return false;
    }	
	
    public boolean getLiteralValue(IExpressionContext context) {
    	return context.lookup( identifier ).getLiteralValue( context );
    }

	@Override
	public boolean isEquals(ASTNode other) 
	{
		if ( other instanceof IdentifierNode) 
		{
			return ObjectUtils.equals( this.identifier , ((IdentifierNode) other).identifier );
		}
		return false;
	}
    
}
