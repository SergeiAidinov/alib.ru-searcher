package ru.yandex.incoming34.Alib.ru.searcher.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.incoming34.Alib.ru.searcher.Application;
import ru.yandex.incoming34.Alib.ru.searcher.dto.SearchRequest;

import java.util.List;

@SpringBootTest(classes = {AlibSearcher.class})
@ContextConfiguration(classes = Application.class)
class AlibSearcherTest {

    private final AlibSearcher alibSearcher;

    AlibSearcherTest(@Qualifier("alibsearcher") AlibSearcher alibSearcher) {
        this.alibSearcher = alibSearcher;
    }

    @Test
    public void searchTest(){
        System.out.println(alibSearcher);
       /* alibSearcher.search(List.of(
                new SearchRequest("Ромашкин", "Современное руководство пчеловода"),
                new SearchRequest("Иванов", "Основы программирования")

        ));*/
        alibSearcher.search(List.of(new SearchRequest("Иванов", null)));
        //alibSearcher.search(List.of(new SearchRequest("Васин", null)));
        //alibSearcher.search(List.of(new SearchRequest("Иванов", "Основы программирования")));
        //"%F0%EE%EC%E0%F8%EA%E8%ED"
    }

}