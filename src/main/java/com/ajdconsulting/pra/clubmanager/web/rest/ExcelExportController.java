package com.ajdconsulting.pra.clubmanager.web.rest;

import com.ajdconsulting.pra.clubmanager.data.export.excel.ExcelHttpOutputStream;
import com.ajdconsulting.pra.clubmanager.data.export.excel.ExcelSqlReport;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@Controller
public class ExcelExportController {

    private static final String DEFAULT_FILE_NAME = "signups.xlsx";

    @RequestMapping("/exportSignups")
    public void exportSignups(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {

        // 1. Fetch your data
        // 2. Create your excel
        // 3. write excel file to your response.
        String query = (
                "select " +
                "concat(w.first_name,' ', w.last_name) name, j.title, j.point_value, j.cash_value, j.reserved, j.job_day, wl.last_name leader, sd.date " +
                "from  " +
                "job j " +
                "inner join schedule_date sd on sd.event_type_id = j.event_type_id " +
                "left join signup s on s.job_id = j.id " +
                "left join member w on w.id = s.worker_id " +
                "left join member  wl on wl.id = j.work_leader_id " +
                "where sd.date <= (select max(date) from schedule_date where date > now() and week(date)-4 <= week(now()))" +
                "order by date, title"
        );

        String[] headerColumns = {"Name", "Job", "Point Value", "Cash Value", "Job Day", "Work Leader", "Job Date"};
        String[] formattingColumns = {"reserved"};
        ExcelSqlReport signupReport = new ExcelSqlReport(query, "Signup", headerColumns, formattingColumns);
        signupReport.write(ExcelHttpOutputStream.getOutputStream(response, DEFAULT_FILE_NAME));
    }

    @RequestMapping("/exportMeetingSignin")
    public void exportMeetingSignIn(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        String query =
            "select concat(last_name, ', ', first_name) name, current_year_points, '' signature from member order by last_name";

        String[] headerColumns = {"Name", "Points", "Signature"};
        int[] columnWidths = {20, 10, 65};
        ExcelSqlReport report = new ExcelSqlReport(query, "meetingSignIn", headerColumns, columnWidths);
        report.write(ExcelHttpOutputStream.getOutputStream(response, "meetingSignIn.xlsx"));
    }

}
