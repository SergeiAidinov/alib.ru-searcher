package ru.yandex.incoming34.Alib.ru.searcher.service;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import ru.yandex.incoming34.Alib.ru.searcher.dto.BookData;
import ru.yandex.incoming34.Alib.ru.searcher.dto.BookSeller;
import ru.yandex.incoming34.Alib.ru.searcher.dto.SearchRequest;

import java.util.*;
import java.util.concurrent.*;

@Service("alibsearcher")
@AllArgsConstructor
public class AlibSearcher {

    private final List<Element> unparsableElements = new ArrayList<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(8);
    private final ConcurrentHashMap<SearchRequest, Future<Set<Document>>> requestsWithResults
            = new ConcurrentHashMap<>();
    private final HashMap<SearchRequest, Set<BookData>> foundBooks = new HashMap<>();


    @SneakyThrows
    public void search(List<SearchRequest> searchRequests) {
        //List<SearchRequest> modifiableSearchRequests = new ArrayList<>(searchRequests);
        ConcurrentLinkedQueue<SearchRequest> requestQueue  = new ConcurrentLinkedQueue<>(searchRequests);
        while (!requestQueue.isEmpty()) {
            //if (phaser.getRegisteredParties() >= 8) continue;
            SearchRequest searchRequest = requestQueue.remove();
            DocumentsCollector documentsCollector = new DocumentsCollector(executor);
            Future<Set<Document>> submitted = executor.submit(() -> documentsCollector.collectDocuments(searchRequest));

            //SearchRequest searchRequest = modifiableSearchRequests.remove(0);
            requestsWithResults.put(searchRequest, submitted);
        }

        while (!executor.isTerminated()) {
        }
        //executor.isTerminated();
        for (Map.Entry<SearchRequest, Future<Set<Document>>> result : requestsWithResults.entrySet()) {
            for (Map.Entry<SearchRequest, Future<Set<Document>>> searchRequestFutureEntry : Arrays.asList(result)) {
                Set<Element> bookElements = findBookElements(searchRequestFutureEntry.getValue().get(), searchRequestFutureEntry.getKey().getAuthor());
                Set<BookData> bookDataSet = compileDataForBooks(bookElements);
                foundBooks.put(searchRequestFutureEntry.getKey(), bookDataSet);
            }
        }
        System.out.println("Unparsable elements: " + unparsableElements);
    }

    private Set<BookData> compileDataForBooks(Set<Element> elements) {
        Set<BookData> bookDataSet = new HashSet<>();
        for (Element element : elements) {
            compileBookData(element).ifPresent(bookDataSet::add);
        }
        return bookDataSet;
    }

    private Set<Element> findBookElements(Set<Document> foundDocuments, String author) {
        final Set<Element> foundBooks = new HashSet<>();
        for (Document document : foundDocuments) {
            Elements elements = document.getElementsByTag("p");
                elements.stream().filter(element -> element.text().contains(author))
                        .forEach(foundBooks::add);
        }
        return foundBooks;
    }

    private Optional<BookData> compileBookData(Element foundBook) {
        Optional<BookData> optionalBookData = Optional.empty();
        try {
            BookData bookData = new BookData(deriveBookSeller(foundBook), deriveBookName(foundBook), derivePrice(foundBook));
            optionalBookData = Optional.of(bookData);
        } catch (Exception e) {
            unparsableElements.add(foundBook);
            return optionalBookData;
        }
        return optionalBookData;
    }

    private BookSeller deriveBookSeller(final Element element) throws Exception {
            final String linkToBookSeller = element.childNodes().get(4).attributes().attribute("href").getValue();
            final int start = element.childNodes().get(4).toString().indexOf("BS");
            final int fin = element.childNodes().get(4).toString().lastIndexOf('<');
            final String bookSellerName = element.childNodes().get(4).toString().substring(start, fin);
            return new BookSeller(bookSellerName, linkToBookSeller);
    }

    private Integer derivePrice(Element element) throws Exception {
        final StringBuilder builder = new StringBuilder();
            final String textOfElement = element.childNodes().get(5).toString();
            final int initPosition = textOfElement.indexOf("Цена: ");
            for (int i = initPosition; i < textOfElement.length(); i++) {
                Character c = textOfElement.charAt(i);
                if (Character.isDigit(c)) builder.append(c);
            }
        return Integer.parseInt(builder.toString().trim());
    }

    private String deriveBookName(final Element element) throws  Exception {
            return element.childNodes().get(0).childNode(0).toString();
    }

}
