package de.codesourcery.booleanalgebra.ast;

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
    public ASTNode evaluate(IExpressionContext context)
    {
        boolean value = context.lookup( identifier );
        return value ? new TrueNode() : new FalseNode();
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
    public boolean hasLiteralValue(IExpressionContext context) {
    	return context.tryLookup( identifier ) != null;
    }	
	
    public boolean getLiteralValue(IExpressionContext context) {
    	return context.lookup( identifier );
    }
    
}
