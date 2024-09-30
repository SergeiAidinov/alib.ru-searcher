package ru.yandex.incoming34.Alib.ru.searcher.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class BookData {

    private final BookSeller bookSeller;
    private final String title;
    private final Integer price;
}
