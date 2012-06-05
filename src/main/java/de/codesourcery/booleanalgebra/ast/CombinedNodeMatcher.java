package de.codesourcery.booleanalgebra.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CombinedNodeMatcher implements INodeMatcher
{
    private final boolean matchAND;
    private final List<INodeMatcher> matchers = new ArrayList<>();
    
    private CombinedNodeMatcher(List<INodeMatcher> matchers,boolean matchAND) 
    {
        if ( matchers == null || matchers.isEmpty() ) {
            throw new IllegalArgumentException("matchers must not be NULL / empty");
        }
        this.matchers.addAll( matchers );
        this.matchAND = matchAND;
    }
    
    public static CombinedNodeMatcher matchAND(INodeMatcher n1,INodeMatcher n2) {
        return new CombinedNodeMatcher( Arrays.asList( n1 , n2 ) , true );
    }
    
    public static CombinedNodeMatcher matchOR(INodeMatcher n1,INodeMatcher n2) {
        return new CombinedNodeMatcher( Arrays.asList( n1 , n2 ) , false );
    }    
    
    public static INodeMatcher matchAND(List<INodeMatcher> matchers)
    {
        return new CombinedNodeMatcher( matchers , true );        
    } 
    
    public static INodeMatcher matchOR(List<INodeMatcher> matchers)
    {
        return new CombinedNodeMatcher( matchers , false );        
    }       

    @Override
    public boolean matches(TreeMatcher matcher, ASTNode n)
    {
        if ( matchAND ) {
            for ( INodeMatcher m : matchers ) {
                if ( ! m.matches( matcher, n ) ) {
                    return false;
                }
            }
            return true;
        }
        
        for ( INodeMatcher m : matchers ) {
            if ( m.matches( matcher, n ) ) {
                return true;
            }
        }
        return false;        
    }

}
