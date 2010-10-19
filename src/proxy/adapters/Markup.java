package proxy.adapters;

import proxy.adapters.markup.MediaWikiRenderer;

/**
** Static utility methods for markup-related adapters.
**
** @author infinity0
*/
final public class Markup {

	final private static MarkupRenderer renderer;

	private Markup() { }

	static {
		renderer = new MediaWikiRenderer();
	}

	/**
	** Render the given text using the system-wide renderer. This is obtained
	** from the {@code proxy.adapters.markup} system property. TODO currently
	** it assumes MediaWiki syntax.
	*/
	public static String render(String text) {
		return renderer.render(text);
	}

}
