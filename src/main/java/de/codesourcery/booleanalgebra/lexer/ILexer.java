package de.codesourcery.booleanalgebra.lexer;

import de.codesourcery.booleanalgebra.exceptions.ParseException;

public interface ILexer
{
    public boolean eof() throws ParseException;
        
    public Token read() throws ParseException;
    
    public Token read(TokenType type) throws ParseException;    
    
    public Token peek() throws ParseException;

    public int currentParseOffset();
}
