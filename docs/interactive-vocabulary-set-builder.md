# Interactive Vocabulary Set Builder

Run the builder from the project root:

```bash
./gradlew runCli -Pargs='--interactive'
```

The prompt accepts one set expression and writes an Anki TSV deck. Enter an output filename when prompted, or
press Enter to write `vocabulary.tsv`.

## Sources

| Source | Meaning |
|---|---|
| `"path/to/list.txt"` | A newline-separated vocabulary list. |
| `"path/to/anki-export.tsv"` | An Anki TSV export. The first column from each row is used as the word. |
| `hsk1` through `hsk6` | Vocabulary from exactly that HSK level. |
| `hsk1-1` through `hsk1-6` | Cumulative vocabulary from HSK 1 up to the specified level. |

Always quote file paths with either single or double quotes. This allows paths containing spaces and keeps
operators unambiguous.

## Operators

| Operator | Name | Result |
|---|---|---|
| `+` | Union | Words in either set. |
| `-` | Difference | Words in the left set that are not in the right set. |
| `&` | Intersection | Words shared by both sets. |
| `( )` | Grouping | Evaluates the enclosed expression first. |

`&` has higher precedence than `+` and `-`. Union and difference are evaluated from left to right. Parentheses
override these rules. Whitespace is optional around operators.

## Examples

Create a deck of Book A words, excluding HSK 5 and all known words from an Anki export:

```text
"book_a_vocabulary.tsv" - (hsk5 + "known_hsk_6_cards.tsv")
```

Keep only Book A words that occur in either HSK 5 or HSK 6:

```text
"book_a_vocabulary.tsv" & (hsk5 + hsk6)
```

Combine two book lists, then remove all vocabulary through HSK 4:

```text
(book_a.tsv + book_b.tsv) - hsk1-4
```

Find words shared by two exported decks:

```text
"hsk_5_deck.tsv" & "hsk_6_deck.tsv"
```

## Errors and empty results

The prompt explains syntax errors and lets you enter a corrected expression. A source must be a readable file or
one of the HSK forms listed above. If the resulting set is empty, no deck is written.
