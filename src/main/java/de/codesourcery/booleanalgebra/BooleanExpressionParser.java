package de.codesourcery.booleanalgebra;

import org.apache.commons.lang.StringUtils;

import de.codesourcery.booleanalgebra.ast.ASTNode;
import de.codesourcery.booleanalgebra.ast.BooleanExpression;
import de.codesourcery.booleanalgebra.ast.TermNode;
import de.codesourcery.booleanalgebra.exceptions.ParseException;
import de.codesourcery.booleanalgebra.lexer.ILexer;
import de.codesourcery.booleanalgebra.lexer.Lexer;
import de.codesourcery.booleanalgebra.lexer.TokenType;

public class BooleanExpressionParser
{
    public ASTNode parse(String expression,boolean wrapInTerm) {
        return internalParse( expression , wrapInTerm );
    }    
    
    private ASTNode internalParse(String expression,boolean wrapInTerm) 
    {
        final ILexer lexer = new Lexer( new Scanner( expression ) );
        
        ASTNode lhs = unwrap( new TermNode().parse( lexer ) );
        
        if ( ! wrapInTerm && lhs instanceof TermNode) 
        {
            final String s = StringUtils.remove( StringUtils.remove( expression, " " ) , "\t" );
            if ( lhs instanceof TermNode && ! s.startsWith("(" ) ) {
                lhs = lhs.child(0);
            }
        } else if ( wrapInTerm && !(lhs instanceof TermNode) ) {
            lhs = new TermNode( lhs );
        }
        
        if ( lexer.eof() ) // no equation
        {
            return lhs;
        }
        
        lexer.read(TokenType.EQUALS );
        
        ASTNode rhs = new TermNode().parse( lexer );

        if ( wrapInTerm && !(rhs instanceof TermNode) ) {
            rhs = new TermNode( rhs );
        }
        
        if ( ! lexer.eof() ) {
            throw new ParseException("Parse error, trailing garbate at offset "+lexer.currentParseOffset(),lexer.currentParseOffset());
        }        
        return new BooleanExpression( unwrap(lhs) , unwrap( rhs ) );
    }
    
    public ASTNode unwrap(ASTNode n) {
        
        ASTNode current = n;
        while ( current instanceof TermNode && current.hasChildren() && current.child(0) instanceof TermNode) {
            current = current.child(0);
        }
        return current;
    }

}
