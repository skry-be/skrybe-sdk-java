package com.skrybe.sdk.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.skrybe.sdk.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParseTest {
    /**
     * The exact payload api/lists/get-lists.php and get-brands.php emit:
     * string-concatenated, unescaped, with their real indentation.
     */
    static String phpNumberedPayload(String prefix, String... idNamePairs) {
        StringBuilder out = new StringBuilder("{");
        for (int i = 0; i + 1 < idNamePairs.length; i += 2) {
            out.append('"').append(prefix).append(i / 2 + 1).append("\":\n\t\t{\n\t\t\t\"id\": \"")
                    .append(idNamePairs[i]).append("\",\n\t\t\t\"name\": \"").append(idNamePairs[i + 1]).append("\"\n\t\t},");
        }
        out.setLength(out.length() - 1);
        return out.append('}').toString();
    }

    private static List<String> flat(List<Parse.NamedEntity> rows) {
        List<String> out = new ArrayList<>();
        for (Parse.NamedEntity row : rows) {
            out.add(row.id + "=" + row.name);
        }
        return out;
    }

    @Test
    void readsAWellFormedPayload() {
        String body = phpNumberedPayload("list", "AbC892763xy", "Newsletter", "ZzZ763892qq", "Announcements");
        assertEquals(List.of("AbC892763xy=Newsletter", "ZzZ763892qq=Announcements"), flat(Parse.numberedObject(body, "list")));
    }

    @Test
    void readsTheBrandPayloadWhoseIdsAreBareIntegers() {
        assertEquals(List.of("86=Cafe One"), flat(Parse.numberedObject(phpNumberedPayload("brand", "86", "Cafe One"), "brand")));
        assertEquals(List.of("86=Cafe One"), flat(Parse.numberedObject("{\"brand1\":{\"id\":86,\"name\":\"Cafe One\"}}", "brand")));
    }

    @Test
    void returnsAnEmptyListForAnEmptyBody() {
        assertTrue(Parse.numberedObject("", "list").isEmpty());
        assertTrue(Parse.numberedObject("   \n ", "list").isEmpty());
    }

    @Test
    void ignoresKeysThatAreNotTheRequestedPrefix() {
        String body = "{\"list1\": {\"id\": \"a\", \"name\": \"Keep\"}, \"total\": {\"id\": \"x\", \"name\": \"Drop\"}}";
        assertEquals(List.of("a=Keep"), flat(Parse.numberedObject(body, "list")));
    }

    @Test
    void ordersByTheNumericSuffixNotByStringOrder() {
        String body = "{\"list1\": {\"id\": \"a\", \"name\": \"One\"},"
                + " \"list10\": {\"id\": \"j\", \"name\": \"Ten\"},"
                + " \"list2\": {\"id\": \"b\", \"name\": \"Two\"}}";
        assertEquals(List.of("a=One", "b=Two", "j=Ten"), flat(Parse.numberedObject(body, "list")));
    }

    private static String surrounded(String name) {
        return phpNumberedPayload("list", "AAA111", "Before", "BBB222", name, "CCC333", "After");
    }

    /** get-lists.php interpolates the name straight into the string it echoes. */
    @ParameterizedTest
    @ValueSource(strings = {"Q4 \"Big\" Push", "Say \"hi\"", "ends}", "Lagos, Abuja", "\"quoted\""})
    void recoversNamesTheServerDoesNotEscape(String name) {
        assertEquals(List.of("AAA111=Before", "BBB222=" + name, "CCC333=After"), flat(Parse.numberedObject(surrounded(name), "list")));
    }

    @Test
    void theQuotedPayloadsReallyAreInvalidJson() {
        assertNull(Json.tryParse(surrounded("Q4 \"Big\" Push")));
        assertNull(Json.tryParse(surrounded("Say \"hi\"")));
    }

    @Test
    void recoversTheLastEntryWhenItsNameEndsInAQuote() {
        String body = phpNumberedPayload("list", "AAA111", "trailing \"quote\"");
        assertEquals(List.of("AAA111=trailing \"quote\""), flat(Parse.numberedObject(body, "list")));
    }

    @Test
    void keepsEveryIdIntactWhenANameForgesASecondNameKey() {
        // `x", "name": "injected` leaves the payload valid JSON with a duplicate
        // key, and the last one wins, so the name reads `injected` and the
        // structural fallback never runs. Cosmetic only: the id is unaffected.
        List<Parse.NamedEntity> rows = Parse.numberedObject(surrounded("x\", \"name\": \"injected"), "list");
        List<String> ids = new ArrayList<>();
        rows.forEach(r -> ids.add(r.id));
        assertEquals(List.of("AAA111", "BBB222", "CCC333"), ids);
        assertEquals("injected", rows.get(1).name);
    }

    @Test
    void throwsWithTheBodyAttachedWhenNothingIsRecognisable() {
        String body = "<br />\n<b>Warning</b>: mysqli_query(): MySQL server has gone away";
        ApiException e = assertThrows(ApiException.class, () -> Parse.numberedObject(body, "list"));
        assertEquals("malformed_response", e.getErrorCode());
        assertEquals(body, e.getRaw());
        assertTrue(e.getHint().contains("MySQL server has gone away"));
    }

    @Test
    void collapsesAMultiLineBodyIntoASingleLineHint() {
        ApiException e = assertThrows(ApiException.class, () -> Parse.numberedObject("one\n\n  two\n\tthree", "list"));
        assertEquals("The server sent: one two three", e.getHint());
    }

    @Test
    void truncatesAVeryLongBodyInTheHintButKeepsItWhole() {
        String body = "x" + "y".repeat(5000);
        ApiException e = assertThrows(ApiException.class, () -> Parse.numberedObject(body, "list"));
        assertTrue(e.getHint().length() < 300);
        assertTrue(e.getHint().endsWith("…"));
        assertEquals(body, e.getRaw());
    }

    @Test
    void jsonObjectRejectsAJsonArrayAndTrailingGarbage() {
        assertThrows(ApiException.class, () -> Parse.jsonObject("[1,2]", "thing"));
        assertThrows(ApiException.class, () -> Parse.jsonObject("{\"a\":1} trailing", "thing"));
        assertThrows(ApiException.class, () -> Parse.jsonObject("", "thing"));
        assertEquals(1, Parse.jsonObject(" {\"a\":1}\n", "thing").get("a").asInt());
    }

    @Test
    void numberedRecordsKeepsRowsWholeInOrder() {
        String body = "{\"campaign2\": {\"id\": 2, \"title\": \"B\"}, \"campaign1\": {\"id\": 1, \"title\": \"A\"},"
                + " \"campaign10\": {\"id\": 10}, \"campaignX\": {\"id\": 99}, \"total\": 2}";
        List<JsonNode> rows = Parse.numberedRecords(body, "campaign");
        assertEquals(3, rows.size());
        assertEquals("{\"id\":1,\"title\":\"A\"}", rows.get(0).toString());
        assertEquals(2, rows.get(1).get("id").asInt());
        assertEquals(10, rows.get(2).get("id").asInt());
    }
}
