package com.jialu.minipage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;

/**
 * @author EB060
 *
 */
public class MiniCell {
	private List<String> classes;
	private String content;
	private String html;
	private MiniInner inner;

	private XSSFCell right;
	private XSSFCell bottom;
	private XSSFCell self;

	public MiniCell() {
		this.setHtml("");
		this.setClasses(new ArrayList<String>());
	}

	public List<String> getClasses() {
		return classes;
	}

	public void setClasses(List<String> classes) {
		this.classes = classes;
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

	public void creatHtml() {
		if (this.content == null || this.inner == null) {
			this.content = "";
		} else {
			if (this.inner.getClasses().size() > 0) {
				changeCellBorderClass(this.classes, this.inner.getClasses());
				this.content = String.format("<div class=\"%s\">%s</div>",
						StringUtils.join(this.inner.getClasses(), " "), content);
			} else {
				this.content = String.format("<div><pre>%s</pre></div>", content);
			}
		}
		if (this.classes.size() > 0) {
			this.html = String.format("<div class=\"%s\"></div>%s", StringUtils.join(this.classes, " "), content);
		} else if (!this.content.equals("")) {
			this.html = String.format("<div></div>%s", content);
		}
	}

	/**
	 * @param father
	 * @param child
	 */
	private void changeCellBorderClass(List<String> father, List<String> child) {
		Iterator<String> css = father.iterator();
		while (css.hasNext()) {
			String name = css.next();
			if (name.startsWith("BT") || name.startsWith("BB") || name.startsWith("BL") || name.startsWith("BR")) {
				css.remove();
				child.add(name);
			}
		}
	}

	public MiniInner getInner() {
		return inner;
	}

	public void setInner(MiniInner font) {
		this.inner = font;
	}

	public XSSFCell getRight() {
		return right;
	}

	public void setRight(XSSFCell right) {
		this.right = right;
	}

	public XSSFCell getBottom() {
		return bottom;
	}

	public void setBottom(XSSFCell bottom) {
		this.bottom = bottom;
	}

	public XSSFCell getSelf() {
		return self;
	}

	public void setSelf(XSSFCell self) {
		this.self = self;
	}

}
