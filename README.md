# Introduction

Pass HanziToAnki a file via command-line or web interface, and generate flashcards for Anki/Pleco/Memrise.

You can generate the flashcards from any Chinese input - a news article you're studying, song lyrics, or even your exported WeChat logs!

## Running HanziToAnki

HanziToAnki supports two modes: **API Server** (web interface) and **CLI Tool** (command-line). The same JAR/Gradle build supports both modes automatically.

### API Server (Web Interface)

**Run with JAR:**
```bash
java -jar build/libs/HanziToAnki-1.0.0.jar
```

**Run with Gradle:**
```bash
./gradlew runApi
```

Then open http://localhost:8080/ in your browser to upload files and generate decks.

### CLI Tool (Command-Line)

**Run with JAR:**
```bash
java -jar build/libs/HanziToAnki-1.0.0.jar input.txt
java -jar build/libs/HanziToAnki-1.0.0.jar input.txt -f ANKI -o output.anki
```

**Run with Gradle:**
```bash
./gradlew runCli -Pargs='input.txt'
./gradlew runCli -Pargs='input.txt -f ANKI -o output.anki'
```

*Command-line* options:
* `-w --word-list` Read from an input file containing a list of words, separated by line breaks. Without this flag, individual characters are extracted
* `-s --single-characters` Extract only single characters from the file 
* `-hsk <hsk level>` Remove any words in any HSK levels up to and including the given one
* `-t --strategy <strategy>` Specify the word finding strategy. See ChineseWordFinder.Strategy enum for details
* `-o <output filename>` Override the default output file name
* `-f --format <output format>` Override the default output file name (ANKI, PLECO, MEMRISE)
* `-c --char-type <char type>` Specify the type of character (TRAD, SIMP or SIMP_AND_TRAD)

Features:
* Both simplified and traditional hanzi can be provided
* English definitions provided by CC-CEDICT
* Pinyin has accented characters (nĭ hăo instead of ni3 hao3)
* Pinyin is coloured by HTML markup
* Can ignore vocabulary below specified HSK level (reduces card count & saves time)
* Fully Open-Source, so you can contribute features, create issue tickets on GitHub, or even help fix those issues!


Please feel free to make suggestions, open/comment on issues, or share code!

# Development

Feel free to fork, create branches, and raise PRs.

## Prerequisites

If you get an error about "Invalid source release", check that `echo $JAVA_HOME` points to your JDK. We recommend [sdkman](https://sdkman.io/install) for setting up Java.

The project uses **Java 21** and **Gradle 8.9**.

## Building

Build the dual-mode JAR (supports both API and CLI):
```bash
./gradlew build
```

The JAR will be at `build/libs/HanziToAnki-1.0.0.jar`

## Running Locally

See [Running HanziToAnki](#running-hanzitoAnki) above for all running options.

Quick reference:
- **API server:** `./gradlew runApi` or `java -jar build/libs/HanziToAnki-1.0.0.jar`
- **CLI tool:** `./gradlew runCli -Pargs='input.txt'` or `java -jar build/libs/HanziToAnki-1.0.0.jar input.txt`

## Tests

Run all tests:
```bash
./gradlew test
```

The project includes:
- 40+ unit tests for core functionality
- 15+ integration tests for output validation with modern Chinese fixtures
- Tests for all 4 word segmentation strategies

You can also run tests with IntelliJ or other IDEs.

## Code Quality

Before raising a PR, please:
- Run tests: `./gradlew test`
- Format code using your IDE with the included `google_checks.xml` CheckStyle configuration


## License 
This project uses a modified version of the CEDICT Chinese dictionary, which can be found here:
https://cc-cedict.org/wiki/
