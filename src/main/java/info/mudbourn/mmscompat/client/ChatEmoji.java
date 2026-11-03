package info.mudbourn.mmscompat.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replaces Discord-style {@code :shortcode:} tokens in outgoing chat with the
 * emoji glyphs bundled in the default font.
 */
public final class ChatEmoji {
    private static final String TABLE = "/assets/mms_compat/emoji/shortcodes.json";
    private static final Pattern SHORTCODE = Pattern.compile(":([a-z0-9_+\\-]+):");
    private static Map<String, String> codes;

    private ChatEmoji() {
    }

    public static String replace(String message) {
        if (message.indexOf(':') < 0) {
            return message;
        }
        Map<String, String> table = table();
        Matcher m = SHORTCODE.matcher(message);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            String emoji = table.get(m.group(1).toLowerCase());
            m.appendReplacement(out, Matcher.quoteReplacement(emoji != null ? emoji : m.group()));
        }
        m.appendTail(out);
        return out.toString();
    }

    public static List<Map.Entry<String, String>> matching(String partial, int limit) {
        String query = partial.toLowerCase();
        return table().entrySet().stream()
            .filter(e -> e.getKey().contains(query))
            .sorted(Comparator
                .<Map.Entry<String, String>>comparingInt(e -> e.getKey().startsWith(query) ? 0 : 1)
                .thenComparingInt(e -> e.getKey().length())
                .thenComparing(Map.Entry::getKey))
            .limit(limit)
            .toList();
    }

    private static synchronized Map<String, String> table() {
        if (codes != null) {
            return codes;
        }
        codes = new HashMap<>();
        try (InputStream in = ChatEmoji.class.getResourceAsStream(TABLE)) {
            if (in == null) {
                return codes;
            }
            JsonObject json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                .getAsJsonObject();
            json.entrySet().forEach(e -> codes.put(
                e.getKey(),
                Character.toString(Integer.parseInt(e.getValue().getAsString().substring(2), 16))
            ));
        } catch (Exception ignored) {
        }
        return codes;
    }
}
