package de.codesourcery.booleanalgebra;

import de.codesourcery.booleanalgebra.ast.BooleanExpression;
import de.codesourcery.booleanalgebra.ast.TermNode;
import de.codesourcery.booleanalgebra.lexer.ILexer;
import de.codesourcery.booleanalgebra.lexer.Lexer;

public class BooleanExpressionParser
{

    public BooleanExpression parseExpression(String expression) {
        ILexer lexer = new Lexer( new Scanner( expression ) );
        BooleanExpression result = (BooleanExpression) new BooleanExpression().parse( lexer , null );
        return result;
    }
    
    public TermNode parseTerm(String expression) {
        ILexer lexer = new Lexer( new Scanner( expression ) );
        TermNode result = (TermNode) new TermNode().parse( lexer , null );
        return result;
    }    

}
