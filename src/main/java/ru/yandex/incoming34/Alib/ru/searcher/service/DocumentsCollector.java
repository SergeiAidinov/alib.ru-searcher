package ru.yandex.incoming34.Alib.ru.searcher.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.yandex.incoming34.Alib.ru.searcher.dto.SearchRequest;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Phaser;

//@Service
public class DocumentsCollector implements Runnable {

    private final Phaser phaser;
    private final ExecutorService executor
            = Executors.newCachedThreadPool();

    public DocumentsCollector(Phaser phaser, List<SearchRequest> searchRequests) {
        this.phaser = phaser;
    }

    protected Future<Set<Document>> collectDocuments(SearchRequest searchRequest) {
        return executor.submit(() -> {
            return doCollectDocuments(searchRequest);
        });

    }

    private Set<Document> doCollectDocuments (SearchRequest searchRequest){
        final Set<Document> documents = new HashSet<>();
        final Set<String> initialLinks = collectInitialLinks(searchRequest);
        initialLinks.stream().filter(link -> !link.isEmpty()).forEach(link ->
                loadDocumentByLink(link).ifPresent(documents::add));
        final Set<String> nextLinks = collectNextLinks(documents);
        nextLinks.forEach(nextLink -> loadDocumentByLink(nextLink).ifPresent(documents::add));
        phaser.arriveAndDeregister();
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
            return optionalDocument;
        }
        optionalDocument.ifPresent(document -> document.charset(StandardCharsets.UTF_16));
        return optionalDocument;
    }

    private Set<String> collectInitialLinks(SearchRequest searchRequest) {
        final Set<String> initialLinks = new HashSet<>();
        //for (SearchRequest searchRequest : searchRequests) {
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
        //}
        return initialLinks;
    }

    @Override
    public void run() {

    }
}
