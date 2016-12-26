package com.jialu.minipage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.common.io.Resources;

public class MiniPage {

	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			System.out.println("Parameters Size Error");
			throw new RuntimeException();
		}
		PageDesignObject obj = readDesignFile(args[0]);
		System.setProperty("webdriver.chrome.driver", args[1]);
		String path = createHtmlFile(obj);
		previewHtmlFile(path, args[2]);
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

	private static String createHtmlFile(PageDesignObject obj) throws IOException {
		int cntSheet = obj.getWorkbook().getNumberOfSheets();
		String indexPath = "";
		for (int index = 0; index < cntSheet; index++) {
			StringBuilder sb = new StringBuilder();
			String headContent = Resources.toString(Resources.getResource("header.html"),
					Charset.forName(CharEncoding.UTF_8));
			sb.append(headContent);
			String bodyContent = createBodyContent(obj.getWorkbook().getSheetAt(index));
			sb.append(bodyContent);
			String footContent = Resources.toString(Resources.getResource("footer.html"),
					Charset.forName(CharEncoding.UTF_8));
			sb.append(footContent);
			File file = null;
			if (index == 0) {
				file = new File("out/index.html");
			} else {
				file = new File(String.format("out/%s.html", obj.getWorkbook().getSheetAt(index).getSheetName()));
			}
			FileUtils.writeStringToFile(file, sb.toString(), Charset.forName(CharEncoding.UTF_8));
			indexPath = file.getCanonicalPath();
		}
		return indexPath;
	}

	private static String createBodyContent(XSSFSheet xssfSheet) throws IOException {
		// TODO convert PageDesignObject to html
		String content = Resources.toString(Resources.getResource("templates/fulltext.html"),
				Charset.forName(CharEncoding.UTF_8));

		return content;
	}

	private static PageDesignObject readDesignFile(String file) throws IOException {
		FileInputStream execlFileStream = null;
		PageDesignObject object = new PageDesignObject();

		try {
			execlFileStream = new FileInputStream(new File(file));
			XSSFWorkbook workbook = new XSSFWorkbook(execlFileStream);
			object.setWorkbook(workbook);
			execlFileStream.close();
		} catch (IOException e) {
			System.out.println("Can Not Read File:" + e.getMessage());
			throw e;
		} finally {
			execlFileStream.close();
		}
		return object;
	}
}
