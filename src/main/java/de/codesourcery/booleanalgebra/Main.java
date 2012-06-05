package de.codesourcery.booleanalgebra;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import de.codesourcery.booleanalgebra.ast.ASTNode;
import de.codesourcery.booleanalgebra.ast.BooleanExpression;
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
        term = parser.parse( expr , true );
        this.expression = expr;
        System.out.println("Expression: "+expression+" (parsed: "+toString( term ) +")" );
    }

    private static String toString(ASTNode n) {
        return n == null ? "<null>" : n.toString( true );
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
        else if ( cmd.equalsIgnoreCase("set" ) ) {
            setVariable( partsWithoutCommand );
        }
        else if ( cmd.equalsIgnoreCase("vars" ) ) {
            showVars();
        }   
        else if ( cmd.equalsIgnoreCase("help" ) ) {
            printHelp();
        } 	    
        else if ( cmd.equalsIgnoreCase("resetVars" ) ) {
            resetVars();
        } 	
        else if ( cmd.equalsIgnoreCase("show" ) ) {
            show();
        }  	    
        else if ( cmd.equalsIgnoreCase("reduce" ) ) {
            reduce();
        }   	    
        else if ( cmd.equalsIgnoreCase("restart" ) ) {
            restart();
        }   
        else if ( cmd.equalsIgnoreCase("common" ) ) {
            substituteCommonTerms();
        }  	    
        else if ( cmd.equalsIgnoreCase("eval" ) ) {
            eval();
        }  
        else if ( cmd.equalsIgnoreCase("expand" ) ) {
            expandVariables();
        }          
        else if ( cmd.equalsIgnoreCase("unset" ) ) {
            unsetVariable( partsWithoutCommand );
        }   	    
        else if ( cmd.equalsIgnoreCase("simplify" ) ) {
            simplify();
        }
        else if ( cmd.equalsIgnoreCase("truth" ) ) {
            printTruthTable();
        }	    
        else if ( cmd.equalsIgnoreCase("quit" ) ) {
            // handled by calling method
        } else {
            System.err.println("Unknown command '"+cmd+"'");
        }
    }

    private void substituteCommonTerms()
    {
        if ( expression != null ) 
        {
            term = transformer.substituteCommonTerms( term , context );
            System.out.println("SUBSTITUTE: "+term);
        } else {
            System.err.println("No expression entered, cannot substitute common terms.");
        }         
    }
    
    private void expandVariables()
    {
        if ( expression != null ) 
        {
            term = transformer.expand( term , context , true );
            System.out.println("EXPANDED: "+toString( term ) );
        } else {
            System.err.println("No expression entered, cannot substitute common terms.");
        }         
    }    

    private void printTruthTable()
    {
        if ( term == null ) {
            System.err.println("No expression defined.");
            return;
        }
        
        transformer.setDebug( false );
        
        final ASTNode copy = transformer.expand( term , context , false );        

        final IValidator validator = new IValidator() {

            private boolean firstRow = true;
            @Override
            public boolean validate(IExpressionContext ctx)
            {
                ExpressionContext context = (ExpressionContext) ctx;
                ASTNode evaluated = transformer.reduce( copy , ctx.createCopy() ); // note that reduce() will remove variables from the context so we need to pass in a copy here
                boolean expressionIsFalse = false;
                if ( evaluated instanceof BooleanExpression) 
                {
                    final ASTNode lhs = ((BooleanExpression) evaluated).getLHS();
                    final ASTNode rhs = ((BooleanExpression) evaluated).getRHS();
                    expressionIsFalse = ! lhs.isEquivalent( rhs  , ctx );
                }
                String value = evaluated.toString(true);
                if ( expressionIsFalse ) {
                    value += " (!!)";
                }
                if ( firstRow ) 
                {
                    System.out.println( context.toString(true , term.toString(true) , value  ) );    
                    firstRow = false;
                } else {
                    System.out.println( context.toString(false , term.toString(true) , value  ) );    
                }
                return true;
            }
        };
        

        final List<Identifier> vars = new ArrayList<Identifier>( transformer.gatherIdentifiers( copy ) );
        Collections.sort( vars );
        assertTermsAreEquivalent( context.createCopy() , validator , vars );
        transformer.setDebug( true );
    }

    private void show()
    {
        System.out.println("Expression: \n\n"+toString( term ) );
        System.out.println("\nVariables:\n\n"+context);
    }

    private void printHelp()
    {
        System.out.println("\n*** HELP ***\n");
        System.out.println("eval                          - evaluate expression");
        System.out.println("expr <expression>             - define expression to work with");
        System.out.println("expand                        - replace variables with their values");
        System.out.println("help                          - show help");
        System.out.println("reduce                        - reduce expression by trying to evaluate sub-expressions");        
        System.out.println("common                        - substitute common terms with vars");
        System.out.println("restart                       - sets all variables to 'undefined' and reset the expression to the last 'expr' command");
        System.out.println("resetVars                     - sets all variables to 'undefined'");
        System.out.println("set <identifier> <expression> - set a variable to a given value/expression");
        System.out.println("show                          - prints the current expression and variable definitions");
        System.out.println("simplify                      - try to simplify the expression");
        System.out.println("truth                         - print truth table");
        System.out.println("quit                          - terminate application");
        System.out.println();
    }

    private void resetVars()
    {
        context.clear();
    }

    private void eval()
    {
        if ( expression != null ) 
        {
            ASTNode result = transformer.eval( term , context );
            if ( result == null ) {
                System.out.println("Failed to evaluate expression "+toString( term ));
            } else {
                System.out.println("EVAL: "+result);
            }
        } else {
            System.err.println("No expression entered, cannot restart");
        }        
    }	

    private void reduce()
    {
        if ( expression != null ) 
        {
            ASTNode result = transformer.reduce( term , context );
            if ( result == null ) {
                System.out.println("Failed to reduce expression "+toString( term ));
            } else {
                System.out.println("REDUCE: "+result);
                term = result;
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
        ASTNode parsedValue = parser.unwrap( parser.parse( value , true ) );
        if ( parsedValue instanceof TermNode && parsedValue.hasChildren()) {
            parsedValue = parsedValue.child(0);
        }
        context.set( new Identifier( identifier ) , parsedValue );
        System.out.println("SET "+identifier+" = "+toString( parsedValue ) );
    }	

    private void simplify()
    {
        if ( term == null ) {
            System.out.println("No term defined.");
            return;
        }
        term = transformer.simplify( term , context );
        System.out.println("Simplified: "+toString( term ) );
    }

    protected interface IValidator {
        public boolean validate(IExpressionContext ctx);
    }

    public void assertTermsAreEquivalent(final ASTNode input , final ASTNode output) 
    {
        final IValidator v = new IValidator() {

            @Override
            public boolean validate(IExpressionContext context) {
                ASTNode val1 = input.evaluate( context );
                ASTNode val2 = output.evaluate( context );
                return val1.isEquivalent( val2 , context); 
            }
        };

        final List<Identifier> vars = new ArrayList<Identifier>( transformer.gatherIdentifiers( input ) );		
        assertTermsAreEquivalent( context.createCopy() , v , vars );
    }

    public void assertTermsAreEquivalent(IExpressionContext context , IValidator validator,List<Identifier> vars) 
    {
        final List<Identifier> reversedVars = new ArrayList<Identifier>( vars );
        Collections.reverse( reversedVars );
        
        // size = 1 => 1 << 1 => 2
        // size = 2 => 1 << 2 => 4
        long maxValue = vars.isEmpty() ? 0 : 1<< vars.size();
        for ( long val = 0 ; val < maxValue ; val++ ) 
        {
            int mask = 1;
            for ( Identifier id : reversedVars ) 
            {
                final ASTNode value = context.tryLookup( id );
                if ( value == null || value.isLiteralValue() ) {
                    final ASTNode bitValue = ( val & mask) != 0 ? new TrueNode() : new FalseNode();
                    context.set( id  , bitValue );
                    mask = mask << 1;
                }
            }

            if ( ! validator.validate( context ) )
            {
                System.out.println("\n------------------------");
                System.out.println( "\nVariables:\n\n"+context.toString() );
                System.out.println("\n------------------------");

                throw new RuntimeException("validator failed.");
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
