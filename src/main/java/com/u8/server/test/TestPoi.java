package com.u8.server.test;

import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TestPoi {

    public static void main(String[] args) {
        String fileName = "C:\\Users\\123\\Desktop\\temp\\每日订单报表模板.xlsx";
        try {
            FileInputStream fis = new FileInputStream(fileName);
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            XSSFSheet sheet = workbook.getSheetAt(0);
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String today = sdf.format(date);
            XSSFRow row = sheet.getRow(0);
            XSSFCell cell = row.getCell(0);
            cell.setCellValue(today+"日订单报表");
            cell.getCellStyle().setAlignment(XSSFCellStyle.ALIGN_CENTER_SELECTION);
            row = sheet.getRow(1);
            List<XSSFCellStyle> cellStyleList = new ArrayList<>();
            List<XSSFColor> colorList = new ArrayList<>();
            for(int i = 0;i < 10;i++) {
                cell = row.getCell(i);
                XSSFCellStyle cellStyle = cell.getCellStyle();
                cellStyleList.add(cellStyle);
                XSSFColor color = cellStyle.getFillForegroundXSSFColor();
                colorList.add(color);
            }

            for(int i = 2;i < 102;i++) {
                row = sheet.createRow(i);
                for (int j = 0;j < 10;j++) {
                    cell = row.createCell(j);
                    XSSFCellStyle style = cellStyleList.get(j);
                    XSSFCellStyle cellStyle = workbook.createCellStyle();
                    cellStyle.setBorderBottom( style.getBorderBottom());
                    cellStyle.setBorderLeft(style.getBorderLeft());
                    cellStyle.setBorderTop(style.getBorderTop());
                    cellStyle.setBorderRight(style.getBorderRight());
                    cell.setCellStyle(cellStyle);
                    cellStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
                    cell.setCellValue("测试数据");
                }
            }

            FileOutputStream fos = new FileOutputStream(new File("C:\\Users\\123\\Desktop\\temp\\报表.xlsx"));
            fis.close();
            workbook.write(fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


