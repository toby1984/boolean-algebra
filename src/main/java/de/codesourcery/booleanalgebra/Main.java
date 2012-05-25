package de.codesourcery.booleanalgebra;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import de.codesourcery.booleanalgebra.ast.Identifier;
import de.codesourcery.booleanalgebra.ast.TermNode;

public class Main
{

    protected static final class Context implements IExpressionContext {

        private final Map<Identifier,Boolean> variables = new HashMap<Identifier,Boolean>();
        
        @Override
        public boolean lookup(Identifier identifier)
        {
            if (identifier == null) {
                throw new IllegalArgumentException("identifier must not be NULL.");
            }
            
            final Boolean result = variables.get(identifier);
            if ( result == null ) {
                throw new RuntimeException("Unknown variable '"+identifier+"'");
            }
            return result.booleanValue(); 
        }
        
        public void setVariable(Identifier identifier,boolean value) {
            if (identifier == null) {
                throw new IllegalArgumentException("identifier must not be NULL.");
            }
            variables.put( identifier, value);
        }
        
        public void unsetVariable(Identifier identifier) {
            if (identifier == null) {
                throw new IllegalArgumentException("identifier must not be NULL.");
            }
            variables.remove( identifier );
        }

        @Override
        public Boolean tryLookup(Identifier identifier)
        {
            if (identifier == null) {
                throw new IllegalArgumentException("identifier must not be NULL.");
            }
            return variables.get(identifier);
        }        
    }
    
    public static void main(String[] args)
    {
        final String expr = " a or b and c";
        
        final TermNode term = new BooleanExpressionParser().parseTerm( expr );
        
        Identifier a = Identifier.id("a");
        Identifier b = Identifier.id("b");
        Identifier c = Identifier.id("c");
        
        Context ctx = new Context();
        int value = 0;
        for ( ; value <= 7 ; value++ ) {
            ctx.setVariable( a , (value & 4) != 0 );
            ctx.setVariable( b , (value & 2) != 0 );
            ctx.setVariable( c , (value & 1) != 0 );
            System.out.print( padRight( " a= "+ctx.lookup( a ) , 10 ) );
            System.out.print( " | "+padRight( " b= "+ctx.lookup( b ) , 10 ) );
            System.out.print( " | "+padRight( " c= "+ctx.lookup( c ) , 10 ) );
            System.out.print(" | "+expr+" = "+term.evaluate( ctx ) );
            System.out.println();
        }
    }
    
    private static final String padRight(String s , int len) 
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
