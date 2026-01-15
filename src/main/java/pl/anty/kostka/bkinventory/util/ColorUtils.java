package pl.anty.kostka.bkinventory.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pl.anty.kostka.bkinventory.compat.Compat;

public class ColorUtils {
    private static final Pattern URL_PATTERN = Pattern.compile(
            "(?:https?://|www\\.)\\S+|dc\\.bkubiak\\.dev|tipply\\.pl\\S*",
            Pattern.CASE_INSENSITIVE);

    public static Text translateColorCodes(String input) {
        if (input == null || input.isEmpty()) {
            return Text.empty();
        }
        MutableText result = Text.empty();
        StringBuilder currentSegment = new StringBuilder();
        Style currentStyle = Style.EMPTY;

        char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '&' && i + 1 < chars.length) {
                if (currentSegment.length() > 0) {
                    appendWithLinks(result, currentSegment.toString(), currentStyle);
                    currentSegment.setLength(0);
                }
                char code = Character.toLowerCase(chars[++i]);
                currentStyle = applyColorCode(code, currentStyle);
            } else {
                currentSegment.append(c);
            }
        }
        if (currentSegment.length() > 0) {
            appendWithLinks(result, currentSegment.toString(), currentStyle);
        }
        return result;
    }

    private static void appendWithLinks(MutableText root, String text, Style style) {
        Matcher matcher = URL_PATTERN.matcher(text);
        int lastEnd = 0;
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            if (start > lastEnd) {
                String pre = text.substring(lastEnd, start);
                root.append(Text.literal(pre).setStyle(style));
            }

            String url = text.substring(start, end);
            String target = url;
            if (!target.toLowerCase().startsWith("http")) {
                target = "https://" + target;
            }

            Style linkStyle = Compat.createLinkStyle(style, target);
            root.append(Text.literal(url).setStyle(linkStyle));

            lastEnd = end;
        }
        if (lastEnd < text.length()) {
            String post = text.substring(lastEnd);
            root.append(Text.literal(post).setStyle(style));
        }
    }

    private static Style applyColorCode(char code, Style existing) {
        switch (code) {
            case '0':
                return Style.EMPTY.withColor(Formatting.BLACK);
            case '1':
                return Style.EMPTY.withColor(Formatting.DARK_BLUE);
            case '2':
                return Style.EMPTY.withColor(Formatting.DARK_GREEN);
            case '3':
                return Style.EMPTY.withColor(Formatting.DARK_AQUA);
            case '4':
                return Style.EMPTY.withColor(Formatting.DARK_RED);
            case '5':
                return Style.EMPTY.withColor(Formatting.DARK_PURPLE);
            case '6':
                return Style.EMPTY.withColor(Formatting.GOLD);
            case '7':
                return Style.EMPTY.withColor(Formatting.GRAY);
            case '8':
                return Style.EMPTY.withColor(Formatting.DARK_GRAY);
            case '9':
                return Style.EMPTY.withColor(Formatting.BLUE);
            case 'a':
                return Style.EMPTY.withColor(Formatting.GREEN);
            case 'b':
                return Style.EMPTY.withColor(Formatting.AQUA);
            case 'c':
                return Style.EMPTY.withColor(Formatting.RED);
            case 'd':
                return Style.EMPTY.withColor(Formatting.LIGHT_PURPLE);
            case 'e':
                return Style.EMPTY.withColor(Formatting.YELLOW);
            case 'f':
                return Style.EMPTY.withColor(Formatting.WHITE);
            case 'k':
                return existing.withObfuscated(true);
            case 'l':
                return existing.withBold(true);
            case 'm':
                return existing.withStrikethrough(true);
            case 'n':
                return Compat.withUnderline(existing, true);
            case 'o':
                return existing.withItalic(true);
            case 'r':
                return Style.EMPTY;
            default:
                return existing;
        }
    }
}
