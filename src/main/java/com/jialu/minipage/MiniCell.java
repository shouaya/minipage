package com.jialu.minipage;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class MiniCell {
	private List<String> classes;
	private List<String> styles;
	private String content;
	private String html;
	private int size;

	public List<String> getClasses() {
		return classes;
	}

	public void setClasses(List<String> classes) {
		this.classes = classes;
	}

	public List<String> getStyles() {
		return styles;
	}

	public void setStyles(List<String> styles) {
		this.styles = styles;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public void creatHtml(int r, int c) {
		String html = "<div class=\"%s\" style=\"%s\"></div>%s";
		if (content == null) {
			content = "";
		} else {
			content = "<div class=\"I R" + r + " C" + c + "\" style=\"font-size: " + this.size + "px;\">" + content
					+ "</div>";
		}
		this.html = String.format(html, StringUtils.join(this.classes, " "), StringUtils.join(this.styles, ";"),
				content);
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

}
