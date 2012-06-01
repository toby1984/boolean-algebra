package de.codesourcery.booleanalgebra;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;

import de.codesourcery.booleanalgebra.ASTTransformations;
import de.codesourcery.booleanalgebra.BooleanExpressionParser;
import de.codesourcery.booleanalgebra.ExpressionContext;
import de.codesourcery.booleanalgebra.IExpressionContext;
import de.codesourcery.booleanalgebra.ast.ASTNode;
import de.codesourcery.booleanalgebra.ast.FalseNode;
import de.codesourcery.booleanalgebra.ast.Identifier;
import de.codesourcery.booleanalgebra.ast.IdentifierNode;
import de.codesourcery.booleanalgebra.ast.OperatorNode;
import de.codesourcery.booleanalgebra.ast.OperatorType;
import de.codesourcery.booleanalgebra.ast.TermNode;
import de.codesourcery.booleanalgebra.ast.TrueNode;

public class ASTTransformationsTest extends TestCase
{
    private final BooleanExpressionParser parser = new BooleanExpressionParser();
    
    private final ASTTransformations transformer = new ASTTransformations();
    
    private final Random r = new Random(System.currentTimeMillis());
    
    public static void main(String[] args) throws FileNotFoundException
    {
        new ASTTransformationsTest().run();
    }
    
    public void testMoveToTop() 
    {
        final String expr = "not( (a or b) and (c or d) )";
        ASTNode term = parser.parse( expr , true );
        transformer.setDebug( true );
        transformer.simplify( term , new ExpressionContext() );
    }
    
    public void testSimplify() throws FileNotFoundException 
    {
        for ( int i = 0 ; i < 10000 ; i++ ) 
        {
            if (  ( i % 1000 ) == 0 ) {
                System.out.println("Tested "+i+" terms...");
            }
            ASTNode t = createTerm( 3 , 4 );
//            t.toDOT( new FileOutputStream("/home/tobi/tmp/generated_term.dot",false) );
            final String stringForm = t.toString(false);
            ASTNode parsed = parseTerm( stringForm );
//            t.toDOT( new FileOutputStream("/home/tobi/tmp/parsed_term.dot",false) );
            testSimplify( parsed );
        }
    }

    private void testSimplify(final ASTNode term) {
        ASTNode simplified = simplify( term ); 
        System.out.println( term+" -> "+simplified+"\n------------------------------\n");
        assertTermsAreEquivalent( term , simplified );
    }
    
    protected interface IValidator {
        public boolean validate(ASTNode val1,ASTNode val2, IExpressionContext ctx);
    }
    
    public void assertTermsAreEquivalent(ASTNode input , ASTNode output) 
    {
        final IValidator v = new IValidator() {
            
            @Override
            public boolean validate(ASTNode term1, ASTNode term2, IExpressionContext context) {
                ASTNode val1 = term1.evaluate( context );
                ASTNode val2 = term2.evaluate( context );
                return val1.isEquivalent( val2 , context); 
            }
        };
        assertTermsAreEquivalent( input , output , v );
    }
    
    public void assertTermsAreEquivalent(ASTNode input , ASTNode output,IValidator validator) 
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
            
            if ( ! validator.validate( input , output , context ) )
            {
                System.out.println("\n------------------------");
                System.out.println( "\nVariables:\n\n"+context.toString() );
                System.out.println("INPUT: "+input);
                System.out.println("\n------------------------");
                System.out.println("OUTPUT: "+output);
                
                throw new RuntimeException("validator failed for: "+input+" <-> "+output);
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
    
    private ASTNode createNode(List<Identifier> variables,int nodeCount,int maxNodeCount)
    {
        int currentNodeCount = nodeCount;
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
        return parser.parse( s , true );
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
