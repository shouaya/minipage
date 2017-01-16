package com.jialu.minipage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.common.io.Resources;

public class MiniPage {

	private static final int MAX_ROW_CNT = 25;
	private static final int MAX_COL_CNT = 23;
	private static final double ROW_HEIGHT = 26.68;
	private static final double COL_WIDTH = 16.3;

	// TODO 不同device对应的css、html控件支持
	public static void main(String[] args) throws IOException {
		if (args.length != 3 && args.length != 4) {
			System.out.println("Parameters Size Error");
			throw new RuntimeException();
		}
		System.setProperty("webdriver.chrome.driver", args[1]);
		XSSFWorkbook book = readDesignFile(args[0]);
		String indexPath = "";
		if (args.length == 4) {
			indexPath = createHtmlFile(book.getSheet(args[3]));
		} else {
			indexPath = createHtmlFile(book);
		}
		System.out.println("HTMLを生成しました。");
		previewHtmlFile(indexPath, args[2]);
	}

	private static String createHtmlFile(XSSFSheet sheet) throws IOException {
		StringBuilder sb = new StringBuilder();
		String headContent = Resources.toString(Resources.getResource("header.html"),
				Charset.forName(CharEncoding.UTF_8));
		sb.append(headContent);
		String bodyContent = createBodyContent(sheet);
		sb.append(bodyContent);
		String footContent = Resources.toString(Resources.getResource("footer.html"),
				Charset.forName(CharEncoding.UTF_8));
		sb.append(footContent);
		File file = new File(String.format("out/%s.html", sheet.getSheetName()));
		FileUtils.writeStringToFile(file, sb.toString(), Charset.forName(CharEncoding.UTF_8));
		return file.getCanonicalPath();
	}

	private static void previewHtmlFile(String path, String device) {
		Map<String, String> mobileEmulation = new HashMap<String, String>();
		mobileEmulation.put("deviceName", device);
		Map<String, Object> chromeOptions = new HashMap<String, Object>();
		chromeOptions.put("mobileEmulation", mobileEmulation);
		DesiredCapabilities capabilities = DesiredCapabilities.chrome();
		capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
		WebDriver driver = new ChromeDriver(capabilities);
		driver.get(path);
	}

	private static String createHtmlFile(XSSFWorkbook book) throws IOException {
		int cntSheet = book.getNumberOfSheets();
		String indexPath = "";
		for (int index = 0; index < cntSheet; index++) {
			String path = createHtmlFile(book.getSheetAt(index));
			if (index == 0) {
				indexPath = path;
			}
		}
		return indexPath;
	}

	private static String createBodyContent(XSSFSheet xssfSheet) throws IOException {
		StringBuilder sb = new StringBuilder();
		String action = xssfSheet.getRow(1).getCell(25).toString();
		String method = xssfSheet.getRow(2).getCell(25).toString();
		if(action != "" && method != ""){
			sb.append("<form action='" + action + "' method='" + method + "' >");
		}
		for (int row = 0; row < MAX_ROW_CNT; row++) {
			for (int col = 0; col < MAX_COL_CNT; col++) {
				XSSFCell right = col < MAX_COL_CNT - 1 ? xssfSheet.getRow(row).getCell(col + 1) : null;
				XSSFCell bottom = row < MAX_ROW_CNT - 1 ? xssfSheet.getRow(row + 1).getCell(col) : null;
				MiniCell cell = converCell(xssfSheet.getRow(row).getCell(col), right, bottom,
						xssfSheet.getMergedRegions());
				sb.append(cell.getHtml());
			}
		}
		if(action != "" && method != ""){
			sb.append("</form>");
		}
		return sb.toString();
	}

	private static MiniCell converCell(XSSFCell cell, XSSFCell right, XSSFCell bottom, List<CellRangeAddress> list) {
		MiniCell mc = new MiniCell();

		if (cell == null) {
			return mc;
		}
		drawCellBorder(mc, cell, right, bottom, list);

		XSSFColor bgColor = cell.getCellStyle().getFillForegroundColorColor();
		if (cell.toString() == "" && mc.getClasses().size() == 0 && bgColor == null) {
			return mc;
		}

		drawCellContent(mc, cell, list);

		return mc;
	}

	@SuppressWarnings("deprecation")
	private static void drawCellContent(MiniCell mc, XSSFCell cell, List<CellRangeAddress> list) {
		mc.getClasses().add("R");
		mc.getClasses().add("C");
		mc.getClasses().add("R" + cell.getRowIndex());
		mc.getClasses().add("C" + cell.getColumnIndex());
		XSSFColor bgColor = cell.getCellStyle().getFillForegroundColorColor();
		if (bgColor != null) {
			String cssColor = bgColor.getARGBHex().substring(2);
			mc.getStyles().add("background-color:#" + cssColor);
		}

		if (cell.toString() == "") {
			mc.creatHtml();
			return;
		}
		mc.setFont(getMiniFont(cell, list));
		switch (cell.getCellType()) {
		case HSSFCell.CELL_TYPE_FORMULA:
			mc.setContent(getControlByCell(cell));
			break;
		default:
			cell.setCellType(Cell.CELL_TYPE_STRING);
			mc.setContent(cell.getStringCellValue());
			break;
		}
		mc.creatHtml();
	}

	private static String getControlByCell(XSSFCell cell) {
		CellReference ref = new CellReference(cell.getCellFormula());
		XSSFRow row = cell.getSheet().getRow(ref.getRow());
		String inId = row.getCell(24).toString();
		String inName = row.getCell(25).toString();
		String inType = row.getCell(26).toString();
		String inValue = row.getCell(27).toString();
		String inClass = row.getCell(28).toString();
		return String.format("<input id=\"%s\"  name=\"%s\"  type=\"%s\"  value=\"%s\" class=\"%s\"/>", inId, inName,
				inType, inValue, inClass);
	}

	private static void drawCellBorder(MiniCell mc, XSSFCell cell, XSSFCell right, XSSFCell bottom,
			List<CellRangeAddress> list) {
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

	private static boolean isRightCellInMergedRange(XSSFCell cell, List<CellRangeAddress> list) {
		for (CellRangeAddress range : list) {
			if (range.getLastColumn() == cell.getColumnIndex()) {
				return true;
			}
		}
		return false;
	}

	private static boolean isBottomCellInMergedRange(XSSFCell cell, List<CellRangeAddress> list) {
		for (CellRangeAddress range : list) {
			if (range.getLastRow() == cell.getRowIndex()) {
				return true;
			}
		}
		return false;
	}

	private static boolean isTopCellInMergedRange(XSSFCell cell, List<CellRangeAddress> list) {
		for (CellRangeAddress range : list) {
			if (range.getFirstRow() == cell.getRowIndex()) {
				return true;
			}
		}
		return false;
	}

	private static boolean isLeftCellInMergedRange(XSSFCell cell, List<CellRangeAddress> list) {
		for (CellRangeAddress range : list) {
			if (range.getFirstColumn() == cell.getColumnIndex()) {
				return true;
			}
		}
		return false;
	}

	private static boolean isInMergedRange(XSSFCell cell, List<CellRangeAddress> list) {
		for (CellRangeAddress range : list) {
			if (range.containsColumn(cell.getColumnIndex()) && range.containsRow(cell.getRowIndex())) {
				return true;
			}
		}
		return false;
	}

	private static MiniFont getMiniFont(XSSFCell cell, List<CellRangeAddress> list) {
		MiniFont font = new MiniFont();
		font.setClasses(new ArrayList<String>());
		font.setStyles(new ArrayList<String>());
		font.getClasses().add("I");
		font.getClasses().add("R" + cell.getRowIndex());
		font.getClasses().add("C" + cell.getColumnIndex());
		if (cell.getCellStyle().getFont().getBold()) {
			font.getClasses().add("IB");
		}
		XSSFCellStyle style = cell.getCellStyle();
		XSSFFont xssfont = style.getFont();
		XSSFColor colour = xssfont.getXSSFColor();
		if (colour != null) {
			font.getStyles().add("color:#" + colour.getARGBHex().substring(2));
		}
		int pt = cell.getCellStyle().getFont().getFontHeightInPoints();
		font.getStyles().add("font-size: " + (pt * 3 / 4 + 3) + "px");
		CellRangeAddress range = getMergedRange(cell, list);
		if (range == null) {
			return font;
		}
		font.getClasses().add("TA" + style.getAlignmentEnum().getCode());
		font.getClasses().add("VA" + style.getVerticalAlignmentEnum().getCode());
		int width = (int) ((range.getLastColumn() - range.getFirstColumn() + 1) * COL_WIDTH);
		int height = (int) ((range.getLastRow() - range.getFirstRow() + 1) * ROW_HEIGHT);
		font.getStyles().add("width: " + width + "px");
		font.getStyles().add("height: " + height + "px");
		return font;
	}

	private static CellRangeAddress getMergedRange(XSSFCell cell, List<CellRangeAddress> list) {
		for (CellRangeAddress range : list) {
			if (range.getFirstRow() == cell.getRowIndex() && range.getFirstColumn() == cell.getColumnIndex()) {
				return range;
			}
		}
		return null;
	}

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
}
