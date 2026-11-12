package com.example.B2C.modules.catalog.util;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SearchQueryUtils {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern WORD_PATTERN = Pattern.compile("[\\p{L}\\p{N}]+");

    private SearchQueryUtils() {
    }

    /**
     * Remove Vietnamese accents and special diacritics.
     * Handles 'đ' / 'Đ' explicitly as Java's Normalizer does not decompose them.
     */
    public static String unaccent(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String replaced = text.replace('đ', 'd').replace('Đ', 'D');
        String normalized = Normalizer.normalize(replaced, Normalizer.Form.NFD);
        return DIACRITICS.matcher(normalized).replaceAll("").toLowerCase(Locale.ROOT);
    }

    /**
     * Converts a raw search string into a PostgreSQL tsquery string with prefix matching (':*')
     * on the final token. This enables responsive search-as-you-type behavior.
     *
     * Examples:
     * - "iPhone 15 pro max" -> "'iphone' & '15' & 'pro' & 'max':*"
     * - "áo thun n"         -> "'ao' & 'thun' & 'n':*"
     * - "giày"              -> "'giay':*"
     *
     * Returns null if no valid alphanumeric tokens exist.
     */
    public static String toPrefixTsQuery(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String cleaned = unaccent(raw);
        Matcher matcher = WORD_PATTERN.matcher(cleaned);
        List<String> tokens = new ArrayList<>();
        while (matcher.find()) {
            String token = matcher.group();
            if (!token.isBlank()) {
                tokens.add(token);
            }
        }

        if (tokens.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            if (i > 0) {
                sb.append(" & ");
            }
            sb.append("'").append(tokens.get(i)).append("'");
            if (i == tokens.size() - 1) {
                sb.append(":*");
            }
        }

        return sb.toString();
    }
}
