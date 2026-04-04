package hanziToAnki;

import hanziToAnki.chinese.ChineseWord;
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
    
    // Letter page dimensions (8.5" x 11" in points)
    private static final int PAGE_WIDTH = 612;
    private static final int PAGE_HEIGHT = 792;
    
    // Index card dimensions: 3" x 5" in points (1 inch = 72 points)
    private static final int CARD_WIDTH = 216;  // 3 * 72
    private static final int CARD_HEIGHT = 360; // 5 * 72
    private static final int MARGIN = 12;
    private static final int PADDING = 8;
    
    // Layout: 2 columns x 2 rows per page
    private static final int CARDS_PER_ROW = 2;
    private static final int ROWS_PER_PAGE = 2;
    private static final int CARDS_PER_PAGE = CARDS_PER_ROW * ROWS_PER_PAGE;
    
    // Fonts
    private static final Font FONT_CHINESE = new Font("SimSun", Font.PLAIN, 72);
    private static final Font FONT_PINYIN = new Font("Arial", Font.PLAIN, 16);
    private static final Font FONT_DEFINITION = new Font("Arial", Font.PLAIN, 11);
    
    public byte[] generateFlashcardPdf(List<Word> words) throws IOException {
        // Generate images for each page
        List<BufferedImage> pageImages = generatePageImages(words);
        
        // Embed images in PDF
        return embedImagesInPdf(pageImages);
    }
    
    private List<BufferedImage> generatePageImages(List<Word> words) {
        List<BufferedImage> pages = new ArrayList<>();
        
        int totalCards = words.size();
        int totalPages = (totalCards + CARDS_PER_PAGE - 1) / CARDS_PER_PAGE;
        
        for (int pageNum = 0; pageNum < totalPages; pageNum++) {
            BufferedImage pageImage = new BufferedImage(PAGE_WIDTH, PAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = pageImage.createGraphics();
            
            // Set white background
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, PAGE_WIDTH, PAGE_HEIGHT);
            
            // Enable anti-aliasing for smooth text
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            int cardIndex = pageNum * CARDS_PER_PAGE;
            
            for (int row = 0; row < ROWS_PER_PAGE; row++) {
                for (int col = 0; col < CARDS_PER_ROW; col++) {
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
        
        logger.info("Generated {} page images for {} flashcards", pages.size(), totalCards);
        return pages;
    }
    
    private void drawCard(Graphics2D g2d, Word word, int col, int row) {
        ChineseWord chineseWord = (ChineseWord) word;
        
        // Calculate card position
        int x = col * CARD_WIDTH + MARGIN;
        int y = row * CARD_HEIGHT + MARGIN;
        
        // Draw card border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRect(x, y, CARD_WIDTH - MARGIN, CARD_HEIGHT - MARGIN);
        
        // Draw dividing line (horizontal)
        int midY = y + (CARD_HEIGHT - MARGIN) / 2;
        g2d.drawLine(x, midY, x + CARD_WIDTH - MARGIN, midY);
        
        // === FRONT SIDE (Top): Hanzi ===
        int frontY = y + PADDING;
        int frontHeight = (CARD_HEIGHT - MARGIN) / 2;
        
        g2d.setFont(FONT_CHINESE);
        g2d.setColor(Color.BLACK);
        
        String hanzi = chineseWord.simplified();
        FontMetrics fm = g2d.getFontMetrics();
        int hanziWidth = fm.stringWidth(hanzi);
        int hanziX = x + (CARD_WIDTH - MARGIN - hanziWidth) / 2;
        int hanziY = frontY + (frontHeight - fm.getHeight()) / 2 + fm.getAscent();
        
        g2d.drawString(hanzi, hanziX, hanziY);
        
        // === BACK SIDE (Bottom): Pinyin + Definition ===
        int backY = midY + PADDING;
        int backHeight = (CARD_HEIGHT - MARGIN) / 2;
        int backBottom = y + CARD_HEIGHT - MARGIN; // Bottom boundary of card
        
        // Draw pinyin
        g2d.setFont(FONT_PINYIN);
        g2d.setColor(new Color(64, 64, 64)); // Dark gray
        
        String pinyin = chineseWord.pinyin() != null ? chineseWord.pinyin() : "";
        int pinyinX = x + PADDING;
        int pinyinY = backY + 14;
        
        g2d.drawString(pinyin, pinyinX, pinyinY);
        
        // Draw definition with text wrapping
        g2d.setFont(FONT_DEFINITION);
        g2d.setColor(Color.BLACK);
        
        String definition = chineseWord.definition() != null ? chineseWord.definition() : "";
        int defX = x + PADDING;
        int defY = pinyinY + 12;
        int maxWidth = CARD_WIDTH - MARGIN - 2 * PADDING;
        
        drawWrappedText(g2d, definition, defX, defY, maxWidth, backBottom, FONT_DEFINITION);
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
                    content.drawImage(pdImage, 0, 0, PAGE_WIDTH, PAGE_HEIGHT);
                }
            }
            
            document.save(output);
            logger.info("Embedded {} page images into PDF", pageImages.size());
            return output.toByteArray();
        }
    }
}
