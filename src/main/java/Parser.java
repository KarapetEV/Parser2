import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    private static String baseUrl = "http://fcpir.ru/participation_in_program/contracts/14.598.11.0098?PAGEN_1=";
    private static List<String> projectLinks = new ArrayList<String>();
    private static List<String> results = new ArrayList<String>();
    private static Workbook wb = new HSSFWorkbook();
    private static CellStyle style = wb.createCellStyle();
    private static Font font = wb.createFont();
    private static int rowNumber = 0;

    private static Document getPage(String url) throws IOException {
        Document page = Jsoup.parse(new URL(url), 3000);
        return page;
    }

    private static String projectNum(Document page) throws Exception {
        String line = page.baseUri();
        Pattern pattern = Pattern.compile("\\d{2}\\.\\d{3}\\.\\d{2}\\.\\d{4}");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group();
        }
        throw new Exception("Can't find the project number");
    }

    private static List<String> parseStages() throws Exception {
        for (String url: projectLinks) {
            Document page = Jsoup.parse(new URL(url), 5000);
            Elements tr = page.select("tr[class=tr-hr-dashed]");
            String etap = "";
            String result = "";
            String outLine = null;
            for (Element el : tr) {
                etap = el.select("td").first().text();
                Elements aElements = el.select("a[class=panel-some-doc preview]");
                String span = el.getElementsByTag("span").first().text().substring(2);
                if (aElements.size() == 0 && !span.equals("Этап в работе")) {
                    result = result + "," + etap;
                }
            }
            if (!result.equals("")) {
                outLine = projectNum(page)+result;
                results.add(outLine);
            }
        }
        return results;
    }

    private static void getLinks(Document page) {
        Element tbody = page.select("tbody").first();
        Elements tr = tbody.select("tr");
        for (Element row : tr) {
            Element td = row.select("td").first();
            String projectPage = "http://fcpir.ru" + td.getElementsByTag("a").attr("href");
            projectLinks.add(projectPage);
        }
    }

    private static int getNumberOfPages(Element pagination) {
        Elements aElements = pagination.getElementsByTag("a");
        int pageNum = Integer.parseInt(aElements.get(aElements.size()-2).text());
        return pageNum;
    }

    private static Workbook output(String[] lines) throws IOException {
        Sheet sheet;
        try {
            sheet = wb.createSheet("Косяки");
        } catch (IllegalArgumentException e) {
            sheet = wb.getSheetAt(0);
        }

        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);


        font.setFontName("Times New Roman");

        style.setFont(font);

        Row row;
        if (rowNumber == 0) {
            row = sheet.createRow(0);
        } else {
            row = sheet.createRow(rowNumber);
        }
        Cell cell;
        for (int i = 0; i < lines.length; i++) {
            cell = row.createCell(i);
            cell.setCellValue(lines[i]);
            cell.setCellStyle(style);
            sheet.autoSizeColumn(i);
        }
        rowNumber++;
        return wb;
    }

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        Document page = getPage(baseUrl);
        String url = "";
        Element pagination = page.select("div[class=pagination]").first();
        int num = getNumberOfPages(pagination);
        for (int i = 9; i <= 10; i++) {
            url = baseUrl + i;
            page = getPage(url);
            getLinks(page);
            parseStages();
            projectLinks.clear();
            for (String line: results) {
                String[] lines = line.split(",");
                output(lines);
            }
            System.out.println(i);
        }
        if (wb.getNumberOfSheets() != 0) {
            FileOutputStream fos = new FileOutputStream("c:\\lessons\\new.xls");
            wb.write(fos);
            fos.close();
            wb.close();
        }
        long stop = System.currentTimeMillis();
        getTime(stop - start);
    }

    private static void getTime(long time) {
        int sec = (int) time/1000;
        int msc = (int) time - sec*1000;
        int min = sec / 60;
        sec = sec - min*60;
        System.out.println("Время выполнения: " + min + " минут " + sec + " секунд " + msc + " миллисекунд");
    }
}