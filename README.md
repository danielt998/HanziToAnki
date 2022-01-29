# HanziToAnki
## Intro

Pass HanziToAnki a file via command-line or GUI, and generate clashcards Anki/Pleco(to-do)/Memrise(to-do) flashcards.

You can generate the flashcards from any Chinese input - a news article you're studying, song lyrics, or even your exported WeChat logs!

CLI options:
* -w --word-list: Read from an input file containing a list of words, separated by line breaks. Without this flag, individual characters are extracted
* -s --single-characters:\tExtract only single characters from the file
* -o <output filename> Override the default output file name
* -f --format <output format> Override the default output file name
* -hsk <hsk level> Remove any words in any HSK levels up to and including the given one

Features:
* Both simplified and traditional hanzi can be provided
* English definitions provided by CC-CEDICT
* Pinyin has accented characters (nĭ hăo instead of ni3 hao3)
* Pinyin is coloured by HTML markup
* Can ignore vocabulary below specified HSK level (reduces card count & saves time)
* Fully Open-Source, so you can contribute features, create issue tickets on GitHub, or even help fix those issues!


Please feel free to make suggestions, open/comment on issues, or share code!

## Tests
You can either run tests with IntelliJ, or with `./gradlew test`


## License 
This project uses a modified version of the CEDICT Chinese dictionary, which can be found here:
https://cc-cedict.org/wiki/
