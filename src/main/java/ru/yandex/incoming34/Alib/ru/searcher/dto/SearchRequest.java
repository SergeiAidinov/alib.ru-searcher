package ru.yandex.incoming34.Alib.ru.searcher.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class SearchRequest {
    final String author;
    final String bookName;
}
