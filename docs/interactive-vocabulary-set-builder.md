# Interactive Vocabulary Set Builder

Run the builder from the project root:

```bash
./gradlew --console=plain runCli -Pargs='--interactive'
```

The prompt starts a vocabulary shell. It evaluates expressions, stores variables, and creates Anki TSV decks.
Type `help` for its command summary, `variables` to list defined variables, and `exit` or `quit` to leave.

## Scripts

Save statements in a UTF-8 script file and run it with:

```bash
./gradlew --console=plain runCli -Pargs='--script study_plan.h2a'
```

Scripts execute one statement per line. Blank lines and text after `#` are ignored, except when `#` is inside a
quoted filename. Errors identify the script filename and line number.

```text
# study_plan.h2a
book = open_cards("book_a_vocabulary.tsv")
known = open_cards("known_hsk_6_cards.tsv")
new_words = book - (old_hsk("1-5") + known)
write_cards(new_words, "book_a_new_words.tsv")
```

The same statements work directly in the interactive shell.

## Functions

| Function | Meaning |
|---|---|
| `open_cards("path/to/list.txt")` | A newline-separated vocabulary list. |
| `open_cards("path/to/anki-export.tsv")` | An Anki TSV export. The first column from each row is used as the word. |
| `old_hsk("5")` | Vocabulary from exactly HSK level 5. |
| `old_hsk("1-5")` | Cumulative vocabulary from HSK 1 through HSK 5. |
| `old_hsk("1,3,5")` | The union of the exact HSK 1, 3, and 5 vocabularies. |

Quote file paths with either single or double quotes. This allows paths containing spaces and keeps operators
unambiguous.

## Statements

| Statement | Effect |
|---|---|
| `name = expression` | Stores the resulting set in `name`. Variable names start with a letter or underscore and may contain letters, numbers, and underscores. |
| `expression` | Evaluates an expression and prints its word count. |
| `write_cards(expression, "output.tsv")` | Writes the expression's words to an Anki TSV deck. |

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
open_cards("book_a_vocabulary.tsv") - (old_hsk("5") + open_cards("known_hsk_6_cards.tsv"))
```

Keep only Book A words that occur in either HSK 5 or HSK 6:

```text
open_cards("book_a_vocabulary.tsv") & (old_hsk("5") + old_hsk("6"))
```

Combine two book lists, then remove all vocabulary through HSK 4:

```text
(open_cards("book_a.tsv") + open_cards("book_b.tsv")) - old_hsk("1-4")
```

Find words shared by two exported decks:

```text
open_cards("hsk_5_deck.tsv") & open_cards("hsk_6_deck.tsv")
```

Use variables to avoid reopening the same inputs:

```text
book = open_cards("book_a_vocabulary.tsv")
known = open_cards("known_hsk_6_cards.tsv")
write_cards(book - (old_hsk("1-5") + known), "book_a_new_words.tsv")
```

## Errors and empty results

The prompt explains syntax errors and lets you enter a corrected expression. `open_cards` requires a readable
file, and `old_hsk` accepts only the argument forms listed above. If the resulting set is empty, no deck is
written. Scripts stop at the first invalid statement.
