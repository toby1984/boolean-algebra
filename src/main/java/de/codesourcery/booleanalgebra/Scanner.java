package de.codesourcery.booleanalgebra;

import java.io.IOException;

public class Scanner implements IScanner
{
    private final String s;
    private int currentIndex = 0;
    
    public Scanner(String s) {
        if (s == null) {
            throw new IllegalArgumentException("s must not be NULL.");
        }
        this.s = s;
    }
    
    @Override
    public char read() throws IOException
    {
        if ( eof() ) {
            throw new IllegalStateException("Scanner is already at EOF");
        }
        return s.charAt( currentIndex ++ );
    }

    @Override
    public char peek() throws IOException
    {
        if ( eof() ) {
            throw new IllegalStateException("Scanner is already at EOF");
        }
        return s.charAt(currentIndex);
    }

    @Override
    public boolean eof()
    {
        return currentIndex >= s.length();
    }

    @Override
    public int currentParseOffset()
    {
        return currentIndex;
    }

}
