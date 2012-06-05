package de.codesourcery.booleanalgebra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

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
    
    private static int width(String s) {
        final int len = s != null ? s.length() : 0;
        return len >= "false".length() ? len : "false".length();
    }
    
    public String toString(boolean printHeaderRow,String additionalColumnHeader,String additionalColumnValue) 
    {
    	final List<Identifier> keys = new ArrayList<Identifier>( variables.keySet() );
    	
        Collections.sort( keys , new Comparator<Identifier>() {

            @Override
            public int compare(Identifier o1, Identifier o2)
            {
                final int result = o1.getValue().compareTo( o2.getValue() );
                if ( o1.getValue().length() < o2.getValue().length() ) {
                    return -1;
                }
                return result;
            }
        } );

        final List<String> columns = new ArrayList<>();
        final List<String> values = new ArrayList<>();
        final List<Integer> columnWidths = new ArrayList<>();
        
    	for ( Identifier id : keys ) {
    	    columns.add( id.getValue() );
    	    values.add( variables.get( id ) != null ? variables.get(id).toString(true) : null );
    	    columnWidths.add( width( id.getValue() ) );
    	}
    	
    	final boolean hasAdditionalColumn = StringUtils.isNotBlank( additionalColumnHeader );
        if ( hasAdditionalColumn ) {
    	    columns.add( additionalColumnHeader );
    	    values.add( additionalColumnValue );
    	    columnWidths.add( width( additionalColumnHeader ) );
    	}
    	
        StringBuilder buffer= new StringBuilder ();
        
    	// header line
        if ( printHeaderRow ) {
            printHeaderLine(buffer, columns , columnWidths ,hasAdditionalColumn);
            buffer.append("\n");            
        }
        
        printSeparatorLine(buffer, columns, columnWidths , hasAdditionalColumn);
        
        buffer.append("\n");

        printValues(buffer, values, columnWidths,hasAdditionalColumn);
        
    	return buffer.toString();
    }

    private void printValues(StringBuilder buffer, final List<String> values, final List<Integer> columnWidths,boolean separateLastColumn)
    {
        final Iterator<Integer> width = columnWidths.iterator();
        for ( Iterator<String> it = values.iterator() ; it.hasNext() ; )         
    	{
            final String value = it.next();
            
            if ( ! it.hasNext() && separateLastColumn) {
                buffer.append("|");
            }
            
    	    final String stringValue = value != null ? value : "<null>";
			buffer.append( "|" ).append( padRight( stringValue , width.next() ) );
			if ( ! it.hasNext() ) {
			    buffer.append( "|" );			    
			}
		}
    }
    
    private void printHeaderLine(StringBuilder buffer, List<String> columns,List<Integer> columnWidths,boolean separateLastColumn) 
    {
        final Iterator<Integer> colWidth = columnWidths.iterator();
        for ( Iterator<String> key = columns.iterator() ; key.hasNext() ; ) 
        {
            final String value = key.next();
            if ( ! key.hasNext() && separateLastColumn ) {
                buffer.append("|"); 
            }
            buffer.append("|").append( center( value  , colWidth.next() ) );
            if ( ! key.hasNext() ) {
                buffer.append("|");
            }
        }
    }    

    private void printSeparatorLine(StringBuilder buffer, final List<String> columns, final List<Integer> columnWidths ,boolean separateLastColumn)
    {
        final Iterator<Integer> colWidth = columnWidths.iterator();
        
        for ( int i = 0 ; i < columns.size() ; i++ )
        {
            if ( (i+1) >= columns.size() && separateLastColumn ) {
                buffer.append("+"); 
            }
            buffer.append("+").append( StringUtils.repeat("-" , colWidth.next() ) );
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

    @Override
    public Identifier createIdentifier(ASTNode value)
    {
        final String prefix = "_tmp_";
        final AtomicLong ID = new AtomicLong(1);
        String name = "";
        do {
            name = prefix + ID.incrementAndGet();
        } while ( tryLookup( new Identifier( name ) ) != null );
        
        final Identifier id = new Identifier( name );
        set( id , value );
        return id;
    }
}