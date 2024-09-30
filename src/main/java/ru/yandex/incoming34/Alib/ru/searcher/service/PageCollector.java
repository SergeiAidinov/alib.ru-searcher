package ru.yandex.incoming34.Alib.ru.searcher.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import ru.yandex.incoming34.Alib.ru.searcher.dto.SearchRequest;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class PageCollector {

    //private final Set<Document> documents = new HashSet<>();

    protected Set<Document> collectDocuments(List<SearchRequest> searchRequests) {
        final Set<Document> documents = new HashSet<>();
        final Set<String> initialLinks = collectInitialLinks(searchRequests);
        initialLinks.stream().filter(link -> !link.isEmpty()).forEach(link ->
                loadDocumentByLink(link).ifPresent(documents::add));
        final Set<String> nextLinks = collectNextLinks(documents);
        nextLinks.forEach(nextLink -> loadDocumentByLink(nextLink).ifPresent(documents::add));

        return documents;
    }

    private Set<String> collectNextLinks(Set<Document> documents) {
        Set<String> nextLinks = new HashSet<>();
        for (Document document : documents) {
            document.getAllElements().stream()
                    .filter(e -> e.outerHtml().startsWith("<b>Cтраницы"))
                    .findAny()
                    .ifPresent(element -> element.childNodes().forEach(e -> {
                        String linkToNextPage = e.absUrl("href").trim();
                        System.out.println(linkToNextPage);
                        if (!linkToNextPage.isEmpty()) nextLinks.add(linkToNextPage);
                    }));
        }
        return nextLinks;
    }

    private Optional<Document> loadDocumentByLink(String link) {
        Optional<Document> optionalDocument = Optional.empty();
        try {
            System.out.println("===> " + link);
            optionalDocument = Optional.of(Jsoup.connect(link).post());
        } catch (IOException e) {
            //continue;
        }
        optionalDocument.ifPresent(document -> document.charset(StandardCharsets.UTF_16));
        return optionalDocument;
    }

    private Set<String> collectInitialLinks(List<SearchRequest> searchRequests) {
        final Set<String> initialLinks = new HashSet<>();

        for (SearchRequest searchRequest : searchRequests) {
            final String windows1251author = Objects.nonNull(searchRequest.getAuthor()) ?
                    URLEncoder.encode(searchRequest.getAuthor(), Charset.forName("windows-1251"))
                    : "";
            final String windows1251bookName = Objects.nonNull(searchRequest.getBookName()) ?
                    URLEncoder.encode(searchRequest.getBookName(), Charset.forName("windows-1251"))
                    : "";
            final String link = "https://www.alib.ru/findp.php4"
                    + "?author=" + windows1251author
                    + "+&title=" + windows1251bookName
                    + "+&seria=+&izdat=+&isbnp=&god1=&god2=&cena1=&cena2=&sod=&bsonly=&gorod=&lday=&minus=+&sumfind=1&tipfind=&sortby="
                    + "0&Bo1=%CD%E0%E9%F2%E8";
            initialLinks.add(link);
        }
        return initialLinks;
    }

    private Set<Document> findDocuments(List<SearchRequest> searchRequests) {
        final Set<Document> foundDocuments = new HashSet<>();
        for (SearchRequest searchRequest : searchRequests) {
            final String windows1251author = Objects.nonNull(searchRequest.getAuthor()) ?
                    URLEncoder.encode(searchRequest.getAuthor(), Charset.forName("windows-1251"))
                    : "";
            final String windows1251bookName = Objects.nonNull(searchRequest.getBookName()) ?
                    URLEncoder.encode(searchRequest.getBookName(), Charset.forName("windows-1251"))
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

}
