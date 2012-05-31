package de.codesourcery.booleanalgebra.lexer;

import java.io.PrintWriter;

import junit.framework.TestCase;
import de.codesourcery.booleanalgebra.BooleanExpressionParser;
import de.codesourcery.booleanalgebra.ast.ASTNode;

public class ParserTest extends TestCase 
{

    public void testParsing() {
        
        String expr = "  a   or   not  ((  b  and  c ) or d)  =  e   ";
        
        ASTNode expression = new BooleanExpressionParser().parse( expr );
        
        System.out.println("PARSED: "+expression);
        
        PrintWriter writer = new PrintWriter( System.out , true );
        expression.print( writer );
        writer.flush();
        writer.close();
        assertEquals("a OR NOT((b AND c) OR d) = e" , expression.toString() );

    }
}
