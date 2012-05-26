package de.codesourcery.booleanalgebra;

import de.codesourcery.booleanalgebra.ast.ASTNode;
import de.codesourcery.booleanalgebra.ast.BooleanExpression;
import de.codesourcery.booleanalgebra.ast.TermNode;
import de.codesourcery.booleanalgebra.lexer.ILexer;
import de.codesourcery.booleanalgebra.lexer.Lexer;

public class BooleanExpressionParser
{

    public BooleanExpression parseExpression(String expression,IExpressionContext context) {
        ILexer lexer = new Lexer( new Scanner( expression ) );
        BooleanExpression result = (BooleanExpression) new BooleanExpression().parse( lexer );
        return result;
    }
    
    public ASTNode parseTerm(String expression,IExpressionContext context) {
        ILexer lexer = new Lexer( new Scanner( expression ) );
        return new TermNode().parse( lexer );
    }    

}
