package com.example.B2C.modules.catalog.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SearchQueryUtilsTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("toPrefixTsQuery returns null for null, empty, or whitespace strings")
    void toPrefixTsQuery_emptyOrBlank_returnsNull(String input) {
        assertThat(SearchQueryUtils.toPrefixTsQuery(input)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"!@#$%", "---", "...()", " ;:, "})
    @DisplayName("toPrefixTsQuery returns null when string has no alphanumeric tokens")
    void toPrefixTsQuery_onlySpecialChars_returnsNull(String input) {
        assertThat(SearchQueryUtils.toPrefixTsQuery(input)).isNull();
    }

    @Test
    @DisplayName("toPrefixTsQuery converts single word to prefix query")
    void toPrefixTsQuery_singleWord_appendsPrefix() {
        String result = SearchQueryUtils.toPrefixTsQuery("iphone");
        assertThat(result).isEqualTo("'iphone':*");
    }

    @Test
    @DisplayName("toPrefixTsQuery converts multiple words with prefix on last token")
    void toPrefixTsQuery_multiWord_appendsPrefixOnLastTokenOnly() {
        String result = SearchQueryUtils.toPrefixTsQuery("iphone 15 pro max");
        assertThat(result).isEqualTo("'iphone' & '15' & 'pro' & 'max':*");
    }

    @Test
    @DisplayName("toPrefixTsQuery handles Vietnamese accented and unaccented terms")
    void toPrefixTsQuery_vietnameseAccents_unaccentedAndPrefixed() {
        String result = SearchQueryUtils.toPrefixTsQuery("áo thun nam");
        assertThat(result).isEqualTo("'ao' & 'thun' & 'nam':*");
    }

    @Test
    @DisplayName("toPrefixTsQuery handles letter 'đ' and 'Đ' properly")
    void toPrefixTsQuery_handlesLetterD_correctly() {
        String result = SearchQueryUtils.toPrefixTsQuery("Đồng hồ nam");
        assertThat(result).isEqualTo("'dong' & 'ho' & 'nam':*");
    }

    @Test
    @DisplayName("toPrefixTsQuery strips punctuation and handles partial typing")
    void toPrefixTsQuery_partialTyping_handlesSpecialChars() {
        String result = SearchQueryUtils.toPrefixTsQuery("áo thun - n");
        assertThat(result).isEqualTo("'ao' & 'thun' & 'n':*");
    }

    @ParameterizedTest
    @CsvSource({
            "'Đồng hồ điện tử', 'dong ho dien tu'",
            "'Áo sơ mi trắng', 'ao so mi trang'",
            "'Giày thể thao Đen', 'giay the thao den'"
    })
    @DisplayName("unaccent strips Vietnamese marks including đ")
    void unaccent_vietnamese(String input, String expected) {
        assertThat(SearchQueryUtils.unaccent(input)).isEqualTo(expected);
    }
}
