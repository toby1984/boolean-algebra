package de.codesourcery.booleanalgebra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

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
        return toString(true);
    }
   
    public String toString(boolean printHeaderRow) 
    {
        return toString( printHeaderRow , null,null );
    }
    
    public String toString(boolean printHeaderRow,String additionalColumnHeader,String additionalColumnValue) 
    {
    	final List<Identifier> keys = new ArrayList<Identifier>( variables.keySet() );
    	Collections.sort( keys );

        final List<String> columns = new ArrayList<>();
        final List<String> values = new ArrayList<>();
        
    	for ( Identifier id : keys ) {
    	    columns.add( id.getValue() );
    	    values.add( variables.get( id ) != null ? variables.get(id).toString(true) : null );
    	}
    	
    	final boolean hasAdditionalColumn = StringUtils.isNotBlank( additionalColumnHeader );
        if ( hasAdditionalColumn ) {
    	    columns.add( additionalColumnHeader );
    	    values.add( additionalColumnValue );
    	}
    	
    	int COL_WIDTH = -1;
    	for ( String col : columns ) {
    	    if ( col.length() > COL_WIDTH ) {
    	        COL_WIDTH =col.length();
    	    }
    	}
    	if ( COL_WIDTH == -1 || COL_WIDTH < "false".length() ) {
    	    COL_WIDTH = "false".length();
    	}
    	
        StringBuilder buffer= new StringBuilder ();
        
    	// header line
        if ( printHeaderRow ) {
            printHeaderLine(buffer, columns , COL_WIDTH ,hasAdditionalColumn);
            buffer.append("\n");            
        }
        
        printSeparatorLine(buffer, columns, COL_WIDTH , hasAdditionalColumn);
        
        buffer.append("\n");

        printValues(buffer, values, COL_WIDTH,hasAdditionalColumn);
        
    	return buffer.toString();
    }

    private void printValues(StringBuilder buffer, final List<String> values, final int COL_WIDTH,boolean separateLastColumn)
    {
        for ( Iterator<String> it = values.iterator() ; it.hasNext() ; )         
    	{
            final String value = it.next();
            
            if ( ! it.hasNext() && separateLastColumn) {
                buffer.append("|");
            }
            
    	    final String stringValue = value != null ? value : "<null>";
			buffer.append( "|" ).append( padRight( stringValue , COL_WIDTH ) );
			if ( ! it.hasNext() ) {
			    buffer.append( "|" );			    
			}
		}
    }
    
    private void printHeaderLine(StringBuilder buffer, List<String> columns,int columnWidth,boolean separateLastColumn) 
    {
        for ( Iterator<String> key = columns.iterator() ; key.hasNext() ; ) 
        {
            final String value = key.next();
            if ( ! key.hasNext() && separateLastColumn ) {
                buffer.append("|"); 
            }
            buffer.append("|").append( center( value  , columnWidth ) );
            if ( ! key.hasNext() ) {
                buffer.append("|");
            }
        }
    }    

    private void printSeparatorLine(StringBuilder buffer, final List<String> columns, final int COL_WIDTH,boolean separateLastColumn)
    {
        for ( int i = 0 ; i < columns.size() ; i++ )
        {
            if ( (i+1) >= columns.size() && separateLastColumn ) {
                buffer.append("+"); 
            }
            buffer.append("+").append( StringUtils.repeat("-" , COL_WIDTH ) );
            if ( (i+1) >= columns.size() ) {
                buffer.append("+");
            }
        }
    } 
    
    private static String padRight(String s,int len) {
        int delta = len - s.length();
        if ( delta > 0 ) {
            return s+StringUtils.repeat(" ",delta);
        }
        return s;
    }
    
    private static String center(String s,int len) 
    {
        int unusedSpace = len - s.length();
        if ( unusedSpace > 0 ) 
        {
            unusedSpace = unusedSpace >>> 1; // 
            final String leftPart = StringUtils.repeat(" ", unusedSpace)+s;
            return leftPart+padRight("" , len - leftPart.length() );
        }
        return s;
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

    @Override
    public IExpressionContext createCopy()
    {
        final IExpressionContext result = new ExpressionContext();
        for ( Identifier id : variables.keySet() ) 
        {
            ASTNode value = variables.get(id);
            result.set( id , value != null ? value.createCopy( true ) : null );
        }
        return result;
    }
}