package ru.yandex.incoming34.Alib.ru.searcher.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.incoming34.Alib.ru.searcher.Application;

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
        alibSearcher.search("Ромашкин", "Современное руководство пчеловода");
        //alibSearcher.search("Иванов", null);
        //"%F0%EE%EC%E0%F8%EA%E8%ED"
    }

}