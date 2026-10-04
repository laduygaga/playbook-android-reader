package com.playbook.reader.data.sample

import com.playbook.reader.domain.model.Book
import com.playbook.reader.domain.model.BookFileType
import com.playbook.reader.domain.model.Chapter

object SampleBooksProvider {

    val SAMPLE_BOOKS = listOf(
        Book(
            id = "sample_alice",
            title = "Alice's Adventures in Wonderland",
            author = "Lewis Carroll",
            filePath = "sample://alice",
            fileType = BookFileType.EPUB,
            totalChapters = 4,
            progress = 0.15f,
            currentChapterIndex = 0,
            currentScrollOffset = 0
        ),
        Book(
            id = "sample_pride",
            title = "Pride and Prejudice",
            author = "Jane Austen",
            filePath = "sample://pride",
            fileType = BookFileType.EPUB,
            totalChapters = 3,
            progress = 0f,
            currentChapterIndex = 0,
            currentScrollOffset = 0
        )
    )

    fun getSampleChapters(bookId: String): List<Chapter> {
        return when (bookId) {
            "sample_alice" -> listOf(
                Chapter(
                    id = "alice_ch1",
                    bookId = "sample_alice",
                    title = "Chapter I: Down the Rabbit-Hole",
                    content = """
                        Alice was beginning to get very tired of sitting by her sister on the bank, and of having nothing to do: once or twice she had peeped into the book her sister was reading, but it had no pictures or conversations in it, 'and what is the use of a book,' thought Alice 'without pictures or conversations?'

                        So she was considering in her own mind (as well as she could, for the hot day made her feel very sleepy and stupid), whether the pleasure of making a daisy-chain would be worth the trouble of getting up and picking the daisies, when suddenly a White Rabbit with pink eyes ran close by her.

                        There was nothing so VERY remarkable in that; nor did Alice think it so VERY much out of the way to hear the Rabbit say to itself, 'Oh dear! Oh dear! I shall be late!' (when she thought it over afterwards, it occurred to her that she ought to have wondered at this, but at the time it all seemed quite natural); but when the Rabbit actually TOOK A WATCH OUT OF ITS WASTECOAT-POCKET, and looked at it, and then hurried on, Alice started to her feet, for it flashed across her mind that she had never before seen a rabbit with either a waistcoat-pocket, or a watch to take out of it, and burning with curiosity, she ran across the field after it, and fortunately was just in time to see it pop down a large rabbit-hole under the hedge.

                        In another moment down went Alice after it, never once considering how in the world she was to get out again.

                        The rabbit-hole went straight on like a tunnel for some way, and then dipped suddenly down, so suddenly that Alice had not a moment to think about stopping herself before she found herself falling down a very deep well.

                        Either the well was very deep, or she fell very slowly, for she had plenty of time as she went down to look about her and to wonder what was going to happen next. First, she tried to look down and make out what she was coming to, but it was too dark to see anything; then she looked at the sides of the well, and noticed that they were filled with cupboards and book-shelves; here and there she saw maps and pictures hung upon pegs. She took down a jar from one of the shelves as she passed; it was labelled 'ORANGE MARMALADE', but to her great disappointment it was empty: she did not like to drop the jar for fear of killing somebody, so managed to put it into one of the cupboards as she fell past it.

                        'Well!' thought Alice to herself, 'after such a fall as this, I shall think nothing of tumbling down stairs! How brave they'll all think me at home! Why, I wouldn't say anything about it, even if I fell off the top of the house!' (Which was very likely true.)
                    """.trimIndent(),
                    chapterIndex = 0
                ),
                Chapter(
                    id = "alice_ch2",
                    bookId = "sample_alice",
                    title = "Chapter II: The Pool of Tears",
                    content = """
                        'Curiouser and curiouser!' cried Alice (she was so much surprised, that for the moment she quite forgot how to speak good English); 'now I'm opening out like the largest telescope that ever was! Good-bye, feet!' (for when she looked down at her feet, they seemed to be almost out of sight, they were getting so far off). 'Oh, my poor little feet, I wonder who will put on your shoes and stockings for you now, dears? I'm sure I shan't be able! I shall be a great deal too far off to trouble myself about you: you must manage the best way you can; —but I must be kind to them,' thought Alice, 'or perhaps they won't walk the way I want to go! Let me see: I'll give them a new pair of boots every Christmas.'

                        And she went on planning to herself how she would manage it. 'They must go by the carrier,' she thought; 'and how funny it'll seem, sending presents to one's own feet! And how odd the directions will look!

                        Alice's Right Foot, Esq.
                        Hearthrug,
                        near the Fender,
                        (with Alice's love).'

                        Oh dear, what nonsense I'm talking!

                        Just then her head struck against the roof of the hall: in fact she was now more than nine feet high, and she at once took up the little golden key and hurried off to the garden door.
                    """.trimIndent(),
                    chapterIndex = 1
                ),
                Chapter(
                    id = "alice_ch3",
                    bookId = "sample_alice",
                    title = "Chapter III: A Caucus-Race and a Long Tale",
                    content = """
                        They were indeed a queer-looking party that assembled on the bank—the birds with draggled feathers, the animals with their fur clinging close to them, and all dripping wet, cross, and uncomfortable.

                        The first question of course was, how to get dry again: they had a consultation about this, and after a few minutes it seemed quite natural to Alice to find herself talking familiarly with them, as if she had known them all her life. Indeed, she had a quite a long argument with the Lory, who at last turned sulky, and would only say, 'I am older than you, and must know better'; and this Alice would not admit without knowing how old it was, and as the Lory positively refused to tell its age, there was no more to be said.

                        At last the Mouse, who seemed to be a person of authority among them, called out, 'Sit down, all of you, and listen to me! I'll soon make you dry enough!' They all sat down at once, in a large ring, with the Mouse in the middle. Alice kept her eyes anxiously fixed on it, for she felt sure she would catch a bad cold if she did not get dry very soon.
                    """.trimIndent(),
                    chapterIndex = 2
                )
            )

            "sample_pride" -> listOf(
                Chapter(
                    id = "pride_ch1",
                    bookId = "sample_pride",
                    title = "Chapter I",
                    content = """
                        It is a truth universally acknowledged, that a single man in possession of a good fortune, must be in want of a wife.

                        However little known the feelings or views of such a man may be on his first entering a neighbourhood, this truth is so well fixed in the minds of the surrounding families, that he is considered the rightful property of some one or other of their daughters.

                        "My dear Mr. Bennet," said his lady to him one day, "have you heard that Netherfield Park is let at last?"

                        Mr. Bennet replied that he had not.

                        "But it is," returned she; "for Mrs. Long has just been here, and she told me all about it."

                        Mr. Bennet made no answer.

                        "Do not you want to know who has taken it?" cried his wife impatiently.

                        "You want to tell me, and I have no objection to hearing it."

                        This was invitation enough.

                        "Why, my dear, you must know, Mrs. Long says that Netherfield is taken by a young man of large fortune from the north of England; that he came down on Monday in a chaise and four to see the place, and was so much delighted with it, that he agreed with Mr. Morris immediately; that he is to take possession before Michaelmas, and some of his servants are to be in the house by the end of next week."
                    """.trimIndent(),
                    chapterIndex = 0
                )
            )

            else -> emptyList()
        }
    }
}
