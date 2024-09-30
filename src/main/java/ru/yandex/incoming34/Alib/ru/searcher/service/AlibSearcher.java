package ru.yandex.incoming34.Alib.ru.searcher.service;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import ru.yandex.incoming34.Alib.ru.searcher.dto.BookSeller;
import ru.yandex.incoming34.Alib.ru.searcher.dto.SearchRequest;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service("alibsearcher")
@AllArgsConstructor
public class AlibSearcher {

    private final PageCollector pageCollector;

    @SneakyThrows
    public void search(List<SearchRequest> searchRequests) {
        pageCollector.collectDocuments(searchRequests);
        final Set<Document> foundDocuments = collectDocuments(searchRequests);
        final Set<String> authors = searchRequests.stream()
                .map(SearchRequest::getAuthor).collect(Collectors.toSet());
        final Set<Element> foundBooks = findBooks(foundDocuments, authors);
        final Set<String> linksToNextPages = findLinksToNextpages(foundDocuments);
        System.out.println();
    }

    private Set<String> findLinksToNextpages(Set<Document> foundDocuments) {
        final Set<String> linksToNextPages = new HashSet<>();
        for (Document document : foundDocuments) {
            document.getAllElements().stream()
                    .filter(e -> e.outerHtml().startsWith("<b>Cтраницы"))
                    .findAny()
                    .ifPresent(element -> element.childNodes().forEach(e -> {
                        String link = e.absUrl("href").trim();
                        System.out.println(link);
                        if (!link.isEmpty()) linksToNextPages.add(link);
                    }));
        }
        return linksToNextPages;
    }

    private Set<Element> findBooks(Set<Document> foundDocuments, Set<String> authors) {
        final Set<Element> foundBooks = new HashSet<>();
        for (Document document : foundDocuments) {
            Elements elements = document.getElementsByTag("p");
            for (String author : authors) {
                elements.stream().filter(element -> element.text().contains(author))
                        .forEach(element -> foundBooks.add(element));
            }
        }
        return foundBooks;
    }

    private Set<Document> collectDocuments(List<SearchRequest> searchRequests) {
        final Set<Document> foundDocuments = new HashSet<>();
        for (SearchRequest searchRequest : searchRequests) {
            final String windows1251author = Objects.nonNull(searchRequest.getAuthor()) ?
                    java.net.URLEncoder.encode(searchRequest.getAuthor(), Charset.forName("windows-1251"))
                    : "";
            final String windows1251bookName = Objects.nonNull(searchRequest.getBookName()) ?
                    java.net.URLEncoder.encode(searchRequest.getBookName(), Charset.forName("windows-1251"))
                    : "";
            final String request = "https://www.alib.ru/findp.php4"
                    + "?author=" + windows1251author
                    + "+&title=" + windows1251bookName
                    + "+&seria=+&izdat=+&isbnp=&god1=&god2=&cena1=&cena2=&sod=&bsonly=&gorod=&lday=&minus=+&sumfind=1&tipfind=&sortby="
                    + "0&Bo1=%CD%E0%E9%F2%E8";
            Document document;
            try {
                document = Jsoup.connect(request).post();
            } catch (IOException e) {
                continue;
            }
            document.charset(StandardCharsets.UTF_16);
            foundDocuments.add(document);
        }
        return foundDocuments;
    }

    private BookSeller deriveBookSeller(final Element element) {
        final String linkToBookSeller = element.childNodes().get(4).attributes().attribute("href").getValue();
        final int start = element.childNodes().get(4).toString().indexOf("BS");
        final int fin = element.childNodes().get(4).toString().lastIndexOf('<');
        final String bookSellername = element.childNodes().get(4).toString().substring(start, fin);
        return new BookSeller(bookSellername, linkToBookSeller);
    }

    private Integer derivePrice(Element element) {
       final String textOfElement =  element.childNodes().get(5).toString();
       final int initPosition = textOfElement.indexOf("Цена: ");
       final StringBuilder builder = new StringBuilder();
       for (int i = initPosition; i < textOfElement.length(); i++) {
           Character c = textOfElement.charAt(i);
           if (Character.isDigit(c)) builder.append(c);
       }
        return Integer.parseInt(builder.toString().trim());
    }

    private String deriveBookName(final Element element) {
       return element.childNodes().get(0).childNode(0).toString();
    }

}
