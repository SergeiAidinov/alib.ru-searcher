package ru.yandex.incoming34.Alib.ru.searcher.service;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import ru.yandex.incoming34.Alib.ru.searcher.dto.BookData;
import ru.yandex.incoming34.Alib.ru.searcher.dto.BookSeller;
import ru.yandex.incoming34.Alib.ru.searcher.dto.SearchRequest;

import java.util.*;
import java.util.stream.Collectors;

@Service("alibsearcher")
@AllArgsConstructor
public class AlibSearcher {

    private final DocumentsCollector documentsCollector;
    private final List<Element> unparsableElements = new ArrayList<>();

    @SneakyThrows
    public void search(List<SearchRequest> searchRequests) {
        Set<Document> collectedDocuments = documentsCollector.collectDocuments(searchRequests);
        final Set<String> authors = searchRequests.stream()
                .map(SearchRequest::getAuthor).collect(Collectors.toSet());
        final Set<Element> foundBooks = findBooks(collectedDocuments, authors);
        System.out.println();
        for (final Element foundBook : foundBooks) {
            Optional<BookData> optionalBookData = compileBookData(foundBook);
            optionalBookData.ifPresent(System.out::println);
            //System.out.println(deriveBookSeller(foundBook) + " " +deriveBookName(foundBook) + " " + derivePrice(foundBook));
        }
        System.out.println("Unparsable elements: " + unparsableElements);
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

    private Set<Element> findBooks(Set<Document> foundDocuments, Set<String> authors) {
        final Set<Element> foundBooks = new HashSet<>();
        for (Document document : foundDocuments) {
            Elements elements = document.getElementsByTag("p");
            for (String author : authors) {
                elements.stream().filter(element -> element.text().contains(author))
                        .forEach(foundBooks::add);
            }
        }
        return foundBooks;
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
