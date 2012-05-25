package de.codesourcery.booleanalgebra.exceptions;

public class ParseException extends RuntimeException
{
    private final int offset;
    
    public ParseException(String message, int offset , Throwable cause)
    {
        super(message, cause);
        this.offset = offset;
    }

    public ParseException(String message,int offset)
    {
        super(message);
        this.offset = offset;
    }

    public int getParseOffset()
    {
        return offset;
    }

}
