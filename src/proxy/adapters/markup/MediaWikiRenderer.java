package proxy.adapters.markup;

import info.bliki.wiki.model.WikiModel;
import proxy.adapters.MarkupRenderer;

/**
** @author infinity0
*/
public class MediaWikiRenderer implements MarkupRenderer {

	 /** Wiki renderer */
	protected WikiModel wiki = new WikiModel("", "");

	public String render(String text) {
		String s = wiki.render(preprocessWikiString(text));
		// strip <p> tags from single-line comments
		return (s.lastIndexOf("<p>") == 1 && s.startsWith("\n<p>") && s.endsWith("</p>"))?
		  "\n" + s.substring(4, s.length()-4): s;
	}

	public static String preprocessWikiString(String b) {
		String lines[] = b.split("\\n");
		int indent=Integer.MAX_VALUE;
		// find smallest indent in the first 4 non-blank lines, excl the first line
		// whose indent is discarded by javadoc
		for (int i=1, l=0; i<lines.length && l<4; ++i) {
			int j=-1;
			while (++j < lines[i].length() && lines[i].charAt(j) == ' ');
			if (j == lines[i].length()) { continue; }
			if (j < indent) { indent = j; }
			++l;
		}
		boolean startedtext = false;
		boolean startparagraph = true;
		char firstchar = 0;
		StringBuffer result = new StringBuffer();
		// selectively merge lines into paragraphs
		for (int i=0; i<lines.length; ++i) {
			int j=-1;
			while (++j < lines[i].length() && lines[i].charAt(j) == ' ');
			if (j == lines[i].length()) {
				// if we haven't seen text yet, skip the entire line
				if (!startedtext) { continue; }
				// if line is empty, start a new paragraph
				result.append('\n').append('\n');
				startparagraph = true;
				continue;
			} else if (startparagraph) {
				// determine the automatic line-merge setting from the first character of
				// each paragraph
				startparagraph = false;
				firstchar = (j > indent)? ' ': Character.isLetter(lines[i].charAt(j))? 0: lines[i].charAt(j);
			} else if (lines[i].charAt(indent > j? j: indent) == firstchar) {
				// if firstchar is ' ', start a new line on ' '
				// if firstchar is a letter, start a new line on \0 (effectively never)
				// if firstchar is a symbol, start a new line on that same symbol
				result.append('\n');
			}
			startedtext = true;
			// strip the indent (or smaller) from each line
			result.append(lines[i].substring(firstchar != ' '? j: indent > j? j: indent)).append(' ');
		}
		return result.toString();
	}

}
