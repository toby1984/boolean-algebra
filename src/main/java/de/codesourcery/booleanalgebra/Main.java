package de.codesourcery.booleanalgebra;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;

import de.codesourcery.booleanalgebra.ast.ASTNode;
import de.codesourcery.booleanalgebra.ast.FalseNode;
import de.codesourcery.booleanalgebra.ast.INodeVisitor;
import de.codesourcery.booleanalgebra.ast.Identifier;
import de.codesourcery.booleanalgebra.ast.IdentifierNode;
import de.codesourcery.booleanalgebra.ast.OperatorNode;
import de.codesourcery.booleanalgebra.ast.OperatorType;
import de.codesourcery.booleanalgebra.ast.TermNode;
import de.codesourcery.booleanalgebra.ast.TrueNode;

public class Main
{
	private final BooleanExpressionParser parser = new BooleanExpressionParser();
	
	private final ASTTransformations transformer = new ASTTransformations();
	
	private final Random r = new Random(System.currentTimeMillis());
	
    public static void main(String[] args) throws FileNotFoundException
    {
    	new Main().run();
    }
    
    public void run() throws FileNotFoundException 
    {
		// Assoziativgesetz
		// (a and b) and c = a and (b and c) 	
		// (a or b) or c = a or (b or c)
    	
//    	final String expr1 = "(b or c) or a";
//    	ASTNode term = parseTerm( expr1 );
//    	term.toDOT( new FileOutputStream("/home/tobi/tmp/test.dot" ) );
//    	simplify( term );
//    	System.exit(0);
//    	
//    	final String expr2 = " c or ( a and b) ";
//    	assertTermsAreEquivalent( parseTerm( expr1 ) , parseTerm( expr2 ) );
//    	System.exit(0);
    	
    	for ( int i = 0 ; i < 10 ; i++ ) 
    	{
    		if (  ( i % 1000 ) == 0 ) {
    			System.out.println("Tested "+i+" terms...");
    		}
    		ASTNode t = createTerm( 3 , 4 );
        	t.toDOT( new FileOutputStream("/home/tobi/tmp/generated_term.dot",false) );
        	final String stringForm = t.toString(false);
        	ASTNode parsed = parseTerm( stringForm );
        	t.toDOT( new FileOutputStream("/home/tobi/tmp/parsed_term.dot",false) );
    		testSimplify( parsed );
    	}
    }

	private void testSimplify(final ASTNode term) {
		ASTNode simplified = simplify( term ); 
		System.out.println( term+" -> "+simplified+"\n------------------------------\n");
		assertTermsAreEquivalent( term , simplified );
	}
    
    public void assertTermsAreEquivalent(ASTNode input , ASTNode output) 
    {
    	ExpressionContext context = new ExpressionContext();
    	
    	final List<Identifier> vars = new ArrayList<Identifier>( transformer.gatherIdentifiers( input ) );
    	
    	// size = 1 => 1 << 1 => 2
    	// size = 2 => 1 << 2 => 4
    	long maxValue = vars.isEmpty() ? 0 : 1<< vars.size();
    	for ( long val = 0 ; val <= maxValue ; val++ ) 
    	{
    		int mask = 1;
    		for ( Identifier id : vars ) 
    		{
    			final ASTNode bitValue = ( val & mask) != 0 ? new TrueNode() : new FalseNode();
    			context.set( id  , bitValue );
    			mask = mask << 1;
    		}
    		ASTNode val1 = input.evaluate( context );
    		ASTNode val2 = output.evaluate( context );
    		if ( ! val1.isEquivalent( val2 , context) ) 
    		{
    			System.out.println("\n------------------------");
    			System.out.println( "\nVariables:\n\n"+context.toString() );
    			
    			
    			System.out.println("Reducing: "+input);
    			ASTNode reduced1 = transformer.reduce( input , context );
    			System.out.println("Reduced input term => "+reduced1);
    			
    			System.out.println("\n------------------------");
    			System.out.println("Reducing: "+output);
    			ASTNode reduced2 = transformer.reduce( output , context );
    			System.out.println("Reduced simplified term => "+reduced2);    			
    			
    			throw new RuntimeException("Not equivalent: "+val1+" <-> "+val2);
    		}
    	}
    }
    
    public ASTNode createTerm(int variablesCount,int nodeCount) 
    {
    	final List<Identifier> variables = new ArrayList<Identifier>();
    	for ( int i = 0 ; i < variablesCount ;i++) {
    		variables.add( new Identifier( Character.toString( (char) ('a'+i) ) ) );
    	}
    	
    	return createNode(variables , 0 , nodeCount);
    }
    
	private ASTNode createNode(List<Identifier> variables,int currentNodeCount,int maxNodeCount) 
	{
		if ( currentNodeCount >= maxNodeCount ) {
				int v = r.nextInt( variables.size() );
				return new IdentifierNode( variables.get(v) );			
		} 
		
 		switch( ( r.nextInt(100) % 4 ) ) {
			case 0: // AND
				ASTNode result = new OperatorNode(OperatorType.AND);
				currentNodeCount++;
				result.addChild( createNode( variables , currentNodeCount,maxNodeCount ) );
				currentNodeCount++;
				result.addChild( createNode( variables , currentNodeCount,maxNodeCount ) );
				return result;
			case 1: // NOT
				result = new OperatorNode(OperatorType.NOT);
				currentNodeCount++;
				result.addChild( createNode( variables , currentNodeCount,maxNodeCount ) );
				return result;				
			case 2: // TERM
				result = new TermNode();
				currentNodeCount++;
				result.addChild( createNode( variables , currentNodeCount,maxNodeCount ) );
				return result;
			case 3: // OR
				result = new OperatorNode(OperatorType.OR);
				currentNodeCount++;
				result.addChild( createNode( variables , currentNodeCount,maxNodeCount ) );
				currentNodeCount++;
				result.addChild( createNode( variables , currentNodeCount,maxNodeCount ) );
				return result;
			default:
				throw new RuntimeException("Unreachable code reached?");
		}
	}
	
	
    public ASTNode simplify(final String expr) {
        ASTNode term = parseTerm( expr );
        return simplify( term );
    }
	
    public ASTNode simplify(ASTNode term) {
        
        final ExpressionContext ctx = new ExpressionContext();
		ASTNode simplified = transformer.simplify( term , ctx );
        return simplified;
    }
    
    public void reduce(final String expr) {
        
        final ExpressionContext ctx = new ExpressionContext();
        ASTNode term = parseTerm( expr );
        
		ASTNode reduced = term.evaluate( ctx );
        System.out.println("REDUCED: "+term+" -> "+reduced.toString(true) );
    }    

    public void evaluate(final String expr) 
	{
		ExpressionContext ctx;
		ctx = new ExpressionContext();
        final ASTNode term = parseTerm( expr );
        
        Identifier a = Identifier.id("a");
        Identifier b = Identifier.id("b");
        Identifier c = Identifier.id("c");

        int value = 0;
        for ( ; value <= 7 ; value++ ) {
            ctx.define( a , (value & 4) != 0 );
            ctx.define( b , (value & 2) != 0 );
            ctx.define( c , (value & 1) != 0 );
            
            System.out.print( padRight( " a= "+ctx.lookup( a ) , 10 ) );
            System.out.print( " | "+padRight( " b= "+ctx.lookup( b ) , 10 ) );
            System.out.print( " | "+padRight( " c= "+ctx.lookup( c ) , 10 ) );
            System.out.print(" | "+expr+" = "+term.evaluate( ctx ) );
            
            ASTNode reduced = term.evaluate( ctx );
            System.out.print(" | reduced = "+reduced);
            System.out.println();
        }
	}
    
    private ASTNode parseTerm(String s) 
    {
    	return parser.parseTerm( s );
    }    
    
    private final String padRight(String s , int len) 
    {
        int delta;
        if ( s.length() < len ) {
            delta = len - s.length();
        } else {
            delta = 0;
        }
        return s+StringUtils.repeat(" " , delta );
    }
}
