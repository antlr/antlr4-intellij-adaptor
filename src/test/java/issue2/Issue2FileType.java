package issue2;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

class Issue2FileType extends LanguageFileType {

	static Issue2FileType INSTANCE = new Issue2FileType();

	private Issue2FileType() {
		super(Issue2Language.INSTANCE);
	}

	@NotNull
	@Override
	public String getName() {
		return "Issue 2";
	}

	@NotNull
	@Override
	public String getDescription() {
		return "Issue 2";
	}

	@NotNull
	@Override
	public String getDefaultExtension() {
		return "ext";
	}

	@Nullable
	@Override
	public Icon getIcon() {
		return null;
	}
}
