package org.antlr.intellij.adaptor.test;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor;
import org.antlr.intellij.adaptor.lexer.PSIElementTypeFactory;
import org.antlr.intellij.adaptor.parser.ANTLRParserAdaptor;
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TestParserDefinition implements ParserDefinition{
	
	private final Class<? extends Parser> parserClass;
	private final Class<? extends Lexer> lexerClass;
	private final Method rootNodeParser;
	private final Language language;
	private final FileType fileType;
	
	public TestParserDefinition(Class<? extends Parser> parserClass, Class<? extends Lexer> lexerClass, Method parser, Language language, FileType fileType){
		this.parserClass = parserClass;
		this.lexerClass = lexerClass;
		rootNodeParser = parser;
		this.language = language;
		this.fileType = fileType;
		
		try{
			PSIElementTypeFactory.defineLanguageIElementTypes(
					language,
					(Vocabulary)lexerClass.getDeclaredField("VOCABULARY").get(null),
					(String[])parserClass.getDeclaredField("ruleNames").get(null)
			);
		}catch(IllegalAccessException | NoSuchFieldException e){
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static TestParserDefinition byName(String name, String root, Language language, FileType fileType){
		try{
			Class<?> parserClass = Class.forName("org.antlr.intellij.adaptor.test.testcases." + name + "Parser");
			Class<?> lexerClass = Class.forName("org.antlr.intellij.adaptor.test.testcases." + name + "Lexer");
			if(!Parser.class.isAssignableFrom(parserClass) || !Lexer.class.isAssignableFrom(lexerClass))
				throw new IllegalArgumentException("Given parser/lexer class doesn't exist, or is invalid");
			Method rootNodeParser = parserClass.getDeclaredMethod(root);
			if(!ParseTree.class.isAssignableFrom(rootNodeParser.getReturnType()))
				throw new IllegalArgumentException("Given root node method is not a parser");
			return new TestParserDefinition((Class<? extends Parser>)parserClass, (Class<? extends Lexer>)lexerClass, rootNodeParser, language, fileType);
		}catch(ReflectiveOperationException e){
			throw new RuntimeException(e);
		}
	}
	
	public @NotNull com.intellij.lexer.Lexer createLexer(Project project){
		try{
			return new ANTLRLexerAdaptor(language, lexerClass.getConstructor(CharStream.class).newInstance((Object)null));
		}catch(InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e){
			throw new RuntimeException(e);
		}
	}
	
	public @NotNull PsiParser createParser(Project project){
		try{
			return new ANTLRParserAdaptor(language, parserClass.getConstructor(TokenStream.class).newInstance((Object)null)){
				protected ParseTree parse(org.antlr.v4.runtime.Parser parser, IElementType root){
					try{
						return (ParseTree)rootNodeParser.invoke(parser);
					}catch(IllegalAccessException | InvocationTargetException e){
						throw new RuntimeException(e);
					}
				}
			};
		}catch(NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e){
			throw new RuntimeException(e);
		}
	}
	
	public @NotNull IFileElementType getFileNodeType(){
		return new IFileElementType(language);
	}
	
	public @NotNull TokenSet getCommentTokens(){
		return TokenSet.EMPTY;
	}
	
	public @NotNull TokenSet getStringLiteralElements(){
		return TokenSet.EMPTY;
	}
	
	public @NotNull PsiElement createElement(ASTNode node){
		return new ANTLRPsiNode(node);
	}
	
	public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider){
		return new PsiFileBase(viewProvider, language){
			public @NotNull FileType getFileType(){
				return fileType;
			}
		};
	}
}