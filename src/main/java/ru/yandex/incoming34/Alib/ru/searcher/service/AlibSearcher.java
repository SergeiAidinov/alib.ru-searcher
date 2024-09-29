package ru.yandex.incoming34.Alib.ru.searcher.service;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("alibsearcher")
public class AlibSearcher {

    @SneakyThrows
    public void search(final String author, final String bookName) {
        final String windows1251author = Objects.nonNull(author) ?
                java.net.URLEncoder.encode(author, Charset.forName("windows-1251"))
                : "";
        final String windows1251bookName = Objects.nonNull(bookName) ?
                java.net.URLEncoder.encode(bookName, Charset.forName("windows-1251"))
                : "";


        final String request = "https://www.alib.ru/findp.php4"
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
        Optional<Element> pages = doc.getAllElements().stream()
                .filter(e -> e.toString().contains("Cтраницы"))
                .filter(e -> e.outerHtml().startsWith("<b>Cтраницы"))
                .findAny();
        findLinksToNextPages(pages);
        List<Element> links = new ArrayList<>();

        for (Element element : foundBooks) {
            System.out.println(element.text());
        }

// author=%F0%EE%EC%E0%F8%EA%E8%ED+&title=&seria=+&izdat=+&isbnp=&god1=&god2=&cena1=&cena2=&sod=&bsonly=&gorod=&lday=&minus=+&sumfind=1&tipfind=&sortby=0&Bo1=%CD%E0%E9%F2%E8
    }

    private void findLinksToNextPages(Optional<Element> pages) {
        int s = pages.get().childNodeSize();
        for (int i = 0; i < s; i++){
            Element qq = pages.get().child(i);
            qq.html();
        }
    }
}
