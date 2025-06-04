package xyz.neeqz.modded.tabmc.client;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;

public class ChatLogColor {

    private static final Map<Character, Formatting> formattingMap = new HashMap<>();

    static {
        formattingMap.put('0', Formatting.BLACK);
        formattingMap.put('1', Formatting.DARK_BLUE);
        formattingMap.put('2', Formatting.DARK_GREEN);
        formattingMap.put('3', Formatting.DARK_AQUA);
        formattingMap.put('4', Formatting.DARK_RED);
        formattingMap.put('5', Formatting.DARK_PURPLE);
        formattingMap.put('6', Formatting.GOLD);
        formattingMap.put('7', Formatting.GRAY);
        formattingMap.put('8', Formatting.DARK_GRAY);
        formattingMap.put('9', Formatting.BLUE);
        formattingMap.put('a', Formatting.GREEN);
        formattingMap.put('b', Formatting.AQUA);
        formattingMap.put('c', Formatting.RED);
        formattingMap.put('d', Formatting.LIGHT_PURPLE);
        formattingMap.put('e', Formatting.YELLOW);
        formattingMap.put('f', Formatting.WHITE);

        // Style
        formattingMap.put('l', Formatting.BOLD);
        formattingMap.put('n', Formatting.UNDERLINE);
        formattingMap.put('o', Formatting.ITALIC);
        formattingMap.put('m', Formatting.STRIKETHROUGH);
        formattingMap.put('r', Formatting.RESET);
    }

    public static Text color(String input) {
        MutableText result = Text.literal("");
        Style currentStyle = Style.EMPTY;
        StringBuilder currentSegment = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '&' && i + 1 < input.length()) {
                char code = Character.toLowerCase(input.charAt(i + 1));
                Formatting format = formattingMap.get(code);

                if (format != null) {
                    if (currentSegment.length() > 0) {
                        result.append(Text.literal(currentSegment.toString()).setStyle(currentStyle));
                        currentSegment.setLength(0);
                    }

                    if (format == Formatting.RESET) {
                        currentStyle = Style.EMPTY;
                    } else {
                        currentStyle = currentStyle.withFormatting(format);
                    }

                    i++;
                    continue;
                }
            }

            currentSegment.append(c);
        }

        if (currentSegment.length() > 0) {
            result.append(Text.literal(currentSegment.toString()).setStyle(currentStyle));
        }

        return result;
    }
}
