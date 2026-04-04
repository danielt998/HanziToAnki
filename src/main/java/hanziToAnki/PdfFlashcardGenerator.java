package hanziToAnki;

import hanziToAnki.chinese.ChineseWord;
import hanziToAnki.chinese.ToneHelper;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PdfFlashcardGenerator {
    private static final Logger logger = LoggerFactory.getLogger(PdfFlashcardGenerator.class);
    
    // Scaling factor for higher resolution (3x = 432 DPI instead of 144 DPI)
    private static final int SCALE = 3;
    
    // Letter page dimensions (8.5" x 11" in points)
    private static final int PAGE_WIDTH = 612 * SCALE;
    private static final int PAGE_HEIGHT = 792 * SCALE;
    
    private static final int MARGIN = 12 * SCALE;
    private static final int PADDING = 8 * SCALE;
    
    // Font size multipliers (relative to card style)
    private static final float PINYIN_SIZE_RATIO = 0.22f;      // 22% of Chinese font size
    private static final float DEFINITION_SIZE_RATIO = 0.15f;  // 15% of Chinese font size
    
    private final CardStyle cardStyle;
    private final boolean useToneColors;
    private final int cardWidth;
    private final int cardHeight;
    private final int cardsPerRow;
    private final int cardsPerCol;
    private final Font fontChinese;
    private final Font fontPinyin;
    private final Font fontDefinition;
    
    public PdfFlashcardGenerator() {
        this(CardStyle.INDEX_CARD_3x5, true);
    }
    
    public PdfFlashcardGenerator(CardStyle cardStyle) {
        this(cardStyle, true);
    }
    
    public PdfFlashcardGenerator(CardStyle cardStyle, boolean useToneColors) {
        this.cardStyle = cardStyle;
        this.useToneColors = useToneColors;
        this.cardWidth = cardStyle.widthPoints() * SCALE;
        this.cardHeight = cardStyle.heightPoints() * SCALE;
        
        int[] cardsPerPage = cardStyle.getCardsPerPage();
        this.cardsPerRow = cardsPerPage[0];
        this.cardsPerCol = cardsPerPage[1];
        
        int chineseFontSize = cardStyle.chineseFontSize() * SCALE;
        int pinyinFontSize = Math.max(8 * SCALE, (int)(chineseFontSize * PINYIN_SIZE_RATIO));
        int definitionFontSize = Math.max(6 * SCALE, (int)(chineseFontSize * DEFINITION_SIZE_RATIO));
        
        this.fontChinese = new Font("SimSun", Font.PLAIN, chineseFontSize);
        this.fontPinyin = new Font("Arial", Font.PLAIN, pinyinFontSize);
        this.fontDefinition = new Font("Arial", Font.PLAIN, definitionFontSize);
    }
    
    public byte[] generateFlashcardPdf(List<Word> words) throws IOException {
        // Generate images for each page
        List<BufferedImage> pageImages = generatePageImages(words);
        
        // Embed images in PDF
        return embedImagesInPdf(pageImages);
    }
    
    private List<BufferedImage> generatePageImages(List<Word> words) {
        List<BufferedImage> pages = new ArrayList<>();
        
        int totalCards = words.size();
        int cardsPerPageCount = cardsPerRow * cardsPerCol;
        int totalPages = (totalCards + cardsPerPageCount - 1) / cardsPerPageCount;
        
        for (int pageNum = 0; pageNum < totalPages; pageNum++) {
            BufferedImage pageImage = new BufferedImage(PAGE_WIDTH, PAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = pageImage.createGraphics();
            
            // Set white background
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, PAGE_WIDTH, PAGE_HEIGHT);
            
            // Enable anti-aliasing for smooth text
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            int cardIndex = pageNum * cardsPerPageCount;
            
            for (int row = 0; row < cardsPerCol; row++) {
                for (int col = 0; col < cardsPerRow; col++) {
                    if (cardIndex < totalCards) {
                        Word word = words.get(cardIndex);
                        drawCard(g2d, word, col, row);
                        cardIndex++;
                    }
                }
            }
            
            g2d.dispose();
            pages.add(pageImage);
        }
        
        logger.info("Generated {} page images for {} flashcards using {}", pages.size(), totalCards, cardStyle.displayName());
        return pages;
    }
    
    private void drawCard(Graphics2D g2d, Word word, int col, int row) {
        ChineseWord chineseWord = (ChineseWord) word;
        
        // Calculate card position
        int x = col * cardWidth + MARGIN;
        int y = row * cardHeight + MARGIN;
        
        // Draw card border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRect(x, y, cardWidth - MARGIN, cardHeight - MARGIN);
        
        // Draw dividing line (horizontal)
        int midY = y + (cardHeight - MARGIN) / 2;
        g2d.drawLine(x, midY, x + cardWidth - MARGIN, midY);
        
        // === FRONT SIDE (Top): Hanzi ===
        int frontY = y + PADDING;
        int frontHeight = (cardHeight - MARGIN) / 2;
        
        g2d.setFont(fontChinese);
        
        String hanzi = chineseWord.simplified();
        String pinyinWithNumbers = chineseWord.pinyinTones() != null ? chineseWord.pinyinTones() : "";
        int tone = getToneFromPinyin(pinyinWithNumbers);
        
        if (useToneColors) {
            g2d.setColor(ToneColor.getToneColor(tone));
        } else {
            g2d.setColor(Color.BLACK);
        }
        
        FontMetrics fm = g2d.getFontMetrics();
        int hanziWidth = fm.stringWidth(hanzi);
        int hanziX = x + (cardWidth - MARGIN - hanziWidth) / 2;
        int hanziY = frontY + (frontHeight - fm.getHeight()) / 2 + fm.getAscent();
        
        g2d.drawString(hanzi, hanziX, hanziY);
        
        // === BACK SIDE (Bottom): Pinyin + Definition ===
        int backY = midY + PADDING;
        int backHeight = (cardHeight - MARGIN) / 2;
        int backBottom = y + cardHeight - MARGIN; // Bottom boundary of card
        
        // Draw pinyin with tone marks
        g2d.setFont(fontPinyin);
        
        String pinyinWithMarks = convertPinyinToToneMarks(pinyinWithNumbers);
        int pinyinX = x + PADDING;
        int pinyinY = backY + 14;
        
        if (useToneColors) {
            drawColoredPinyin(g2d, pinyinWithNumbers, pinyinX, pinyinY, fontPinyin);
        } else {
            g2d.setColor(new Color(64, 64, 64)); // Dark gray
            g2d.drawString(pinyinWithMarks, pinyinX, pinyinY);
        }
        
        // Draw definition with text wrapping - add more space between pinyin and definition
        g2d.setFont(fontDefinition);
        g2d.setColor(Color.BLACK);
        
        String definition = chineseWord.definition() != null ? chineseWord.definition() : "";
        int defX = x + PADDING;
        int defY = pinyinY + 24;  // Increased from 12 to 24 for better spacing
        int maxWidth = cardWidth - MARGIN - 2 * PADDING;
        
        drawWrappedText(g2d, definition, defX, defY, maxWidth, backBottom, fontDefinition);
    }
    
    private int getToneFromPinyin(String pinyinWithNumbers) {
        if (pinyinWithNumbers == null || pinyinWithNumbers.isEmpty()) {
            return 5;
        }
        try {
            String firstSyllable = pinyinWithNumbers.split(" ")[0];
            return Integer.parseInt("" + firstSyllable.charAt(firstSyllable.length() - 1));
        } catch (Exception e) {
            return 5;
        }
    }
    
    private void drawColoredPinyin(Graphics2D g2d, String pinyinWithNumbers, int x, int y, Font font) {
        if (pinyinWithNumbers == null || pinyinWithNumbers.isEmpty()) {
            return;
        }
        
        String[] syllables = pinyinWithNumbers.split(" ");
        int currentX = x;
        
        for (String syllable : syllables) {
            int tone = Integer.parseInt("" + syllable.charAt(syllable.length() - 1));
            g2d.setColor(ToneColor.getToneColor(tone));
            String marked = ToneHelper.convertNumberedSyllableToAccentedSyllable(syllable);
            g2d.drawString(marked, currentX, y);
            
            FontMetrics fm = g2d.getFontMetrics();
            currentX += fm.stringWidth(marked) + fm.stringWidth(" ");
        }
    }
    
    private String convertPinyinToToneMarks(String pinyinWithNumbers) {
        if (pinyinWithNumbers == null || pinyinWithNumbers.isEmpty()) {
            return "";
        }
        
        String[] syllables = pinyinWithNumbers.split(" ");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < syllables.length; i++) {
            if (i > 0) {
                result.append(" ");
            }
            result.append(ToneHelper.convertNumberedSyllableToAccentedSyllable(syllables[i]));
        }
        
        return result.toString();
    }
    
    private void drawWrappedText(Graphics2D g2d, String text, int x, int y, int maxWidth, int maxY, Font font) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        
        FontMetrics fm = g2d.getFontMetrics(font);
        int lineHeight = fm.getHeight();
        int currentY = y;
        
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        
        for (String word : words) {
            // Check if we have space for another line
            if (currentY + lineHeight > maxY) {
                break;
            }
            
            // Try adding word to current line
            String testLine = line.length() == 0 ? word : line + " " + word;
            int testWidth = fm.stringWidth(testLine);
            
            if (testWidth <= maxWidth) {
                line = new StringBuilder(testLine);
            } else {
                // Word doesn't fit on current line
                if (line.length() > 0) {
                    // Flush current line first
                    if (currentY + lineHeight <= maxY) {
                        g2d.drawString(line.toString(), x, currentY);
                        currentY += lineHeight;
                    } else {
                        break;
                    }
                }
                
                // Try to fit word on next line
                line = new StringBuilder();
                int wordWidth = fm.stringWidth(word);
                
                if (wordWidth <= maxWidth) {
                    // Word fits on its own line
                    line.append(word);
                } else {
                    // Word is too long, break it at character level
                    StringBuilder charBuffer = new StringBuilder();
                    for (char c : word.toCharArray()) {
                        if (currentY + lineHeight > maxY) {
                            break;
                        }
                        
                        String testChar = charBuffer.toString() + c;
                        if (fm.stringWidth(testChar) <= maxWidth) {
                            charBuffer.append(c);
                        } else {
                            // Flush line
                            if (charBuffer.length() > 0 && currentY + lineHeight <= maxY) {
                                g2d.drawString(charBuffer.toString(), x, currentY);
                                currentY += lineHeight;
                            }
                            charBuffer = new StringBuilder(String.valueOf(c));
                            
                            if (currentY + lineHeight > maxY) {
                                break;
                            }
                        }
                    }
                    line = charBuffer;
                }
            }
        }
        
        // Draw remaining text if it fits
        if (line.length() > 0 && currentY + lineHeight <= maxY) {
            g2d.drawString(line.toString(), x, currentY);
        }
    }
    
    private byte[] embedImagesInPdf(List<BufferedImage> pageImages) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            
            for (BufferedImage image : pageImages) {
                PDPage page = new PDPage(PDRectangle.LETTER);
                document.addPage(page);
                
                PDImageXObject pdImage = LosslessFactory.createFromImage(document, image);
                
                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    // Draw at original page size (unscaled) so PDF dimensions stay correct
                    content.drawImage(pdImage, 0, 0, 612, 792);
                }
            }
            
            document.save(output);
            logger.info("Embedded {} page images into PDF", pageImages.size());
            return output.toByteArray();
        }
    }
}
