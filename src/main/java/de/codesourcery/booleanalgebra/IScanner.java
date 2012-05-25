package de.codesourcery.booleanalgebra;

import java.io.IOException;

public interface IScanner
{
    public char read() throws IOException;
    
    public char peek() throws IOException;
    
    public boolean eof() throws IOException;
    
    public int currentParseOffset();
}
