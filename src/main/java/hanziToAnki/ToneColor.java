package hanziToAnki;

import java.awt.Color;

/**
 * Tone color definitions following Anki flashcard conventions.
 * Colors are used to distinguish between the four tones of Mandarin Chinese.
 */
public class ToneColor {
    
    // Standard Anki tone colors
    public static final Color TONE_1_COLOR = new Color(220, 50, 50);     // Red
    public static final Color TONE_2_COLOR = new Color(255, 157, 51);    // Orange
    public static final Color TONE_3_COLOR = new Color(76, 175, 80);     // Green
    public static final Color TONE_4_COLOR = new Color(66, 133, 244);    // Blue
    public static final Color TONE_5_COLOR = new Color(155, 155, 155);   // Gray (neutral/no tone)
    
    public static Color getToneColor(int tone) {
        return switch (tone) {
            case 1 -> TONE_1_COLOR;
            case 2 -> TONE_2_COLOR;
            case 3 -> TONE_3_COLOR;
            case 4 -> TONE_4_COLOR;
            default -> TONE_5_COLOR;
        };
    }
}
