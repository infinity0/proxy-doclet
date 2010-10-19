package proxy.adapters.markup;

import proxy.adapters.MarkupRenderer;

/**
** {@link MarkupRenderer} that just wraps the text in {@code pre} tags.
**
** @author infinity0
*/
public class PreformatRenderer implements MarkupRenderer {

	public String render(String text) {
		return "<pre>" + text + "</pre>";
	}

}
