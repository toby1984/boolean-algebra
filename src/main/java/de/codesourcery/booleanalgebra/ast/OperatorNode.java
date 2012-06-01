package de.codesourcery.booleanalgebra.ast;

import org.apache.commons.lang.ObjectUtils;

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
        return 2;
    }
    
    public OperatorType getType()
    {
        return type;
    }
    
    public static OperatorNode not(ASTNode value) {
        return new OperatorNode(OperatorType.NOT , value );
    }
    
    public static OperatorNode and(ASTNode leftValue,ASTNode rightValue) {
        return new OperatorNode(OperatorType.AND , leftValue , rightValue );
    }
    
    public static OperatorNode or(ASTNode leftValue,ASTNode rightValue) {
        return new OperatorNode(OperatorType.OR , leftValue , rightValue );
    }    

    @Override
    public ASTNode parse(ILexer lexer) throws ParseException
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
    public String toString(boolean prettyPrint)
    {
        if ( type == null ) {
            return "<operator node without type?>";
        }
        switch( getType() ) {
            case AND:
                return childToString(0,prettyPrint)+" AND "+childToString(1,prettyPrint);
            case NOT:
                if ( hasChild( 0 ) && child(0) instanceof TermNode) { // termnode outputs it's own parens anyway
                    return "NOT "+childToString(0,prettyPrint);
                }
                return "NOT( "+childToString(0,prettyPrint)+" )";
            case OR:
                return childToString(0,prettyPrint)+" OR "+childToString(1,prettyPrint);
            default:
                throw new RuntimeException("Unhandled type "+getType() );
        }
    }
    
	@Override
	public boolean hasLiteralValue(IExpressionContext context) 
	{
		ASTNode result = evaluate( context );
		return result != this && result.hasLiteralValue( context );
	}      
	
	private ASTNode toNode(boolean value) {
		if ( value == true ) {
			return new TrueNode();
		}
		return new FalseNode();
	}
    
    @Override
	public ASTNode evaluate(IExpressionContext context)
    {
        switch( getType() ) {
            case OR: 
            	// $FALL-THROUGH$
            case AND:
            	ASTNode leftChild = child(0).evaluate(context);
            	ASTNode rightChild = child(1).evaluate(context);
            	if ( leftChild != null && rightChild != null &&
            		 leftChild.hasLiteralValue(context) && rightChild.hasLiteralValue(context) ) 
            	{
                    switch( getType() ) {
                    	case OR:
                    		return toNode( leftChild.getLiteralValue( context ) | 
                    				       rightChild.getLiteralValue( context ) );
                        case AND:
                    	    return toNode( leftChild.getLiteralValue( context ) & 
                    	    		       rightChild.getLiteralValue( context ) );                    	
                    	default:
                            throw new RuntimeException("Unhandled type "+getType() );                     		
                    }
            	}
                break;
            case NOT:
            	ASTNode value = child(0).evaluate( context );
            	if ( value != null && value.isLiteralValue() ) {
            		return value.getLiteralValue(context) ? new FalseNode() : new TrueNode(); 
            	}
            	break;
            default:
                throw new RuntimeException("Unhandled type "+getType() );                
        }        
        return this;
    }

	@Override
	protected OperatorNode copyThisNode() {
		if ( this.type == null ) {
			return new OperatorNode();
		}
		return new OperatorNode(this.type);
	}

	@Override
	public boolean isEquals(ASTNode other) 
	{
		if ( other instanceof OperatorNode) 
		{
			if ( ! ObjectUtils.equals( this.type , ((OperatorNode) other).type ) ) {
				return false;
			}
			final int len = getChildCount();
			if ( len != other.getChildCount() ) {
				return false;
			}
			for ( int i = 0 ; i < len ; i++ ) {
				if ( ! child(i).isEquals( other.child(i) ) ) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

}