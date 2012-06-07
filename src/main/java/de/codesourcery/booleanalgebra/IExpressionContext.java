package de.codesourcery.booleanalgebra;

import java.util.Set;

import de.codesourcery.booleanalgebra.ast.ASTNode;
import de.codesourcery.booleanalgebra.ast.Identifier;

public interface IExpressionContext
{
    public IExpressionContext createCopy();
    
    public ASTNode lookup(Identifier identifier);
    
    public ASTNode tryLookup(Identifier identifier);
    
    public void set(Identifier name,ASTNode value);
    
    public Set<Identifier> getAllIdentifiers();
    
    public Identifier createIdentifier(ASTNode value);

	public void retainOnly(Set<Identifier> ids);
	
	public void remove(Identifier identifier);
	
    public void clear();
}
