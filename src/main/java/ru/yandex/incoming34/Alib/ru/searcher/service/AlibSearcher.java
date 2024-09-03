package ru.yandex.incoming34.Alib.ru.searcher.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service("alibsearcher")
public class AlibSearcher {

    @SneakyThrows
    public void search(String author, String bookName) {
        String windows1251author = java.net.URLEncoder.encode(author, Charset.forName("windows-1251"));
        String windows1251bookName = java.net.URLEncoder.encode(bookName, Charset.forName("windows-1251"));


        String request = "https://www.alib.ru/findp.php4"
                + "?author=" + windows1251author
                + "+&title=" + windows1251bookName
                + "+&seria=+&izdat=+&isbnp=&god1=&god2=&cena1=&cena2=&sod=&bsonly=&gorod=&lday=&minus=+&sumfind=1&tipfind=&sortby="
                + "0&Bo1=%CD%E0%E9%F2%E8";


        Document doc = Jsoup.connect(request).post();
        doc.charset(StandardCharsets.UTF_16);
        //System.out.println(doc);
        Elements elements = doc.getElementsByTag("p");
        //System.out.println(elements);
        List<Element> foundBooks = elements.stream().filter(element -> element.text().contains(author)).collect(Collectors.toList());
        for (Element element : foundBooks) {
            System.out.println(element.text());
        }

// author=%F0%EE%EC%E0%F8%EA%E8%ED+&title=&seria=+&izdat=+&isbnp=&god1=&god2=&cena1=&cena2=&sod=&bsonly=&gorod=&lday=&minus=+&sumfind=1&tipfind=&sortby=0&Bo1=%CD%E0%E9%F2%E8
    }
}
