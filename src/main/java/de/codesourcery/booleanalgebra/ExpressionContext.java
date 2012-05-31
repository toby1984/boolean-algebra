package de.codesourcery.booleanalgebra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.codesourcery.booleanalgebra.ast.ASTNode;
import de.codesourcery.booleanalgebra.ast.FalseNode;
import de.codesourcery.booleanalgebra.ast.Identifier;
import de.codesourcery.booleanalgebra.ast.TrueNode;

public class ExpressionContext implements IExpressionContext {

    private final Map<Identifier,ASTNode> variables = new HashMap<Identifier,ASTNode>();
    
    @Override
    public ASTNode lookup(Identifier identifier)
    {
        if (identifier == null) {
            throw new IllegalArgumentException("identifier must not be NULL.");
        }
        
        final ASTNode result = variables.get(identifier);
        if ( result == null ) {
            throw new RuntimeException("Unknown variable '"+identifier+"'");
        }
        return result;
    }
    
    public void unsetVariable(Identifier identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException("identifier must not be NULL.");
        }
        variables.remove( identifier );
    }
    
    @Override
    public String toString() 
    {
    	StringBuilder buffer= new StringBuilder ();
    	final List<Identifier> keys = new ArrayList<Identifier>( variables.keySet() );
    	Collections.sort( keys );
    	for ( Identifier key : keys ) {
			buffer.append( key+"="+variables.get( key ) ).append("\n");
		}
    	return buffer.toString();
    } 
    

    @Override
    public ASTNode tryLookup(Identifier identifier)
    {
        if (identifier == null) {
            throw new IllegalArgumentException("identifier must not be NULL.");
        }
        return variables.get(identifier);
    }

	@Override
	public void set(Identifier name, ASTNode value) {
		if ( name == null ) {
			throw new IllegalArgumentException("name must not be null");
		}
		variables.put( name , value );
	}   
	
	public void define(Identifier name, boolean value) {
		set( name , value ? new TrueNode() : new FalseNode() );
	}

	@Override
	public Set<Identifier> getAllIdentifiers() {
		return variables.keySet();
	}

	@Override
	public void retainOnly(Set<Identifier> ids) 
	{
		if (ids == null) {
			throw new IllegalArgumentException("ids must not be null");
		}
		final Iterator<Identifier> it = variables.keySet().iterator();
		while(it.hasNext() ) {
			if ( ! ids.contains( it.next() ) ) {
				it.remove();
			}
		}
	}

	@Override
	public void remove(Identifier identifier) {
		if (identifier == null) {
			throw new IllegalArgumentException("identifier must not be null");
		}
		variables.remove( identifier );
	}

    public void clear()
    {
        variables.clear();
    }
}