package de.codesourcery.booleanalgebra;

import de.codesourcery.booleanalgebra.ast.ASTNode;
import de.codesourcery.booleanalgebra.ast.INodeVisitor;

public class ASTTransformations 
{

	public static ASTNode reduce(ASTNode term,final IExpressionContext context) 
	{
		ASTNode result = term.createCopy( true );

		final boolean[] astMutated =  { false };
		
		final INodeVisitor visitor = new INodeVisitor() {

			@Override
			public boolean visit(ASTNode node) 
			{
				if ( node.hasLiteralValue( context ) ) 
				{
					final ASTNode reduced = node.evaluate( context );
					if ( node.hasParent() && reduced != node )
					{
						node.replaceWith( reduced );
						astMutated[0] = true;
						return false;
					} else {
					}
				}
				return true;
			}
		};
		
		do {
			astMutated[0] = false;
			result.visitInOrder( visitor );
		} while ( astMutated[0] );

		return result;
	}
}
