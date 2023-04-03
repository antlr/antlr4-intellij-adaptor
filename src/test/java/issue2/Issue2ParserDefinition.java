package issue2;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.antlr.intellij.adaptor.issue2.Issue2Lexer;
import org.antlr.intellij.adaptor.issue2.Issue2Parser;
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor;
import org.antlr.intellij.adaptor.lexer.PSIElementTypeFactory;
import org.antlr.intellij.adaptor.parser.ANTLRParserAdaptor;
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.jetbrains.annotations.NotNull;

public class Issue2ParserDefinition implements ParserDefinition {

	public Issue2ParserDefinition() {
		PSIElementTypeFactory.defineLanguageIElementTypes(
			Issue2Language.INSTANCE,
			Issue2Lexer.VOCABULARY,
			Issue2Parser.ruleNames
		);
	}

	@NotNull
	@Override
	public Lexer createLexer(Project project) {
		return new ANTLRLexerAdaptor(Issue2Language.INSTANCE, new Issue2Lexer(null));
	}

	@Override
	public PsiParser createParser(Project project) {
		return new ANTLRParserAdaptor(Issue2Language.INSTANCE, new Issue2Parser(null)) {
			@Override
			protected ParseTree parse(Parser parser, IElementType root) {
				return ((Issue2Parser) parser).block();
			}
		};
	}

	@Override
	public IFileElementType getFileNodeType() {
		return new IFileElementType(Issue2Language.INSTANCE);
	}

	@NotNull
	@Override
	public TokenSet getCommentTokens() {
		return TokenSet.EMPTY;
	}

	@NotNull
	@Override
	public TokenSet getStringLiteralElements() {
		return TokenSet.EMPTY;
	}

	@NotNull
	@Override
	public PsiElement createElement(ASTNode node) {
		return new ANTLRPsiNode(node);
	}

	@Override
	public PsiFile createFile(FileViewProvider viewProvider) {
		return new PsiFileBase(viewProvider, Issue2Language.INSTANCE) {
			@NotNull
			@Override
			public FileType getFileType() {
				return Issue2FileType.INSTANCE;
			}
		};
	}
}
