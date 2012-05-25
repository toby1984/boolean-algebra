package de.codesourcery.booleanalgebra.ast;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;


public final class Identifier
{
    private static final Pattern PATTERN = Pattern.compile("^[\\-_0-9a-zA-Z]$");
    
    private final String s;
    
    public Identifier(String s) 
    {
        if (StringUtils.isBlank(s) ) {
            throw new IllegalArgumentException("enclosing_method_arguments must not be NULL/blank.");
        }
        if ( ! isValidIdentifier( s ) ) {
            throw new IllegalArgumentException( "'"+s+"' is not a valid identifier");
        }
        this.s = s ;
    }
    
    public static Identifier id(String s) {
        return new Identifier( s );
    }
    
    @Override
    public boolean equals(Object obj)
    {
        return obj== this || ( ( obj instanceof Identifier ) && ((Identifier) obj).s.equals( this.s ) );
    }
    
    @Override
    public int hashCode()
    {
        return s.hashCode();
    }
    
    public String getValue() {
        return s;
    }
    
    @Override
    public String toString()
    {
        return s;
    }
    
    public static boolean isValidIdentifier(String s) {
        if ( StringUtils.isBlank(s ) ) {
            return false;
        }
        return PATTERN.matcher( s ).matches();
    }

}
