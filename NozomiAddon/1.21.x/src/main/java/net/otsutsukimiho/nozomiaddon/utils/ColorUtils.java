package net.otsutsukimiho.nozomiaddon.utils;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {

    private static final Pattern GRADIENT_PATTERN = Pattern.compile("(?i)<g:#([0-9a-f]{6}):#([0-9a-f]{6})>(.*?)</g>");
    private static final Pattern COLOR_PATTERN = Pattern.compile("(?i)(?:§|&)([0-9a-fk-or])|(?:§|&)?#([0-9a-f]{6})");

    public static MutableText parseColor(String message) {
        if (message == null || message.isEmpty()) return Text.empty();

        Matcher gradientMatcher = GRADIENT_PATTERN.matcher(message);
        StringBuilder expandedMessage = new StringBuilder();
        int lastEnd = 0;

        while (gradientMatcher.find()) {
            expandedMessage.append(message, lastEnd, gradientMatcher.start());

            int startColor = Integer.parseInt(gradientMatcher.group(1), 16);
            int endColor = Integer.parseInt(gradientMatcher.group(2), 16);
            String gradientText = gradientMatcher.group(3);

            for (int i = 0; i < gradientText.length(); i++) {
                float ratio = gradientText.length() > 1 ? (float) i / (gradientText.length() - 1) : 0;
                int interpolatedColor = interpolateColor(startColor, endColor, ratio);

                expandedMessage.append(String.format("&#%06X", interpolatedColor)).append(gradientText.charAt(i));
            }

            lastEnd = gradientMatcher.end();
        }
        expandedMessage.append(message.substring(lastEnd));

        return parseLegacyAndHex(expandedMessage.toString());
    }

    private static MutableText parseLegacyAndHex(String message) {
        MutableText finalText = Text.empty();
        Matcher matcher = COLOR_PATTERN.matcher(message);
        int lastEnd = 0;
        Style currentStyle = Style.EMPTY;

        while (matcher.find()) {
            String textSegment = message.substring(lastEnd, matcher.start());
            if (!textSegment.isEmpty()) {
                finalText.append(Text.literal(textSegment).setStyle(currentStyle));
            }

            String legacyCode = matcher.group(1);
            String hexCode = matcher.group(2);

            if (legacyCode != null) {
                char code = legacyCode.toLowerCase().charAt(0);
                Formatting formatting = Formatting.byCode(code);
                if (formatting != null) {
                    if (formatting.isColor()) {
                        currentStyle = currentStyle.withColor(formatting);
                    } else if (formatting == Formatting.RESET) {
                        currentStyle = Style.EMPTY;
                    } else {
                        currentStyle = currentStyle.withFormatting(formatting);
                    }
                }
            } else if (hexCode != null) {
                int rgb = Integer.parseInt(hexCode, 16);
                currentStyle = currentStyle.withColor(TextColor.fromRgb(rgb));
            }
            lastEnd = matcher.end();
        }

        String remaining = message.substring(lastEnd);
        if (!remaining.isEmpty()) {
            finalText.append(Text.literal(remaining).setStyle(currentStyle));
        }
        return finalText;
    }

    private static int interpolateColor(int colorStart, int colorEnd, float ratio) {
        int r1 = (colorStart >> 16) & 0xFF;
        int g1 = (colorStart >> 8) & 0xFF;
        int b1 = colorStart & 0xFF;

        int r2 = (colorEnd >> 16) & 0xFF;
        int g2 = (colorEnd >> 8) & 0xFF;
        int b2 = colorEnd & 0xFF;

        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);

        return (r << 16) | (g << 8) | b;
    }
}