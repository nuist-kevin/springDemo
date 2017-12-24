package com.focus.mic.test.controller.view;

import com.focus.mic.test.entity.User;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/*      根据 model 构建 EXCEL文件
*
* */
public class UserListExcelView extends AbstractXlsxView {
    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setHeader("Content-Disposition", "inline; filename=" +
                new String("用户列表.xlsx".getBytes(), "iso8859-1"));
        List<User> userList = (List<User>) model.get("userList");
        Sheet sheet = workbook.createSheet("users");
        Row headRow = sheet.createRow(0);
        headRow.createCell(0).setCellValue("用户名");
        headRow.createCell(1).setCellValue("年龄");
        headRow.createCell(2).setCellValue("生日");

        int rowNum = 1;
        for (User user : userList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(user.getUsername());
            row.createCell(1).setCellValue(user.getAge());
            row.createCell(2).setCellValue(user.getBirthday().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
    }
}
