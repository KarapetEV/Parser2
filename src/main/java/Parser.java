import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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

    private static void output(String projectNum, String stage) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("c:\\lessons\\4.txt", true));
        writer.write(projectNum + "\t" + stage);
        writer.newLine();
        writer.close();
    }

    private static void parseStages() throws Exception {
        for (String url: projectLinks) {
            Document page = Jsoup.parse(new URL(url), 5000);
            Elements tr = page.select("tr[class=tr-hr-dashed]");
            String etap = "";
            String result = "";
            for (Element el : tr) {
                etap = el.select("td").first().text();
                Elements aElements = el.select("a[class=panel-some-doc preview]");
                String span = el.getElementsByTag("span").first().text().substring(2);
                if (aElements.size() == 0 && !span.equals("Этап в работе")) {
                    result = result + " " + etap;
                }
            }
            if (!result.equals("")) {
                output(projectNum(page), result.trim());
            }
        }
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

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        Document page = getPage(baseUrl);
        String url = "";
        Element pagination = page.select("div[class=pagination]").first();
        int num = getNumberOfPages(pagination);
        for (int i = 1; i <= num; i++) {
            url = baseUrl + i;
            page = getPage(url);
            getLinks(page);
            parseStages();
            projectLinks.clear();
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