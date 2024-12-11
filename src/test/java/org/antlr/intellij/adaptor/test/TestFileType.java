package org.antlr.intellij.adaptor.test;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class TestFileType extends LanguageFileType{
	
	private final String name;
	
	public TestFileType(String name, Language language){
		super(language);
		this.name = name;
	}
	
	public @NonNls @NotNull String getName(){
		return name;
	}
	
	public @NlsContexts.Label @NotNull String getDescription(){
		return name;
	}
	
	public @NotNull String getDefaultExtension(){
		return name;
	}
	
	public Icon getIcon(){
		return null;
	}
}