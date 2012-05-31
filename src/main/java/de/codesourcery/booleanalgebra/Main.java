package de.codesourcery.booleanalgebra;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import de.codesourcery.booleanalgebra.ast.ASTNode;
import de.codesourcery.booleanalgebra.ast.FalseNode;
import de.codesourcery.booleanalgebra.ast.Identifier;
import de.codesourcery.booleanalgebra.ast.TermNode;
import de.codesourcery.booleanalgebra.ast.TrueNode;

public class Main
{
	private final BooleanExpressionParser parser = new BooleanExpressionParser();
	private final ASTTransformations transformer = new ASTTransformations();
	
	private ASTNode term;
    private String expression;
    
    private final ExpressionContext context = new ExpressionContext();
	
    public static void main(String[] args) throws IOException
    {
    	new Main().run();
    }
    
    public void setTerm(String expr) 
    {
        term = parser.parse( expr );
        this.expression = expr;
        System.out.println("Expression: "+expression+" (parsed: "+term+")" );
    }
    
    private String readLine() throws IOException {
        Console console = System.console();
        if ( console == null ) {
            BufferedReader reader = new BufferedReader(new InputStreamReader( System.in ) );
            return reader.readLine();
        }
        return console.readLine();
    }
    
    public void run() throws IOException 
    {
        transformer.setDebug( true );
        
        String input="";

        System.console();
        while( input != null && ! "quit".equalsIgnoreCase( input ) ) 
        {
            System.out.print("INPUT > ");
            input = readLine();
            if ( input != null ) 
            {
                try {
                    executeCommand( input );
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Application terminated.");
    }

	private void executeCommand(String input)
    {
	    final String[] parts = input.trim().split("[ \t]");
	    final String[] partsWithoutCommand;
	    if ( parts.length > 1 ) {
	        partsWithoutCommand = (String[]) ArrayUtils.subarray( parts , 1 , parts.length );
	    } else {
	        partsWithoutCommand = new String[0];
	    }
	    
	    final String cmd = parts[0].trim().toLowerCase();
	    if ( cmd.startsWith( "expr" ) ) {
	        setTerm( StringUtils.join(partsWithoutCommand , " " ) );
	        return;
	    } 
        else if ( cmd.equals("set" ) ) {
            setVariable( partsWithoutCommand );
        }
        else if ( cmd.equals("vars" ) ) {
            showVars();
        }   	    
        else if ( cmd.equals("restart" ) ) {
            restart();
        }   	
        else if ( cmd.equals("eval" ) ) {
            eval();
        }  	    
        else if ( cmd.equals("unset" ) ) {
            unsetVariable( partsWithoutCommand );
        }   	    
        else if ( cmd.equals("simplify" ) ) {
            simplify();
        }
	    else if ( cmd.equals("quit" ) ) {
	        // handled by calling method
	    } else {
	        System.err.println("Unknown command '"+cmd+"'");
	    }
    }
	
    private void eval()
    {
        if ( expression != null ) 
        {
            ASTNode result = transformer.eval( term , context );
            if ( result == null ) {
                System.out.println("Failed to evaluate expression "+term);
            } else {
                System.out.println("EVAL: "+result);
            }
        } else {
            System.err.println("No expression entered, cannot restart");
        }        
    }	
	
    private void showVars()
    {
        System.out.println( context.toString() );
    }

    private void restart()
    {
        if ( expression != null ) 
        {
            setTerm( expression );
            context.clear();
        } else {
            System.err.println("No expression entered, cannot restart");
        }
    }

    private void unsetVariable(String[] parts)
    {
        if ( parts.length != 1 ) {
            System.out.println("Syntax: UNSET <variable>");
            return;
        }
        
        final String identifier = parts[0];
        context.remove( new Identifier( identifier ) );
        System.out.println("UNSET "+identifier);
    }	
	
    private void setVariable(String[] parts)
    {
        if ( parts.length < 2 ) {
            System.out.println("Syntax: SET <variable> <value or expression>");
            return;
        }
        final String identifier = parts[0];
        final String value = StringUtils.join( ArrayUtils.subarray( parts , 1 , parts.length ) , " " );
        ASTNode parsedValue = parser.unwrap( parser.parse( value ) );
        if ( parsedValue instanceof TermNode && parsedValue.hasChildren()) {
            parsedValue = parsedValue.child(0);
        }
        context.set( new Identifier( identifier ) , parsedValue );
        System.out.println("SET "+identifier+" = "+parsedValue);
    }	

    private void simplify()
    {
        if ( term == null ) {
            System.out.println("No term defined.");
            return;
        }
        term = transformer.simplify( term , context );
        System.out.println("Simplified: "+term);
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
    		System.out.println( context );
    		
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
    	return parser.parse( s );
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
