package de.codesourcery.booleanalgebra.ast;

public enum OperatorType {
    AND,
    OR,
    NOT,
    PARENS;

    public int getPrecedence() {
        /*
         * operators by ASCENDING precedence:
         * 
         * all others (0)
         * OR (8)
         * AND (9)
         * NOT (10)
         * EXPRESSION (100)
         */
        switch( this ) 
        {
            case NOT:
                return 10;
            case AND:
                return 9;
            case OR:
                return 8;
            case PARENS:
                return 100;            
        }
        return 0;
    }
}