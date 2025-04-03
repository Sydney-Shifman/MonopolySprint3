package org.monopoly.Model;

import org.monopoly.Model.Cards.ColorGroup;
import org.monopoly.Model.GameTiles.PropertySpace;
import org.monopoly.Model.Cards.TitleDeedDeck;
import org.monopoly.Model.Players.Player;

import java.util.*;

public class Banker {
    private double bankBalance;
    private TitleDeedDeck deck;
    private int numHouses;
    private int numHotels;
    private static Banker instance;

    public Banker() {
        this.bankBalance = Double.POSITIVE_INFINITY;
        this.deck = new TitleDeedDeck();
        this.numHouses = 32;
        this.numHotels = 32;
    }

    /**
     * Singleton pattern to ensure only one instance of Banker is created.
     * @return The instance of Banker.
     *
     * @author shifmans
     */
    public static Banker getInstance() {
        if (instance == null) {
            instance = new Banker();
        }
        return instance;
    }

    public void sellProperty(String propertyName, Player player) {
        if (deck.getTitleDeeds().getProperty(propertyName).isMortgaged()) {
            throw new IllegalStateException("Property is mortgaged and cannot be sold.");
        }

        deck.getTitleDeeds().getProperty(propertyName).setOwner(player.getName());
        player.subtractFromBalance(deck.getTitleDeeds().getProperty(propertyName).getPrice());
        deck.drawCard(propertyName);
    }

    public void sellHouse(String propertyName, Player player) {
        if (deck.getTitleDeeds().getProperty(propertyName) instanceof PropertySpace) {
            if (this.numHouses == 0) {
                throw new IllegalStateException("There are no houses left.");
            }
            else {
                this.numHouses -= 1;

                int numPropertyHouses = ((PropertySpace) deck.getTitleDeeds().getProperty(propertyName)).getNumHouses();
                player.subtractFromBalance(((PropertySpace) deck.getTitleDeeds().getProperty(propertyName)).getHousePrice());
                ((PropertySpace) deck.getTitleDeeds().getProperty(propertyName)).setNumHouses(numPropertyHouses+1);
            }
        }

        else {
            throw new IllegalArgumentException("This property does not have houses.");
        }
    }

    public void receiveHouse(String propertyName, Player player) {
        if (deck.getTitleDeeds().getProperty(propertyName) instanceof PropertySpace) {
            if (this.numHouses == 0) {
                throw new IllegalStateException("There are no houses left.");
            }
            else {
                this.numHouses += 1;

                int numPropertyHouses = ((PropertySpace) deck.getTitleDeeds().getProperty(propertyName)).getNumHouses();
                player.addToBalance(((PropertySpace) deck.getTitleDeeds().getProperty(propertyName)).getHousePrice()/2);
                ((PropertySpace) deck.getTitleDeeds().getProperty(propertyName)).setNumHouses(numPropertyHouses-1);
            }
        }

        else {
            throw new IllegalArgumentException("This property does not have houses.");
        }
    }

    public void sellHotel(String propertyName, Player player) {
        if (deck.getTitleDeeds().getProperty(propertyName) instanceof PropertySpace) {
            if (this.numHotels == 0) {
                throw new IllegalStateException("There are no hotels left.");
            }
            else {
                this.numHotels -= 1;

                int numPropertyHotels = ((PropertySpace) deck.getTitleDeeds().getProperty(propertyName)).getNumHotels();
                player.subtractFromBalance(((PropertySpace) deck.getTitleDeeds().getProperty(propertyName)).getHousePrice());
                ((PropertySpace) deck.getTitleDeeds().getProperty(propertyName)).setNumHotels(numPropertyHotels+1);
            }
        }

        else {
            throw new IllegalArgumentException("This property does not have hotels.");
        }
    }

    public void receiveHotel(String propertyName, Player player) {
        if (deck.getTitleDeeds().getProperty(propertyName) instanceof PropertySpace) {
            if (this.numHotels == 0) {
                throw new IllegalStateException("There are no hotels left.");
            }
            else {
                this.numHotels += 1;

                int numPropertyHotels = ((PropertySpace) deck.getTitleDeeds().getProperty(propertyName)).getNumHotels();
                player.addToBalance(((PropertySpace) deck.getTitleDeeds().getProperty(propertyName)).getHotelPrice()/2);
                ((PropertySpace) deck.getTitleDeeds().getProperty(propertyName)).setNumHotels(numPropertyHotels-1);
            }
        }

        else {
            throw new IllegalArgumentException("This property does not have hotels.");
        }
    }

    public void auctionProperty(String propertyName, ArrayList<Player> players) {
        int currentBidAmount = 1;
        HashMap<Player, Integer> currentBidding = new HashMap<>();
        Scanner keyboard = new Scanner(System.in);
        Player currentHighestBidder;
        int numRounds = 1;

        while (true) {
            if (numRounds == 1) {
                System.out.println("Starting Bid for " + propertyName + " starting at $" + currentBidAmount);
            } else {
                System.out.println("Round " + numRounds + ", the bid now starts at $" + currentBidAmount);
            }

            ArrayList<Player> bidders = getCurrentBidders(players, currentBidAmount, keyboard);
            ArrayList<Integer> bidAmounts = getBidAmount(currentBidAmount, bidders, keyboard);

            if ((numRounds == 1) && (bidders.isEmpty())) {
                deck.getTitleDeeds().getProperty(propertyName).setOwner("");
                deck.returnCard(propertyName);
                break;
            }

            if (bidders.size() <= 1) {
                endAuction(propertyName, bidders, bidAmounts);
                break;
            }

            for (int i = 0; i < bidders.size(); i++) {
                currentBidding.put(bidders.get(i), bidAmounts.get(i));
            }

            currentHighestBidder = getHighestBidder(currentBidding);
            currentBidAmount = currentBidding.get(currentHighestBidder);

            numRounds++;
        }
    }

    private void endAuction(String propertyName, ArrayList<Player> bidders, ArrayList<Integer> bidAmounts) {
        if (bidders.isEmpty() || bidAmounts.isEmpty()) {
            System.out.println("No valid bids placed for the property.");
            return;
        }

        System.out.println("The Auction for " + propertyName + " ended!");
        System.out.println("Winner: " + bidders.get(0).getName());
        System.out.println("Bid Amount: $" + bidAmounts.get(0));

        deck.getTitleDeeds().getProperty(propertyName).setOwner(bidders.get(0).getName());
        bidders.get(0).subtractFromBalance(bidAmounts.get(0));
    }

    private ArrayList<Player> getCurrentBidders(ArrayList<Player> players, int currentBidAmount, Scanner keyboard) {
        ArrayList<Player> bidders = new ArrayList<>();

        for (Player player : players) {
            if (currentBidAmount <= player.getBalance()) {
                System.out.println(player.getName() + ", do you want to bid on this property (Y/N)? ");
                char answer = keyboard.next().charAt(0);

                //Add countdown timer feature later, 3 seconds to bid

                while ((answer != 'Y') && (answer != 'y') && (answer != 'N') && (answer != 'n')) {
                    System.out.println("Invalid response, " + player.getName() + " do you want to bid on this property (Y/N)? ");
                    answer = keyboard.next().charAt(0);
                }

                if ((answer == 'Y') || (answer == 'y')) {
                    bidders.add(player);
                }
            }

            else {
                System.out.println(player.getName() + " does not have enough money to bid.");
            }
        }
        return bidders;
    }

    private ArrayList<Integer> getBidAmount(int currentBid, ArrayList<Player> bidders, Scanner keyboard) {
        ArrayList<Integer> bidAmounts = new ArrayList<>();

        for (Player bidder : bidders) {
            System.out.println(bidder.getName() + ", how much do you want to bid? ");
            int answer = keyboard.nextInt();

            while (answer <= currentBid) {
                System.out.println("Invalid response, " + bidder.getName() + " how much do you want to bid? ");
                answer = keyboard.nextInt();
            }

            bidAmounts.add(answer);
        }

        return bidAmounts;
    }

    private Player getHighestBidder(HashMap<Player, Integer> currentBidding) {
        int bid = 0;
        Player highestBidder = null;

        for (Player bidder : currentBidding.keySet()) {
            if (currentBidding.get(bidder) > bid) {
                bid = currentBidding.get(bidder);
                highestBidder = bidder;
            }
        }

        return highestBidder;
    }

    public void mortgageProperty(String propertyName) {
        deck.getTitleDeeds().getProperties().get(propertyName).setMortgagedStatus(true);
    }

    public void payGoSpace(Player playerName) {
        playerName.addToBalance(200);
        this.bankBalance -= 200;
    }

    public void receiveMoney(Player playerName, int money) {
        playerName.subtractFromBalance(money);
        this.bankBalance += money;
    }

    /**
     * Checks if the player has a monopoly.
     * @author walshj05
     */
    public void checkForMonopolies(ArrayList<String> propertiesOwned, ArrayList<Monopoly> monopolies, ArrayList<ColorGroup> colorGroups) {
        if (propertiesOwned.contains("Mediterranean Avenue") && propertiesOwned.contains("Baltic Avenue")) {
            if (!colorGroups.contains(ColorGroup.BROWN)) {
                String[] brownProperties = {"Mediterranean Avenue", "Baltic Avenue"};
                monopolies.add(new Monopoly(brownProperties, ColorGroup.BROWN));
            }
        }
        if (propertiesOwned.contains("Oriental Avenue") && propertiesOwned.contains("Vermont Avenue") && propertiesOwned.contains("Connecticut Avenue")) {
            if (!colorGroups.contains(ColorGroup.LIGHT_BLUE)) {
                String[] lightBlueProperties = {"Oriental Avenue", "Vermont Avenue", "Connecticut Avenue"};
                monopolies.add(new Monopoly(lightBlueProperties, ColorGroup.LIGHT_BLUE));
            }
        }
        if (propertiesOwned.contains("St. Charles Place") && propertiesOwned.contains("States Avenue") && propertiesOwned.contains("Virginia Avenue")) {
            if (!colorGroups.contains(ColorGroup.PINK)) {
                String[] pinkProperties = {"St. Charles Place", "States Avenue", "Virginia Avenue"};
                monopolies.add(new Monopoly(pinkProperties, ColorGroup.PINK));
            }
        }
        if (propertiesOwned.contains("St. James Place") && propertiesOwned.contains("Tennessee Avenue") && propertiesOwned.contains("New York Avenue")) {
            if (!colorGroups.contains(ColorGroup.ORANGE)) {
                String[] orangeProperties = {"St. James Place", "Tennessee Avenue", "New York Avenue"};
                monopolies.add(new Monopoly(orangeProperties, ColorGroup.ORANGE));
            }
        }
        if (propertiesOwned.contains("Kentucky Avenue") && propertiesOwned.contains("Indiana Avenue") && propertiesOwned.contains("Illinois Avenue")) {
            if (!colorGroups.contains(ColorGroup.RED)) {
                String[] redProperties = {"Kentucky Avenue", "Indiana Avenue", "Illinois Avenue"};
                monopolies.add(new Monopoly(redProperties, ColorGroup.RED));
            }
        }
        if (propertiesOwned.contains("Atlantic Avenue") && propertiesOwned.contains("Ventnor Avenue") && propertiesOwned.contains("Marvin Gardens")) {
            if (!colorGroups.contains(ColorGroup.YELLOW)) {
                String[] yellowProperties = {"Atlantic Avenue", "Ventnor Avenue", "Marvin Gardens"};
                monopolies.add(new Monopoly(yellowProperties, ColorGroup.YELLOW));
            }
        }
        if (propertiesOwned.contains("Pacific Avenue") && propertiesOwned.contains("North Carolina Avenue") && propertiesOwned.contains("Pennsylvania Avenue")) {
            if (!colorGroups.contains(ColorGroup.GREEN)) {
                String[] greenProperties = {"Pacific Avenue", "North Carolina Avenue", "Pennsylvania Avenue"};
                monopolies.add(new Monopoly(greenProperties, ColorGroup.GREEN));
            }
        }
        if (propertiesOwned.contains("Park Place") && propertiesOwned.contains("Boardwalk")) {
            if (!colorGroups.contains(ColorGroup.DARK_BLUE)) {
                String[] darkBlueProperties = {"Park Place", "Boardwalk"};
                monopolies.add(new Monopoly(darkBlueProperties, ColorGroup.DARK_BLUE));
            }
        }
    }

    public TitleDeedDeck getDeck() {
        return this.deck;
    }

    public int getHouses() {
        return this.numHouses;
    }

    public int getHotels() {
        return this.numHotels;
    }

    public double getBalance() {
        return this.bankBalance;
    }

    /*
    NOTE: THE FOLLOWING METHODS ARE MEANT TO BE USED FOR THE NEW IMPLEMENTATION OF MONOPOLY CLASS
    NOTE: WE CAN REFACTOR THE BANKER CLASS OR MONOPOLY CLASS AS NEEDED, BUT THIS IS NEEDED FOR TESTS
    NOTE: TO PASS!
     */

    /**
     * This method is used to buy a house from the banker.
     * @author walshj05
     */
    public void buyHouse() {
        this.numHouses--;
    }

    /**
     * This method is used to buy a hotel from the banker.
     * @author walshj05
     */
    public void buyHotel() {
        this.numHotels--;
    }

    /**
     * This method is used to return a number of houses to the banker.
     * @param numHouses The number of houses to return.
     * @author walshj05
     */
    public void returnHouses(int numHouses) {
        this.numHouses += numHouses;
    }

    /**
     * This method is used to return a hotel to the banker.
     * @author walshj05
     */
    public void returnHotel() {
        this.numHotels++;
        this.numHouses -= 4;
    }
}
