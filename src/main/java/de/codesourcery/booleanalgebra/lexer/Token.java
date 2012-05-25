package de.codesourcery.booleanalgebra.lexer;

public class Token
{
    private final int parseOffset;
    private final String contents;
    private final TokenType type;
    
    public Token(StringBuilder buffer, int parseOffset, TokenType type)
    {
        this( buffer.toString() , parseOffset, type );
    }
    
    public Token(char c, int parseOffset, TokenType type)
    {
        this( Character.toString( c ) , parseOffset, type );
    }
    
    public Token(String contents, int parseOffset,TokenType type)
    {
        if ( contents == null ) {
            throw new IllegalArgumentException("contents must not be NULL.");
        }
        if ( type == null ) {
            throw new IllegalArgumentException("type must not be NULL.");
        }
        this.parseOffset = parseOffset;
        this.contents = contents;
        this.type = type;
    }
    
    public int getParseOffset()
    {
        return parseOffset;
    }
    
    public String getContents()
    {
        return contents;
    }
    
    public TokenType getType()
    {
        return type;
    }

    public boolean hasType(TokenType t) {
        if (t == null) {
            throw new IllegalArgumentException("t must not be NULL.");
        }
        return getType() == t;
    }
    
    @Override
    public String toString()
    {
        return "'"+getContents()+"' ( "+getType()+" , offset "+parseOffset+" )";
    }
    
}
