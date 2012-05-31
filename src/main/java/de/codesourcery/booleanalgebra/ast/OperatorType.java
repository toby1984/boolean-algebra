package de.codesourcery.booleanalgebra.ast;

public enum OperatorType {
    AND,
    OR,
    NOT,
    PARENS_OPEN,
    PARENS_CLOSE;

    public int getPrecedence() {
        /*
         * operators by ASCENDING precedence (the higher the value the stronger this operator binds):
         * 
         * all others (0)
         * OR (8)
         * AND (9)
         * NOT (10)
         * EXPRESSION (100)
         */
        switch( this ) 
        {
            case PARENS_OPEN:
                return 100;              
            case NOT:
                return 10;
            case AND:
                return 9;
            case OR:
                return 8;
            default:
                // $FALL-THROUGH$
        }
        return 0;
    }
}