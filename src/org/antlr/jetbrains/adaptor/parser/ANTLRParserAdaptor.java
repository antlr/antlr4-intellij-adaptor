package org.antlr.jetbrains.adaptor.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.psi.tree.IElementType;
import org.antlr.jetbrains.adaptor.lexer.PSITokenSource;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.jetbrains.annotations.NotNull;

/** An adaptor that makes an ANTLR parser look like a PsiParser. */
public abstract class ANTLRParserAdaptor implements PsiParser {
	protected final Language language;
	protected final Parser parser;

	/** Create a jetbrains adaptor for an ANTLR parser object. When
	 *  the IDE requests a {@link #parse(IElementType, PsiBuilder)},
	 *  the token stream will be set on the parser.
	 */
	public ANTLRParserAdaptor(Language language, Parser parser) {
		this.language = language;
		this.parser = parser;
	}

	public Language getLanguage() {
		return language;
	}

	@NotNull
	@Override
	public ASTNode parse(IElementType root, PsiBuilder builder) {
		ProgressIndicatorProvider.checkCanceled();

		TokenSource source = new PSITokenSource(builder);
		TokenStream tokens = new CommonTokenStream(source);
		parser.setTokenStream(tokens);
		parser.setErrorHandler(new ErrorStrategyAdaptor()); // tweaks missing tokens
		parser.removeErrorListeners();
		parser.addErrorListener(new SyntaxErrorListener()); // trap errors
		ParseTree parseTree = null;
		PsiBuilder.Marker rollbackMarker = builder.mark();
		try {
			parseTree = parse(parser, root);
		}
		finally {
			rollbackMarker.rollbackTo();
		}

		// Now convert ANTLR parser tree to PSI tree by mimicking subtree
		// enter/exit with mark/done calls. I *think* this creates their parse
		// tree (AST as they call it) when you call {@link PsiBuilder#getTreeBuilt}
		ANTLRParseTreeToPSIConverter listener = createListener(parser, root, builder);
		PsiBuilder.Marker rootMarker = builder.mark();
		ParseTreeWalker.DEFAULT.walk(listener, parseTree);
		while (!builder.eof()) {
			ProgressIndicatorProvider.checkCanceled();
			builder.advanceLexer();
		}
		// NOTE: parse tree returned from parse will be the usual ANTLR tree
		// ANTLRParseTreeToPSIConverter will convert that to the analogous jetbrains AST nodes
		// When parsing an entire file, the root IElementType will be a
		// IFileElementType. When trying to replace nodes and so on, we get
		// a dummy root and the type arg is likely an identifier IElementType.
		// This results in a weird tree that has for example
		// (ID (expr (primary ID))) with the ID IElementType as a subtree root
		// as well as the appropriate leaf. The dummy ID root is a CompositeElement
		// and it appears to be a requirement. When I tried to always make
		// the root have type IFileElementType, I got an exception that leads me
		// to believe that rootMarker.done(root) is the appropriate code to
		// finish the AST.
		rootMarker.done(root);
		return builder.getTreeBuilt(); // calls the ASTFactory.createComposite() etc...
	}

	protected abstract ParseTree parse(Parser parser, IElementType root);

	protected ANTLRParseTreeToPSIConverter createListener(Parser parser, IElementType root, PsiBuilder builder) {
		return new ANTLRParseTreeToPSIConverter(language, parser, builder);
	}
}
