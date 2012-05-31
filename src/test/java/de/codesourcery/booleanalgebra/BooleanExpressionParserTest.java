package de.codesourcery.booleanalgebra;

import java.io.PrintWriter;
import java.util.Iterator;

import junit.framework.TestCase;
import de.codesourcery.booleanalgebra.ast.ASTNode;
import de.codesourcery.booleanalgebra.ast.IdentifierNode;
import de.codesourcery.booleanalgebra.ast.OperatorNode;
import de.codesourcery.booleanalgebra.ast.OperatorType;
import de.codesourcery.booleanalgebra.ast.TermNode;
import de.codesourcery.booleanalgebra.exceptions.ParseException;

public class BooleanExpressionParserTest extends TestCase
{
    private BooleanExpressionParser parser;
    
    @Override
    protected void setUp() throws Exception
    {
        parser = new BooleanExpressionParser();
    }
    
    public void testParseEmptyExpression() {
        
        final String expr = "()";
        try {
            parser.parse( expr );
            fail("Should've failed");
        } catch(ParseException e) {
            // ok
        }
    }
    
    public void testIncompleteBlankExpression1() {
        
        final String expr = "not";
        try {
            parser.parse( expr );
            fail("Should've failed");
        } catch(ParseException e) {
            // ok
        }
    }   
    
    public void testIncompleteBlankExpression2() {
        
        final String expr = "a and";
        try {
            parser.parse( expr );
            fail("Should've failed");
        } catch(ParseException e) {
            // ok
        }
    }  
    
    public void testIncompleteBlankExpression3() {
        
        final String expr = "and a";
        try {
            parser.parse( expr );
            fail("Should've failed");
        } catch(ParseException e) {
            // ok
        }
    }  
    
    private void print(ASTNode node) {
        node.print( new PrintWriter(System.out,true) );
    }
    
    public void testParseAndExpression() {
        
        final String expr = "a and b";
        ASTNode term = parser.parse( expr );
        
        Iterator<ASTNode> it = term.createPreOrderIterator();
        
        ASTNode node = it.next();
        assertTrue( node instanceof TermNode );
        
        node = it.next();
        assertTrue( "was: "+node.getClass() , node instanceof OperatorNode );
        assertEquals( OperatorType.AND , ((OperatorNode) node).getType() );
        
        node = it.next();
        assertTrue( node instanceof IdentifierNode );
        assertEquals("a" , ((IdentifierNode) node).getIdentifier().getValue() );
        
        node = it.next();
        assertTrue( node instanceof IdentifierNode );
        assertEquals("b" , ((IdentifierNode) node).getIdentifier().getValue() );
        
        assertIteratorAtEOF(it);
    }
    
    public void testParseExpressionWithParens() {
        
        final String expr = "(a and b)";
        ASTNode term = parser.parse( expr );
        
        Iterator<ASTNode> it = term.createPreOrderIterator();
        
        ASTNode node = it.next();
        assertTrue( node instanceof TermNode );
        
        node = it.next();
        assertTrue( "was: "+node.getClass() , node instanceof OperatorNode );
        assertEquals( OperatorType.AND , ((OperatorNode) node).getType() );
        
        node = it.next();
        assertTrue( node instanceof IdentifierNode );
        assertEquals("a" , ((IdentifierNode) node).getIdentifier().getValue() );
        
        node = it.next();
        assertTrue( node instanceof IdentifierNode );
        assertEquals("b" , ((IdentifierNode) node).getIdentifier().getValue() );
        assertIteratorAtEOF(it);
    }    
    
    public void testParseNestedExpression() {
        
        final String expr = "not(a and b)";
        ASTNode term = parser.parse( expr );
        print( term );
        
        Iterator<ASTNode> it = term.createPreOrderIterator();
        
        ASTNode node = it.next();
        assertTrue( node instanceof TermNode );
        
        node = it.next();
        assertTrue( "was: "+node.getClass() , node instanceof OperatorNode );
        assertEquals( OperatorType.NOT , ((OperatorNode) node).getType() );
        
        node = it.next();
        assertTrue( node instanceof TermNode );
        
        node = it.next();
        assertTrue( "was: "+node.getClass() , node instanceof OperatorNode );
        assertEquals( OperatorType.AND , ((OperatorNode) node).getType() );
        
        node = it.next();
        assertTrue( node instanceof IdentifierNode );
        assertEquals("a" , ((IdentifierNode) node).getIdentifier().getValue() );
        
        node = it.next();
        assertTrue( node instanceof IdentifierNode );
        assertEquals("b" , ((IdentifierNode) node).getIdentifier().getValue() );
        
        assertIteratorAtEOF(it);
    }     
    
    private void assertIteratorAtEOF(Iterator<ASTNode> it ) {
        if ( it.hasNext() ) {
            fail("Iterator should be at EOF but returns "+it.next());
        }
    }
    
    public void testParseOrExpression() {
        
        final String expr = "a or b";
        ASTNode term = parser.parse( expr );
        
        Iterator<ASTNode> it = term.createPreOrderIterator();
        
        ASTNode node = it.next();
        assertTrue( node instanceof TermNode );
        
        node = it.next();
        assertTrue( node instanceof OperatorNode );
        assertEquals( OperatorType.OR , ((OperatorNode) node).getType() );
        
        node = it.next();
        assertTrue( node instanceof IdentifierNode );
        assertEquals("a" , ((IdentifierNode) node).getIdentifier().getValue() );
        
        node = it.next();
        assertTrue( node instanceof IdentifierNode );
        assertEquals("b" , ((IdentifierNode) node).getIdentifier().getValue() );
        
        assertIteratorAtEOF(it);
    }     
    
    public void testParseNotExpression() {
        
        final String expr = "not a";
        ASTNode term = parser.parse( expr );
        
        Iterator<ASTNode> it = term.createPreOrderIterator();
        
        ASTNode node = it.next();
        assertTrue( node instanceof TermNode );
        
        node = it.next();
        assertTrue( node instanceof OperatorNode );
        assertEquals( OperatorType.NOT , ((OperatorNode) node).getType() );
        
        node = it.next();
        assertTrue( node instanceof IdentifierNode );
        assertEquals("a" , ((IdentifierNode) node).getIdentifier().getValue() );
        
        assertIteratorAtEOF(it);
    }    
}
