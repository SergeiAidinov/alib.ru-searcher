package ru.yandex.incoming34.Alib.ru.searcher.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.yandex.incoming34.Alib.ru.searcher.dto.SearchRequest;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

//@Service
public class DocumentsCollector {

    private final ExecutorService executor;
    private final ConcurrentLinkedQueue<String> links = new ConcurrentLinkedQueue<>();
    private final List<Future<Optional<Document>>> futures = new ArrayList<>();

    public DocumentsCollector(ExecutorService executor) {
        this.executor = executor;
    }

    protected Set<Document> collectDocuments(SearchRequest searchRequest) {
        return doCollectDocuments(searchRequest);
    }

    private Set<Document> doCollectDocuments (SearchRequest searchRequest){
        final Set<Document> documents = new HashSet<>();
        final String initialLink = compileInitialLink(searchRequest);
        loadDocumentByLink(initialLink).ifPresent(documents::add);
        final Set<String> nextLinks = collectNextLinks(documents);
        links.addAll(nextLinks);
        while (!links.isEmpty()) {
            final String link = links.poll();
            Future<Optional<Document>> submitted = executor.submit(() -> loadDocumentByLink(link));
            futures.add(submitted);
        }
        while (!futures.isEmpty()) {
            for (int i = 0; i < futures.size(); i++) {
                final Future<Optional<Document>> future = futures.get(i);
                if (future.isDone()) {
                    futures.remove(i);
                    try {
                        future.get().ifPresent(documents::add);
                    } catch (Exception e) {
                        executor.shutdown();
                    }
                }
            }
        }
        executor.shutdown();
        System.out.println("Finished at " + LocalDateTime.now() + " collecting documents");
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
                        if (!linkToNextPage.isEmpty()) nextLinks.add(linkToNextPage);
                    }));
        }
        return nextLinks;
    }

    private Optional<Document> loadDocumentByLink(String link) {
        Optional<Document> optionalDocument = Optional.empty();
        try {
            System.out.println("Started loading data at " + LocalDateTime.now() + " by link " + link);
            optionalDocument = Optional.of(Jsoup.connect(link).post());
        } catch (IOException e) {
            return optionalDocument;
        }
        optionalDocument.ifPresent(document -> document.charset(StandardCharsets.UTF_16));
        //executor.shutdown();
        return optionalDocument;
    }

    private String compileInitialLink(SearchRequest searchRequest) {
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
        return link;
    }
}
