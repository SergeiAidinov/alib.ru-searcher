package ru.yandex.incoming34.Alib.ru.searcher.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SearchRequest {
    final String author;
    final String bookName;
}
