package de.codesourcery.booleanalgebra;

import java.util.Set;

import de.codesourcery.booleanalgebra.ast.ASTNode;
import de.codesourcery.booleanalgebra.ast.Identifier;
import de.codesourcery.booleanalgebra.lexer.Lexer;

public class ParseContext extends Lexer implements IParseContext {

	private final IExpressionContext ctx;
	
	public ParseContext(IScanner scanner,IExpressionContext ctx) {
		super(scanner);
		if ( ctx == null ) {
			throw new IllegalArgumentException("ctx must not be null");
		}
		this.ctx = ctx;
	}

	@Override
	public ASTNode lookup(Identifier identifier) {
		return ctx.lookup( identifier );
	}

	@Override
	public ASTNode tryLookup(Identifier identifier) {
		return ctx.tryLookup(identifier);
	}

	@Override
	public void set(Identifier name, ASTNode value) {
		ctx.set( name , value );
	}

	@Override
	public Set<Identifier> getAllIdentifiers() {
		return ctx.getAllIdentifiers();
	}

	@Override
	public void retainOnly(Set<Identifier> ids) {
		ctx.retainOnly( ids );
	}

	@Override
	public void remove(Identifier identifier) {
		ctx.remove( identifier );
	}

    @Override
    public IExpressionContext createCopy()
    {
        return ctx.createCopy();
    }

    @Override
    public void clear()
    {
        ctx.clear();
    }

}
