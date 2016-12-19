package com.jialu.minipage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class MiniPage {

	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			System.out.println("Parameters Size Error");
			throw new RuntimeException();
		}
		PageDesignObject obj = readDesignFile(args[2]);
		Boolean suucess = executeDesignAll(args[0], obj);
		if (suucess) {
			System.exit(0);
		} else {
			System.exit(1);
		}
	}

	private static Boolean executeDesignAll(String string, PageDesignObject obj) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	private static PageDesignObject readDesignFile(String file) throws IOException {
		FileInputStream execlFileStream = null;
		PageDesignObject object = new PageDesignObject();

		try {
			execlFileStream = new FileInputStream(new File(file));
			HSSFWorkbook workbook = new HSSFWorkbook(execlFileStream);
			// object.setSteps(createTestSteps(workbook.getSheetAt(0)));
			// object.setDatas(createTestDatas(workbook.getSheetAt(1)));
		} catch (IOException e) {
			System.out.println("Can Not Read File:" + e.getMessage());
			throw e;
		} finally {
			execlFileStream.close();
		}
		return object;
	}
}
