package de.codesourcery.booleanalgebra.lexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.codesourcery.booleanalgebra.IScanner;
import de.codesourcery.booleanalgebra.ast.Identifier;
import de.codesourcery.booleanalgebra.exceptions.ParseException;

public class Lexer implements ILexer
{
    private final IScanner scanner;
    
    private final StringBuilder buffer = new StringBuilder();
    private final List<Token> tokens=new ArrayList<Token>();
    
    private boolean skipWhitespace = true;
    
    public Lexer(IScanner scanner) {
        this.scanner = scanner;
    }
    
    private Token currentToken() {
        
        if ( tokens.isEmpty() )
        {
            try {
                if ( scanner.eof() ) {
                    return null;
                }
                parseNextTokens();                
            } catch (IOException e) {
                throw new ParseException(e.getMessage(),scanner.currentParseOffset(),e);                
            }
            
            if ( tokens.isEmpty() ) {
                return null;
            }
        }
        return tokens.get(0);
    }
    
    private void parseNextTokens() throws IOException
    {
        int startOffset = scanner.currentParseOffset();
        buffer.setLength( 0 );
outer_loop:        
        while ( ! scanner.eof() && tokens.isEmpty() ) 
        {
            char c = scanner.read();
            switch( c ) {
                case '=':
                    bufferToTokens(startOffset,true);
                    tokens.add( new Token( c , scanner.currentParseOffset() - 1, TokenType.EQUALS) );
                    return;                    
                case ' ': /* whitespace */
                case '\t':
                    bufferToTokens( startOffset , true );
                    if ( skipWhitespace ) 
                    {
                        while( ! scanner.eof() ) 
                        {
                            c = scanner.peek();
                            if ( c != ' ' && c != '\t' ) {
                                continue outer_loop;
                            }
                            scanner.read();
                        }
                        return;
                    }
                    startOffset = scanner.currentParseOffset() -1;
                    buffer.append( c );
outer:                    
                    while( ! scanner.eof() ) {
                        c = scanner.peek();
                        switch( c ) {
                            case ' ':
                            case '\t':
                                buffer.append( scanner.read() );
                                break;
                            default:
                                break outer;
                        }
                    }
                    tokens.add( new Token( buffer , startOffset , TokenType.WHITESPACE) );
                    return;
                case '(':
                    bufferToTokens(startOffset,true);
                    tokens.add( new Token( c , scanner.currentParseOffset() - 1 , TokenType.PARENS_OPEN ) );
                    return;
                case ')':
                    bufferToTokens(startOffset,true);
                    tokens.add( new Token( c , scanner.currentParseOffset() - 1 , TokenType.PARENS_CLOSE ) );
                    return;
            }

            buffer.append( c );
            if ( bufferToTokens(startOffset,false) ) {
                return;
            }
        }
        bufferToTokens(startOffset,true);
    }
    
    private boolean bufferToTokens(int startOffset,boolean delimiterSeen) 
    {
        if ( buffer.length() == 0 ) {
            return false;
        }
        if ( internalBufferToTokens( startOffset , delimiterSeen ) ) {
            buffer.setLength( 0 );
            return true;
        }
        return false;
    }
    
    private boolean internalBufferToTokens(int startOffset,boolean delimiterSeen) 
    {
        if ( "OR".equalsIgnoreCase( buffer.toString() ) ) {
            tokens.add( new Token( buffer , startOffset , TokenType.OR ) );
            return true;
        }
        if ( "AND".equalsIgnoreCase( buffer.toString() ) ) {
            tokens.add(new Token( buffer , startOffset , TokenType.AND ) );
            return true;
        }    
        if ( "NOT".equalsIgnoreCase( buffer.toString() ) ) {
            tokens.add( new Token( buffer , startOffset , TokenType.NOT ) );
            return true;
        }  
        if ( "true".equalsIgnoreCase( buffer.toString() ) ) {
            tokens.add( new Token( buffer , startOffset , TokenType.TRUE ) );
            return true;
        }  
        if ( "false".equalsIgnoreCase( buffer.toString() ) ) {
            tokens.add( new Token( buffer , startOffset , TokenType.FALSE ) );
            return true;
        }         
        
        if ( delimiterSeen ) 
        {
            if ( Identifier.isValidIdentifier( buffer.toString() ) ) {
                tokens.add( new Token( buffer , startOffset , TokenType.IDENTIFIER ) );                
            } else {
                tokens.add( new Token( buffer , startOffset , TokenType.CHARACTERS ) );
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean eof()
    {
        try {
            return currentToken() == null && scanner.eof();
        } catch (IOException e) {
            throw new ParseException(e.getMessage(),scanner.currentParseOffset(),e);
        }
    }

    @Override
    public Token read()
    {
        checkEOF();
        final Token result = currentToken();
        tokens.remove( 0 );
        return result;
    }

    private void checkEOF()
    {
        if ( eof() ) {
            throw new ParseException("Already at EOF",scanner.currentParseOffset() );
        }
    }

    @Override
    public Token read(TokenType type)
    {
        if ( ! peek().hasType( type ) ) {
            throw new ParseException("Expected token of type "+type+" but got "+peek().getType(),scanner.currentParseOffset() );            
        }
        return read();
    }

    @Override
    public Token peek()
    {
        checkEOF();
        return currentToken();
    }

    @Override
    public int currentParseOffset()
    {
        Token tok = currentToken();
        if ( tok == null ) {
            return scanner.currentParseOffset();
        }
        return tok.getParseOffset();
    }

}
