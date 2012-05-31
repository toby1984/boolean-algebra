package de.codesourcery.booleanalgebra;

import de.codesourcery.booleanalgebra.ast.ASTNode;
import de.codesourcery.booleanalgebra.ast.BooleanExpression;
import de.codesourcery.booleanalgebra.ast.TermNode;
import de.codesourcery.booleanalgebra.lexer.ILexer;
import de.codesourcery.booleanalgebra.lexer.Lexer;
import de.codesourcery.booleanalgebra.lexer.TokenType;

public class BooleanExpressionParser
{
    public ASTNode parse(String expression) {
        return internalParse( expression );
    }    
    
    public ASTNode internalParse(String expression) 
    {
        ILexer lexer = new Lexer( new Scanner( expression ) );
        ASTNode lhs = new TermNode().parse( lexer );
        if ( lexer.eof() ) {
            return unwrap( lhs );
        }
        lexer.read(TokenType.EQUALS );
        ASTNode rhs = new TermNode().parse( lexer );
        if ( ! lexer.eof() ) {
            throw new RuntimeException("Internal error, parser failed to consume all tokens");
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
