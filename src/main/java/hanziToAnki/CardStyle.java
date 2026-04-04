package hanziToAnki;

/**
 * Card style options for PDF flashcard generation.
 * Each style defines card dimensions and layout.
 */
public enum CardStyle {
    INDEX_CARD_3x5("3x5 Index Card", 216, 360, 72),      // 3" x 5" (standard)
    BUSINESS_CARD("Business Card", 252, 144, 44),        // 3.5" x 2"
    POSTCARD("Postcard", 360, 216, 56);                   // 5" x 3"
    
    private final String displayName;
    private final int widthPoints;   // Width in points (1 inch = 72 points)
    private final int heightPoints;  // Height in points
    private final int chineseFontSize; // Font size for Chinese characters
    
    CardStyle(String displayName, int widthPoints, int heightPoints, int chineseFontSize) {
        this.displayName = displayName;
        this.widthPoints = widthPoints;
        this.heightPoints = heightPoints;
        this.chineseFontSize = chineseFontSize;
    }
    
    public String displayName() {
        return displayName;
    }
    
    public int widthPoints() {
        return widthPoints;
    }
    
    public int heightPoints() {
        return heightPoints;
    }
    
    public int chineseFontSize() {
        return chineseFontSize;
    }
    
    /**
     * Get the number of cards that fit per row and column on letter page.
     * Returns [cardsPerRow, cardsPerColumn]
     */
    public int[] getCardsPerPage() {
        // Letter page: 612x792 points
        int cardsPerRow = 612 / (widthPoints + 12); // 12 point margin
        int cardsPerCol = 792 / (heightPoints + 12);
        return new int[]{Math.max(1, cardsPerRow), Math.max(1, cardsPerCol)};
    }
}
