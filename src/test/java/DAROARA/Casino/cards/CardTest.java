//package DAROARA.Casino.cards;
//
//import DAROARA.Casino.server.cards.Card;
//import DAROARA.Casino.server.cards.CardValues;
//import DAROARA.Casino.server.cards.Suits;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class CardTest {
//    Card card = new Card(Suits.CLUBS, CardValues.SIX);
//    Card card2 = new Card(Suits.DIAMONDS,CardValues.EIGHT);
//    Card card3 = new Card(Suits.HEARTS,CardValues.SEVEN);
//    Card card4 = new Card(Suits.SPADES,CardValues.NINE);
//
//    @Test
//    void getSuit() {
//        assertEquals(Suits.CLUBS,card.getSuit());
//        assertEquals(Suits.DIAMONDS,card2.getSuit());
//        assertEquals(Suits.HEARTS,card3.getSuit());
//        assertEquals(Suits.SPADES,card4.getSuit());
//    }
//
//    @Test
//    void getValues() {
//        assertEquals(CardValues.SIX,card.getValues());
//        assertEquals(CardValues.EIGHT,card2.getValues());
//        assertEquals(CardValues.SEVEN,card3.getValues());
//        assertEquals(CardValues.NINE,card4.getValues());
//
//
//
//    }
//
//    @Test
//    void testToString() {
//        assertEquals("Card(6 of CLUBS",card.toString());
//    }
//}