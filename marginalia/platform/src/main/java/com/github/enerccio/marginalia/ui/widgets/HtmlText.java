package com.github.enerccio.marginalia.ui.widgets;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.html.Span;

/**
 * A span with <b>markup</b>: the text is HTML and is put into the page as it is. Whatever came from a user, a model, a file or a
 * server must be escaped by the caller first ({@code HtmlUtils.htmlEscape}); {@link Notification} does it unless it is asked not to.
 * For plain text use a {@link Span}.
 */
public class HtmlText extends Composite<Span> implements HasText, HasStyle, ClickNotifier<HtmlText> {

	public static final String NBSP = "&nbsp";
	private static final String SPAN_OVERLAY = "<span>%s</span>";
	
	private final Span content;
	private String text;
	
	public HtmlText(String text) {
		content = new Span();
		setText(text);
	}
	
	@Override
	protected Span initContent() {
		return content;
	}
	
	@Override
	public void setText(String text) {
		if (text == null)
			text = "";
		
		if (text.equals(this.text))
			return;
		
		this.text = text;
		content.removeAll();
		content.add(new Html(String.format(SPAN_OVERLAY, this.text)));
	}
	
	@Override
	public String getText() {
		return text;
	}

	public static HtmlText nbsp() {
		return new HtmlText(NBSP);
	}

}
