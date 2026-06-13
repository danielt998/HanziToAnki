package hanziToAnki.chinese;

import hanziToAnki.DictionaryExtractor;
import hanziToAnki.Word;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts Chinese word definitions from CC-CEDICT dictionary format.
 * Adds an on-disk binary snapshot for fast startup and optional string pooling
 * to reduce memory usage.
 */
public class ChineseDictionaryExtractor implements DictionaryExtractor {
    private static final Logger logger = LoggerFactory.getLogger(ChineseDictionaryExtractor.class);
    private static final String DEFAULT_DICTIONARY_FILENAME = "cedict_ts.u8";
    private static final char COMMENT_CHARACTER = '#';

    // Pre-size maps to reduce rehashes during bulk dictionary load. CEDICT has ~110k entries.
    private static final int EXPECTED_DICT_ENTRIES = 140000;
    private final Map<String, Word> simplifiedMapping = new HashMap<>(EXPECTED_DICT_ENTRIES);
    private final Map<String, Word> traditionalMapping = new HashMap<>(EXPECTED_DICT_ENTRIES);

    // Simple string pool to deduplicate repeated strings and reduce retained memory.
    private final Map<String, String> stringPool = new HashMap<>();

    private static final String CACHE_DIR = System.getProperty("user.home") + "/.cache/";
    private static final String CACHE_FILE = "cedict.ser";
    private static final String CACHE_MAGIC = "CEDICTSER_v1";

    @Override
    public void readInDictionary() {
        InputStream dictionaryStream = getClass().getResourceAsStream("/dictionary/" + DEFAULT_DICTIONARY_FILENAME);
        if (dictionaryStream == null) {
            logger.error("Dictionary file not found: /dictionary/{}", DEFAULT_DICTIONARY_FILENAME);
            return;
        }

        try {
            // read all bytes so we can compute CRC and also reuse stream
            byte[] raw = dictionaryStream.readAllBytes();
            long crc = computeCrc32(raw);
            Path cachePath = Paths.get(CACHE_DIR, CACHE_FILE);

            if (Files.exists(cachePath)) {
                try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(cachePath)))) {
                    String magic = in.readUTF();
                    if (CACHE_MAGIC.equals(magic)) {
                        long cachedCrc = in.readLong();
                        if (cachedCrc == crc) {
                            // load snapshot
                            int n = in.readInt();
                            for (int i = 0; i < n; i++) {
                                String simplified = readString(in);
                                String traditional = readString(in);
                                String pinyinNoTones = readString(in);
                                String pinyinWithTones = readString(in);
                                String definition = readString(in);
                                // pool strings
                                simplified = pool(simplified);
                                traditional = pool(traditional);
                                pinyinNoTones = pool(pinyinNoTones);
                                pinyinWithTones = pool(pinyinWithTones);
                                definition = pool(definition);
                                ChineseWord cw = new ChineseWord(traditional, simplified, pinyinNoTones, pinyinWithTones, definition);
                                simplifiedMapping.put(simplified, cw);
                                traditionalMapping.put(traditional, cw);
                            }
                            logger.info("Loaded dictionary from cache ({} entries)", simplifiedMapping.size());
                            return;
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Failed to read cache, falling back to parsing: {}", e.toString());
                }
            }

            // parse from raw bytes
            try (InputStream is = new java.io.ByteArrayInputStream(raw)) {
                readInDictionary(is);
            }

            // save snapshot
            try {
                Files.createDirectories(Paths.get(CACHE_DIR));
                try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(cachePath)))) {
                    out.writeUTF(CACHE_MAGIC);
                    out.writeLong(crc);
                    // write number of simplified entries (use simplified map size)
                    out.writeInt(simplifiedMapping.size());
                    for (Map.Entry<String, Word> e : simplifiedMapping.entrySet()) {
                        ChineseWord w = (ChineseWord) e.getValue();
                        writeString(out, w.simplified());
                        writeString(out, w.traditional());
                        writeString(out, w.pinyin());
                        writeString(out, w.pinyinTones());
                        writeString(out, w.definition());
                    }
                }
                logger.info("Wrote dictionary snapshot to {}", cachePath);
            } catch (Exception e) {
                logger.warn("Failed to write cache: {}", e.toString());
            }

        } catch (IOException e) {
            logger.error("Could not load dictionary file", e);
        }
    }

    private void readInDictionary(InputStream stream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            List<String> lines = reader.lines()
                    .filter(line -> !line.isEmpty() && line.charAt(0) != COMMENT_CHARACTER)
                    .toList();

            int cores = Math.max(1, Runtime.getRuntime().availableProcessors());
            logger.info("Parsing dictionary with {} lines using up to {} threads", lines.size(), cores);

            ConcurrentHashMap<String, Word> simpLocal = new ConcurrentHashMap<>(EXPECTED_DICT_ENTRIES);
            ConcurrentHashMap<String, Word> tradLocal = new ConcurrentHashMap<>(EXPECTED_DICT_ENTRIES);

            lines.parallelStream()
                    .map(this::getWordFromLineWithPool)
                    .filter(Objects::nonNull)
                    .forEach(word -> {
                        if (word instanceof ChineseWord w) {
                            simpLocal.put(w.simplified(), word);
                            tradLocal.put(w.traditional(), word);
                        }
                    });

            simplifiedMapping.putAll(simpLocal);
            traditionalMapping.putAll(tradLocal);

            logger.info("Successfully loaded dictionary with {} simplified and {} traditional words (parallel)",
                    simplifiedMapping.size(), traditionalMapping.size());
        } catch (IOException e) {
            logger.error("Could not load dictionary file", e);
        }
    }

    @Override
    public Optional<Word> getWord(char c) {
        return getWord(String.valueOf(c));
    }

    @Override
    public Optional<Word> getWord(String s) {
        Word word = simplifiedMapping.getOrDefault(s, traditionalMapping.get(s));
        if (word != null) {
            return Optional.of(word);
        }

        if (mightBeErhua(s)) {
            String stripped = sanitiseErhua(s);
            word = simplifiedMapping.getOrDefault(stripped, traditionalMapping.get(stripped));
            if (word != null) {
                return Optional.of(word);
            }
        }

        return Optional.empty();
    }

    private Word getWordFromLineWithPool(String line) {
        Word raw = getWordFromLine(line);
        if (raw == null) return null;
        if (raw instanceof ChineseWord w) {
            String simp = pool(w.simplified());
            String trad = pool(w.traditional());
            String pNo = pool(w.pinyin());
            String pTone = pool(w.pinyinTones());
            String def = pool(w.definition());
            return new ChineseWord(trad, simp, pNo, pTone, def);
        }
        return raw;
    }

    /**
     * Parses a CC-CEDICT format line: "Traditional Simplified [pinyin] /definition/"
     */
    private Word getWordFromLine(String line) {
        int pinyinStart = line.indexOf('[');
        int pinyinEnd = (pinyinStart == -1) ? -1 : line.indexOf(']', pinyinStart);
        if (pinyinStart == -1 || pinyinEnd == -1) {
            return null;
        }

        String pinyinRaw = line.substring(pinyinStart + 1, pinyinEnd);
        StringBuilder noTones = new StringBuilder(pinyinRaw.length());
        for (int i = 0; i < pinyinRaw.length(); i++) {
            char c = pinyinRaw.charAt(i);
            if (c >= '1' && c <= '5') continue;
            if (c == ' ') continue;
            noTones.append(c);
        }
        String pinyinNoTones = noTones.toString().toLowerCase();
        String pinyinWithTones = pinyinRaw.toLowerCase();

        String charsPart = line.substring(0, pinyinStart).trim();
        if (charsPart.isEmpty()) return null;
        int firstSpace = charsPart.indexOf(' ');
        if (firstSpace == -1) return null;
        int startSimplified = firstSpace + 1;
        while (startSimplified < charsPart.length() && charsPart.charAt(startSimplified) == ' ') startSimplified++;
        if (startSimplified >= charsPart.length()) return null;
        int endSimplified = charsPart.indexOf(' ', startSimplified);
        String traditional = charsPart.substring(0, firstSpace);
        String simplified = (endSimplified == -1) ? charsPart.substring(startSimplified) : charsPart.substring(startSimplified, endSimplified);

        int defStart = line.indexOf('/', pinyinEnd);
        if (defStart == -1) return null;
        int defEnd = line.lastIndexOf('/');
        if (defEnd <= defStart) return null;
        String definition = line.substring(defStart + 1, defEnd);

        return new ChineseWord(traditional, simplified, pinyinNoTones, pinyinWithTones, definition);
    }

    private boolean mightBeErhua(String word) {
        return word.endsWith("儿");
    }

    private String sanitiseErhua(String word) {
        return word.substring(0, word.length() - 1);
    }

    // Simple string pool to reuse identical strings and reduce memory footprint
    private synchronized String pool(String s) {
        if (s == null) return null;
        String existing = stringPool.get(s);
        if (existing != null) return existing;
        stringPool.put(s, s);
        return s;
    }

    private long computeCrc32(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return crc.getValue();
    }

    private void writeString(DataOutputStream out, String s) throws IOException {
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        out.writeInt(b.length);
        out.write(b);
    }

    private String readString(DataInputStream in) throws IOException {
        int len = in.readInt();
        byte[] b = new byte[len];
        in.readFully(b);
        return new String(b, StandardCharsets.UTF_8);
    }
}
