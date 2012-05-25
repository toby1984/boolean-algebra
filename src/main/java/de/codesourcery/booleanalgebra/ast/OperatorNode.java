package de.codesourcery.booleanalgebra.ast;

import de.codesourcery.booleanalgebra.IExpressionContext;
import de.codesourcery.booleanalgebra.exceptions.ParseException;
import de.codesourcery.booleanalgebra.lexer.ILexer;
import de.codesourcery.booleanalgebra.lexer.Token;

public class OperatorNode extends ASTNode
{
    private OperatorType type;
    
    public OperatorNode() {
    }
    
    public OperatorNode(OperatorType type) 
    {
        if (  type == null ) {
            throw new IllegalArgumentException("type must not be NULL.");
        }
        this.type = type;
    }      
    
    public OperatorNode(OperatorType type,ASTNode n1,ASTNode... nodes) {
        super(n1,nodes);
        if (  type == null ) {
            throw new IllegalArgumentException("type must not be NULL.");
        }
        this.type = type;
    }    
    
    @Override
    protected int getMaxSupportedChildCount()
    {
        return -1;
    }
    
    public OperatorType getType()
    {
        return type;
    }
    
    public static OperatorNode createNOT(ASTNode value) {
        return new OperatorNode(OperatorType.NOT , value );
    }
    
    public static OperatorNode createAND(ASTNode leftValue,ASTNode rightValue) {
        return new OperatorNode(OperatorType.AND , leftValue , rightValue );
    }
    
    public static OperatorNode createOR(ASTNode leftValue,ASTNode rightValue) {
        return new OperatorNode(OperatorType.OR , leftValue , rightValue );
    }    

    @Override
    public ASTNode parse(ILexer lexer, ASTNode previousNode) throws ParseException
    {
        Token tok = lexer.peek();
        switch( tok.getType() ) {
            case NOT:
                type = OperatorType.NOT;
                break;
            case AND:
                type = OperatorType.AND;
                break;
            case OR:
                type = OperatorType.OR;
                break;
                default:
                    throw new ParseException("Expected AND,OR or NOT , got "+tok,tok.getParseOffset() );
        }
        lexer.read();
        return this;
    }
    
    @Override
    public boolean isNOT()
    {
        return this.type == OperatorType.NOT;
    }
    
    public boolean isAND() {
        return this.type == OperatorType.AND;
    }
    
    public boolean isOR() {
        return this.type == OperatorType.OR;
    }

    @Override
    public String toString()
    {
        if ( type == null ) {
            return "<operator node without type?>";
        }
        switch( getType() ) {
            case AND:
                return childToString(0)+" AND "+childToString(1);
            case NOT:
                if ( hasChild( 0 ) && child(0) instanceof TermNode) { // termnode outputs it's own parens anyway
                    return "NOT"+childToString(0);
                }
                return "NOT("+childToString(0)+")";
            case OR:
                return childToString(0)+" OR "+childToString(1);
            case PARENS:
                return "()";
            default:
                throw new RuntimeException("Unhandled type "+getType() );
        }
    }
    
    @Override
    public boolean evaluate(IExpressionContext context)
    {
        switch( getType() ) {
            case AND:
                return child(0).evaluate(context) & child(1).evaluate( context );
            case NOT:
                return ! child(0).evaluate( context );
            case OR:
                return child(0).evaluate(context) | child(1).evaluate( context );
            default:
                throw new RuntimeException("Unhandled type "+getType() );                
        }        
    }  
    
}