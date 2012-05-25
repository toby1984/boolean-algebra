package de.codesourcery.booleanalgebra.lexer;

import de.codesourcery.booleanalgebra.Scanner;
import de.codesourcery.booleanalgebra.exceptions.ParseException;
import junit.framework.TestCase;

public class LexerTest  extends TestCase
{

    private ILexer lexer;
    
    public void testLexNULLString() {
        
        String expr = null;
        try {
            lexer = new Lexer(new Scanner( expr ) );
            fail("Should've failed");
        } catch(IllegalArgumentException e) {
            // OK
        }
    }
    
    public void testLexEmptyString() {
        String expr = "";
        lexer = new Lexer(new Scanner( expr ) );
        assertTrue( lexer.eof() );
        try {
            lexer.read();
        } catch(ParseException e) {
            // OK
        }
        try {
            lexer.peek();
        } catch(ParseException e) {
            // OK
        }       
        try {
            lexer.read(TokenType.AND);
        } catch(ParseException e) {
            // OK
        }           
    }  
    
    public void testLexExpression() 
    {
        String expr = "  a   or   not  (  b  and  c )  =  d $#!  ";
        
        lexer = new Lexer(new Scanner( expr ) );
        assertFalse( lexer.eof() );
        
        Token tok = lexer.read(TokenType.IDENTIFIER);
        assertEquals("a" , tok.getContents() );
        
        tok = lexer.read(TokenType.OR);
        assertEquals("or" , tok.getContents() );        
        
        tok = lexer.read(TokenType.NOT);
        assertEquals("not" , tok.getContents() ); 
        
        tok = lexer.read(TokenType.PARENS_OPEN);
        assertEquals("(" , tok.getContents() ); 
        
        tok = lexer.read(TokenType.IDENTIFIER);
        assertEquals("b" , tok.getContents() ); 
        
        tok = lexer.read(TokenType.AND);
        assertEquals("and" , tok.getContents() ); 
        
        tok = lexer.read(TokenType.IDENTIFIER);
        assertEquals("c" , tok.getContents() ); 
        
        tok = lexer.read(TokenType.PARENS_CLOSE);
        assertEquals(")" , tok.getContents() );         
      
        tok = lexer.read(TokenType.EQUALS);
        assertEquals("=" , tok.getContents() );   
        
        tok = lexer.read(TokenType.IDENTIFIER);
        assertEquals("d" , tok.getContents() );   
        
        tok = lexer.read(TokenType.CHARACTERS);
        assertEquals("$#!" , tok.getContents() );  
        
        assertTrue( lexer.eof() );
    }     
}
