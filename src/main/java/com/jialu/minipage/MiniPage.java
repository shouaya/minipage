package com.jialu.minipage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.google.common.io.Resources;
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
		XSSFWorkbook book = readDesignFile(args[0]);
		createHtmlFile(book);
		System.out.println("HTMLを生成しました。");
	}
	
	/**
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static XSSFWorkbook readDesignFile(String file) throws IOException {
		FileInputStream execlFileStream = null;
		XSSFWorkbook book = null;

		try {
			execlFileStream = new FileInputStream(new File(file));
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
	 * @param book
	 * @return
	 * @throws IOException
	 */
	private static String createHtmlFile(XSSFWorkbook book) throws IOException {
		int cntSheet = book.getNumberOfSheets();
		String indexPath = "";
		HashMap<String, String> needAddCss = new HashMap<String, String>();
		for (int index = 0; index < cntSheet; index++) {
			XSSFSheet sheet = book.getSheetAt(index);
			if (sheet.getSheetName().startsWith("_")) {
				continue;
			}
			MiniBody body = createHtmlFile(sheet);
			needAddCss.putAll(body.getCss());
		}
		createCssFile(book.getSheet("_css"), needAddCss);
		return indexPath;
	}
	
	/**
	 * @param sheet
	 * @param needAddCss
	 * @throws IOException
	 */
	private static void createCssFile(XSSFSheet sheet, HashMap<String, String> needAddCss) throws IOException {
		StringBuilder sb = new StringBuilder();
		String cssContent = createCssContent(sheet, needAddCss);
		sb.append(cssContent).append("\r\n");
		try (StringReader reader = new StringReader(sb.toString());
				FileWriter fw = new FileWriter(String.format("out/app.css", sheet.getSheetName()));) {
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
	private static MiniBody createHtmlFile(XSSFSheet sheet) throws IOException {
		MiniBody body = createBodyContent(sheet);
		File file = new File(String.format("out/%s.html", sheet.getSheetName()));
		FileUtils.writeStringToFile(file, body.getHtml(), Charset.forName(CharEncoding.UTF_8));
		return body;
	}
	
	/**
	 * @param xssfSheet
	 * @return
	 * @throws IOException
	 */
	private static MiniBody createBodyContent(XSSFSheet xssfSheet) throws IOException {
		MiniBody body = new MiniBody();
		body.setCss(new HashMap<String, String>());
		StringBuilder sb = new StringBuilder();
		sb.append("<").append(xssfSheet.getSheetName()).append(">\r\n");
		for (int row = 0; row < MAX_ROW_CNT; row++) {
			for (int col = 0; col < MAX_COL_CNT; col++) {
				XSSFCell right = col < MAX_COL_CNT - 1 ? xssfSheet.getRow(row).getCell(col + 1) : null;
				XSSFCell bottom = row < MAX_ROW_CNT - 1 ? xssfSheet.getRow(row + 1).getCell(col) : null;
				XSSFCell self = xssfSheet.getRow(row).getCell(col);
				MiniCell cell = createMiniCell(body, self, right, bottom);
				String html = cell.getHtml();
				sb.append(html);
				if (html.length() > 0) {
					sb.append("\r\n");
				}
			}
		}
		String scriptPath = "javascript/" + xssfSheet.getSheetName() + ".js";
		URL p = MiniPage.class.getClassLoader().getResource(scriptPath);
		if (p != null) {
			String script = Resources.toString(Resources.getResource(scriptPath), Charset.forName(CharEncoding.UTF_8));
			sb.append("\r\n<script>\r\n").append(script).append("\r\n</script>\r\n");
		}
		sb.append("</").append(xssfSheet.getSheetName()).append(">");
		body.setHtml(sb.toString());
		return body;
	}
	
	/**
	 * @param body
	 * @param self
	 * @param right
	 * @param bottom
	 * @return
	 */
	private static MiniCell createMiniCell(MiniBody body, XSSFCell self, XSSFCell right, XSSFCell bottom) {
		MiniCell mc = new MiniCell();
		if (self == null) {
			return mc;
		}
		drawCellBorder(mc, self, right, bottom);

		XSSFColor bgColor = self.getCellStyle().getFillForegroundColorColor();
		if (self.toString() == "" && mc.getClasses().size() == 0 && bgColor == null) {
			return mc;
		}

		drawCellContent(mc, self, body);

		return mc;
	}
	
	/**
	 * @param mc
	 * @param cell
	 * @param right
	 * @param bottom
	 */
	private static void drawCellBorder(MiniCell mc, XSSFCell cell, XSSFCell right, XSSFCell bottom) {
		//TODO fix bug
		List<CellRangeAddress> list = cell.getSheet().getMergedRegions();
		if (isInMergedRange(cell, list)) {
			drawMergedCellBorder(mc, cell, list, right, bottom);
			return;
		}
		if (cell.getCellStyle().getBorderLeftEnum().getCode() > 0) {
			mc.getClasses().add("BL" + getCssBorder(cell.getCellStyle().getBorderLeftEnum()));
		}
		if (cell.getCellStyle().getBorderRightEnum().getCode() > 0) {
			if (right == null || right.getCellStyle().getBorderLeftEnum().getCode() <= 0) {
				mc.getClasses().add("BR" + getCssBorder(cell.getCellStyle().getBorderRightEnum()));
			}
		}
		if (cell.getCellStyle().getBorderTopEnum().getCode() > 0) {
			mc.getClasses().add("BT" + getCssBorder(cell.getCellStyle().getBorderTopEnum()));
		}
		if (cell.getCellStyle().getBorderBottomEnum().getCode() > 0) {
			if (bottom == null || bottom.getCellStyle().getBorderTopEnum().getCode() <= 0) {
				mc.getClasses().add("BB" + getCssBorder(cell.getCellStyle().getBorderBottomEnum()));
			}
		}
	}

	/**
	 * @param mc
	 * @param cell
	 * @param list
	 * @param right
	 * @param bottom
	 */
	private static void drawMergedCellBorder(MiniCell mc, XSSFCell cell, List<CellRangeAddress> list, XSSFCell right,
			XSSFCell bottom) {
		boolean isLeftCell = isLeftCellInMergedRange(cell, list);
		if (isLeftCell && cell.getCellStyle().getBorderLeftEnum().getCode() > 0) {
			mc.getClasses().add("BL" + getCssBorder(cell.getCellStyle().getBorderLeftEnum()));
		}
		boolean isBottomCell = isBottomCellInMergedRange(cell, list);
		if (isBottomCell && cell.getCellStyle().getBorderBottomEnum().getCode() > 0) {
			if (bottom == null || bottom.getCellStyle().getBorderTopEnum().getCode() <= 0) {
				mc.getClasses().add("BB" + getCssBorder(cell.getCellStyle().getBorderBottomEnum()));
			}
		}
		boolean isTopCell = isTopCellInMergedRange(cell, list);
		if (isTopCell && cell.getCellStyle().getBorderTopEnum().getCode() > 0) {
			mc.getClasses().add("BT" + getCssBorder(cell.getCellStyle().getBorderTopEnum()));
		}
		boolean isRightCell = isRightCellInMergedRange(cell, list);
		if (isRightCell && cell.getCellStyle().getBorderRightEnum().getCode() > 0) {
			if (right == null || right.getCellStyle().getBorderLeftEnum().getCode() <= 0) {
				mc.getClasses().add("BR" + getCssBorder(cell.getCellStyle().getBorderRightEnum()));
			}
		}
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
	 * @param list
	 * @return
	 */
	private static boolean isInMergedRange(XSSFCell cell, List<CellRangeAddress> list) {
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

	private static void drawCellContent(MiniCell mc, XSSFCell cell, MiniBody body) {
		
		mc.getClasses().add("R");
		mc.getClasses().add("C");
		mc.getClasses().add("R" + cell.getRowIndex());
		mc.getClasses().add("C" + cell.getColumnIndex());
		
		CellRangeAddress range = getMergedRangeByAllCell(cell);
		if (range != null) {
			XSSFCell firstCell = cell.getSheet().getRow(range.getFirstRow()).getCell(range.getFirstColumn());
			if (cell.getRowIndex() == firstCell.getRowIndex() && cell.getColumnIndex() == firstCell.getColumnIndex()) {
				drawFirstCellContent(mc, firstCell, body);
			}
		} else {
			drawFirstCellContent(mc, cell, body);
		}
	}

	@SuppressWarnings("deprecation")
	private static void drawFirstCellContent(MiniCell mc, XSSFCell cell, MiniBody body) {

		XSSFComment comment = cell.getCellComment();
		XSSFColor bgColor = cell.getCellStyle().getFillForegroundColorColor();

		if (cell.toString() == "" && comment == null && bgColor == null) {
			mc.creatHtml();
			return;
		}

		mc.setInner(getMiniInner(cell, body));
		switch (cell.getCellType()) {
		case HSSFCell.CELL_TYPE_FORMULA:
			mc.setContent(getControlByCell(cell));
			break;
		default:
			cell.setCellType(Cell.CELL_TYPE_STRING);
			mc.setContent("<pre>" + cell.getStringCellValue() + "</pre>");
			break;
		}
		mc.creatHtml();
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
	 * @return
	 */
	private static String getControlByCell(XSSFCell cell) {
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
