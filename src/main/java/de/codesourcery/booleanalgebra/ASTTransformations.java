package de.codesourcery.booleanalgebra;

import java.util.HashSet;
import java.util.Set;

import de.codesourcery.booleanalgebra.ASTTransformations.MutatingNodeVisitor;
import de.codesourcery.booleanalgebra.ast.ASTNode;
import de.codesourcery.booleanalgebra.ast.FalseNode;
import de.codesourcery.booleanalgebra.ast.INodeVisitor;
import de.codesourcery.booleanalgebra.ast.Identifier;
import de.codesourcery.booleanalgebra.ast.IdentifierNode;
import de.codesourcery.booleanalgebra.ast.OperatorNode;
import de.codesourcery.booleanalgebra.ast.TermNode;
import de.codesourcery.booleanalgebra.ast.TrueNode;

public class ASTTransformations 
{

	protected interface IIterationContext 
	{
		public void stop();
		public void astMutated();
	}
	
	/*
Kommutativgesetze 	  => a and b = b and a 	
                      => a or b = b or a
                      
Assoziativgesetze 	  => (a and b) and c = a and (b and c) 	
                      => (a or b) or c = a or (b or c)
                      
[OK] Idempotenzgesetze 	=> a and a=a 	
                        => a or a=a
                      
Distributivgesetze 	  => a and (b or c) = (a and b)  or (a  and c) 	
                      => a or (b and c) = (a or b)  and (a  or c)
                      
[OK] Neutralitätsgesetze   => a and 1 = a 	
                      => a or 0 = a
                      
[OK] Extremalgesetze 	  => a and 0=0 	
                      => a or 1=1
                      
[OK] Doppelnegationsgesetz => not( not a)=a
 	
De Morgansche Gesetze => not(a and b)= not a or not b 	
                      => not(a or b)= not a and not b
                      
[OK] Komplementärgesetze   => a and not a=0 	
                      => a or not a=1
                      
[OK] Dualitätsgesetze 	  => not 0 = 1 
                      => not 1 = 0
                      
[OK] Absorptionsgesetze 	  => a or(a and b)=a 	
                      => a and(a or b)=a	 
	 */
	public static ASTNode simplify(ASTNode term,final IExpressionContext context) {
		
		System.out.println("INPUT: "+term.toString());
		ASTNode result = term.createCopy( true );
		
		result = reduce( result , context );
		
        // De Morgansche Gesetze  => not(a and b) = not a or  not b 	
        //                        => not(a or  b) = not a and not b		
		boolean simplified = applyLawOfDeMorgan(context,result);
		do {
			simplified = false;
			// Idempotenzgesetze:    => x and x = x 
			//                       => x or  x = x
			simplified |= applyLawOfIdemPotency(context, result);

			// double negation:      => not( not a) = a
			simplified |= applyRuleOfDoubleNegation(context, result);

			// Neutralitätsgesetze  => a and 1 = a 	
			//                      => a or  0 = a
			simplified |= applyLawOfNeutrality(context, result);		

			// Extremalgesetze 	 => a and 0 =0 	
			//                      => a or  1 =1		 
			simplified |= applyLawOfExtrema(context, result);		

			// Komplementärgesetze   => a and not a = 0 	
			//                       => a or  not a = 1			
			simplified |= applyComplementaryLaw(context, result);

			// Absorptionsgesetze 	 => a or (a and b) = a 	
			//                       => a and(a or  b) = a			
			simplified |= applyLawOfAbsorption(context, result);
			
	        // De Morgansche Gesetze  => not a or  not b = not(a and b)  	
	        //                        => not a and not b = not(a or  b)
			simplified |= applyInverseLawOfDeMorgan(context,result);			
		} while ( simplified );

		// get rid of all variables we eliminated
		context.retainOnly( gatherIdentifiers( result ));
		
		System.out.println("SIMPLIFIED: "+result.toString());
		return result;
	}
	
	public static ASTNode substituteIdentifiers(ASTNode input,IExpressionContext context) {
		
		ASTNode result = input.createCopy( true );
		
		final MutatingNodeVisitor visitor = new MutatingNodeVisitor(context) {
			
			@Override
			protected void visit(ASTNode node, IExpressionContext context,
					IIterationContext it) 
			{
				if ( node instanceof IdentifierNode) 
				{
					Identifier id = ((IdentifierNode) node).getIdentifier();
					ASTNode value = context.tryLookup( id );
					if ( value != null ) {
						node.replaceWith( value );
						it.astMutated();
					}
				}
			}
		};
		
		applyInOrder( result , visitor );
		return result;
	}
	
	public static Set<Identifier> gatherIdentifiers(ASTNode node) 
	{
		final Set<Identifier> result = new HashSet<Identifier>();
		INodeVisitor visitor = new INodeVisitor() {
			
			@Override
			public boolean visit(ASTNode node, int currentDepth) 
			{
				if ( node instanceof IdentifierNode) {
					result.add(( (IdentifierNode) node).getIdentifier() );
				}
				return true;
			}
		};
		node.visitInOrder( visitor );
		return result;
	}

	private static boolean applyLawOfIdemPotency(final IExpressionContext context,
			ASTNode result) 
	{
		
		// 1. Idempotenzgesetze ( x and x = x ) / ( x or x ) = x
		
		final MutatingNodeVisitor visitor = new MutatingNodeVisitor( context ) {

			@Override
			public void visit(ASTNode node,IExpressionContext context,IIterationContext it) 
			{
				final ASTNode unwrapped = unwrap( node );
				if ( unwrapped.isAND() || unwrapped.isOR() ) 
				{
					final boolean isEquivalent = unwrapped.child(0).isEquivalent( 
							unwrapped.child(1) , context 
					);
					if ( isEquivalent ) 
					{
						if ( unwrapped.hasParent() ) {
							System.out.println("IDEM: Replacing "+unwrapped.toString(false)+" -> "+unwrapped.child(0).toString(false));
							unwrapped.replaceWith( unwrapped.child(0) );
							it.astMutated();
						}
					}
				} 
			}
		};
		return applyInOrder( result , visitor );
	}

	private static boolean applyRuleOfDoubleNegation(final IExpressionContext context,
			ASTNode result) 
	{
		// double negation: Doppelnegationsgesetz => not( not a)=a
		final MutatingNodeVisitor visitor2 = new MutatingNodeVisitor( context ) {

			@Override
			public void visit(ASTNode node,IExpressionContext context,IIterationContext it) 
			{
				final ASTNode unwrapped = unwrap( node );
				if ( unwrapped.isNOT() ) 
				{
					if ( unwrapped.hasParent() && unwrap( unwrapped.child(0) ).isNOT() ) 
					{
							System.out.println("NOT-NOT: Replacing "+unwrapped.toString(false)+
									" -> "+unwrapped.child(0).child(0).toString(false));
							unwrapped.replaceWith( unwrap( unwrapped.child(0) ).child(0) );
							it.astMutated();
					}
				} 
			}
		};
		
		return applyInOrder( result , visitor2 );
	}

	private static boolean applyLawOfNeutrality(final IExpressionContext context,
			ASTNode result) 
	{
		 // Neutralitätsgesetze   => a and 1 = a 	
        //                       => a or 0 = a		
		final MutatingNodeVisitor visitor3 = new MutatingNodeVisitor( context ) {

			@Override
			public void visit(ASTNode node,IExpressionContext context,IIterationContext it) 
			{
				final ASTNode unwrapped = unwrap( node );
				if ( unwrapped.isOR() || unwrapped.isAND() ) 
				{
					final ASTNode leftChild = unwrap( unwrapped.child(0) );
					final ASTNode rightChild = unwrap( unwrapped.child(1) );
					
					final ASTNode neutralElement = unwrapped.isAND() ? new TrueNode() : new FalseNode();
					
					if ( leftChild.isEquivalent( neutralElement , context) ) 
					{
						System.out.println("NEUTRALITY: Replacing "+unwrapped.toString(false)+
								" -> "+rightChild);
						unwrapped.replaceWith( rightChild );
						it.astMutated();						
					} 
					else if ( rightChild.isEquivalent( neutralElement , context ) ) 
					{
						System.out.println("NEUTRALITY: Replacing "+unwrapped.toString(false)+
								" -> "+leftChild);						
							unwrapped.replaceWith( leftChild );
							it.astMutated();
					}
				} 
			}
		};
		
		return applyInOrder( result , visitor3 );
	}

	private static boolean applyLawOfExtrema(final IExpressionContext context,
			ASTNode result) 
	{
		// Extremalgesetze 	  => a and 0=0 	
        //                    => a or 1=1				
		final MutatingNodeVisitor visitor4 = new MutatingNodeVisitor( context ) {

			@Override
			public void visit(ASTNode node,IExpressionContext context,IIterationContext it) 
			{
				final ASTNode unwrapped = unwrap( node );
				if ( unwrapped.isOR() || unwrapped.isAND() ) 
				{
					final ASTNode leftChild = unwrap( unwrapped.child(0) );
					final ASTNode rightChild = unwrap( unwrapped.child(1) );
					
					final ASTNode neutralElement = unwrapped.isAND() ? new FalseNode() : new TrueNode();
					
					if ( leftChild.isEquivalent( neutralElement , context) ) 
					{
						System.out.println("EXTREMES: Replacing "+unwrapped.toString(false)+
								" -> "+rightChild);
						unwrapped.replaceWith( neutralElement );
						it.astMutated();						
					} 
					else if ( rightChild.isEquivalent( neutralElement , context ) ) 
					{
						System.out.println("NEUTRALITY: Replacing "+unwrapped.toString(false)+
								" -> "+leftChild);						
							unwrapped.replaceWith( neutralElement );
							it.astMutated();
					}
				} 
			}
		};
		
		return applyInOrder( result , visitor4 );
	}

	private static boolean applyComplementaryLaw(final IExpressionContext context,
			ASTNode result) 
	{
		// Komplementärgesetze   => a and not a=0 	
        //                       => a or not a=1		 
		final MutatingNodeVisitor visitor5 = new MutatingNodeVisitor( context ) {

			@Override
			public void visit(ASTNode node,IExpressionContext context,IIterationContext it) 
			{
				final ASTNode unwrapped = unwrap( node );
				if ( unwrapped.isOR() || unwrapped.isAND() ) 
				{
					final ASTNode leftChild = unwrap( unwrapped.child(0) );					
					final ASTNode rightChild = unwrap( unwrapped.child(1) );
					
					if ( rightChild.isNOT() ) 
					{
						ASTNode subTerm = unwrap( rightChild.child(0) );
						if ( subTerm.isEquals( leftChild) ) 
						{ 
							final ASTNode result = unwrapped.isAND() ? new FalseNode() : new TrueNode();
							
							System.out.println("COMPLEMENTARY: Replacing "+unwrapped.toString(false)+
									" -> "+rightChild);
							unwrapped.replaceWith( result );
							it.astMutated();			
						}
					} 
				} 
			}
		};
		
		return applyInOrder( result , visitor5);
	}

	private static boolean applyLawOfAbsorption(final IExpressionContext context,
			ASTNode result) 
	{
        // Absorptionsgesetze 	 => a or (a and b) = a 	
        //                       => a and(a or  b) = a			
		final MutatingNodeVisitor visitor6 = new MutatingNodeVisitor( context ) {

			@Override
			public void visit(ASTNode node,IExpressionContext context,IIterationContext it) 
			{
				final ASTNode unwrapped = unwrap( node );
				if ( unwrapped.isOR() || unwrapped.isAND() ) 
				{
					final ASTNode leftChild = unwrap( unwrapped.child(0) );					
					final ASTNode rightChild = unwrap( unwrapped.child(1) );
					
					if ( ( unwrapped.isOR()  && rightChild.isAND() ) || 
						 ( unwrapped.isAND() && rightChild.isOR()  ) ) 
					{
						ASTNode subTerm = unwrap( rightChild.child(0) );
						if ( subTerm.isEquals( leftChild) ) 
						{ 
							System.out.println("ABSORPTION: Replacing "+unwrapped.toString(false)+
									" -> "+leftChild);
							unwrapped.replaceWith( leftChild );
							it.astMutated();			
						}
					} 
				} 
			}
		};
		
		return applyInOrder( result , visitor6 );
	}
	
	private static boolean applyLawOfDeMorgan(IExpressionContext context,ASTNode result) 
	{
        // De Morgansche Gesetze  => not(a and b) = not a or  not b 	
        //                        => not(a or  b) = not a and not b
		final MutatingNodeVisitor visitor6 = new MutatingNodeVisitor( context ) {

			@Override
			public void visit(ASTNode node,IExpressionContext context,IIterationContext it) 
			{
				final ASTNode unwrapped = unwrap( node );
				if ( unwrapped.isNOT() )
				{
					final ASTNode child = unwrap( unwrapped.child(0) );
					if ( child.isAND() || child.isOR() ) 
					{
						final ASTNode leftChild = unwrap( child.child(0) );					
						final ASTNode rightChild = unwrap( child.child(1) );

						final ASTNode notLeft = OperatorNode.createNOT( leftChild.createCopy(true) );
						final ASTNode notRight = OperatorNode.createNOT( rightChild.createCopy(true) );
						
						final ASTNode newTerm = child.isAND() ? 
								OperatorNode.createOR( notLeft , notRight ) :
									OperatorNode.createAND( notLeft , notRight );
						
						System.out.println("DE-MORGAN: Replacing "+unwrapped.toString(false)+
										" -> "+newTerm);
								unwrapped.replaceWith( newTerm );
						it.astMutated();			
					}
				} 
			}
		};
		
		return applyInOrder( result , visitor6 );		
	}
	
	private static boolean applyInverseLawOfDeMorgan(IExpressionContext context,ASTNode result) 
	{
        // De Morgansche Gesetze  => not a or  not b = not(a and b)   	
        //                        => not a and not b = not(a or  b) 
		
		final MutatingNodeVisitor visitor6 = new MutatingNodeVisitor( context ) {

			@Override
			public void visit(ASTNode node,IExpressionContext context,IIterationContext it) 
			{
				final ASTNode unwrapped = unwrap( node );
				if ( unwrapped.isOR() || unwrapped.isAND() )
				{
					final ASTNode leftChild = unwrap( unwrapped.child(0) );					
					final ASTNode rightChild = unwrap( unwrapped.child(1) );
					
					if ( leftChild.isNOT() && rightChild.isNOT() ) 
					{
						final ASTNode leftArgument = unwrap( leftChild.child(0 ) );
						final ASTNode rightArgument = unwrap( rightChild.child( 0 ) );
						
						final ASTNode newTerm;
						if ( unwrapped.isOR() ) {
							newTerm = OperatorNode.createNOT( 
									OperatorNode.createAND( leftArgument.createCopy(true) ,
											rightArgument.createCopy(true) ) );
						} else { // AND
							newTerm = OperatorNode.createNOT( 
									OperatorNode.createOR( leftArgument.createCopy(true) ,
											rightArgument.createCopy(true) ) );							
						}
						System.out.println("INV. DE-MORGAN: Replacing "+unwrapped.toString(false)+
										" -> "+newTerm);
						unwrapped.replaceWith( newTerm );
						it.astMutated();			
					}
				} 
			}
		};
		
		return applyInOrder( result , visitor6 );		
	}	
	
	protected static ASTNode unwrap(ASTNode node) 
	{
		ASTNode result = node;
		while ( result instanceof TermNode && result.hasChildren() ) {
			result = result.child(0);
		}
		return result;
	}
	
	/**
	 * Tries to reduce a term by replacing expressions that
	 * evaluate to a literal value (true or false) with the corresponding value.
	 * 
	 * @param term
	 * @param context
	 * @return
	 */
	private static ASTNode reduce(ASTNode term,final IExpressionContext context) 
	{
		final ASTNode result = term.createCopy( true );

		final MutatingNodeVisitor visitor = new MutatingNodeVisitor( context ) {

			@Override
			public void visit(ASTNode node,IExpressionContext context,IIterationContext it) 
			{
				if ( node.hasLiteralValue( context ) ) 
				{
					final ASTNode reduced = node.evaluate( context );
					if ( node.hasParent() && reduced != node )
					{
						System.out.println("REDUCE: Replacing "+node+" -> "+reduced);
						node.replaceWith( reduced );
						it.astMutated();
						it.stop();
					} 
				}
			}
		};

		applyPostOrder( result , visitor );
		return result;
	}
	
	public ASTNode eval(ASTNode term,IExpressionContext context) {
		return reduce( term , context);
	}
	
	protected static boolean applyInOrder(ASTNode term,MutatingNodeVisitor visitor) {
		do {
			term.visitInOrder( visitor );
		} while ( visitor.isASTMutated() );
		return visitor.getMutationCount() > 0;
	}	
	
	protected static boolean applyPostOrder(ASTNode term,MutatingNodeVisitor visitor) {
		do {
			term.visitPostOrder( visitor );
		} while ( visitor.isASTMutated() );
		return visitor.getMutationCount() > 0;
	}	
	
	protected static boolean applyPreOrder(ASTNode term,MutatingNodeVisitor visitor) {
		do {
			term.visitPreOrder( visitor );
		} while ( visitor.isASTMutated() );
		return visitor.getMutationCount() > 0;
	}		
	
	protected static abstract class MutatingNodeVisitor implements INodeVisitor {

		private final IExpressionContext context;
		
		private boolean stop= false;
		private boolean astMutated = false;

		private int mutationCount = 0;
		private final IIterationContext it = new IIterationContext() {

			@Override
			public void stop() {
				stop = true;
			}

			@Override
			public void astMutated() {
				mutationCount++;
				astMutated = true;
			}
		};		
		
		public int getMutationCount() {
			return mutationCount;
		}
		
		public MutatingNodeVisitor(IExpressionContext context) 
		{
			if (context == null) {
				throw new IllegalArgumentException("context must not be null");
			}
			this.context = context;
		}
		
		public boolean isASTMutated() {
			return astMutated;
		}
		
		@Override
		public final boolean visit(ASTNode node, int currentDepth) 
		{
			stop = false;
			astMutated = false;
			visit( node , context , it );
			return ! stop;
		}
		
		protected abstract void visit(ASTNode node,IExpressionContext context,IIterationContext it);
	}	
}
