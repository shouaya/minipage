package com.jialu.minipage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.yahoo.platform.yui.compressor.CssCompressor;

/**
 * @author EB060
 *
 */
public class MiniPage {

	private static final int MAX_ROW_CNT = 35;
	private static final int MAX_COL_CNT = 20;
	private static final double ROW_HEIGHT = 1;
	private static final double COL_WIDTH = 1;

	/**
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.out.println("Parameters Size Error");
			throw new RuntimeException();
		}
		File template = new File(args[0]);
		if(!template.exists()){
			System.out.println("file not found:" + template.getAbsolutePath());
			throw new RuntimeException();
		}
		XSSFWorkbook book = readDesignFile(template);
		createHtmlFile(template, book);
		System.out.println("HTMLを生成しました。");
	}

	/**
	 * @param template
	 * @return
	 * @throws IOException
	 */
	private static XSSFWorkbook readDesignFile(File template) throws IOException {
		FileInputStream execlFileStream = null;
		XSSFWorkbook book = null;

		try {
			execlFileStream = new FileInputStream(template);
			book = new XSSFWorkbook(execlFileStream);
			execlFileStream.close();
		} catch (IOException e) {
			System.out.println("Can Not Read File:" + e.getMessage());
			throw e;
		} finally {
			execlFileStream.close();
		}
		return book;
	}

	/**
	 * @param template 
	 * @param book
	 * @return
	 * @throws IOException
	 */
	private static String createHtmlFile(File template, XSSFWorkbook book) throws IOException {
		int cntSheet = book.getNumberOfSheets();
		String indexPath = "";
		HashMap<String, String> needAddCss = new HashMap<String, String>();
		for (int index = 0; index < cntSheet; index++) {
			XSSFSheet sheet = book.getSheetAt(index);
			if (sheet.getSheetName().startsWith("_")) {
				continue;
			}
			System.out.println(sheet.getSheetName() + "はじまります。");
			MiniBody body = createHtmlFile(template, sheet);
			needAddCss.putAll(body.getCss());
			System.out.println(sheet.getSheetName() + "終わりました。");
		}
		createCssFile(template, book.getSheet("_css"), needAddCss);
		return indexPath;
	}

	/**
	 * @param template 
	 * @param sheet
	 * @param needAddCss
	 * @throws IOException
	 */
	private static void createCssFile(File template, XSSFSheet sheet, HashMap<String, String> needAddCss) throws IOException {
		StringBuilder sb = new StringBuilder();
		String cssContent = createCssContent(sheet, needAddCss);
		sb.append(cssContent).append("\r\n");
		String outPath = template.getParentFile().getAbsolutePath() + "/../app";
		try (StringReader reader = new StringReader(sb.toString());
				FileWriter fw = new FileWriter(String.format(outPath + "/app.css", sheet.getSheetName()));) {
			CssCompressor cssc = new CssCompressor(reader);
			cssc.compress(fw, 1000);
		} catch (IOException e) {
			throw e;
		}
	}

	/**
	 * @param sheet
	 * @param needAddCss
	 * @return
	 */
	private static String createCssContent(XSSFSheet sheet, HashMap<String, String> needAddCss) {
		StringBuilder sb = new StringBuilder();
		int lastRow = sheet.getLastRowNum();
		for (int row = 0; row <= lastRow; row++) {
			int lastCol = sheet.getRow(row).getLastCellNum();
			for (int col = 0; col <= lastCol; col++) {
				XSSFCell cell = sheet.getRow(row).getCell(col);
				if (cell != null) {
					String content = cell.toString();
					if (lastCol > 1) {
						if (col == 0) {
							sb.append(content).append("{");
						} else if (col == (lastCol - 1)) {
							sb.append(content).append("}");
						} else {
							sb.append(content);
						}
					} else {
						sb.append(content);
					}
				}
			}
			sb.append("\r\n");
		}
		for (String key : needAddCss.keySet()) {
			if (needAddCss.get(key).equals("color:#000")) {
				continue;
			}
			sb.append(".").append(key).append("{").append(needAddCss.get(key)).append("}").append("\r\n");
		}
		return sb.toString();
	}

	/**
	 * @param sheet
	 * @return
	 * @throws IOException
	 */
	private static MiniBody createHtmlFile(File template, XSSFSheet sheet) throws IOException {
		MiniBody body = createBodyContent(template, sheet);
		String outPath = template.getParentFile().getAbsolutePath() + "/../app";
		File file = new File(String.format(outPath + "/%s.html", sheet.getSheetName()));
		FileUtils.writeStringToFile(file, body.getHtml(), Charset.forName(CharEncoding.UTF_8));
		return body;
	}

	/**
	 * @param template 
	 * @param xssfSheet
	 * @return
	 * @throws IOException
	 */
	private static MiniBody createBodyContent(File template, XSSFSheet xssfSheet) throws IOException {
		MiniBody body = new MiniBody();
		body.setCss(new HashMap<String, String>());
		StringBuilder sb = new StringBuilder();
		sb.append("<").append(xssfSheet.getSheetName()).append(">\r\n");
		body.setCells(new LinkedHashMap<String, MiniCell>());
		for (int row = 0; row < MAX_ROW_CNT; row++) {
			for (int col = 0; col < MAX_COL_CNT; col++) {
				MiniCell cell = new MiniCell();
				cell.setBottom(row < MAX_ROW_CNT - 1 ? xssfSheet.getRow(row + 1).getCell(col) : null);
				cell.setRight(col < MAX_COL_CNT - 1 ? xssfSheet.getRow(row).getCell(col + 1) : null);
				cell.setSelf(xssfSheet.getRow(row).getCell(col));
				body.getCells().put(xssfSheet.getSheetName() + "R" + row + "C" + col, cell);
				createMiniCell(body, cell);
			}
		}
		for (String key : body.getCells().keySet()) {
			body.getCells().get(key).creatHtml();
			String html = body.getCells().get(key).getHtml();
			sb.append(html);
			if (html.length() > 0) {
				sb.append("\r\n");
			}
		}
		String outPath = template.getParentFile().getAbsolutePath();
		String scriptPath = outPath + "/" + xssfSheet.getSheetName() + ".js";
		File script = new File(scriptPath);
		if (script.exists()) {
			sb.append("\r\n<script>\r\n")
			.append(FileUtils.readFileToString(script, "UTF-8"))
			.append("\r\n</script>\r\n");
		}
		sb.append("</").append(xssfSheet.getSheetName()).append(">");
		body.setHtml(sb.toString());
		return body;
	}

	/**
	 * @param body
	 * @param mc
	 * @return
	 */
	private static MiniCell createMiniCell(MiniBody body, MiniCell mc) {
		if (mc.getSelf() == null) {
			return mc;
		}
		drawCellBorder(mc, body);

		XSSFColor bgColor = mc.getSelf().getCellStyle().getFillForegroundColorColor();
		if (mc.getSelf().toString() == "" && mc.getClasses().size() == 0 && bgColor == null) {
			return mc;
		}

		drawCellContent(mc, body);

		return mc;
	}

	/**
	 * @param mc
	 * @param body
	 */
	private static void drawCellBorder(MiniCell mc, MiniBody body) {
		if (isInMergedRange(mc.getSelf())) {
			drawMergedCellBorder(mc, body);
			return;
		}
		if (mc.getSelf().getCellStyle().getBorderLeftEnum().getCode() > 0) {
			mc.getClasses().add("BL" + getCssBorder(mc.getSelf().getCellStyle().getBorderLeftEnum()));
		}
		if (mc.getSelf().getCellStyle().getBorderRightEnum().getCode() > 0) {
			if (mc.getRight() == null || mc.getRight().getCellStyle().getBorderLeftEnum().getCode() <= 0) {
				mc.getClasses().add("BR" + getCssBorder(mc.getSelf().getCellStyle().getBorderRightEnum()));
			}
		}
		if (mc.getSelf().getCellStyle().getBorderTopEnum().getCode() > 0) {
			mc.getClasses().add("BT" + getCssBorder(mc.getSelf().getCellStyle().getBorderTopEnum()));
		}
		if (mc.getSelf().getCellStyle().getBorderBottomEnum().getCode() > 0) {
			if (mc.getBottom() == null || mc.getBottom().getCellStyle().getBorderTopEnum().getCode() <= 0) {
				mc.getClasses().add("BB" + getCssBorder(mc.getSelf().getCellStyle().getBorderBottomEnum()));
			}
		}
	}

	/**
	 * @param mc
	 * @param body
	 */
	private static void drawMergedCellBorder(MiniCell mc, MiniBody body) {
		// System.out.print("R" + mc.getSelf().getRowIndex() + "C" +
		// mc.getSelf().getColumnIndex());
		CellRangeAddress range = getMergedRangeByAllCell(mc.getSelf());
		String firstCellKey = mc.getSelf().getSheet().getSheetName() + "R" + range.getFirstRow() + "C"
				+ range.getFirstColumn();
		// System.out.print(" F:" + "R" + range.getFirstRow() + "C" +
		// range.getFirstColumn());
		MiniCell firstCell = body.getCells().get(firstCellKey);
		List<CellRangeAddress> list = mc.getSelf().getSheet().getMergedRegions();
		boolean isLeftCell = isLeftCellInMergedRange(mc.getSelf(), list);
		if (isLeftCell && mc.getSelf().getCellStyle().getBorderLeftEnum().getCode() > 0) {
			// first mc add this class
			String style = "BL" + getCssBorder(mc.getSelf().getCellStyle().getBorderLeftEnum());
			if (!firstCell.getClasses().contains(style)) {
				firstCell.getClasses().add(style);
				// System.out.print(style);
			}
		}
		boolean isBottomCell = isBottomCellInMergedRange(mc.getSelf(), list);
		if (isBottomCell && mc.getSelf().getCellStyle().getBorderBottomEnum().getCode() > 0) {
			// first mc add this class
			String style = "BB" + getCssBorder(mc.getSelf().getCellStyle().getBorderBottomEnum());
			if (!firstCell.getClasses().contains(style)) {
				firstCell.getClasses().add(style);
				// System.out.print(style);
			}
		}
		boolean isTopCell = isTopCellInMergedRange(mc.getSelf(), list);
		if (isTopCell && mc.getSelf().getCellStyle().getBorderTopEnum().getCode() > 0) {
			// first mc add this class
			String style = "BT" + getCssBorder(mc.getSelf().getCellStyle().getBorderTopEnum());
			if (!firstCell.getClasses().contains(style)) {
				firstCell.getClasses().add(style);
				// System.out.print(style);
			}
		}
		boolean isRightCell = isRightCellInMergedRange(mc.getSelf(), list);
		if (isRightCell && mc.getSelf().getCellStyle().getBorderRightEnum().getCode() > 0) {
			// first mc add this class
			String style = "BR" + getCssBorder(mc.getSelf().getCellStyle().getBorderRightEnum());
			if (!firstCell.getClasses().contains(style)) {
				firstCell.getClasses().add(style);
				// System.out.print(style);
			}
		}
		// System.out.print("\t");
	}

	/**
	 * @param cell
	 * @param list
	 * @return
	 */
	private static boolean isRightCellInMergedRange(XSSFCell cell, List<CellRangeAddress> list) {
		for (CellRangeAddress range : list) {
			if (range.getLastColumn() == cell.getColumnIndex()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param cell
	 * @param list
	 * @return
	 */
	private static boolean isBottomCellInMergedRange(XSSFCell cell, List<CellRangeAddress> list) {
		for (CellRangeAddress range : list) {
			if (range.getLastRow() == cell.getRowIndex()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param cell
	 * @param list
	 * @return
	 */
	private static boolean isTopCellInMergedRange(XSSFCell cell, List<CellRangeAddress> list) {
		for (CellRangeAddress range : list) {
			if (range.getFirstRow() == cell.getRowIndex()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param cell
	 * @param list
	 * @return
	 */
	private static boolean isLeftCellInMergedRange(XSSFCell cell, List<CellRangeAddress> list) {
		for (CellRangeAddress range : list) {
			if (range.getFirstColumn() == cell.getColumnIndex()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param cell
	 * @return
	 */
	private static boolean isInMergedRange(XSSFCell cell) {
		List<CellRangeAddress> list = cell.getSheet().getMergedRegions();
		for (CellRangeAddress range : list) {
			if (range.containsColumn(cell.getColumnIndex()) && range.containsRow(cell.getRowIndex())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param mc
	 * @param cell
	 * @param body
	 */

	private static void drawCellContent(MiniCell mc, MiniBody body) {

		mc.getClasses().add("R");
		mc.getClasses().add("C");
		mc.getClasses().add("R" + mc.getSelf().getRowIndex());
		mc.getClasses().add("C" + mc.getSelf().getColumnIndex());

		CellRangeAddress range = getMergedRangeByAllCell(mc.getSelf());
		if (range != null) {
			XSSFCell firstCell = mc.getSelf().getSheet().getRow(range.getFirstRow()).getCell(range.getFirstColumn());
			if (mc.getSelf().getRowIndex() == firstCell.getRowIndex()
					&& mc.getSelf().getColumnIndex() == firstCell.getColumnIndex()) {
				drawFirstCellContent(mc, firstCell, body);
			}
		} else {
			drawFirstCellContent(mc, mc.getSelf(), body);
		}
	}

	@SuppressWarnings("deprecation")
	private static void drawFirstCellContent(MiniCell mc, XSSFCell cell, MiniBody body) {

		XSSFComment comment = cell.getCellComment();
		XSSFColor bgColor = cell.getCellStyle().getFillForegroundColorColor();

		if (cell.toString() == "" && comment == null && bgColor == null) {
			return;
		}

		mc.setInner(getMiniInner(cell, body));
		switch (cell.getCellType()) {
		case HSSFCell.CELL_TYPE_FORMULA:
			mc.setContent(getControlByCell(cell, mc));
			break;
		default:
			cell.setCellType(Cell.CELL_TYPE_STRING);
			mc.setContent("<pre>" + cell.getStringCellValue() + "</pre>");
			break;
		}
	}

	/**
	 * @param mc
	 * @param cell
	 * @return
	 */
	private static MiniInner getMiniInner(XSSFCell cell, MiniBody body) {
		MiniInner inner = new MiniInner();
		inner.setClasses(new ArrayList<String>());
		inner.getClasses().add("I");
		inner.getClasses().add("R" + cell.getRowIndex());
		inner.getClasses().add("C" + cell.getColumnIndex());
		if (cell.getCellStyle().getFont().getBold()) {
			inner.getClasses().add("IB");
		}
		XSSFComment comment = cell.getCellComment();
		XSSFColor bgColor = cell.getCellStyle().getFillForegroundColorColor();
		XSSFCellStyle style = cell.getCellStyle();
		XSSFFont xssfont = style.getFont();
		XSSFColor colour = xssfont.getXSSFColor();

		if (comment != null) {
			RichTextString richString = comment.getString();
			inner.getClasses().add(richString.toString().trim().replace("\r", "").replace("\n", ""));
		} else {
			if (bgColor != null) {
				String className = getCellClassName(cell);
				String classValue = "background-color:" + getBgARGBHex(cell, bgColor);
				String key = getCssKeyByValue(body.getCss(), classValue);
				if (key != "") {
					inner.getClasses().add(key);
				} else {
					inner.getClasses().add(className);
					body.getCss().put(className, classValue);
				}
			}

			if (colour != null) {
				String className = getCellClassName(cell);
				String classValue = "color:" + getFontARGBHex(cell, colour);
				String key = getCssKeyByValue(body.getCss(), classValue);
				if (key != "") {
					inner.getClasses().add(key);
				} else {
					inner.getClasses().add(className);
					body.getCss().put(className, classValue);
				}
			}
		}

		int pt = cell.getCellStyle().getFont().getFontHeightInPoints();
		inner.getClasses().add("F" + (pt * 3 / 4 + 3));
		CellRangeAddress range = getMergedRangeByFirstCell(cell);
		if (range == null) {
			return inner;
		}
		inner.getClasses().add("TA" + style.getAlignmentEnum().getCode());
		inner.getClasses().add("VA" + style.getVerticalAlignmentEnum().getCode());
		int width = (int) ((range.getLastColumn() - range.getFirstColumn() + 1) * COL_WIDTH);
		int height = (int) ((range.getLastRow() - range.getFirstRow() + 1) * ROW_HEIGHT);
		inner.getClasses().add("W" + width);
		inner.getClasses().add("H" + height);
		return inner;
	}

	/**
	 * @param cell
	 * @param mc 
	 * @return
	 */
	private static String getControlByCell(XSSFCell cell, MiniCell mc) {
		CellReference ref = new CellReference(cell.getCellFormula());
		XSSFRow row = cell.getSheet().getRow(ref.getRow());
		HashMap<String, String> properties = new HashMap<String, String>();
		properties.put("id", row.getCell(24).toString());
		properties.put("name", row.getCell(25).toString());
		properties.put("type", row.getCell(26).toString());
		properties.put("value", row.getCell(27).toString());
		properties.put("class", row.getCell(28).toString());
		properties.put("onclick", row.getCell(29).toString());
		properties.put("mode", row.getCell(30).toString());
		if (properties.get("type").equals("label")) {
			return properties.get("value");
		}else if(properties.get("type").equals("tag")){
			String tagName = properties.get("name");
			String tag = "<%s %s ></%s>";
			String html = String.format(tag, tagName, properties.get("value"), tagName);
			return html;
		}
		if (properties.get("mode") != "") {
			String div = "<div hide={ mode=='" + properties.get("mode") + "' }>" + properties.get("value") + "</div>"
					+ "<div show={ mode=='" + properties.get("mode") + "' }>%s</div>";
			return String.format(div, getControlHtml(properties));
		}
		
		return getControlHtml(properties);
	}

	/**
	 * @param properties
	 * @return
	 */
	private static String getControlHtml(HashMap<String, String> properties) {
		StringBuilder sb = new StringBuilder();
		sb.append("<input ");
		for (String key : properties.keySet()) {
			String property = properties.get(key);
			if (property != "") {
				sb.append(key).append("=\"").append(property).append("\" ");
			}
		}
		return sb.append(" />").toString();
	}

	/**
	 * @param needCss
	 * @param classValue
	 * @return
	 */
	private static String getCssKeyByValue(HashMap<String, String> needCss, String classValue) {
		for (String key : needCss.keySet()) {
			if (needCss.get(key).equals(classValue)) {
				return key;
			}
		}
		return "";
	}

	/**
	 * @param cell
	 * @return
	 */
	private static String getCellClassName(XSSFCell cell) {
		return "S" + getSheetIndex(cell.getSheet()) + "R" + cell.getRowIndex() + "C" + cell.getColumnIndex();
	}

	/**
	 * @param xssfSheet
	 * @return
	 */
	private static int getSheetIndex(XSSFSheet xssfSheet) {
		XSSFWorkbook book = xssfSheet.getWorkbook();
		int cntSheet = book.getNumberOfSheets();
		for (int index = 0; index < cntSheet; index++) {
			XSSFSheet sheet = book.getSheetAt(index);
			if (sheet.getSheetName().equals(xssfSheet.getSheetName())) {
				return index;
			}
		}
		return 0;
	}

	/**
	 * @param cell
	 * @param colour
	 * @return
	 */
	private static String getARGBHexByComment(XSSFCell cell, XSSFColor colour) {
		String argb = "#" + colour.getARGBHex().substring(2);
		return argb;
	}

	/**
	 * @param cell
	 * @param colour
	 * @return
	 */
	private static String getFontARGBHex(XSSFCell cell, XSSFColor colour) {
		String argb = getARGBHexByComment(cell, colour);
		return argb;
	}

	/**
	 * @param cell
	 * @param colour
	 * @return
	 */
	private static String getBgARGBHex(XSSFCell cell, XSSFColor colour) {
		String argb = getARGBHexByComment(cell, colour);
		return argb;
	}

	/**
	 * @param cell
	 * @return
	 */
	private static CellRangeAddress getMergedRangeByAllCell(XSSFCell cell) {
		List<CellRangeAddress> list = cell.getSheet().getMergedRegions();
		for (CellRangeAddress range : list) {
			if (range.containsRow(cell.getRowIndex()) && range.containsColumn(cell.getColumnIndex())) {
				return range;
			}
		}
		return null;
	}

	/**
	 * @param cell
	 * @return
	 */
	private static CellRangeAddress getMergedRangeByFirstCell(XSSFCell cell) {
		List<CellRangeAddress> list = cell.getSheet().getMergedRegions();
		for (CellRangeAddress range : list) {

			if (range.getFirstRow() == cell.getRowIndex() && range.getFirstColumn() == cell.getColumnIndex()) {
				return range;
			}
		}
		return null;
	}

	/**
	 * @param borderStyle
	 * @return
	 */
	private static String getCssBorder(BorderStyle borderStyle) {
		if (borderStyle == BorderStyle.THIN) {
			return "3";
		} else if (borderStyle == BorderStyle.MEDIUM) {
			return "3";
		} else if (borderStyle == BorderStyle.DASHED) {
			return "1";
		} else if (borderStyle == BorderStyle.DOTTED) {
			return "1";
		} else if (borderStyle == BorderStyle.THICK) {
			return "1";
		} else if (borderStyle == BorderStyle.DOUBLE) {
			return "4";
		} else if (borderStyle == BorderStyle.HAIR) {
			return "1";
		} else if (borderStyle == BorderStyle.MEDIUM_DASHED) {
			return "2";
		} else if (borderStyle == BorderStyle.DASH_DOT) {
			return "1";
		} else if (borderStyle == BorderStyle.MEDIUM_DASH_DOT) {
			return "2";
		} else if (borderStyle == BorderStyle.DASH_DOT_DOT) {
			return "1";
		} else if (borderStyle == BorderStyle.MEDIUM_DASH_DOT_DOT) {
			return "2";
		} else if (borderStyle == BorderStyle.SLANTED_DASH_DOT) {
			return "2";
		}
		return "";
	}
}
