package issue2;

import com.intellij.lang.Language;

class Issue2Language extends Language {

	static Issue2Language INSTANCE = new Issue2Language();

	private Issue2Language() {
		super("Issue2");
	}
}
