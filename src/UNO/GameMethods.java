package UNO;

import java.util.Random;
import java.util.Scanner;

import static UNO.Bot.botPlaysCard;


public class GameMethods {

    private static CardDeck cardDeck = new CardDeck();
    static DiscardPile discardPile = new DiscardPile();
    private static PlayerList playerList = new PlayerList();
    private static boolean penaltyGiven; //NEW
    public static String color;
    private static Player currentPlayer;

    protected static Player previousPlayer;
    boolean discardPileCardIsOfLastTurn = false;
    boolean isClockwise = true;
    static int currentPlayerIndex;

    public static boolean isNextRound() {
        return nextRound;
    }


    static boolean blocked;
    protected static boolean nextRound;

    protected static boolean chosenCardValid;

    public static boolean isChosenCardValid() {
        return chosenCardValid;
    }

    public static void setPreviousPlayer(Player previousPlayer) {
        GameMethods.previousPlayer = previousPlayer;
    }

    public static void setChosenCardValid(boolean chosenCardValid) {
        GameMethods.chosenCardValid = chosenCardValid;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }


    public static Player getCurrentPlayer() {
        return currentPlayer;
    }

    public static void setCurrentPlayer(Player currentPlayer) {
        GameMethods.currentPlayer = currentPlayer;
    }

    public static DiscardPile getDiscardPile() {
        return discardPile;
    }

    public static String getColor() {
        return color;
    }

    public static void setColor(String color) {
        GameMethods.color = color;
    }

    public static boolean isPenaltyGiven() {
        return penaltyGiven;
    }

    public static void setPenaltyGiven(boolean penaltyGiven) {
        GameMethods.penaltyGiven = penaltyGiven;
    }

    public static boolean isBlocked() {
        return blocked;
    }

    public static void setBlocked(boolean blocked) {
        GameMethods.blocked = blocked;
    }

    Helpdesk helpdesk = new Helpdesk();

    public void prepareGame() {  // creates the cards, shuffles them, puts the first card on the table
        cardDeck.createCards(Type.YELLOW);
        cardDeck.createCards(Type.RED);
        cardDeck.createCards(Type.BLUE);
        cardDeck.createCards(Type.GREEN);
        cardDeck.createActionCards();
        cardDeck.shuffleCards();
        setPlayersForTheRound(); // player can choose numbers of human players and bots
        System.out.println();
        cardDeck.distributeCards(playerList, cardDeck); //each player receives 7 cards
        setBlocked(false);
        System.out.println();
        firstPlayer(); //defines, which player will start the game
        System.out.println();
        putFirstCardOnTable();

    }

    public void prepareNextRound() {
        putAllCardsBackIntoCardDeck(); //before a next round is played, all played cards and the cards in hand are put back into the cardDeck
        cardDeck.shuffleCards();
        cardDeck.distributeCards(playerList, cardDeck);
        setBlocked(false);
        firstPlayer();
        System.out.println();
        putFirstCardOnTable();

    }

    public void putAllCardsBackIntoCardDeck() {
        for (int i = 1; i <= 4; i++) { //each player puts his cards back into the cardDeck
            Player player = playerList.getPlayerByID(i);
            cardDeck.cardDeck.addAll(player.cardsInHand);
            player.cardsInHand.removeAll(player.cardsInHand);
        }
        cardDeck.cardDeck.addAll(discardPile.getDiscardPile()); // the cards on the discardpile are also put back into the carddeck
        DiscardPile.discardPile.removeAll(discardPile.getDiscardPile());
    }

    public void firstPlayer() {
        System.out.println("Setting the first player...");
        Random rand = new Random();
        int initialPlayerIndex = rand.nextInt(3); //chooses a random int between 1 and 4
        setCurrentPlayerIndex(initialPlayerIndex); //the first player is setted as the currentplayer
        setCurrentPlayer(getPlayerByIndex(initialPlayerIndex));
        System.out.println(getPlayerByIndex(currentPlayerIndex).getName() + ", you start the game. ");
        if (currentPlayer instanceof Human) {
            helpdesk.helpFile();
        }
    }

    public void putFirstCardOnTable() {

        Card firstCard = cardDeck.dealCard();
        discardPile.addCardIn(0, firstCard);
    }

    public static void colorChangeCard() { //method for when a player used a COLORCHANGE card
        Player currentPlayer = getCurrentPlayer();
        Card playedCard = currentPlayer.getPlayedCard();
        try {
            if (playedCard.getType().equals(Type.PLUS_4) || playedCard.getType().equals(Type.COLORCHANGE)) {
                System.out.println(currentPlayer.getName() + " please choose a color: ");
                if (currentPlayer instanceof Bot) { //random color will be generated if player is Bot
                    Random random = new Random();
                    String[] colors = {"RED", "YELLOW", "BLUE", "GREEN"}; //String array of colors we can use to generate a random color
                    String color = (colors[random.nextInt(colors.length)]);
                    System.out.println(color);
                    setColor(color);
                } else { // color will be entered if player is human
                    Scanner input = new Scanner(System.in);
                    String color = input.nextLine().toUpperCase();
                    setColor(color);
                }
            }
        } catch (
                NullPointerException e) { //NPE can happen if current player has a valid card to play but inputs a card that she does not have in hand.
            System.out.printf(currentPlayer.getName() + ", you just made an invalid move!");
            currentPlayer.cardsInHand.add(cardDeck.dealCard());
            System.out.println("You have to take a card as a penalty and you are now blocked from making further moves.");

        }
    }

    public void checkIfCurrentPlayerMustBePenalized() {
        Player currentPlayer = getCurrentPlayer();
        Card card = discardPile.showLastCard(); //we look at the last card that has been played and check if the currentplayer must be penalized

        if (card.getType().equals(Type.PLUS_4) && !isPenaltyGiven()) {
            System.out.println(getCurrentPlayer().getName() + ", you get 4 penalty cards and you are blocked from playing this turn.");
            plus4Card();
            currentPlayer.printCardsInHand();
            setPenaltyGiven(true); //this is to tell the program that the penalty has been "claimed"
            setBlocked(true); //this is to block the player from making a turn.
            setPreviousPlayer(getPlayerByIndex(currentPlayerIndex));
        } else if ((card.getType().equals(Type.RED_PLUS2) || card.getType().equals(Type.YELLOW_PLUS2)
                || card.getType().equals(Type.GREEN_PLUS2) || card.getType().equals(Type.BLUE_PLUS2)) && !isPenaltyGiven()) {
            System.out.println(getCurrentPlayer().getName() + ", you get 2 penalty cards and you are blocked form playing this turn.");
            plus2Card();
            currentPlayer.printCardsInHand();
            setPenaltyGiven(true);
            setBlocked(true);
            setPreviousPlayer(getPlayerByIndex(currentPlayerIndex));
        }
    }

    public void plus4Card() { //simple but important method to be called in the checkIfCurrentPlayerMustBePenalized() method.
        Player currentPlayer = getCurrentPlayer();
        Card topDiscardCard = discardPile.showLastCard();
        if (topDiscardCard.getType().equals(Type.PLUS_4)) {
            currentPlayer.takeCard(cardDeck.dealCard());
            currentPlayer.takeCard(cardDeck.dealCard());
            currentPlayer.takeCard(cardDeck.dealCard());
            currentPlayer.takeCard(cardDeck.dealCard());
        }
    }

    public void plus2Card() { //simple but important method to be called in the checkIfCurrentPlayerMustBePenalized() method.
        Player currentPlayer = getCurrentPlayer();
        Card topDiscardCard = discardPile.showLastCard();

        if (topDiscardCard.getType().equals(Type.RED_PLUS2) || topDiscardCard.getType().equals(Type.YELLOW_PLUS2)
                || topDiscardCard.getType().equals(Type.GREEN_PLUS2) || topDiscardCard.getType().equals(Type.BLUE_PLUS2)) {
            currentPlayer.takeCard(cardDeck.dealCard());
            currentPlayer.takeCard(cardDeck.dealCard());
        }
    }


    public boolean chosenCardValidityCheck() { // checks if the chosen card is valid to be played
        Player currentPlayer = getCurrentPlayer();
        boolean chosenCardValid = false;
        Card card = currentPlayer.getPlayedCard(); //we compare the played card to the last card on the discardpile
        Card discard = discardPile.showLastCard();

        if (discard.getType().equals(card.getType()) || card.getType().equals(Type.PLUS_4)
                || card.getType().equals(Type.COLORCHANGE) || card.getType().name().charAt(0) == discard.getType().name().charAt(0) //charAt0 is to make sure, that when f.ex. a card with type yellow is put on a card of the type yellow_reverse, the method will see it as a valid move
                || (discard.getType().name().endsWith("PASS") && card.getType().name().endsWith("PASS"))
                || (discard.getType().name().endsWith("2") && card.getType().name().endsWith("2"))
                || (discard.getType().name().endsWith("REVERSE") && card.getType().name().endsWith("REVERSE"))) {
            chosenCardValid = true;
        } else if (discard.getType().equals(Type.COLORCHANGE) || discard.getType().equals(Type.PLUS_4)) {
            if (card.getType().name().charAt(0) == getColor().charAt(0)) { //checks if the played card is of the chosen color of the previously played colorchange/plus4 card
                chosenCardValid = true;
            }
        } else if (discard.getType().equals(Type.GREEN) || discard.getType().equals(Type.YELLOW)
                || discard.getType().equals(Type.RED) || discard.getType().equals(Type.BLUE)) {
            if ((!card.getType().equals(Type.COLORCHANGE) && !card.getType().equals(Type.GREEN_PLUS2)
                    && !card.getType().equals(Type.BLUE_PLUS2) && !card.getType().equals(Type.RED_PLUS2)
                    && !card.getType().equals(Type.YELLOW_PLUS2) && !card.getType().equals(Type.RED_PASS)
                    && !card.getType().equals(Type.GREEN_PASS) && !card.getType().equals(Type.BLUE_PASS)
                    && !card.getType().equals(Type.YELLOW_PASS) && !card.getType().equals(Type.RED_REVERSE)
                    && !card.getType().equals(Type.BLUE_REVERSE) && !card.getType().equals(Type.GREEN_REVERSE)
                    && !card.getType().equals(Type.YELLOW_REVERSE)) && discard.getNumber() == card.getNumber()) {
                chosenCardValid = true;
            }
        } else { //if player chose a card that is not valid based on what is on the top of discard deck he is penalized
            chosenCardValid = false;
            System.out.println("Sorry, this is not a valid move. Now you have to draw a penalty card!");
            currentPlayer.cardsInHand.add(cardDeck.dealCard()); //player draws penalty card
        }
        setChosenCardValid(chosenCardValid);
        return chosenCardValid;
    }


    public int isReverseCard() { //This method is to decide who has the next turn when the card "<->" is played
        int indexOfTheCurrentPlayer = getCurrentPlayerIndex(); //index of the current player

        if (indexOfTheCurrentPlayer == 0) {
            if (isClockwise) {
                indexOfTheCurrentPlayer = 3;
                isClockwise = false;
            } else {
                indexOfTheCurrentPlayer = 1;
                isClockwise = true;
            }
        } else if (indexOfTheCurrentPlayer == 3) {
            if (isClockwise) {
                indexOfTheCurrentPlayer = 2;
                isClockwise = false;
            } else {
                indexOfTheCurrentPlayer = 0;
                isClockwise = true;
            }
        } else {
            if (isClockwise) {
                indexOfTheCurrentPlayer--;
                isClockwise = false;
            } else {
                indexOfTheCurrentPlayer++;
                isClockwise = true;
            }
        }
        return indexOfTheCurrentPlayer;
    }


    public int isPassCard() { //This method is to decide who has the next turn when the passcard is played
        //boolean nicht besser?
        int indexOfTheCurrentPlayer = getCurrentPlayerIndex(); //index of the current player

        if (indexOfTheCurrentPlayer == 1) {
            if (isClockwise) {
                indexOfTheCurrentPlayer = 3;
            } else {
                indexOfTheCurrentPlayer = 3;
            }
        } else if (indexOfTheCurrentPlayer == 3) {
            if (isClockwise) {
                indexOfTheCurrentPlayer = 1;
            } else {
                indexOfTheCurrentPlayer = 1;
            }
        } else if (indexOfTheCurrentPlayer == 2) {
            if (isClockwise) {
                indexOfTheCurrentPlayer = 0;
            } else {
                indexOfTheCurrentPlayer = 0;
            }
        } else {
            if (isClockwise) {
                indexOfTheCurrentPlayer = 2;
            } else {
                indexOfTheCurrentPlayer = 2;

            }
        }
        return indexOfTheCurrentPlayer;
    }

    public int isRegularCard() { //This method is to decide who has the next turn when a normal card is played
        int indexOfTheCurrentPlayer = getCurrentPlayerIndex(); //index of the current player

        if (indexOfTheCurrentPlayer == 1) {
            if (isClockwise) {
                indexOfTheCurrentPlayer = 2;
            } else {
                indexOfTheCurrentPlayer = 0;
            }
        } else if (indexOfTheCurrentPlayer == 0) {
            if (isClockwise) {
                indexOfTheCurrentPlayer = 1;
            } else {
                indexOfTheCurrentPlayer = 3;
            }
        } else if (indexOfTheCurrentPlayer == 3) {
            if (isClockwise) {
                indexOfTheCurrentPlayer = 0;
            } else {
                indexOfTheCurrentPlayer = 2;
            }
        } else {
            if (isClockwise) {
                indexOfTheCurrentPlayer++;
            } else {
                indexOfTheCurrentPlayer--;

            }
        }
        return indexOfTheCurrentPlayer;
    }


    public void playerPlaysCard() {
        Player currentPlayer = getCurrentPlayer();
        Scanner input = new Scanner(System.in);

        if (!discardPileCardIsOfLastTurn && discardPile.getDiscardPile().size() == 1) { //checks if this is a new round
            initialPlayerPlaysCard();
        } else {
            if (!discardPileCardIsOfLastTurn) { //to check whether the card on top of the dcpile is from the last turn (for example pass card)
                checkIfCurrentPlayerMustBePenalized(); //before a player makes a move, it will be checked if the player must receive a penalty.
            }
            if (!isBlocked()) {
                if (hasValidCardToPlay()) {
                    System.out.println(currentPlayer);
                    currentPlayer.printCardsInHand();
                    if (currentPlayer instanceof Human) { //if the currentplayer is human  he can read the helpfile at the beginning of each move
                        helpdesk.helpFile();
                    }
                    System.out.println(currentPlayer.getName() + " , your move! Type in the ID of the card you would like to play.");
                    if (currentPlayer instanceof Bot) {
                        Card cardToPlay = botPlaysCard();
                        currentPlayer.setPlayedCard(cardToPlay);
                    } else {
                        Card cardToPlay = currentPlayer.getCardByID(HumanPlayerPlaysCard());
                        currentPlayer.setPlayedCard(cardToPlay);
                    }
                    colorChangeCard();
                    discardPileCardIsOfLastTurn = false;
                } else {
                    System.out.println("Sorry, " + currentPlayer.getName() + ", you don't have a valid card to play. Please draw a card.");
                    currentPlayer.cardsInHand.add(cardDeck.dealCard()); //cP draws a card
                    currentPlayer.printCardsInHand();
                    if (hasValidCardToPlay()) {
                        if (currentPlayer instanceof Human) { //if the currentplayer is human,he can read the helpfile at the beginning of each move
                            helpdesk.helpFile();
                        }
                        System.out.println(currentPlayer.getName() + " , your move! Type in the ID of the card you would like to play.");
                        if (currentPlayer instanceof Bot) {
                            Card cardToPlay = botPlaysCard();
                            currentPlayer.setPlayedCard(cardToPlay);
                        } else {
                            Card cardToPlay = currentPlayer.getCardByID(HumanPlayerPlaysCard());
                            currentPlayer.setPlayedCard(cardToPlay);
                        }
                        colorChangeCard();
                        discardPileCardIsOfLastTurn = false;
                    } else {
                        System.out.println("Sorry, " + currentPlayer.getName() + " you STILL don't have a card to play out this turn.");
                        discardPileCardIsOfLastTurn = true;
                    }
                }
            }
        }
    }

    public int HumanPlayerPlaysCard() {
        Scanner input = new Scanner(System.in);
        int intCardID = -1;  // Initialize a value that is not a valid card ID
        boolean validInput = false;
        while (!validInput) { //as long as the input is not an int, the player will be asked to put in an integer value
            if (input.hasNextInt()) {
                intCardID = input.nextInt();
                validInput = true;
            } else {
                System.out.println("Invalid input. Please enter an integer value for the card ID.");
                input.next();
            }
        }
        return intCardID;
    }


    public static Player getPlayerByIndex(int playerIndex) {  //returns player by indexnumber
        Player result;
        result = playerList.getPlayerlist().get(playerIndex);
        return result;
    }

    public void nextTurn() { // sets the currentplayerindex according to which type of card has been played
        Card topCard = discardPile.showLastCard();

        try {
            if (!discardPileCardIsOfLastTurn && topCard.getType().equals(Type.YELLOW_REVERSE) || topCard.getType().equals(Type.BLUE_REVERSE)
                    || topCard.getType().equals(Type.RED_REVERSE) || topCard.getType().equals(Type.GREEN_REVERSE)) {
                currentPlayerIndex = isReverseCard();
            } else if (!discardPileCardIsOfLastTurn && (topCard.getType().equals(Type.YELLOW_PASS) || topCard.getType().equals(Type.BLUE_PASS)
                    || topCard.getType().equals(Type.RED_PASS) || topCard.getType().equals(Type.GREEN_PASS))) {
                currentPlayerIndex = isPassCard();
            } else {
                currentPlayerIndex = isRegularCard();
            }
        } catch (NullPointerException e) {
            System.out.println("The previous player skipped a turn because he/she is penalized or blocked."); //or has no card to play
            currentPlayerIndex = isRegularCard();
        }
        setCurrentPlayer(getPlayerByIndex(currentPlayerIndex));
    }


    public void initialPlayerPlaysCard() { //this will be run just once every round
        Player currentPlayer = getCurrentPlayer();
        boolean validFirstCard = true;
        int currentPlayerIndex = getCurrentPlayerIndex();
        int totalPlayers = playerList.getPlayerlist().size();
        // Calculate the previous player index using modulo to wrap around to the last index when currentPlayerIndex is 0.
        int previousPlayerIndex = (currentPlayerIndex - 1 + totalPlayers) % totalPlayers;
        // Set the previous player
        setPreviousPlayer(playerList.getPlayerByID(previousPlayerIndex));

        Scanner input = new Scanner(System.in);
        Card cardToPlay = null;
        Card firstCard = discardPile.showLastCard();

        validFirstCard = isValidFirstCard(firstCard);
        while (!validFirstCard) { //as long as the first card is not valid (plus4), the card will be put back, the deck will be shuffled and another card will be put on the table
            cardDeck.add(firstCard);
            discardPile.getDiscardPile().clear();
            cardDeck.shuffleCards();
            putFirstCardOnTable();
            firstCard = discardPile.showLastCard();
            printTopCardOfDiscardPile();
            validFirstCard = isValidFirstCard(firstCard);
        }

        if (firstCard.getType().equals(Type.RED_PASS) || firstCard.getType().equals(Type.GREEN_PASS) || //if a passcard is the first card on the discard pile, the first player will be skipped
                firstCard.getType().equals(Type.BLUE_PASS) || firstCard.getType().equals(Type.YELLOW_PASS)) {
            System.out.println(currentPlayer.getName() + ", you have skip  this turn.");
            setPreviousPlayer(getPlayerByIndex(currentPlayerIndex));
            setBlocked(true);
            discardPileCardIsOfLastTurn = true;
        } else if (firstCard.getType().equals(Type.RED_PLUS2) || firstCard.getType().equals(Type.YELLOW_PLUS2) //firstplayer has to draw 2 cards and can not play out a card
                || firstCard.getType().equals(Type.GREEN_PLUS2) || firstCard.getType().equals(Type.BLUE_PLUS2)) {
            System.out.println(currentPlayer.getName() + ", you have to draw 2 penalty cards and have to skip this turn.");
            plus2Card();
            setPreviousPlayer(getPlayerByIndex(currentPlayerIndex));
            setBlocked(true);
            discardPileCardIsOfLastTurn = true;
        } else if (firstCard.getType().equals(Type.COLORCHANGE)) { // current player will set the color but the player on the left (nextPlayer) will resume the game

            System.out.println(currentPlayer.getName() + ", choose a color:");

            String color;
            if (currentPlayer instanceof Bot) { //random color will be generated if player is Bot
                Random random = new Random();
                String[] colors = {"RED", "YELLOW", "BLUE", "GREEN"}; //String array of colors we can use to generate a random color
                color = (colors[random.nextInt(colors.length)]);
            } else { // color will be entered if player is human
                input = new Scanner(System.in);
                color = input.nextLine().toUpperCase();
            }

            setColor(color);
            currentPlayer.setPlayedCard(firstCard); // player didn't play any card. just set/added the newColor for the COLORCHANGE card
            setPreviousPlayer(getPlayerByIndex(currentPlayerIndex));

        } else {
            if (hasValidCardToPlay() && !isBlocked()) {
                currentPlayer.printCardsInHand();
                System.out.println(currentPlayer.getName() + " ,your move! Type in the ID of the card you would like to play");
                if (currentPlayer instanceof Bot) {
                    cardToPlay = botPlaysCard();
                    currentPlayer.setPlayedCard(cardToPlay);
                } else {
                    cardToPlay = currentPlayer.getCardByID(HumanPlayerPlaysCard());
                    currentPlayer.setPlayedCard(cardToPlay);
                }
                colorChangeCard(); //to handle COLORCHANGE cards in case player used it
            } else {
                System.out.println("Sorry, " + currentPlayer.getName() + ", you don't have a valid card to play. Please draw a card.");
                //current player nimmt eine Karte vom Deck und fügt sie seinen Karten hinzu
                currentPlayer.cardsInHand.add(cardDeck.dealCard());
                System.out.println(currentPlayer.cardsInHand);
                if (hasValidCardToPlay()) {
                    // remove card from hand, add to card to discard pile = play this card
                    System.out.println(currentPlayer.getName() + " ,your move! Type in the ID of the card you would like to play");
                    if (currentPlayer instanceof Bot) {
                        cardToPlay = botPlaysCard();
                        currentPlayer.setPlayedCard(cardToPlay);
                    } else {
                        cardToPlay = currentPlayer.getCardByID(HumanPlayerPlaysCard());
                        currentPlayer.setPlayedCard(cardToPlay);
                    }
                    colorChangeCard();
                } else {
                    System.out.println("Sorry, " + currentPlayer.getName() + " you don't have a card to play out this turn.");
                    discardPileCardIsOfLastTurn = true;
                }
            }
        }

    }

    private static boolean isValidFirstCard(Card firstCard) { //a plus4 card cannot be the first card in a round
        if (firstCard.getType().equals(Type.PLUS_4)) {
            return false;
        }
        return true;
    }

    public void printTopCardOfDiscardPile() { // just used another color so it is easier to find it on the console
        Card card = discardPile.showLastCard();
        String specialFontColor = "\u001B[35m"; // ANSI escape sequence for pink color
        String resetDefaultFontColor = "\u001B[0m"; // Reset the color back to default

        System.out.println();

        System.out.print(specialFontColor + "DISCARD PILE: ");
        if ((card.getType().equals(Type.COLORCHANGE) || card.getType().equals(Type.PLUS_4))) { //if COLORCHANGE, the newColor must be printed too.
            System.out.print(card + " New Color: " + getColor());
        } else {
            System.out.print(card);
        }
        System.out.println(resetDefaultFontColor);
    }

    public boolean isSameColor() {
        Player currentPlayer = getCurrentPlayer();
        Card c1 = currentPlayer.getPlayedCard();
        Card c2 = discardPile.showLastCard();
        boolean samecolor = false;
        if (c2.getType().equals(Type.PLUS_4) || c2.getType().equals(Type.COLORCHANGE)) {
            if (c1.getType().name().charAt(0) == getColor().charAt(0)) {
                samecolor = true;
            }
        } else {
            char firstLetterCard1 = c1.getType().name().charAt(0);
            char firstLetterCard2 = c2.getType().name().charAt(0);
            if (firstLetterCard1 == firstLetterCard2) {
                samecolor = true;
            }
        }
        return samecolor;
    }

    public static boolean isSameColorWithCardInHand() {
        Player currentPlayer = getCurrentPlayer();
        Card topcard = discardPile.showLastCard();
        boolean topCardIsSameColorWithCardInHand = false;

        for (Card card : currentPlayer.getCardsInHand()) {
            if (topcard.getType().equals(Type.PLUS_4) || topcard.getType().equals(Type.COLORCHANGE)) {
                if (card.getType().name().charAt(0) == getColor().charAt(0)) {
                    topCardIsSameColorWithCardInHand = true;
                    break;
                }
            } else {
                char firstLetterCard1 = card.getType().name().charAt(0);
                char firstLetterCard2 = topcard.getType().name().charAt(0);
                if (firstLetterCard1 == firstLetterCard2) {
                    topCardIsSameColorWithCardInHand = true;
                    break;
                } else {
                    topCardIsSameColorWithCardInHand = false;
                }
            }
        }
        return topCardIsSameColorWithCardInHand;
    }


    public static boolean passCardCheck() {
        Player currentPlayer = getCurrentPlayer();
        Card c1 = currentPlayer.getPlayedCard();
        Card c2 = discardPile.showLastCard();
        boolean bothArePassCards = false;

        if (c1.getType().name().endsWith("PASS") && c2.getType().name().endsWith("PASS")) {
            bothArePassCards = true;
        }

        return bothArePassCards;
    }

    public static boolean plus2Check() {
        Player currentPlayer = getCurrentPlayer();
        Card c1 = currentPlayer.getPlayedCard();
        Card c2 = discardPile.showLastCard();
        boolean bothArePlus2Cards = false;

        if (c1.getType().name().endsWith("2") && c2.getType().name().endsWith("2")) {
            bothArePlus2Cards = true;
        }
        return bothArePlus2Cards;
    }

    public static boolean passCardCheckCardInHand() {
        Player currentPlayer = getCurrentPlayer();
        Card c2 = discardPile.showLastCard();
        boolean bothArePassCards = false;

        for (Card c1 : currentPlayer.getCardsInHand()) {
            if (c1.getType().name().endsWith("PASS") && c2.getType().name().endsWith("PASS")) {
                bothArePassCards = true;
                break;
            }
        }
        return bothArePassCards;
    }

    public static boolean plus2CheckCardInHand() {
        Player currentPlayer = getCurrentPlayer();
        Card c2 = discardPile.showLastCard();
        boolean bothArePlus2Cards = false;

        for (Card c1 : currentPlayer.getCardsInHand()) {
            if (c1.getType().name().endsWith("2") && c2.getType().name().endsWith("2")) {
                bothArePlus2Cards = true;
            }
        }
        return bothArePlus2Cards;
    }


    public static boolean hasValidCardToPlay() { //to check if the currentplayer has at least one valid card to make a move
        boolean isValid = false;
        Player currentPlayer = getCurrentPlayer();
        Card discard = getDiscardPile().showLastCard();

        for (Card card : currentPlayer.cardsInHand) {
            if (discard.getType().equals(card.getType()) || card.getType().equals(Type.PLUS_4)
                    || card.getType().equals(Type.COLORCHANGE) || discard.getType().name().charAt(0) == card.getType().name().charAt(0)
                    || (discard.getType().name().endsWith("PASS") && card.getType().name().endsWith("PASS"))
                    || (discard.getType().name().endsWith("2") && card.getType().name().endsWith("2"))
                    || (discard.getType().name().endsWith("REVERSE") && card.getType().name().endsWith("REVERSE"))) {
                isValid = true;
                break;
            } else if ((discard.getType().equals(Type.GREEN) || discard.getType().equals(Type.YELLOW)
                    || discard.getType().equals(Type.RED) || discard.getType().equals(Type.BLUE))) {

                if ((!card.getType().equals(Type.COLORCHANGE) && !card.getType().equals(Type.GREEN_PLUS2)
                        && !card.getType().equals(Type.BLUE_PLUS2) && !card.getType().equals(Type.RED_PLUS2)
                        && !card.getType().equals(Type.YELLOW_PLUS2) && !card.getType().equals(Type.RED_PASS)
                        && !card.getType().equals(Type.GREEN_PASS) && !card.getType().equals(Type.BLUE_PASS)
                        && !card.getType().equals(Type.YELLOW_PASS) && !card.getType().equals(Type.RED_REVERSE)
                        && !card.getType().equals(Type.BLUE_REVERSE) && !card.getType().equals(Type.GREEN_REVERSE)
                        && !card.getType().equals(Type.YELLOW_REVERSE)) && discard.getNumber() == card.getNumber()) {
                    isValid = true;
                    break;
                }
            } else if (discard.getType().equals(Type.COLORCHANGE) || discard.getType().equals(Type.PLUS_4)) {
                if (discard.getType().equals(card.getType()) || card.getType().name().charAt(0) == getColor().charAt(0)
                        || card.getType().equals(Type.PLUS_4) || card.getType().equals(Type.COLORCHANGE)) {
                    isValid = true;
                    break;
                } else {
                    isValid = false;
                }
            }
        }
        return isValid;
    }


    public static void acceptPlayersInput() { //this method will take the playedCard from the player's hand and add it to the DISCARD DECK.
        Player currentPlayer = getCurrentPlayer();
        Card playedCard = currentPlayer.getPlayedCard();
        if (isChosenCardValid()) {
            if (playedCard.getType().equals(Type.PLUS_4) || playedCard.getType().equals(Type.BLUE_PLUS2) ||
                    playedCard.getType().equals(Type.GREEN_PLUS2) || playedCard.getType().equals(Type.RED_PLUS2)
                    || playedCard.getType().equals(Type.YELLOW_PLUS2)) {
                setBlocked(false);
                setPenaltyGiven(false);
                currentPlayer.setPlayedCard(null);
            } else {
                setBlocked(false); //IMPORTANT! Resets this value after every player's turn
                setPenaltyGiven(true);
                currentPlayer.setPlayedCard(null);
            }
            discardPile.addCardIn(0, playedCard);
            currentPlayer.cardsInHand.remove(playedCard);
        } else {
            System.out.println("Chosen card is invalid!! Now you have to draw a penalty card!");
            currentPlayer.cardsInHand.add(cardDeck.dealCard());
        }
    }

    public Player getPreviousPlayer() {
        int currentPlayerIndex = getCurrentPlayerIndex();
        if (currentPlayerIndex == 0) {
            if (isClockwise) {
                currentPlayerIndex = 3;
            } else {
                currentPlayerIndex++;
            }
        } else if (currentPlayerIndex == 3) {
            if (isClockwise) {
                currentPlayerIndex = 2;
            } else {
                currentPlayerIndex = 0;
            }
        } else {
            if (isClockwise) {
                currentPlayerIndex--;
            } else {
                currentPlayerIndex++;
            }
        }
        Player previousPlayer = playerList.getPlayerByID(currentPlayerIndex);
        return previousPlayer;
    }

    public static void resetColorToDefault() {
        if (discardPile.getSizeofDiscardPile() > 1) {
            if ((discardPile.getDiscardPile().get(1).getType().equals(Type.COLORCHANGE)
                    || discardPile.getDiscardPile().get(1).getType().equals(Type.PLUS_4)) && (!discardPile.getDiscardPile().get(0).getType().equals(Type.COLORCHANGE)
                    && !discardPile.getDiscardPile().get(0).getType().equals(Type.PLUS_4))) {
                setColor(null);
            }
        }
    }

    public boolean sayUno() { //the player who as only one card left has to type in "uno" (correctly), otherwise he will be penalized
        String string;
        boolean UNO = false;
        if (currentPlayer.cardsInHand.size() == 1) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("You have only one card left in your hand! Tres, dos, ...");
            if (currentPlayer instanceof Human) {
                string = scanner.next().toLowerCase();
            } else {
                string = "uno";
                System.out.println("uno");
            }

            if (string.equals("uno")) {
                UNO = true;
            } else if (!string.equals("uno") || string == null) {
                System.out.println("Oops, now you have to draw a penalty card!");
                currentPlayer.cardsInHand.add(cardDeck.dealCard());
                currentPlayer.printCardsInHand();
            }
        }
        getCurrentPlayer().setSaidUno(UNO);
        return UNO;
    }

    public boolean winnerOftheRound() { // to check if there is a winner of the round, so the current round is over.
        boolean isWinnerofTheRound = false;
        if (currentPlayer.cardsInHand.size() == 0) {
            System.out.println(currentPlayer.getName() + ", you win this round!");
            isWinnerofTheRound = true;
        }
        getCurrentPlayer().setWinnerOftheRound(isWinnerofTheRound);
        return isWinnerofTheRound;
    }

    public void countPoints() { //counts the points of the remaining cards in the other players hands
        int points = 0;
        Player winneroftheRound = null;
        for (Player p : playerList.getPlayerlist()) {

            for (Card c : p.cardsInHand) {
                if (c.getType().equals(Type.BLUE_PASS) || c.getType().equals(Type.RED_PASS) || c.getType().equals(Type.GREEN_PASS) || c.getType().equals(Type.YELLOW_PASS)
                        || c.getType().equals(Type.BLUE_REVERSE) || c.getType().equals(Type.RED_REVERSE) || c.getType().equals(Type.YELLOW_REVERSE) || c.getType().equals(Type.GREEN_REVERSE)
                        || c.getType().equals(Type.BLUE_PLUS2) || c.getType().equals(Type.RED_PLUS2) || c.getType().equals(Type.GREEN_PLUS2) || c.getType().equals(Type.YELLOW_PLUS2)) {
                    points = points + 20;
                }
                if (c.getType().equals(Type.PLUS_4) || c.getType().equals(Type.COLORCHANGE)) {
                    points = points + 50;
                }
            }
            if (p.isWinnerOftheRound()) {
                winneroftheRound = p;
            }
        }
        winneroftheRound.setPoints(winneroftheRound.getPoints() + points);
        System.out.println(winneroftheRound.getName() + ", you receive " + points + " points this round! Total points: " + winneroftheRound.getPoints());

    }

    public boolean PlayerWantsToExitTheGame() { //at the end of each round, the game can stop the game
        boolean exit = false;
        if (currentPlayer instanceof Human) {
            Scanner scanner = new Scanner(System.in);
            String input;
            do {
                System.out.println("Press 'Enter' to continue or 'X' to exit the game.");
                input = scanner.nextLine();
                if (input.equalsIgnoreCase("X")) {
                    exit = true;
                } else {
                    exit = false;
                }
            } while (!input.equalsIgnoreCase("X") && !input.isEmpty());
        }
        return exit;
    }


    public void GameWinner() {

        for (Player p : playerList.getPlayerlist()) {
            if (p.getPoints() >= 500) {
                System.out.println("The Game is over and " + p.getName() + " is the winner of the Game! Congrats!");
            }
        }

    }
    public boolean GameIsOver(){
        boolean itsOver = false;
        for (Player p : playerList.getPlayerlist()) {
            if (p.getPoints() >= 500) {
                itsOver = true;
            }
        }
        return itsOver;
    }


    public void shuffleCardsWhenCardDeckIsEmpty() { //checks if the CardDeck is empty and puts the discard pile cards back into the CardDeck
        if (cardDeck.getSizeofCardDeck() == 0) {
            cardDeck.cardDeck.addAll(discardPile.getDiscardPile());
            DiscardPile.discardPile.removeAll(discardPile.getDiscardPile());
            cardDeck.shuffleCards();
            putFirstCardOnTable();
        }
    }

    public static void botsPlayers(int bots) { //method to set up Bot players and add these to player's list
        String[] botNames = {"BOT 1", "BOT 2", "BOT 3", "BOT 4"};
        String name;
        Random random = new Random();
        for (int i = 0; i < bots; i++) {
            boolean nameExists;
            do {
                int temp = random.nextInt(botNames.length); //generate random name from the botNames[] array
                name = botNames[temp];
                nameExists = false; //resets to default value

                for (Player player : playerList.getPlayerlist()) {
                    if (player.getName().equals(name.toUpperCase())) {
                        nameExists = true; //check if name already exists so that name will be unique
                        break;
                    }
                }
            } while (nameExists);
            playerList.getPlayerlist().add(new Bot(name.toUpperCase())); //Created an instance of Bot with (name) then adds Bot to the playerList
            System.out.println(name + " is added.");
        }
    }

    public static void humanPlayers(int humanPlayers) { //method to collect names for Human Players and add these to player's list
        Scanner scanner = new Scanner(System.in);
        for (int i = 0; i < humanPlayers; ) {
            String name;
            boolean nameExists;

            do {
                System.out.println("Please type in your name: ");
                name = scanner.nextLine().toUpperCase();
                nameExists = false; //resets to default value

                for (Player player : playerList.getPlayerlist()) {
                    if (player.getName().equals(name)) { //check if name already exists
                        break;
                    }
                }

                if (nameExists || name.isEmpty()) {
                    System.out.println("This field cannot be empty and name must be unique!");
                }
            } while (nameExists || name.isEmpty());

            playerList.add(new Human(name)); //Created an instance of Human with (name) then adds Human to the playerList
            i++;
        }
    }

    protected static void setPlayersForTheRound() { // set up players for the round (humans and bots)
        Scanner input = new Scanner(System.in);
        int answer = 0;
        while (true) {
            System.out.println("Enter the number of Bots that will be playing in this game (0-4): ");
            answer = input.nextInt();
            try {
                if (answer >= 0 && answer <= 4) {
                    break; // Valid input, exit the loop
                } else {
                    System.out.println("Invalid input! Please enter a number between 0 and 4."); //will be repeated till desired input is entered
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number between 0 and 4.");
            }
        }

        if (answer > 0 && answer <= 4) {
            botsPlayers(answer); //this is a method to set up bot players
        } else {
            System.out.println("OK, only humans will play this round!");
        }
        int numberOfHumanPlayers = 4 - answer; //max player(4) - number of bots = number of human players
        humanPlayers(numberOfHumanPlayers); // method to set up human players
    }
}

