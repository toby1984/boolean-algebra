package de.codesourcery.booleanalgebra;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import de.codesourcery.booleanalgebra.ast.ASTNode;
import de.codesourcery.booleanalgebra.ast.BooleanExpression;
import de.codesourcery.booleanalgebra.ast.FalseNode;
import de.codesourcery.booleanalgebra.ast.INodeVisitor;
import de.codesourcery.booleanalgebra.ast.Identifier;
import de.codesourcery.booleanalgebra.ast.IdentifierNode;
import de.codesourcery.booleanalgebra.ast.OperatorNode;
import de.codesourcery.booleanalgebra.ast.TermNode;
import de.codesourcery.booleanalgebra.ast.TrueNode;

public class ASTTransformations 
{
	private boolean debug = false;
	
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

[OK] Idempotenzgesetze 	  => a and a=a 	
                          => a or a=a

[OK] Distributivgesetze   => a and (b or c) = (a and b)  or (a  and c) 	
                           => a or (b and c) = (a or b)  and (a  or c)

[OK] Neutralitätsgesetze   => a and 1 = a 	
                           => a or 0 = a

[OK] Extremalgesetze 	   => a and 0=0 	
                           => a or 1=1

[OK] Doppelnegationsgesetz => not( not a)=a

[OK] De Morgansche Gesetze => not(a and b)= not a or not b 	
                           => not(a or b)= not a and not b

[OK] Komplementärgesetze   => a and not a=0 	
                           => a or not a=1

[OK] Dualitätsgesetze 	   => not 0 = 1 
                           => not 1 = 0

[OK] Absorptionsgesetze    => a or(a and b)=a 	
                           => a and(a or b)=a	 
	 */
	public ASTNode simplify(ASTNode term,final IExpressionContext context) {

		debugPrintln("INPUT: "+term.toString());
		
		final Comparator<ASTNode> comp = new Comparator<ASTNode>() {
			
			@Override
			public int compare(ASTNode o1, ASTNode o2) {
				return o2.toString().compareTo( o1.toString() );
			}
		};
		
		ASTNode result = term.createCopy( true );

		result = reduce( result , context );

		// De Morgansche Gesetze  => not(a and b) = not a or  not b 	
		//                        => not(a or  b) = not a and not b		
		boolean simplified = applyLawOfDeMorgan(context,result);
		do {
			simplified = false;
			
			// sort children (Kommutativgesetz)
//			System.out.println("Before sorting: "+result);
//			simplified |= result.sortChildrenAscending( comp );
//			System.out.println("After sorting: "+result);
//			
			// Assoziativgesetz
			// (a and b) and c = a and (b and c) 	
			// (a or b) or c = a or (b or c)
			simplified = applyLawOfAssociativity(context,result);
			
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
			
			// Distributionsgesetz
			// 	a and (b or  c) = (a and b) or  (a and c) 	
			//  a or  (b and c) = (a or  b) and (a or  c)
			simplified |= applyDistributiveLaw(context,result);

			// De Morgansche Gesetze  => not a or  not b = not(a and b)  	
			//                        => not a and not b = not(a or  b)
			simplified |= applyInverseLawOfDeMorgan(context,result);			
		} while ( simplified );

		// get rid of all variables we eliminated
		context.retainOnly( gatherIdentifiers( result ));

		debugPrintln("SIMPLIFIED: "+result.toString());
		return result;
	}
	
	public Boolean isTrue(BooleanExpression expr,IExpressionContext context) {
		
		ASTNode lhs = expr.getLHS();
		ASTNode rhs = expr.getRHS();
		
		ASTNode value1 = lhs.evaluate( context );
		ASTNode value2= rhs.evaluate( context );
		if ( value1 != null && value2 != null ) {
			return value1.isEquivalent( value2 , context );
		}
		return null;
	}

	private boolean applyLawOfAssociativity(IExpressionContext context,
			ASTNode result) 
	{

		// Assoziativgesetz
		// (a and b) and c = a and (b and c) 	
		// (a or b) or c = a or (b or c)		
		
		final MutatingNodeVisitor visitor = new MutatingNodeVisitor( context ) {

			@Override
			public void visit(ASTNode node,IExpressionContext context,IIterationContext it) 
			{
				final ASTNode unwrapped = unwrap( node );
				
				if ( unwrapped.hasParent()  ) {
					if ( unwrapped.isAND() ) 
					{
						// (a and b) and c = a and (b and c) 	
						ASTNode leftChild = unwrapped.leftChild();
						ASTNode rightChild = unwrapped.rightChild();
						
						if ( unwrapped.isAND() &&
							 rightChild.isLeafNode() && 
							 isNonTrivialTerm( leftChild ) && 
							 unwrap( leftChild ).isAND() )
						{
							ASTNode term1 = 
									new TermNode( OperatorNode.and(
											unwrap( leftChild ).rightChild().createCopy( true ) ,
											rightChild ).createCopy( true ) );
							
							ASTNode term2 = 
									OperatorNode.and( unwrap( leftChild ).leftChild().createCopy( true ) , term1 ) ;
							
							debugPrintln("ASSOCIATIVITY: Replacing "+unwrapped.toString(false)+" -> "+term2);
							unwrapped.replaceWith( term2 );
							it.astMutated();						
						} 
					} 
					else if ( unwrapped.isOR() ) 
					{
						// (a or b) or c = a or (b or c) 	
						ASTNode leftChild = unwrapped.leftChild();
						ASTNode rightChild = unwrapped.rightChild();
						
						if ( unwrapped.isOR() &&
							 rightChild.isLeafNode() && 
							 isNonTrivialTerm( leftChild ) && 
							 unwrap( leftChild ).isOR() )
						{
							ASTNode term1 = 
									new TermNode( OperatorNode.or(
											unwrap( leftChild ).rightChild().createCopy( true ) ,
											rightChild.createCopy( true ) ) );
							
							ASTNode term2 = 
									OperatorNode.or( unwrap( leftChild ).leftChild().createCopy( true ) , term1 ) ;
							
							debugPrintln("ASSOCIATIVITY: Replacing "+unwrapped.toString(false)+" -> "+term2);
							unwrapped.replaceWith( term2 );
							it.astMutated();						
						} 
											
					}
				}
			}
		};
		return applyInOrder( result , visitor );
	}
	
	private static boolean isNonTrivialTerm(ASTNode node) 
	{
		return node instanceof TermNode && ! node.child(0).isLeafNode();
	}

	private boolean applyDistributiveLaw(IExpressionContext context,
			ASTNode result) 
	{
		// Distributionsgesetz
		// 	a and (b or  c) = (a and b) or  (a and c) 	
		//  a or  (b and c) = (a or  b) and (a or  c)
		
		final MutatingNodeVisitor visitor = new MutatingNodeVisitor( context ) {

			@Override
			public void visit(ASTNode node,IExpressionContext context,IIterationContext it) 
			{
				final ASTNode unwrapped = unwrap( node );
				if ( unwrapped.hasParent() && ( unwrapped.isAND() || unwrapped.isOR() ) ) 
				{
					ASTNode leftChild = unwrap( unwrapped.child(0) );
					ASTNode rightChild = unwrap( unwrapped.child(1) );
					if ( unwrapped.isAND() && rightChild.isOR() && leftChild.isLeafNode() ) 
					{
						ASTNode term1 = 
								OperatorNode.and( leftChild , unwrap( rightChild.child(0) ) );
						ASTNode term2 = 
								OperatorNode.and( leftChild , unwrap( rightChild.child(1) ) );
						ASTNode term = OperatorNode.or( term1  , term2 );
						
						debugPrintln("DISTRIBUTIVE LAW: Replacing "+unwrapped.toString(false)+" -> "+term);
						unwrapped.replaceWith( term );
						it.astMutated();						
					} 
					else if ( unwrapped.isOR() && rightChild.isAND() && leftChild.isLeafNode() ) 
					{
						ASTNode term1 = 
								OperatorNode.or( leftChild , unwrap( rightChild.child(0) ) );
						ASTNode term2 = 
								OperatorNode.or( leftChild , unwrap( rightChild.child(1) ) );
						ASTNode term = OperatorNode.and( term1  , term2 );
						debugPrintln("DISTRIBUTIVE LAW: Replacing "+unwrapped.toString(false)+" -> "+term);
						unwrapped.replaceWith( term );
						it.astMutated();
					}
				} 
			}
		};
		return applyInOrder( result , visitor );
	

	}

	public ASTNode substituteIdentifiers(ASTNode input,IExpressionContext context) {

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

	public Set<Identifier> gatherIdentifiers(ASTNode node) 
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

	private boolean applyLawOfIdemPotency(final IExpressionContext context,
			ASTNode result) 
	{
		// 1. Idempotenzgesetze ( x and x = x ) / ( x or x ) = x

		final MutatingNodeVisitor visitor = new MutatingNodeVisitor( context ) {

			@Override
			public void visit(ASTNode node,IExpressionContext context,IIterationContext it) 
			{
				final ASTNode unwrapped = unwrap( node );
				if ( unwrapped.hasParent() && ( unwrapped.isAND() || unwrapped.isOR() ) ) 
				{
					final boolean isEquivalent = unwrapped.child(0).isEquivalent( 
							unwrapped.child(1) , context 
							);
					if ( isEquivalent ) 
					{
						debugPrintln("IDEM: Replacing "+unwrapped.toString(false)+" -> "+unwrapped.child(0).toString(false));
						unwrapped.replaceWith( unwrapped.child(0) );
						it.astMutated();
					}
				} 
			}
		};
		return applyInOrder( result , visitor );
	}

	private boolean applyRuleOfDoubleNegation(final IExpressionContext context,
			ASTNode result) 
	{
		// double negation: Doppelnegationsgesetz => not( not a)=a
		final MutatingNodeVisitor visitor2 = new MutatingNodeVisitor( context ) {

			@Override
			public void visit(ASTNode node,IExpressionContext context,IIterationContext it) 
			{
				final ASTNode unwrapped = unwrap( node );
				if ( unwrapped.hasParent() && unwrapped.isNOT() ) 
				{
					if ( unwrap( unwrapped.child(0) ).isNOT() ) 
					{
						debugPrintln("NOT-NOT: Replacing "+unwrapped.toString(false)+
								" -> "+unwrapped.child(0).child(0).toString(false));
						unwrapped.replaceWith( unwrap( unwrapped.child(0) ).child(0) );
						it.astMutated();
					}
				} 
			}
		};

		return applyInOrder( result , visitor2 );
	}

	private boolean applyLawOfNeutrality(final IExpressionContext context,
			ASTNode result) 
	{
		// Neutralitätsgesetze   => a and 1 = a 	
		//                       => a or 0 = a		
		final MutatingNodeVisitor visitor3 = new MutatingNodeVisitor( context ) {

			@Override
			public void visit(ASTNode node,IExpressionContext context,IIterationContext it) 
			{
				final ASTNode unwrapped = unwrap( node );
				if ( unwrapped.hasParent() && ( unwrapped.isOR() || unwrapped.isAND() ) ) 
				{
					final ASTNode leftChild = unwrap( unwrapped.child(0) );
					final ASTNode rightChild = unwrap( unwrapped.child(1) );

					final ASTNode neutralElement = unwrapped.isAND() ? new TrueNode() : new FalseNode();

					if ( leftChild.isEquivalent( neutralElement , context) ) 
					{
						debugPrintln("NEUTRALITY: Replacing "+unwrapped.toString(false)+
								" -> "+rightChild);
						unwrapped.replaceWith( rightChild );
						it.astMutated();						
					} 
					else if ( rightChild.isEquivalent( neutralElement , context ) ) 
					{
						debugPrintln("NEUTRALITY: Replacing "+unwrapped.toString(false)+
								" -> "+leftChild);						
						unwrapped.replaceWith( leftChild );
						it.astMutated();
					}
				} 
			}
		};

		return applyInOrder( result , visitor3 );
	}

	private boolean applyLawOfExtrema(final IExpressionContext context,
			ASTNode result) 
	{
		// Extremalgesetze 	  => a and 0=0 	
		//                    => a or 1=1				
		final MutatingNodeVisitor visitor4 = new MutatingNodeVisitor( context ) {

			@Override
			public void visit(ASTNode node,IExpressionContext context,IIterationContext it) 
			{
				final ASTNode unwrapped = unwrap( node );
				if ( unwrapped.hasParent() && ( unwrapped.isOR() || unwrapped.isAND() ) ) 
				{
					final ASTNode leftChild = unwrap( unwrapped.child(0) );
					final ASTNode rightChild = unwrap( unwrapped.child(1) );

					final ASTNode neutralElement = unwrapped.isAND() ? new FalseNode() : new TrueNode();

						if ( leftChild.isEquivalent( neutralElement , context) ) 
						{
							debugPrintln("EXTREMES: Replacing "+unwrapped.toString(false)+
									" -> "+rightChild);
							unwrapped.replaceWith( neutralElement );
							it.astMutated();						
						} 
						else if ( rightChild.isEquivalent( neutralElement , context ) ) 
						{
							debugPrintln("NEUTRALITY: Replacing "+unwrapped.toString(false)+
									" -> "+leftChild);						
							unwrapped.replaceWith( neutralElement );
							it.astMutated();
						}
				} 
			}
		};

		return applyInOrder( result , visitor4 );
	}

	private boolean applyComplementaryLaw(final IExpressionContext context,
			ASTNode result) 
	{
		// Komplementärgesetze   => a and not a=0 	
		//                       => a or not a=1		 
		final MutatingNodeVisitor visitor5 = new MutatingNodeVisitor( context ) {

			@Override
			public void visit(ASTNode node,IExpressionContext context,IIterationContext it) 
			{
				final ASTNode unwrapped = unwrap( node );
				if ( unwrapped.hasParent() && ( unwrapped.isOR() || unwrapped.isAND() ) ) 
				{
					final ASTNode leftChild = unwrap( unwrapped.child(0) );					
					final ASTNode rightChild = unwrap( unwrapped.child(1) );

					if ( rightChild.isNOT() ) 
					{
						ASTNode subTerm = unwrap( rightChild.child(0) );
						if ( subTerm.isEquals( leftChild) )
						{ 
							final ASTNode result = unwrapped.isAND() ? new FalseNode() : new TrueNode();

							debugPrintln("COMPLEMENTARY: Replacing "+unwrapped.toString(false)+
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

	private boolean applyLawOfAbsorption(final IExpressionContext context,
			ASTNode result) 
	{
		// Absorptionsgesetze 	 => a or (a and b) = a 	
		//                       => a and(a or  b) = a			
		final MutatingNodeVisitor visitor6 = new MutatingNodeVisitor( context ) {

			@Override
			public void visit(ASTNode node,IExpressionContext context,IIterationContext it) 
			{
				final ASTNode unwrapped = unwrap( node );
				if ( unwrapped.hasParent() && ( unwrapped.isOR() || unwrapped.isAND() ) ) 
				{
					final ASTNode leftChild = unwrap( unwrapped.child(0) );					
					final ASTNode rightChild = unwrap( unwrapped.child(1) );

					if ( ( ( unwrapped.isOR()  && rightChild.isAND() ) ||
						   ( unwrapped.isAND() && rightChild.isOR()  ) ) ) 
					{
						ASTNode subTerm = unwrap( rightChild.child(0) );
						if ( subTerm.isEquals( leftChild) ) 
						{ 
							debugPrintln("ABSORPTION: Replacing "+unwrapped.toString(false)+
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

	private boolean applyLawOfDeMorgan(IExpressionContext context,ASTNode result) 
	{
		// De Morgansche Gesetze  => not(a and b) = not a or  not b 	
		//                        => not(a or  b) = not a and not b
		final MutatingNodeVisitor visitor6 = new MutatingNodeVisitor( context ) {

			@Override
			public void visit(ASTNode node,IExpressionContext context,IIterationContext it) 
			{
				final ASTNode unwrapped = unwrap( node );
				if ( unwrapped.isNOT() && unwrapped.hasParent()  )
				{
					final ASTNode child = unwrap( unwrapped.child(0) );
					if ( ( child.isAND() || child.isOR() ) ) 
					{
						final ASTNode leftChild = unwrap( child.child(0) );					
						final ASTNode rightChild = unwrap( child.child(1) );

						final ASTNode notLeft = OperatorNode.not( leftChild.createCopy(true) );
						final ASTNode notRight = OperatorNode.not( rightChild.createCopy(true) );

						final ASTNode newTerm = child.isAND() ? 
								OperatorNode.or( notLeft , notRight ) :
									OperatorNode.and( notLeft , notRight );

								debugPrintln("DE-MORGAN: Replacing "+unwrapped.toString(false)+
										" -> "+newTerm);
								unwrapped.replaceWith( newTerm );
								it.astMutated();			
					}
				} 
			}
		};

		return applyInOrder( result , visitor6 );		
	}

	private boolean applyInverseLawOfDeMorgan(IExpressionContext context,ASTNode result) 
	{
		// De Morgansche Gesetze  => not a or  not b = not(a and b)   	
		//                        => not a and not b = not(a or  b) 

		final MutatingNodeVisitor visitor6 = new MutatingNodeVisitor( context ) {

			@Override
			public void visit(ASTNode node,IExpressionContext context,IIterationContext it) 
			{
				final ASTNode unwrapped = unwrap( node );
				if ( unwrapped.hasParent() && ( unwrapped.isOR() || unwrapped.isAND() ) )
				{
					final ASTNode leftChild = unwrap( unwrapped.child(0) );					
					final ASTNode rightChild = unwrap( unwrapped.child(1) );

					if ( (leftChild.isNOT() && rightChild.isNOT() ) ) 
					{
						final ASTNode leftArgument = unwrap( leftChild.child(0 ) );
						final ASTNode rightArgument = unwrap( rightChild.child( 0 ) );

						final ASTNode newTerm;
						if ( unwrapped.isOR() ) {
							newTerm = OperatorNode.not( 
									OperatorNode.and( leftArgument.createCopy(true) ,
											rightArgument.createCopy(true) ) );
						} else { // AND
							newTerm = OperatorNode.not( 
									OperatorNode.or( leftArgument.createCopy(true) ,
											rightArgument.createCopy(true) ) );							
						}
						debugPrintln("INV. DE-MORGAN: Replacing "+unwrapped.toString(false)+
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
	public ASTNode reduce(ASTNode term,final IExpressionContext context) 
	{
		final ASTNode result = term.createCopy( true );

		final MutatingNodeVisitor visitor = new MutatingNodeVisitor( context ) {

			@Override
			public void visit(ASTNode node,IExpressionContext context,IIterationContext it) 
			{
				if ( node.hasParent() && node.hasLiteralValue( context ) ) 
				{
					final ASTNode reduced = node.evaluate( context );
					if ( reduced != node )
					{
						debugPrintln("REDUCE: Replacing "+node+" -> "+reduced);
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
	
	private void debugPrintln(String message) {
		if ( debug ) {
			 System.out.println( message );
		}
	}
	
	private void debugPrint(String message) {
		if ( debug ) {
			 System.out.print( message );
		}
	}	
}
