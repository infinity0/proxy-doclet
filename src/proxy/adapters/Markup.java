package proxy.adapters;

import proxy.adapters.markup.MediaWikiRenderer;

/**
** @author infinity0
*/
final public class Markup {

	final private static MarkupRenderer renderer;

	private Markup() { }

	static {
		renderer = new MediaWikiRenderer();
	}

	public static String render(String text) {
		return renderer.render(text);
	}

}
