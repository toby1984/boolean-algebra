package de.codesourcery.booleanalgebra;

import de.codesourcery.booleanalgebra.ast.Identifier;

public interface IExpressionContext
{
    public boolean lookup(Identifier identifier);
    
    public Boolean tryLookup(Identifier identifier);
}
