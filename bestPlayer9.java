import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

/**
 * This is the base class for computer player/bots.
 */

public class bestPlayer9 extends Bot {
    Random r = new Random();
    HashMap<String, Piece> pieces; // Keyed off of guest name
    Board board;
    Piece me;
    HashMap<String, Player> players; // Keyed off of player name
    String[] otherPlayerNames;
    TextDisplay display;
    String cardInHand1;
    String cardInHand2;
    String myName;


    private final String[] allCharacters = {"Buford Barnswallow", "Earl of Volesworthy", "Mildred Wellington", "Nadia Bwalya",
            "Viola Chung", "Dr. Ashraf Najem", "Remy La Rocque", "Lily Nesbit", "Trudie Mudge",
            "Stefano Laconi"};
    private String[] cardActions = {
            "get,yellow:ask,Remy La Rocque,",
            "get,:viewDeck",
            "get,red:ask,Nadia Bwalya,",
            "get,green:ask,Lily Nesbit,",
            "viewDeck:ask,Buford Barnswallow,",
            "get,red:ask,Earl of Volesworthy,",
            "get,:ask,Nadia Bwalya,",
            "get,green:ask,Stefano Laconi,",
            "get,yellow:viewDeck",
            "get,:ask,Dr. Ashraf Najem,",
            "get,green:viewDeck",
            "get,red:viewDeck",
            "get,:ask,Mildred Wellington,",
            "get,:move,",
            "get,:ask,Earl of Volesworthy,",
            "get,:ask,Remy La Rocque,",
            "viewDeck:ask,Viola Chung,",
            "get,:ask,Stefano Laconi,",
            "get,:ask,Viola Chung,",
            "get,:viewDeck",
            "get,:ask,Lily Nesbit,",
            "get,yellow:ask,Mildred Wellington,",
            "get,:ask,Buford Barnswallow,",
            "get,:move,",
            "move,:ask,Dr. Ashraf Najem,",
            "get,:viewDeck",
            "get,:ask,Trudie Mudge,",
            "move,:ask,Trudie Mudge,"
    };


    int[] gemCounts = new int[3];

    private String[] cardsRemaining;
    private String[] cardsInHands;

    int[] myGems = new int[3];
    int[] gemsRemaining = new int[3];
    private Boolean firstLoadOfCardsInHand;


    private ArrayList<String> getPiecesNameInRoomWithGem(String gem) {
        ArrayList<String> rval = new ArrayList<>();
        for (Piece pieceName : pieces.values()) {
            for (String gemsAvailable : board.rooms[pieceName.row][pieceName.col].availableGems) {
                if (gemsAvailable.equals(gem)) {
                    rval.add(pieceName.name);
                }
            }
        }
        return rval;
    }

    public static class Board {
        public Room[][] rooms;
        public String gemLocations;


        public class Room {
            public final boolean[] gems = new boolean[3];
            public final String[] availableGems;
            public final int row;
            public final int col;
            private HashMap<String, Piece> pieces; //change back to private after testing

            public void removePlayer(Piece piece) {
                removePlayer(piece.name);
                piece.col = -1;
                piece.row = -1;
            }

            public void removePlayer(String name) {
                pieces.remove(name);
            }

            public void addPlayer(Piece piece) {
                piece.col = this.col;
                piece.row = this.row;
                pieces.put(piece.name, piece);

            }


            public Room(boolean red, boolean green, boolean yellow, int row, int col) {
                pieces = new HashMap<String, Piece>();
                this.row = row;
                this.col = col;
                gems[Suspicion.RED] = red;
                gems[Suspicion.GREEN] = green;
                gems[Suspicion.YELLOW] = yellow;
                String temp = "";
                if (red) temp += "red,";
                if (green) temp += "green,";
                if (yellow) temp += "yellow,";
                availableGems = (temp.substring(0, temp.length() - 1)).split(",");
            }
        }

        public void movePlayer(Piece player, int row, int col) {
            rooms[player.row][player.col].removePlayer(player);
            rooms[row][col].addPlayer(player);
        }

        public void clearRooms() {
            rooms = new Room[3][4];
            int x = 0, y = 0;
            boolean red, green, yellow;

            for (String gems : gemLocations.trim().split(":")) {
                red = gems.contains("red");
                green = gems.contains("green");
                yellow = gems.contains("yellow");
                rooms[x][y] = new Room(red, green, yellow, x, y);
                y++;
                x += y / 4;
                y %= 4;
            }
        }

        public Board(String piecePositions, HashMap<String, Piece> pieces, String gemLocations) {
            Piece piece;
            this.gemLocations = gemLocations;
            clearRooms();
            int col = 0;
            int row = 0;
            for (String room : piecePositions.split(":", -1)) // Split out each room
            {
                room = room.trim();
                if (room.length() != 0) for (String guest : room.split(",")) // Split guests out of each room
                {
                    guest = guest.trim();
                    piece = pieces.get(guest);
                    rooms[row][col].addPlayer(piece);
                }
                col++;
                row = row + col / 4;
                col = col % 4;
            }
        }
    }

    public Piece getPiece(String name) {
        return pieces.get(name);
    }

    public class Player {
        public String playerName;
        public ArrayList<String> possibleGuestNames;
        public ArrayList<String> possibleGuestNamesofMe;
        public int[] gemCounts;

        public void adjustKnowledge(ArrayList<String> possibleGuests) {
            Iterator<String> it = possibleGuestNames.iterator();
            while (it.hasNext()) {
                String g;
                if (!possibleGuests.contains(g = it.next())) {
                    it.remove();
                }
            }
        }

        public void adjustKnowledge(String notPossibleGuest) {
            Iterator<String> it = possibleGuestNames.iterator();
            while (it.hasNext()) {
                if (it.next().equals(notPossibleGuest)) {
                    it.remove();
                    break;
                }
            }
        }

        public void adjustKnowledgeAboutMe(ArrayList<String> possibleGuests) {
            Iterator<String> it = possibleGuestNamesofMe.iterator();
            while (it.hasNext()) {
                String g;
                if (!possibleGuests.contains(g = it.next())) {
                    it.remove();
                }
            }
        }

        public void adjustKnowledgeAboutMe(String notPossibleGuest) {
            Iterator<String> it = possibleGuestNamesofMe.iterator();
            while (it.hasNext()) {
                if (it.next().equals(notPossibleGuest)) {
                    it.remove();
                    break;
                }
            }
        }

        public Player(String name, String[] guests) {
            playerName = name;
            possibleGuestNames = new ArrayList<String>();
            possibleGuestNamesofMe = new ArrayList<String>();
            for (String g : guests) {
                possibleGuestNames.add(g);
                possibleGuestNamesofMe.add(g);
            }
            this.gemCounts = new int[3];
        }
    }

    public class Piece {
        public int row, col;
        public String name;

        public Piece(String name) {
            this.name = name;
        }

        public void setCordinates(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    private String[] getPossibleMoves(Piece p) {
        LinkedList<String> moves = new LinkedList<String>();
        if (p.row > 0) moves.push((p.row - 1) + "," + p.col);
        if (p.row < 2) moves.push((p.row + 1) + "," + p.col);
        if (p.col > 0) moves.push((p.row) + "," + (p.col - 1));
        if (p.col < 3) moves.push((p.row) + "," + (p.col + 1));

        return moves.toArray(new String[moves.size()]);
    }

    public String diveCards(String d1, String d2, String card1, String card2, int maxply, int ply) {

        String finalD1 = "";    //final face value of d1
        String finalD2 = "";    //final face value of d2
        int finalCard = 0;      //which final card to play
        String actions = "";    //return action
        int maxmoverow = 0;     //stores the best row to move for a move card action
        int maxmovecol = 0;     //stores the best col to move for a move card action
        String maxwhotoask = "";    //stores the value of the best player to ask for a ask card action
        String bestgemtotake = "";      //stores the value of the best gem to take for a take gem action
        String bestguesttomove = "";    //stores the best guest to move for a move card action
        ArrayList<String> d1list = new ArrayList<>();   //contains the possible face values of d1
        ArrayList<String> d2list = new ArrayList<>();   //contains the possible face values of d2
        ArrayList<String> gemsneeded = new ArrayList<>();   //keep track of the gems I need to get to a full set
        String MaxP1Move = "";      //store the row,col of guest to move with d1
        String MaxP2Move = "";      //store the row,col of the guest to move with d2
        double lastGemForSet = 4.0;     //weight for the value of a last gem needed to complete a set
        double secondGemForSet = 3.0;       //weight for the value of the two gems needed to complete a set
        double firstGemForSet = 2.0;        //weight of the three gems if there is no current incomplete set
        double extraGemForSet = 1.0;        //weight of one or two gems if this does not contribute to a current set being complete
        double informationLost = -1.01;     //weight of losing information about myself to other players
        double informationGain = 1.01;      //weight of gaining information about other players possible guest IDs
//        final double viewcardwweight = (1/otherPlayerNames.length);
        double maxPointGain = -1000;        //track the highest possible points
        double valueOfViewCared = 0;        //weight of the view card action
        for (String playername : otherPlayerNames) {
            valueOfViewCared += players.get(playername).possibleGuestNames.size();
        }
//        valueOfViewCared = (valueOfViewCared / otherPlayerNames.length) * viewcardwweight;
        valueOfViewCared = (valueOfViewCared / (2 * otherPlayerNames.length));
//        if(ply == 0){
//            lastGemForSet = (lastGemForSet * 0.75);
//            secondGemForSet = (secondGemForSet * 0.75);
//            firstGemForSet = (firstGemForSet * 0.75);
//            extraGemForSet = (extraGemForSet * 0.75);
//            informationLost = (informationLost * 0.75);
//            informationGain = (informationGain * 0.75);
//            valueOfViewCared = (valueOfViewCared * 0.75);
//        }
        if (gemCounts[0] == gemCounts[1]) {
            if (gemCounts[0] == gemCounts[2]) {
                gemsneeded.add("red");
                gemsneeded.add("green");
                gemsneeded.add("yellow");
            } else if (gemCounts[0] < gemCounts[2]) {
                gemsneeded.add("red");
                gemsneeded.add("green");
            } else {
                gemsneeded.add("yellow");
            }
        } else if (gemCounts[0] == gemCounts[2]) {
            if (gemCounts[0] < gemCounts[1]) {
                gemsneeded.add("red");
                gemsneeded.add("yellow");
            } else {
                gemsneeded.add("green");
            }
        } else if (gemCounts[1] == gemCounts[2]) {
            if (gemCounts[0] < gemCounts[1])
                gemsneeded.add("red");
            else {
                gemsneeded.add("yellow");
                gemsneeded.add("green");
            }
        } else if (gemCounts[0] < gemCounts[1] && gemCounts[0] < gemCounts[2]) {
            gemsneeded.add("red");
        } else if (gemCounts[1] < gemCounts[0] && gemCounts[1] < gemCounts[2]) {
            gemsneeded.add("green");
        } else if (gemCounts[2] < gemCounts[0] && gemCounts[2] < gemCounts[1]) {
            gemsneeded.add("yellow");
        }
        if (d1.equals("?")) {
            d1list.addAll(Arrays.asList(allCharacters));
        } else {
            d1list.add(d1);
        }
        if (d2.equals("?")) {
            d2list.addAll(Arrays.asList(allCharacters));
        } else {
            d2list.add(d2);
        }
        for (String d1guest : d1list) {
            Piece piece1 = this.pieces.get(d1guest);
            int p1OGcol = piece1.col;
            int p1OGrow = piece1.row;
            String[] p1moves = getPossibleMoves(piece1);
            for (String p1move : p1moves) {   //loop all possible moves of d1
                this.board.movePlayer(piece1, Integer.parseInt(p1move.split(",")[0]), Integer.parseInt(p1move.split(",")[1])); // Perform the move on my board
                pieces.remove(d1guest);
                Piece p1 = new Piece(d1guest);
                p1.setCordinates(Integer.parseInt(p1move.split(",")[0]), Integer.parseInt(p1move.split(",")[1]));
                pieces.put(d1guest, p1);
                for (String d2guest : d2list) {
                    Piece piece2 = pieces.get(d2guest);
                    int p2OGcol = piece2.col;
                    int p2OGrow = piece2.row;
                    String[] p2moves = getPossibleMoves(pieces.get(d2guest));
                    if (d2guest.equals(d1guest)) {
                        Piece p5 = new Piece(d1guest);
                        p5.setCordinates(Integer.parseInt(p1move.split(",")[0]), Integer.parseInt(p1move.split(",")[1]));
                        p2moves = getPossibleMoves(p5);
                    }
                    for (String p2move : p2moves) {   //loop all possible moves of d2
                        this.board.movePlayer(piece2, Integer.parseInt(p2move.split(",")[0]), Integer.parseInt(p2move.split(",")[1])); // Perform the move on my board
                        pieces.remove(d2guest);
                        Piece p2 = new Piece(d2guest);
                        p2.setCordinates(Integer.parseInt(p2move.split(",")[0]), Integer.parseInt(p2move.split(",")[1]));
                        pieces.put(d2guest, p2);
                        ArrayList<String> myGemOptions = new ArrayList<>();
                        if (d2guest.equals(me.name)) {
                            myGemOptions.addAll(Arrays.asList(this.board.rooms[Integer.parseInt(p2move.split(",")[0])][Integer.parseInt(p2move.split(",")[1])].availableGems));
                        } else if (d1guest.equals(me.name)) {
                            myGemOptions.addAll(Arrays.asList(this.board.rooms[Integer.parseInt(p1move.split(",")[0])][Integer.parseInt(p1move.split(",")[1])].availableGems));
                        } else {
                            myGemOptions.addAll(Arrays.asList(this.board.rooms[pieces.get(me.name).row][pieces.get(me.name).col].availableGems));
                        }
                        if (card1.contains("move")) {
                            double calculatedScore = 0;
                            String GEMM = "";
                            int MOVEI = 0;
                            int MOVEJ = 0;
                            String TRACKbestguesttomove = "";
                            String TRACKmaxwhotoask = "";
                            double trackCalcScore = -10000;
                            //false for testing
//                            if(ply == -10000){
                            if(ply != maxply){
                                int[][] midBoardRow = new int[][]{{2,0},{2,1},{2,2},{2,3}};
//                                String guest = allCharacters[r.nextInt(allCharacters.length)];
                                for(String guest: allCharacters) {
                                    for (int[] moveMidBoard : midBoardRow) {
                                        int i = moveMidBoard[0];
                                        int j = moveMidBoard[1];
//                                int i = r.nextInt(3);
//                                int j = r.nextInt(4);
                                        int moverow = pieces.get(guest).row;
                                        int movecol = pieces.get(guest).col;
                                        if (i != moverow || j != movecol) {
                                            this.board.movePlayer(pieces.get(guest), i, j); // Perform the move on my board
                                            pieces.remove(guest);
                                            Piece p3 = new Piece(guest);
                                            p3.setCordinates(i, j);
                                            pieces.put(guest, p3);
                                            if (card1.split(":")[0].startsWith("move,")) {
                                                String whotoask = card1.split(":")[1].split(",")[1];
                                                for (String playername : otherPlayerNames) {
                                                    double cansee = 0.0;
                                                    double cantsee = 0.0;
                                                    double total = 0.0;
                                                    for (String moveGuest : players.get(playername).possibleGuestNames) {
                                                        if (pieces.get(moveGuest).row == pieces.get(whotoask).row || pieces.get(moveGuest).col == pieces.get(whotoask).col) {
                                                            cansee++;
                                                            total++;
                                                        } else {
                                                            cantsee++;
                                                            total++;
                                                        }
                                                    }
                                                    calculatedScore = 0;
                                                    if (cansee != 0 && cantsee != 0) {
                                                        double entropy = -(((cansee / total) * (Math.log(cansee / total) / Math.log(2))) + ((cantsee / total) * (Math.log(cantsee / total) / Math.log(2))));
                                                        calculatedScore = entropy * informationGain;
                                                    }
                                                    if (calculatedScore > trackCalcScore) {
                                                        trackCalcScore = calculatedScore;
                                                        MOVEI = i;
                                                        MOVEJ = j;
                                                        TRACKmaxwhotoask = playername;
                                                        TRACKbestguesttomove = guest;
                                                    }
                                                }
                                            } else {
                                                double gemPointsPossible = extraGemForSet;
                                                for (String gem : myGemOptions) {
                                                    if (gemsneeded.contains(gem)) {
                                                        if (gemsneeded.size() == 1) {
                                                            gemPointsPossible = lastGemForSet;
                                                        } else if (gemsneeded.size() == 2)
                                                            gemPointsPossible = secondGemForSet;
                                                        else
                                                            gemPointsPossible = firstGemForSet;
                                                    }
                                                    double infoloss = 0;
                                                    ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem(gem);
                                                    for (String playername : otherPlayerNames) {
                                                        for (String playersgems : players.get(playername).possibleGuestNamesofMe) {
                                                            if (!possibleGuests.contains(playersgems)) {
                                                                infoloss += (1.0 / players.get(playername).possibleGuestNamesofMe.size());
                                                            }
                                                        }
                                                    }
                                                    calculatedScore = (infoloss * informationLost) + gemPointsPossible;
                                                    if (calculatedScore > trackCalcScore) {
                                                        trackCalcScore = calculatedScore;
                                                        GEMM = gem;
                                                        MOVEI = i;
                                                        MOVEJ = j;
                                                        TRACKbestguesttomove = guest;
                                                    }
                                                }
                                            }
                                            this.board.movePlayer(pieces.get(guest), moverow, movecol); // Perform the move on my board
                                            pieces.remove(guest);
                                            Piece p4 = new Piece(guest);
                                            p4.setCordinates(moverow, movecol);
                                            pieces.put(guest, p4);
                                        }
                                    }
                                }
                            } else {
                                for (String guest : allCharacters) {
                                    for (int i = 0; i < 3; i++) {
                                        for (int j = 0; j < 4; j++) {
                                            int moverow = pieces.get(guest).row;
                                            int movecol = pieces.get(guest).col;
                                            if (i != moverow || j != movecol) {
                                                this.board.movePlayer(pieces.get(guest), i, j); // Perform the move on my board
                                                pieces.remove(guest);
                                                Piece p3 = new Piece(guest);
                                                p3.setCordinates(i, j);
                                                pieces.put(guest, p3);
                                                if (card1.split(":")[0].startsWith("move,")) {
                                                    String whotoask = card1.split(":")[1].split(",")[1];
                                                    for (String playername : otherPlayerNames) {
                                                        double cansee = 0.0;
                                                        double cantsee = 0.0;
                                                        double total = 0.0;
                                                        for (String moveGuest : players.get(playername).possibleGuestNames) {
                                                            if (pieces.get(moveGuest).row == pieces.get(whotoask).row || pieces.get(moveGuest).col == pieces.get(whotoask).col) {
                                                                cansee++;
                                                                total++;
                                                            } else {
                                                                cantsee++;
                                                                total++;
                                                            }
                                                        }
                                                        calculatedScore = 0;
                                                        if (cansee != 0 && cantsee != 0) {
                                                            double entropy = -(((cansee / total) * (Math.log(cansee / total) / Math.log(2))) + ((cantsee / total) * (Math.log(cantsee / total) / Math.log(2))));
                                                            calculatedScore = entropy * informationGain;
                                                        }
                                                        if (calculatedScore > trackCalcScore) {
                                                            trackCalcScore = calculatedScore;
                                                            MOVEI = i;
                                                            MOVEJ = j;
                                                            TRACKmaxwhotoask = playername;
                                                            TRACKbestguesttomove = guest;
                                                        }
                                                    }
                                                } else {
                                                    double gemPointsPossible = extraGemForSet;
                                                    for (String gem : myGemOptions) {
                                                        if (gemsneeded.contains(gem)) {
                                                            if (gemsneeded.size() == 1) {
                                                                gemPointsPossible = lastGemForSet;
                                                            } else if (gemsneeded.size() == 2)
                                                                gemPointsPossible = secondGemForSet;
                                                            else
                                                                gemPointsPossible = firstGemForSet;
                                                        }
                                                        double infoloss = 0;
                                                        ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem(gem);
                                                        for (String playername : otherPlayerNames) {
                                                            for (String playersgems : players.get(playername).possibleGuestNamesofMe) {
                                                                if (!possibleGuests.contains(playersgems)) {
                                                                    infoloss += (1.0 / players.get(playername).possibleGuestNamesofMe.size());
                                                                }
                                                            }
                                                        }
                                                        calculatedScore = (infoloss * informationLost) + gemPointsPossible;
                                                        if (calculatedScore > trackCalcScore) {
                                                            trackCalcScore = calculatedScore;
                                                            GEMM = gem;
                                                            MOVEI = i;
                                                            MOVEJ = j;
                                                            TRACKbestguesttomove = guest;
                                                        }
                                                    }
                                                }
                                                this.board.movePlayer(pieces.get(guest), moverow, movecol); // Perform the move on my board
                                                pieces.remove(guest);
                                                Piece p4 = new Piece(guest);
                                                p4.setCordinates(moverow, movecol);
                                                pieces.put(guest, p4);
                                            }
                                        }
                                    }
                                }
                            }
                            if (ply > 0) {
                                String[] diceFaces1 = {"Buford Barnswallow", "Earl of Volesworthy", "Mildred Wellington", "Viola Chung", "Dr. Ashraf Najem"};
                                String[] diceFaces2 = {"Nadia Bwalya", "Remy La Rocque", "Lily Nesbit", "Trudie Mudge", "Stefano Laconi"};
                                ArrayList<String> newRemainingCards = new ArrayList<>(Arrays.asList(cardsRemaining));
                                ArrayList<String> newPlyPlayerName = new ArrayList<>(Arrays.asList(otherPlayerNames));
                                trackCalcScore += exploreOtherPlayersOptions(newRemainingCards, 0, newPlyPlayerName, card1, card2);
//                                    System.out.println("this is the calculated score frome card1 move: " + trackCalcScore);
                                trackCalcScore += Double.parseDouble(diveCards(diceFaces1[r.nextInt(diceFaces1.length)], diceFaces2[r.nextInt(diceFaces2.length)], card2, cardsRemaining[r.nextInt(cardsRemaining.length)], maxply, ply - 1));
                            }
                            if (trackCalcScore > maxPointGain) {
                                maxPointGain = trackCalcScore;
                                bestgemtotake = GEMM;
                                maxmoverow = MOVEI;
                                maxmovecol = MOVEJ;
                                bestguesttomove = TRACKbestguesttomove;
                                maxwhotoask = TRACKmaxwhotoask;
                                MaxP1Move = p1move;
                                MaxP2Move = p2move;
                                finalCard = 1;
                                finalD1 = d1guest;
                                finalD2 = d2guest;
                            }
                        } else if (card1.split(":")[0].startsWith("viewDeck")) { //view deck is first action on card 1
                            String whotoask = card1.split(":")[1].split(",")[1];
                            double topInfoGainFromAsking = -10000;
                            String topPlayerToAskTemp = "";
                            for (String playername : otherPlayerNames) {
                                double cansee = 0;
                                double cantsee = 0;
                                double calculatedScore = 0;
                                double total = 0;
                                for (String moveGuest : players.get(playername).possibleGuestNames) {
                                    if (pieces.get(moveGuest).row == pieces.get(whotoask).row || pieces.get(moveGuest).col == pieces.get(whotoask).col) {
                                        total++;
                                        cansee++;
                                    } else {
                                        cantsee++;
                                        total++;
                                    }
                                }
                                if (cansee != 0 && cantsee != 0) {
                                    double entropy = -(((cansee / total) * (Math.log(cansee / total) / Math.log(2))) + ((cantsee / total) * (Math.log(cantsee / total) / Math.log(2))));
                                    calculatedScore = entropy * informationGain + valueOfViewCared;
                                }
                                if (calculatedScore > topInfoGainFromAsking) {
                                    topInfoGainFromAsking = calculatedScore;
                                    topPlayerToAskTemp = playername;
                                }
                            }
                            if (ply > 0) {
                                String[] diceFaces1 = {"Buford Barnswallow", "Earl of Volesworthy", "Mildred Wellington", "Viola Chung", "Dr. Ashraf Najem"};
                                String[] diceFaces2 = {"Nadia Bwalya", "Remy La Rocque", "Lily Nesbit", "Trudie Mudge", "Stefano Laconi"};
                                ArrayList<String> newRemainingCards = new ArrayList<>(Arrays.asList(cardsRemaining));
                                ArrayList<String> newPlyPlayerName = new ArrayList<>(Arrays.asList(otherPlayerNames));
                                topInfoGainFromAsking += exploreOtherPlayersOptions(newRemainingCards, 0, newPlyPlayerName, card1, card2);
//                                System.out.println("this is the calculated score frome card1 viewdeck, ask: " + topInfoGainFromAsking);
                                topInfoGainFromAsking += Double.parseDouble(diveCards(diceFaces1[r.nextInt(diceFaces1.length)], diceFaces2[r.nextInt(diceFaces2.length)], card2, cardsRemaining[r.nextInt(cardsRemaining.length)], maxply, ply - 1));
                            }
                            if (topInfoGainFromAsking > maxPointGain) {
                                maxPointGain = topInfoGainFromAsking;
                                maxwhotoask = topPlayerToAskTemp;
                                MaxP1Move = p1move;
                                MaxP2Move = p2move;
                                finalCard = 1;
                                finalD1 = d1guest;
                                finalD2 = d2guest;
                            }
                        } else if (card1.split(":")[0].startsWith("get,yellow") || card1.split(":")[0].startsWith("get,red")
                                || card1.split(":")[0].startsWith("get,green")) {   // get given gem is the first action on card 1
                            double gemPointsPossible = extraGemForSet;
                            if (gemsneeded.contains(card1.split(":")[0].split(",")[1])) {
                                if (gemsneeded.size() == 1) {
                                    gemPointsPossible = lastGemForSet;
                                } else if (gemsneeded.size() == 2)
                                    gemPointsPossible = secondGemForSet;
                                else
                                    gemPointsPossible = firstGemForSet;
                            }
                            if (card1.split(":")[1].startsWith("viewDeck")) { //view deck is first action on card 1
                                double calculatedScore = 0;
                                calculatedScore = valueOfViewCared + gemPointsPossible;
                                if (ply > 0) {
                                    String[] diceFaces1 = {"Buford Barnswallow", "Earl of Volesworthy", "Mildred Wellington", "Viola Chung", "Dr. Ashraf Najem"};
                                    String[] diceFaces2 = {"Nadia Bwalya", "Remy La Rocque", "Lily Nesbit", "Trudie Mudge", "Stefano Laconi"};
                                    ArrayList<String> newRemainingCards = new ArrayList<>(Arrays.asList(cardsRemaining));
                                    ArrayList<String> newPlyPlayerName = new ArrayList<>(Arrays.asList(otherPlayerNames));
                                    calculatedScore += exploreOtherPlayersOptions(newRemainingCards, 0, newPlyPlayerName, card1, card2);
//                                    System.out.println("this is the calculated score frome card1 get givengem, viewdeck: " + calculatedScore);
                                    calculatedScore += Double.parseDouble(diveCards(diceFaces1[r.nextInt(diceFaces1.length)], diceFaces2[r.nextInt(diceFaces2.length)], card2, cardsRemaining[r.nextInt(cardsRemaining.length)], maxply, ply - 1));
                                }
                                if (calculatedScore > maxPointGain) {
                                    maxPointGain = calculatedScore;
                                    MaxP1Move = p1move;
                                    MaxP2Move = p2move;
                                    finalCard = 1;
                                    finalD1 = d1guest;
                                    finalD2 = d2guest;
                                }
                            } else {
                                String whotoask = card1.split(":")[1].split(",")[1];
                                double topInfoGainFromAsking = -10000;
                                String topPlayerToAskTemp = "";
                                for (String playername : otherPlayerNames) {
                                    double cansee = 0;
                                    double cantsee = 0;
                                    double total = 0;

                                    double calculatedScore = 0;
                                    for (String moveGuest : players.get(playername).possibleGuestNames) {
                                        if (pieces.get(moveGuest).row == pieces.get(whotoask).row || pieces.get(moveGuest).col == pieces.get(whotoask).col) {
                                            total++;
                                            cansee++;
                                        } else {
                                            cantsee++;
                                            total++;
                                        }
                                    }
                                    if (cansee != 0 && cantsee != 0) {
                                        double entropy = -(((cansee / total) * (Math.log(cansee / total) / Math.log(2))) + ((cantsee / total) * (Math.log(cantsee / total) / Math.log(2))));
                                        calculatedScore = entropy * informationGain + gemPointsPossible;
                                    }
                                    if (calculatedScore > topInfoGainFromAsking) {
                                        topInfoGainFromAsking = calculatedScore;
                                        topPlayerToAskTemp = playername;
                                    }
                                }
                                if (ply > 0) {
                                    String[] diceFaces1 = {"Buford Barnswallow", "Earl of Volesworthy", "Mildred Wellington", "Viola Chung", "Dr. Ashraf Najem"};
                                    String[] diceFaces2 = {"Nadia Bwalya", "Remy La Rocque", "Lily Nesbit", "Trudie Mudge", "Stefano Laconi"};
                                    ArrayList<String> newRemainingCards = new ArrayList<>(Arrays.asList(cardsRemaining));
                                    ArrayList<String> newPlyPlayerName = new ArrayList<>(Arrays.asList(otherPlayerNames));
                                    topInfoGainFromAsking += exploreOtherPlayersOptions(newRemainingCards, 0, newPlyPlayerName, card1, card2);
//                                    System.out.println("this is the calculated score frome card1 get givengem, ask: " + topInfoGainFromAsking);
                                    topInfoGainFromAsking += Double.parseDouble(diveCards(diceFaces1[r.nextInt(diceFaces1.length)], diceFaces2[r.nextInt(diceFaces2.length)], card2, cardsRemaining[r.nextInt(cardsRemaining.length)], maxply, ply - 1));
                                }
                                if (topInfoGainFromAsking > maxPointGain) {
                                    maxPointGain = topInfoGainFromAsking;
                                    maxwhotoask = topPlayerToAskTemp;
                                    MaxP1Move = p1move;
                                    MaxP2Move = p2move;
                                    finalCard = 1;
                                    finalD1 = d1guest;
                                    finalD2 = d2guest;
                                }
                            }
                        } else {
                            if (card1.split(":")[1].startsWith("viewDeck")) {
                                double gemPointsPossible = extraGemForSet;
                                String topGemToGetTemp = "";
                                double topInfoGainFromGem = -10000;
                                for (String gem : myGemOptions) {
                                    if (gemsneeded.contains(gem)) {
                                        if (gemsneeded.size() == 1) {
                                            gemPointsPossible = lastGemForSet;
                                        } else if (gemsneeded.size() == 2)
                                            gemPointsPossible = secondGemForSet;
                                        else
                                            gemPointsPossible = firstGemForSet;
                                    }
                                    double infoloss = 0;
                                    ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem(gem);
                                    for (String playername : otherPlayerNames) {
                                        for (String playersgems : players.get(playername).possibleGuestNamesofMe) {
                                            if (!possibleGuests.contains(playersgems)) {
                                                infoloss += (1.0 / players.get(playername).possibleGuestNamesofMe.size());
                                            }
                                        }
                                    }
                                    double calculatedScore = (infoloss * informationLost) + gemPointsPossible + valueOfViewCared;
                                    if (calculatedScore > topInfoGainFromGem) {
                                        topInfoGainFromGem = calculatedScore;
                                        topGemToGetTemp = gem;
                                    }
                                }
                                if (ply > 0) {
                                    String[] diceFaces1 = {"Buford Barnswallow", "Earl of Volesworthy", "Mildred Wellington", "Viola Chung", "Dr. Ashraf Najem"};
                                    String[] diceFaces2 = {"Nadia Bwalya", "Remy La Rocque", "Lily Nesbit", "Trudie Mudge", "Stefano Laconi"};
                                    ArrayList<String> newRemainingCards = new ArrayList<>(Arrays.asList(cardsRemaining));
                                    ArrayList<String> newPlyPlayerName = new ArrayList<>(Arrays.asList(otherPlayerNames));
                                    topInfoGainFromGem += exploreOtherPlayersOptions(newRemainingCards, 0, newPlyPlayerName, card1, card2);
//                                    System.out.println("this is the calculated score frome card1 get, viewDeck: " + topInfoGainFromGem);
                                    topInfoGainFromGem += Double.parseDouble(diveCards(diceFaces1[r.nextInt(diceFaces1.length)], diceFaces2[r.nextInt(diceFaces2.length)], card2, cardsRemaining[r.nextInt(cardsRemaining.length)], maxply, ply - 1));
                                }
                                if (topInfoGainFromGem > maxPointGain) {
                                    maxPointGain = topInfoGainFromGem;
                                    bestgemtotake = topGemToGetTemp;
                                    MaxP1Move = p1move;
                                    MaxP2Move = p2move;
                                    finalCard = 1;
                                    finalD1 = d1guest;
                                    finalD2 = d2guest;
                                }
                            } else {
                                String whotoask = card1.split(":")[1].split(",")[1];
                                double gemPointsPossible = extraGemForSet;
                                double topInfoGainFromAsking = -10000;
                                String topPlayerToAskTemp = "";
                                String topGemToTakeTemp = "";
                                for (String gem : myGemOptions) {
                                    if (gemsneeded.contains(gem)) {
                                        if (gemsneeded.size() == 1) {
                                            gemPointsPossible = lastGemForSet;
                                        } else if (gemsneeded.size() == 2)
                                            gemPointsPossible = secondGemForSet;
                                        else
                                            gemPointsPossible = firstGemForSet;
                                    }
                                    double infoloss = 0;
                                    ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem(gem);
                                    for (String playername : otherPlayerNames) {
                                        for (String playersgems : players.get(playername).possibleGuestNamesofMe) {
                                            if (!possibleGuests.contains(playersgems)) {
                                                infoloss += (1.0 / players.get(playername).possibleGuestNamesofMe.size());
                                            }
                                        }
                                    }
                                    for (String playername : otherPlayerNames) {
                                        double cansee = 0.0;
                                        double cantsee = 0.0;
                                        double total = 0.0;
                                        for (String moveGuest : players.get(playername).possibleGuestNames) {
                                            if (pieces.get(moveGuest).row == pieces.get(whotoask).row || pieces.get(moveGuest).col == pieces.get(whotoask).col) {
                                                cansee++;
                                                total++;
                                            } else {
                                                cantsee++;
                                                total++;
                                            }
                                        }
                                        double calculatedScore = (infoloss * informationLost) + gemPointsPossible;
                                        if (cansee != 0 && cantsee != 0) {
                                            double entropy = -(((cansee / total) * (Math.log(cansee / total) / Math.log(2))) + ((cantsee / total) * (Math.log(cantsee / total) / Math.log(2))));
                                            calculatedScore = entropy * informationGain + (infoloss * informationLost) + gemPointsPossible;
                                        }
                                        if (calculatedScore > topInfoGainFromAsking) {
                                            topInfoGainFromAsking = calculatedScore;
                                            topPlayerToAskTemp = playername;
                                            topGemToTakeTemp = gem;
                                        }
                                    }
                                }
                                if (ply > 0) {
                                    String[] diceFaces1 = {"Buford Barnswallow", "Earl of Volesworthy", "Mildred Wellington", "Viola Chung", "Dr. Ashraf Najem"};
                                    String[] diceFaces2 = {"Nadia Bwalya", "Remy La Rocque", "Lily Nesbit", "Trudie Mudge", "Stefano Laconi"};
                                    ArrayList<String> newRemainingCards = new ArrayList<>(Arrays.asList(cardsRemaining));
                                    ArrayList<String> newPlyPlayerName = new ArrayList<>(Arrays.asList(otherPlayerNames));
                                    topInfoGainFromAsking += exploreOtherPlayersOptions(newRemainingCards, 0, newPlyPlayerName, card1, card2);
//                                    System.out.println("this is the calculated score frome card1 get, ask: " + topInfoGainFromAsking);
                                    topInfoGainFromAsking += Double.parseDouble(diveCards(diceFaces1[r.nextInt(diceFaces1.length)], diceFaces2[r.nextInt(diceFaces2.length)], card2, cardsRemaining[r.nextInt(cardsRemaining.length)], maxply, ply - 1));
                                }
                                if (topInfoGainFromAsking > maxPointGain) {
                                    bestgemtotake = topGemToTakeTemp;
                                    maxPointGain = topInfoGainFromAsking;
                                    maxwhotoask = topPlayerToAskTemp;
                                    MaxP1Move = p1move;
                                    MaxP2Move = p2move;
                                    finalCard = 1;
                                    finalD1 = d1guest;
                                    finalD2 = d2guest;
                                }
                            }
                        }
//                        if (ply != 0) {
                            if (card2.contains("move")) {
                                double calculatedScore = 0;
                                String GEMM = "";
                                int MOVEI = 0;
                                int MOVEJ = 0;
                                String TRACKbestguesttomove = "";
                                String TRACKmaxwhotoask = "";
                                double trackCalcScore = -10000;
                                if(ply != maxply){
                                    int[][] midBoardRow = new int[][]{{2,0},{2,1},{2,2},{2,3}};
                                    for (String guest : allCharacters) {
//                                String guest = allCharacters[r.nextInt(allCharacters.length)];

                                            for (int[] moveMidBoard : midBoardRow) {
                                                int i = moveMidBoard[0];
                                                int j = moveMidBoard[1];
//                                        for (int i = 0; i < 3; i++) {
//                                            for (int j = 0; j < 4; j++) {
                                                int moverow = pieces.get(guest).row;
                                                int movecol = pieces.get(guest).col;
                                                if (i != moverow || j != movecol) {
                                                    this.board.movePlayer(pieces.get(guest), i, j); // Perform the move on my board
                                                    pieces.remove(guest);
                                                    Piece p3 = new Piece(guest);
                                                    p3.setCordinates(i, j);
                                                    pieces.put(guest, p3);
                                                    if (card2.split(":")[0].startsWith("move,")) {
                                                        String whotoask = card2.split(":")[1].split(",")[1];
                                                        for (String playername : otherPlayerNames) {
                                                            double cansee = 0.0;
                                                            double cantsee = 0.0;
                                                            double total = 0.0;
                                                            for (String moveGuest : players.get(playername).possibleGuestNames) {
                                                                if (pieces.get(moveGuest).row == pieces.get(whotoask).row || pieces.get(moveGuest).col == pieces.get(whotoask).col) {
                                                                    cansee++;
                                                                    total++;
                                                                } else {
                                                                    cantsee++;
                                                                    total++;
                                                                }
                                                            }
                                                            calculatedScore = 0;
                                                            if (cansee != 0 && cantsee != 0) {
                                                                double entropy = -(((cansee / total) * (Math.log(cansee / total) / Math.log(2))) + ((cantsee / total) * (Math.log(cantsee / total) / Math.log(2))));
                                                                calculatedScore = entropy * informationGain;
                                                            }
                                                            if (calculatedScore > trackCalcScore) {
                                                                trackCalcScore = calculatedScore;
                                                                MOVEI = i;
                                                                MOVEJ = j;
                                                                TRACKmaxwhotoask = playername;
                                                                TRACKbestguesttomove = guest;
                                                            }
                                                        }
                                                    } else {
                                                        double gemPointsPossible = extraGemForSet;
                                                        for (String gem : myGemOptions) {
                                                            if (gemsneeded.contains(gem)) {
                                                                if (gemsneeded.size() == 1) {
                                                                    gemPointsPossible = lastGemForSet;
                                                                } else if (gemsneeded.size() == 2)
                                                                    gemPointsPossible = secondGemForSet;
                                                                else
                                                                    gemPointsPossible = firstGemForSet;
                                                            }
                                                            double infoloss = 0;
                                                            ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem(gem);
                                                            for (String playername : otherPlayerNames) {
                                                                for (String playersgems : players.get(playername).possibleGuestNamesofMe) {
                                                                    if (!possibleGuests.contains(playersgems)) {
                                                                        infoloss += (1.0 / players.get(playername).possibleGuestNamesofMe.size());
                                                                    }
                                                                }
                                                            }
                                                            calculatedScore = (infoloss * informationLost) + gemPointsPossible;
                                                            if (calculatedScore > trackCalcScore) {
                                                                trackCalcScore = calculatedScore;
                                                                GEMM = gem;
                                                                MOVEI = i;
                                                                MOVEJ = j;
                                                                TRACKbestguesttomove = guest;
                                                            }
                                                        }
                                                    }
                                                    this.board.movePlayer(pieces.get(guest), moverow, movecol); // Perform the move on my board
                                                    pieces.remove(guest);
                                                    Piece p4 = new Piece(guest);
                                                    p4.setCordinates(moverow, movecol);
                                                    pieces.put(guest, p4);
                                                }
//                                            }
                                        }
                                    }
                                }
                                else {
                                    for (String guest : allCharacters) {
                                        for (int i = 0; i < 3; i++) {
                                            for (int j = 0; j < 4; j++) {
                                                int moverow = pieces.get(guest).row;
                                                int movecol = pieces.get(guest).col;
                                                if (i != moverow || j != movecol) {
                                                    this.board.movePlayer(pieces.get(guest), i, j); // Perform the move on my board
                                                    pieces.remove(guest);
                                                    Piece p3 = new Piece(guest);
                                                    p3.setCordinates(i, j);
                                                    pieces.put(guest, p3);
                                                    if (card2.split(":")[0].startsWith("move,")) {
                                                        String whotoask = card2.split(":")[1].split(",")[1];
                                                        for (String playername : otherPlayerNames) {
                                                            double cansee = 0.0;
                                                            double cantsee = 0.0;
                                                            double total = 0.0;
                                                            for (String moveGuest : players.get(playername).possibleGuestNames) {
                                                                if (pieces.get(moveGuest).row == pieces.get(whotoask).row || pieces.get(moveGuest).col == pieces.get(whotoask).col) {
                                                                    cansee++;
                                                                    total++;
                                                                } else {
                                                                    cantsee++;
                                                                    total++;
                                                                }
                                                            }
                                                            calculatedScore = 0;
                                                            if (cansee != 0 && cantsee != 0) {
                                                                double entropy = -(((cansee / total) * (Math.log(cansee / total) / Math.log(2))) + ((cantsee / total) * (Math.log(cantsee / total) / Math.log(2))));
                                                                calculatedScore = entropy * informationGain;
                                                            }
                                                            if (calculatedScore > trackCalcScore) {
                                                                trackCalcScore = calculatedScore;
                                                                MOVEI = i;
                                                                MOVEJ = j;
                                                                TRACKmaxwhotoask = playername;
                                                                TRACKbestguesttomove = guest;
                                                            }
                                                        }
                                                    } else {
                                                        double gemPointsPossible = extraGemForSet;
                                                        for (String gem : myGemOptions) {
                                                            if (gemsneeded.contains(gem)) {
                                                                if (gemsneeded.size() == 1) {
                                                                    gemPointsPossible = lastGemForSet;
                                                                } else if (gemsneeded.size() == 2)
                                                                    gemPointsPossible = secondGemForSet;
                                                                else
                                                                    gemPointsPossible = firstGemForSet;
                                                            }
                                                            double infoloss = 0;
                                                            ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem(gem);
                                                            for (String playername : otherPlayerNames) {
                                                                for (String playersgems : players.get(playername).possibleGuestNamesofMe) {
                                                                    if (!possibleGuests.contains(playersgems)) {
                                                                        infoloss += (1.0 / players.get(playername).possibleGuestNamesofMe.size());
                                                                    }
                                                                }
                                                            }
                                                            calculatedScore = (infoloss * informationLost) + gemPointsPossible;
                                                            if (calculatedScore > trackCalcScore) {
                                                                trackCalcScore = calculatedScore;
                                                                GEMM = gem;
                                                                MOVEI = i;
                                                                MOVEJ = j;
                                                                TRACKbestguesttomove = guest;
                                                            }
                                                        }
                                                    }
                                                    this.board.movePlayer(pieces.get(guest), moverow, movecol); // Perform the move on my board
                                                    pieces.remove(guest);
                                                    Piece p4 = new Piece(guest);
                                                    p4.setCordinates(moverow, movecol);
                                                    pieces.put(guest, p4);
                                                }
                                            }
                                        }
                                    }
                                }
                                if (ply > 0) {
                                    String[] diceFaces1 = {"Buford Barnswallow", "Earl of Volesworthy", "Mildred Wellington", "Viola Chung", "Dr. Ashraf Najem"};
                                    String[] diceFaces2 = {"Nadia Bwalya", "Remy La Rocque", "Lily Nesbit", "Trudie Mudge", "Stefano Laconi"};
                                    ArrayList<String> newRemainingCards = new ArrayList<>(Arrays.asList(cardsRemaining));
                                    ArrayList<String> newPlyPlayerName = new ArrayList<>(Arrays.asList(otherPlayerNames));
                                    trackCalcScore += exploreOtherPlayersOptions(newRemainingCards, 0, newPlyPlayerName, card2, card1);
//                                System.out.println("this is the calculated score frome card2 move: " + trackCalcScore);
                                    trackCalcScore += Double.parseDouble(diveCards(diceFaces1[r.nextInt(diceFaces1.length)], diceFaces2[r.nextInt(diceFaces2.length)], card1, cardsRemaining[r.nextInt(cardsRemaining.length)], maxply, ply - 1));
                                }
                                if (trackCalcScore > maxPointGain) {
                                    maxPointGain = trackCalcScore;
                                    bestgemtotake = GEMM;
                                    maxmoverow = MOVEI;
                                    maxmovecol = MOVEJ;
                                    bestguesttomove = TRACKbestguesttomove;
                                    maxwhotoask = TRACKmaxwhotoask;
                                    MaxP1Move = p1move;
                                    MaxP2Move = p2move;
                                    finalCard = 2;
                                    finalD1 = d1guest;
                                    finalD2 = d2guest;
                                }
                            } else if (card2.split(":")[0].startsWith("viewDeck")) { //view deck is first action on card 1
                                String whotoask = card2.split(":")[1].split(",")[1];
                                double topInfoGainFromAsking = -10000;
                                String topPlayerToAskTemp = "";
                                for (String playername : otherPlayerNames) {
                                    double cansee = 0;
                                    double cantsee = 0;
                                    double total = 0;

                                    double calculatedScore = 0;
                                    for (String moveGuest : players.get(playername).possibleGuestNames) {
                                        if (pieces.get(moveGuest).row == pieces.get(whotoask).row || pieces.get(moveGuest).col == pieces.get(whotoask).col) {
                                            total++;
                                            cansee++;
                                        } else {
                                            cantsee++;
                                            total++;
                                        }
                                    }
                                    if (cansee != 0 && cantsee != 0) {
                                        double entropy = -(((cansee / total) * (Math.log(cansee / total) / Math.log(2))) + ((cantsee / total) * (Math.log(cantsee / total) / Math.log(2))));
                                        calculatedScore = entropy * informationGain + valueOfViewCared;
                                    }
                                    if (calculatedScore > topInfoGainFromAsking) {
                                        topInfoGainFromAsking = calculatedScore;
                                        topPlayerToAskTemp = playername;
                                    }
                                }
                                if (ply > 0) {
                                    String[] diceFaces1 = {"Buford Barnswallow", "Earl of Volesworthy", "Mildred Wellington", "Viola Chung", "Dr. Ashraf Najem"};
                                    String[] diceFaces2 = {"Nadia Bwalya", "Remy La Rocque", "Lily Nesbit", "Trudie Mudge", "Stefano Laconi"};
                                    ArrayList<String> newRemainingCards = new ArrayList<>(Arrays.asList(cardsRemaining));
                                    ArrayList<String> newPlyPlayerName = new ArrayList<>(Arrays.asList(otherPlayerNames));
                                    topInfoGainFromAsking += exploreOtherPlayersOptions(newRemainingCards, 0, newPlyPlayerName, card2, card1);
//                                System.out.println("this is the calculated score frome card2 viewDeck, ask: " + topInfoGainFromAsking);
                                    topInfoGainFromAsking += Double.parseDouble(diveCards(diceFaces1[r.nextInt(diceFaces1.length)], diceFaces2[r.nextInt(diceFaces2.length)], card1, cardsRemaining[r.nextInt(cardsRemaining.length)], maxply, ply - 1));
                                }
                                if (topInfoGainFromAsking > maxPointGain) {
                                    maxPointGain = topInfoGainFromAsking;
                                    maxwhotoask = topPlayerToAskTemp;
                                    MaxP1Move = p1move;
                                    MaxP2Move = p2move;
                                    finalCard = 2;
                                    finalD1 = d1guest;
                                    finalD2 = d2guest;
                                }
                            } else if (card2.split(":")[0].startsWith("get,yellow") || card2.split(":")[0].startsWith("get,red")
                                    || card2.split(":")[0].startsWith("get,green")) {   // get given gem is the first action on card 1
                                double calculatedScore = extraGemForSet;
                                double gemPointsPossible = extraGemForSet;
                                if (gemsneeded.contains(card2.split(":")[0].split(",")[1])) {
                                    if (gemsneeded.size() == 1) {
                                        gemPointsPossible = lastGemForSet;
                                    } else if (gemsneeded.size() == 2)
                                        gemPointsPossible = secondGemForSet;
                                    else
                                        gemPointsPossible = firstGemForSet;
                                }
                                if (card2.split(":")[1].startsWith("viewDeck")) { //view deck is first action on card 1
                                    calculatedScore = valueOfViewCared + gemPointsPossible;
                                    if (ply > 0) {
                                        String[] diceFaces1 = {"Buford Barnswallow", "Earl of Volesworthy", "Mildred Wellington", "Viola Chung", "Dr. Ashraf Najem"};
                                        String[] diceFaces2 = {"Nadia Bwalya", "Remy La Rocque", "Lily Nesbit", "Trudie Mudge", "Stefano Laconi"};
                                        ArrayList<String> newRemainingCards = new ArrayList<>(Arrays.asList(cardsRemaining));
                                        ArrayList<String> newPlyPlayerName = new ArrayList<>(Arrays.asList(otherPlayerNames));
                                        calculatedScore += exploreOtherPlayersOptions(newRemainingCards, 0, newPlyPlayerName, card2, card1);
//                                    System.out.println("this is the calculated score frome card2 givengem, viewDeck: " + calculatedScore);
                                        calculatedScore += Double.parseDouble(diveCards(diceFaces1[r.nextInt(diceFaces1.length)], diceFaces2[r.nextInt(diceFaces2.length)], card1, cardsRemaining[r.nextInt(cardsRemaining.length)], maxply, ply - 1));
                                    }
                                    if (calculatedScore > maxPointGain) {
                                        maxPointGain = calculatedScore;
                                        MaxP1Move = p1move;
                                        MaxP2Move = p2move;
                                        finalCard = 2;
                                        finalD1 = d1guest;
                                        finalD2 = d2guest;
                                    }
                                } else {
                                    String whotoask = card2.split(":")[1].split(",")[1];
                                    double topInfoGainFromAsking = -10000;
                                    String topPlayerToAskTemp = "";
                                    for (String playername : otherPlayerNames) {
                                        double cansee = 0.0;
                                        double cantsee = 0.0;
                                        double total = 0.0;
                                        for (String moveGuest : players.get(playername).possibleGuestNames) {
                                            if (pieces.get(moveGuest).row == pieces.get(whotoask).row || pieces.get(moveGuest).col == pieces.get(whotoask).col) {
                                                cansee++;
                                                total++;
                                            } else {
                                                cantsee++;
                                                total++;
                                            }
                                        }
                                        if (cansee != 0 && cantsee != 0) {
                                            double entropy = -(((cansee / total) * (Math.log(cansee / total) / Math.log(2))) + ((cantsee / total) * (Math.log(cantsee / total) / Math.log(2))));
                                            calculatedScore = entropy * informationGain;
                                        }
                                        if (calculatedScore > topInfoGainFromAsking) {
                                            topInfoGainFromAsking = calculatedScore;
                                            topPlayerToAskTemp = playername;
                                        }
                                    }
                                    if (ply > 0) {
                                        String[] diceFaces1 = {"Buford Barnswallow", "Earl of Volesworthy", "Mildred Wellington", "Viola Chung", "Dr. Ashraf Najem"};
                                        String[] diceFaces2 = {"Nadia Bwalya", "Remy La Rocque", "Lily Nesbit", "Trudie Mudge", "Stefano Laconi"};
                                        ArrayList<String> newRemainingCards = new ArrayList<>(Arrays.asList(cardsRemaining));
                                        ArrayList<String> newPlyPlayerName = new ArrayList<>(Arrays.asList(otherPlayerNames));
                                        topInfoGainFromAsking += exploreOtherPlayersOptions(newRemainingCards, 0, newPlyPlayerName, card2, card1);
//                                    System.out.println("this is the calculated score frome card2 givengem, ask: " + topInfoGainFromAsking);
                                        topInfoGainFromAsking += Double.parseDouble(diveCards(diceFaces1[r.nextInt(diceFaces1.length)], diceFaces2[r.nextInt(diceFaces2.length)], card1, cardsRemaining[r.nextInt(cardsRemaining.length)], maxply, ply - 1));
                                    }
                                    if (topInfoGainFromAsking > maxPointGain) {
                                        maxPointGain = topInfoGainFromAsking;
                                        maxwhotoask = topPlayerToAskTemp;
                                        MaxP1Move = p1move;
                                        MaxP2Move = p2move;
                                        finalCard = 2;
                                        finalD1 = d1guest;
                                        finalD2 = d2guest;
                                    }
                                }
                            } else {
                                if (card2.split(":")[1].startsWith("viewDeck")) {
                                    double gemPointsPossible = extraGemForSet;
                                    double topInfoGainFromGem = -10000;
                                    String topGemToGetTemp = "";
                                    for (String gem : myGemOptions) {
                                        if (gemsneeded.contains(gem)) {
                                            if (gemsneeded.size() == 1) {
                                                gemPointsPossible = lastGemForSet;
                                            } else if (gemsneeded.size() == 2)
                                                gemPointsPossible = secondGemForSet;
                                            else
                                                gemPointsPossible = firstGemForSet;
                                        }
                                        double infoloss = 0;
                                        ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem(gem);
                                        for (String playername : otherPlayerNames) {
                                            for (String playersgems : players.get(playername).possibleGuestNamesofMe) {
                                                if (!possibleGuests.contains(playersgems)) {
                                                    infoloss += (1.0 / players.get(playername).possibleGuestNamesofMe.size());
                                                }
                                            }
                                        }
                                        double calculatedScore = (infoloss * informationLost) + gemPointsPossible + valueOfViewCared;
                                        if (calculatedScore > topInfoGainFromGem) {
                                            topInfoGainFromGem = calculatedScore;
                                            topGemToGetTemp = gem;
                                        }
                                    }
                                    if (ply > 0) {
                                        String[] diceFaces1 = {"Buford Barnswallow", "Earl of Volesworthy", "Mildred Wellington", "Viola Chung", "Dr. Ashraf Najem"};
                                        String[] diceFaces2 = {"Nadia Bwalya", "Remy La Rocque", "Lily Nesbit", "Trudie Mudge", "Stefano Laconi"};
                                        ArrayList<String> newRemainingCards = new ArrayList<>(Arrays.asList(cardsRemaining));
                                        ArrayList<String> newPlyPlayerName = new ArrayList<>(Arrays.asList(otherPlayerNames));
                                        topInfoGainFromGem += exploreOtherPlayersOptions(newRemainingCards, 0, newPlyPlayerName, card2, card1);
//                                    System.out.println("this is the calculated score frome card2 get gem, view: " + topInfoGainFromGem);
                                        topInfoGainFromGem += Double.parseDouble(diveCards(diceFaces1[r.nextInt(diceFaces1.length)], diceFaces2[r.nextInt(diceFaces2.length)], card1, cardsRemaining[r.nextInt(cardsRemaining.length)], maxply, ply - 1));
                                    }
                                    if (topInfoGainFromGem > maxPointGain) {
                                        maxPointGain = topInfoGainFromGem;
                                        bestgemtotake = topGemToGetTemp;
                                        MaxP1Move = p1move;
                                        MaxP2Move = p2move;
                                        finalCard = 2;
                                        finalD1 = d1guest;
                                        finalD2 = d2guest;
                                    }
                                } else {
                                    String whotoask = card2.split(":")[1].split(",")[1];
                                    double gemPointsPossible = extraGemForSet;
                                    double topInfoGainFromGem = -10000;
                                    String topGemToGetTemp = "";
                                    String topPlayerToAskTemp = "";
                                    for (String gem : myGemOptions) {
                                        if (gemsneeded.contains(gem)) {
                                            if (gemsneeded.size() == 1) {
                                                gemPointsPossible = lastGemForSet;
                                            } else if (gemsneeded.size() == 2)
                                                gemPointsPossible = secondGemForSet;
                                            else
                                                gemPointsPossible = firstGemForSet;
                                        }
                                        double infoloss = 0;
                                        ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem(gem);

                                        for (String playername : otherPlayerNames) {

                                            for (String playersgems : players.get(playername).possibleGuestNamesofMe) {
                                                if (!possibleGuests.contains(playersgems)) {
                                                    infoloss += (1.0 / players.get(playername).possibleGuestNamesofMe.size());
                                                }
                                            }
                                        }
                                        for (String playername : otherPlayerNames) {
                                            double cansee = 0.0;
                                            double cantsee = 0.0;
                                            double total = 0.0;
                                            for (String moveGuest : players.get(playername).possibleGuestNames) {
                                                if (pieces.get(moveGuest).row == pieces.get(whotoask).row || pieces.get(moveGuest).col == pieces.get(whotoask).col) {
                                                    cansee++;
                                                    total++;
                                                } else {
                                                    cantsee++;
                                                    total++;
                                                }
                                            }
                                            double calculatedScore = (infoloss * informationLost) + gemPointsPossible;
                                            if (cansee != 0 && cantsee != 0) {
                                                double entropy = -(((cansee / total) * (Math.log(cansee / total) / Math.log(2))) + ((cantsee / total) * (Math.log(cantsee / total) / Math.log(2))));
                                                calculatedScore = entropy * informationGain + (infoloss * informationLost) + gemPointsPossible;
                                            }
                                            if (calculatedScore > topInfoGainFromGem) {
                                                topInfoGainFromGem = calculatedScore;
                                                topGemToGetTemp = gem;
                                                topPlayerToAskTemp = playername;
                                            }

                                        }
                                    }
                                    if (ply > 0) {
                                        String[] diceFaces1 = {"Buford Barnswallow", "Earl of Volesworthy", "Mildred Wellington", "Viola Chung", "Dr. Ashraf Najem"};
                                        String[] diceFaces2 = {"Nadia Bwalya", "Remy La Rocque", "Lily Nesbit", "Trudie Mudge", "Stefano Laconi"};
                                        ArrayList<String> newRemainingCards = new ArrayList<>(Arrays.asList(cardsRemaining));
                                        ArrayList<String> newPlyPlayerName = new ArrayList<>(Arrays.asList(otherPlayerNames));
                                        topInfoGainFromGem += exploreOtherPlayersOptions(newRemainingCards, 0, newPlyPlayerName, card2, card1);
//                                    System.out.println("this is the calculated score frome card2 get gem, ask: " + topInfoGainFromGem);
                                        topInfoGainFromGem += Double.parseDouble(diveCards(diceFaces1[r.nextInt(diceFaces1.length)], diceFaces2[r.nextInt(diceFaces2.length)], card1, cardsRemaining[r.nextInt(cardsRemaining.length)], maxply, ply - 1));
                                    }
                                    if (topInfoGainFromGem > maxPointGain) {
                                        bestgemtotake = topGemToGetTemp;
                                        maxPointGain = topInfoGainFromGem;
                                        maxwhotoask = topPlayerToAskTemp;
                                        MaxP1Move = p1move;
                                        MaxP2Move = p2move;
                                        finalCard = 2;
                                        finalD1 = d1guest;
                                        finalD2 = d2guest;
                                    }
                                }
                            }
//                        }
                        this.board.movePlayer(pieces.get(d2guest), p2OGrow, p2OGcol); // Perform the move on my board
                        pieces.remove(d2guest);
                        Piece p4 = new Piece(d2guest);
                        p4.setCordinates(p2OGrow, p2OGcol);
                        pieces.put(d2guest, p4);
                    }
                }
                //add ? chance end here
                this.board.movePlayer(pieces.get(d1guest), p1OGrow, p1OGcol); // Perform the move on my board
                pieces.remove(d1guest);
                Piece p4 = new Piece(d1guest);
                p4.setCordinates(p1OGrow, p1OGcol);
                pieces.put(d1guest, p4);
            }
        }
        if (ply != maxply) {
            return "" + maxPointGain;
        }
        actions += "move," + finalD1 + "," + MaxP1Move;
        this.board.movePlayer(pieces.get(finalD1), Integer.parseInt(MaxP1Move.split(",")[0]), Integer.parseInt(MaxP1Move.split(",")[1])); // Perform the move on my board
        pieces.remove(finalD1);
        Piece p4 = new Piece(finalD1);
        p4.setCordinates(Integer.parseInt(MaxP1Move.split(",")[0]), Integer.parseInt(MaxP1Move.split(",")[1]));
        pieces.put(finalD1, p4);
        actions += ":move," + finalD2 + "," + MaxP2Move;
        this.board.movePlayer(pieces.get(finalD2), Integer.parseInt(MaxP2Move.split(",")[0]), Integer.parseInt(MaxP2Move.split(",")[1])); // Perform the move on my board
        pieces.remove(finalD2);
        Piece p5 = new Piece(finalD2);
        p5.setCordinates(Integer.parseInt(MaxP2Move.split(",")[0]), Integer.parseInt(MaxP2Move.split(",")[1]));
        pieces.put(finalD2, p5);
        actions += (":play,card" + (finalCard));

        if (finalCard == 1) {
            if (card1.split(":")[0].startsWith("move,")) {
                actions += ":move," + bestguesttomove + "," + maxmoverow + "," + maxmovecol + ":" + card1.split(":")[1] + maxwhotoask;
                this.board.movePlayer(pieces.get(bestguesttomove), maxmoverow, maxmovecol); // Perform the move on my board
                pieces.remove(bestguesttomove);
                Piece p6 = new Piece(bestguesttomove);
                p6.setCordinates(maxmoverow, maxmovecol);
                pieces.put(bestguesttomove, p6);
            } else if (card1.split(":")[1].startsWith("move,")) {

                actions += ":get," + bestgemtotake + ":move," + bestguesttomove + "," + maxmoverow + "," + maxmovecol;
//                this.board.movePlayer(pieces.get(bestguesttomove), maxmoverow, maxmovecol); // Perform the move on my board
//                pieces.remove(bestguesttomove);
//                Piece p6 = new Piece(bestguesttomove);
//                p6.setCordinates(maxmoverow, maxmovecol);
//                pieces.put(bestguesttomove, p6);
                switch (bestgemtotake) {
                    case "red":
                        gemsRemaining[0]--;
                        gemCounts[Suspicion.RED]++;
                        for (String playerNames : otherPlayerNames) {
                            ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem("red");
                            players.get(playerNames).adjustKnowledgeAboutMe(possibleGuests);
                        }
                        break;
                    case "green":
                        gemCounts[Suspicion.GREEN]++;
                        gemsRemaining[1]--;
                        for (String playerNames : otherPlayerNames) {
                            ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem("green");
                            players.get(playerNames).adjustKnowledgeAboutMe(possibleGuests);
                        }
                        break;
                    case "yellow":
                        gemsRemaining[2]--;
                        gemCounts[Suspicion.YELLOW]++;
                        for (String playerNames : otherPlayerNames) {
                            ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem("yellow");
                            players.get(playerNames).adjustKnowledgeAboutMe(possibleGuests);
                        }
                        break;
                    default:
                }
                this.board.movePlayer(pieces.get(bestguesttomove), maxmoverow, maxmovecol); // Perform the move on my board
                pieces.remove(bestguesttomove);
                Piece p6 = new Piece(bestguesttomove);
                p6.setCordinates(maxmoverow, maxmovecol);
                pieces.put(bestguesttomove, p6);
            } else if (card1.split(":")[0].startsWith("viewDeck")) {
                actions += ":viewDeck" + ":" + card1.split(":")[1] + maxwhotoask;
            } else if (card1.split(":")[0].startsWith("get,red")) {
                gemsRemaining[0]--;
                gemCounts[Suspicion.RED]++;
                if (card1.split(":")[1].startsWith("viewDeck")) {
                    actions += ":get,red:viewDeck";
                } else {
                    actions += ":" + card1.split(":")[0] + ":" + card1.split(":")[1] + maxwhotoask;
                }
            } else if (card1.split(":")[0].startsWith("get,green")) {
                gemCounts[Suspicion.GREEN]++;
                gemsRemaining[1]--;
                if (card1.split(":")[1].startsWith("viewDeck")) {
                    actions += ":" + card1.split(":")[0] + ":viewDeck";
                } else {
                    actions += ":" + card1.split(":")[0] + ":" + card1.split(":")[1] + maxwhotoask;
                }
            } else if (card1.split(":")[0].startsWith("get,yellow")) {
                gemsRemaining[2]--;
                gemCounts[Suspicion.YELLOW]++;
                if (card1.split(":")[1].startsWith("viewDeck")) {
                    actions += ":" + card1.split(":")[0] + ":viewDeck";
                } else {
                    actions += ":" + card1.split(":")[0] + ":" + card1.split(":")[1] + maxwhotoask;
                }
            } else {
                if (card1.split(":")[1].startsWith("viewDeck")) {
                    actions += ":get," + bestgemtotake + ":viewDeck";
                    switch (bestgemtotake) {
                        case "red":
                            gemsRemaining[0]--;
                            gemCounts[Suspicion.RED]++;
                            for (String playerNames : otherPlayerNames) {
                                ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem("red");
                                players.get(playerNames).adjustKnowledgeAboutMe(possibleGuests);
                            }
                            break;
                        case "green":
                            gemCounts[Suspicion.GREEN]++;
                            gemsRemaining[1]--;
                            for (String playerNames : otherPlayerNames) {
                                ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem("green");
                                players.get(playerNames).adjustKnowledgeAboutMe(possibleGuests);
                            }
                            break;
                        case "yellow":
                            gemsRemaining[2]--;
                            gemCounts[Suspicion.YELLOW]++;
                            for (String playerNames : otherPlayerNames) {
                                ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem("yellow");
                                players.get(playerNames).adjustKnowledgeAboutMe(possibleGuests);
                            }
                            break;
                        default:
                    }
                } else {
                    actions += ":get," + bestgemtotake + ":" + card1.split(":")[1] + maxwhotoask;
                    switch (bestgemtotake) {
                        case "red":
                            gemsRemaining[0]--;
                            gemCounts[Suspicion.RED]++;
                            for (String playerNames : otherPlayerNames) {
                                ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem("red");
                                players.get(playerNames).adjustKnowledgeAboutMe(possibleGuests);
                            }
                            break;
                        case "green":
                            gemCounts[Suspicion.GREEN]++;
                            gemsRemaining[1]--;
                            for (String playerNames : otherPlayerNames) {
                                ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem("green");
                                players.get(playerNames).adjustKnowledgeAboutMe(possibleGuests);
                            }
                            break;
                        case "yellow":
                            gemsRemaining[2]--;
                            gemCounts[Suspicion.YELLOW]++;
                            for (String playerNames : otherPlayerNames) {
                                ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem("yellow");
                                players.get(playerNames).adjustKnowledgeAboutMe(possibleGuests);
                            }
                            break;
                        default:
                    }
                }
            }
        }
        if (finalCard == 2) {
            if (card2.split(":")[0].startsWith("move,")) {
                actions += ":move," + bestguesttomove + "," + maxmoverow + "," + maxmovecol + ":" + card2.split(":")[1] + maxwhotoask;
                this.board.movePlayer(pieces.get(bestguesttomove), maxmoverow, maxmovecol); // Perform the move on my board
                pieces.remove(bestguesttomove);
                Piece p6 = new Piece(bestguesttomove);
                p6.setCordinates(maxmoverow, maxmovecol);
                pieces.put(bestguesttomove, p6);
            } else if (card2.split(":")[1].startsWith("move,")) {
                actions += ":get," + bestgemtotake + ":move," + bestguesttomove + "," + maxmoverow + "," + maxmovecol;
//                this.board.movePlayer(pieces.get(bestguesttomove), maxmoverow, maxmovecol); // Perform the move on my board
//                pieces.remove(bestguesttomove);
//                Piece p6 = new Piece(bestguesttomove);
//                p6.setCordinates(maxmoverow, maxmovecol);
//                pieces.put(bestguesttomove, p6);
                switch (bestgemtotake) {
                    case "red":
                        gemsRemaining[0]--;
                        gemCounts[Suspicion.RED]++;
                        for (String playerNames : otherPlayerNames) {
                            ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem("red");
                            players.get(playerNames).adjustKnowledgeAboutMe(possibleGuests);
                        }
                        break;
                    case "green":
                        gemCounts[Suspicion.GREEN]++;
                        gemsRemaining[1]--;
                        for (String playerNames : otherPlayerNames) {
                            ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem("green");
                            players.get(playerNames).adjustKnowledgeAboutMe(possibleGuests);
                        }
                        break;
                    case "yellow":
                        gemsRemaining[2]--;
                        gemCounts[Suspicion.YELLOW]++;
                        for (String playerNames : otherPlayerNames) {
                            ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem("yellow");
                            players.get(playerNames).adjustKnowledgeAboutMe(possibleGuests);
                        }
                        break;
                    default:
                }
                this.board.movePlayer(pieces.get(bestguesttomove), maxmoverow, maxmovecol); // Perform the move on my board
                pieces.remove(bestguesttomove);
                Piece p6 = new Piece(bestguesttomove);
                p6.setCordinates(maxmoverow, maxmovecol);
                pieces.put(bestguesttomove, p6);
            } else if (card2.split(":")[0].startsWith("viewDeck")) {
                actions += ":viewDeck" + ":" + card2.split(":")[1] + maxwhotoask;
            } else if (card2.split(":")[0].startsWith("get,red")) {
                gemsRemaining[0]--;
                gemCounts[Suspicion.RED]++;
                if (card2.split(":")[1].startsWith("viewDeck")) {
                    actions += ":" + card2.split(":")[0] + ":viewDeck";
                } else {
                    actions += ":" + card2.split(":")[0] + ":" + card2.split(":")[1] + maxwhotoask;
                }
            } else if (card2.split(":")[0].startsWith("get,green")) {
                gemCounts[Suspicion.GREEN]++;
                gemsRemaining[1]--;
                if (card2.split(":")[1].startsWith("viewDeck")) {
                    actions += ":" + card2.split(":")[0] + ":viewDeck";
                } else {
                    actions += ":" + card2.split(":")[0] + ":" + card2.split(":")[1] + maxwhotoask;
                }
            } else if (card2.split(":")[0].startsWith("get,yellow")) {
                gemsRemaining[2]--;
                gemCounts[Suspicion.YELLOW]++;
                if (card2.split(":")[1].startsWith("viewDeck")) {
                    actions += ":" + card2.split(":")[0] + ":viewDeck";
                } else {
                    actions += ":" + card2.split(":")[0] + ":" + card2.split(":")[1] + maxwhotoask;
                }
            } else {
                if (card2.split(":")[1].startsWith("viewDeck")) {
                    actions += ":get," + bestgemtotake + ":viewDeck";
                    switch (bestgemtotake) {
                        case "red":
                            gemsRemaining[0]--;
                            gemCounts[Suspicion.RED]++;
                            for (String playerNames : otherPlayerNames) {
                                ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem("red");
                                players.get(playerNames).adjustKnowledgeAboutMe(possibleGuests);
                            }
                            break;
                        case "green":
                            gemCounts[Suspicion.GREEN]++;
                            gemsRemaining[1]--;
                            for (String playerNames : otherPlayerNames) {
                                ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem("green");
                                players.get(playerNames).adjustKnowledgeAboutMe(possibleGuests);
                            }
                            break;
                        case "yellow":
                            gemsRemaining[2]--;
                            gemCounts[Suspicion.YELLOW]++;
                            for (String playerNames : otherPlayerNames) {
                                ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem("yellow");
                                players.get(playerNames).adjustKnowledgeAboutMe(possibleGuests);
                            }
                            break;
                        default:
                    }
                } else {
                    actions += ":get," + bestgemtotake + ":" + card2.split(":")[1] + maxwhotoask;
                    switch (bestgemtotake) {
                        case "red":
                            gemsRemaining[0]--;
                            gemCounts[Suspicion.RED]++;
                            for (String playerNames : otherPlayerNames) {
                                ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem("red");
                                players.get(playerNames).adjustKnowledgeAboutMe(possibleGuests);
                            }
                            break;
                        case "green":
                            gemCounts[Suspicion.GREEN]++;
                            gemsRemaining[1]--;
                            for (String playerNames : otherPlayerNames) {
                                ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem("green");
                                players.get(playerNames).adjustKnowledgeAboutMe(possibleGuests);
                            }
                            break;
                        case "yellow":
                            gemsRemaining[2]--;
                            gemCounts[Suspicion.YELLOW]++;
                            for (String playerNames : otherPlayerNames) {
                                ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem("yellow");
                                players.get(playerNames).adjustKnowledgeAboutMe(possibleGuests);
                            }
                            break;
                        default:
                    }
                }
            }
        }
        System.out.println(actions);
        return actions;
    }

    public double exploreOtherPlayersOptions(ArrayList<String> remainingCards, int plyplayer, ArrayList<String> plyPlayerName, String playedCard, String nextCardToPlay) {
//        public double exploreOtherPlayersOptions(ArrayList<String> remainingCards, int plyplayer, ArrayList<String> plyPlayerName, int alpha, int beta, String SecondCard) {
        if (remainingCards.size() < 1) {
            remainingCards.addAll(Arrays.asList(cardActions));
            remainingCards.remove(playedCard);
            remainingCards.remove(nextCardToPlay);
        }
        String[] diceFaces1 = {"Buford Barnswallow", "Earl of Volesworthy", "Mildred Wellington", "Viola Chung", "Dr. Ashraf Najem"};
        String[] diceFaces2 = {"Nadia Bwalya", "Remy La Rocque", "Lily Nesbit", "Trudie Mudge", "Stefano Laconi"};
        ArrayList<String> d1list = new ArrayList<>();
        ArrayList<String> d2list = new ArrayList<>();
        ArrayList<String> gemsneeded = new ArrayList<>();
        String MaxP1Move = "";
        String MaxP2Move = "";
        String finalD1 = "";
        String finalD2 = "";
        double calculatedScore = 0;
        double lastGemForSet = (-4.0 / (otherPlayerNames.length * (plyplayer+1)));
        double secondGemForSet = (-3.0 / (otherPlayerNames.length* (plyplayer+1)));
        double firstGemForSet = (-2.0 / (otherPlayerNames.length* (plyplayer+1)));
        double extraGemForSet = (-1.0 / (otherPlayerNames.length* (plyplayer+1)));
        double informationLost = (1.01 / (otherPlayerNames.length* (plyplayer+1)));
        double informationGain = (-1.01 / (otherPlayerNames.length* (plyplayer+1)));
        double maxPointGain = 1000000;
        double returnMaxPoint = 1000000;
        double valueOfViewCared = 0;
        for (String playername : otherPlayerNames) {
            valueOfViewCared += players.get(playername).possibleGuestNames.size();
        }
        valueOfViewCared = (valueOfViewCared / (-3 * otherPlayerNames.length));
        String checkedPlyPlayerName = plyPlayerName.get(0);
        int[] plyPlayerGemcount = players.get(checkedPlyPlayerName).gemCounts;
        if (plyPlayerGemcount[0] == plyPlayerGemcount[1]) {
            if (plyPlayerGemcount[0] == plyPlayerGemcount[2]) {
                gemsneeded.add("red");
                gemsneeded.add("green");
                gemsneeded.add("yellow");
            } else if (plyPlayerGemcount[0] < plyPlayerGemcount[2]) {
                gemsneeded.add("red");
                gemsneeded.add("green");
            } else {
                gemsneeded.add("yellow");
            }
        } else if (plyPlayerGemcount[0] == plyPlayerGemcount[2]) {
            if (plyPlayerGemcount[0] < plyPlayerGemcount[1]) {
                gemsneeded.add("red");
                gemsneeded.add("yellow");
            } else {
                gemsneeded.add("green");
            }
        } else if (plyPlayerGemcount[1] == plyPlayerGemcount[2]) {
            if (plyPlayerGemcount[0] < plyPlayerGemcount[1])
                gemsneeded.add("red");
            else {
                gemsneeded.add("yellow");
                gemsneeded.add("green");
            }
        } else if (plyPlayerGemcount[0] < plyPlayerGemcount[1] && plyPlayerGemcount[0] < plyPlayerGemcount[2]) {
            gemsneeded.add("red");
        } else if (plyPlayerGemcount[1] < plyPlayerGemcount[0] && plyPlayerGemcount[1] < plyPlayerGemcount[2]) {
            gemsneeded.add("green");
        } else if (plyPlayerGemcount[2] < plyPlayerGemcount[0] && plyPlayerGemcount[2] < plyPlayerGemcount[1]) {
            gemsneeded.add("yellow");
        }


//        for(String d1 : diceFaces1) {
//            if (d1.equals("?")) {
        d1list.addAll(Arrays.asList(diceFaces1));
//            } else {
//                d1list.add(d1);
//            }
//
//            for (String d2 : diceFaces2) {
//
//                if (d2.equals("?")) {
        d2list.addAll(Arrays.asList(diceFaces2));
//                } else {
//                    d2list.add(d2);
//                }
        int indexOfCardPlayed = r.nextInt(remainingCards.size());
        String card1 = remainingCards.get(indexOfCardPlayed);

//        String d1guest = d1list.get(r.nextInt(d1list.size()));
//        String d2guest = d2list.get(r.nextInt(d2list.size()));

//                for (String d1guest : d1list) {
                    for (String d1guest : diceFaces1) {

                        Piece piece1 = this.pieces.get(d1guest);
                        int p1OGcol = piece1.col;
                        int p1OGrow = piece1.row;
                        String[] p1moves = getPossibleMoves(piece1);
                        for (String p1move : p1moves) {   //loop all possible moves of d1
                            this.board.movePlayer(piece1, Integer.parseInt(p1move.split(",")[0]), Integer.parseInt(p1move.split(",")[1])); // Perform the move on my board
                            pieces.remove(d1guest);
                            Piece p1 = new Piece(d1guest);
                            p1.setCordinates(Integer.parseInt(p1move.split(",")[0]), Integer.parseInt(p1move.split(",")[1]));
                            pieces.put(d1guest, p1);

//                        for (String d2guest : d2list) {
                            for (String d2guest : diceFaces2) {

                                Piece piece2 = pieces.get(d2guest);
                                int p2OGcol = piece2.col;
                                int p2OGrow = piece2.row;
                                String[] p2moves = getPossibleMoves(pieces.get(d2guest));
                                if (d2guest.equals(d1guest)) {
                                    Piece p5 = new Piece(d1guest);
                                    p5.setCordinates(Integer.parseInt(p1move.split(",")[0]), Integer.parseInt(p1move.split(",")[1]));
                                    p2moves = getPossibleMoves(p5);
                                }
                                for (String p2move : p2moves) {   //loop all possible moves of d2
                                    this.board.movePlayer(piece2, Integer.parseInt(p2move.split(",")[0]), Integer.parseInt(p2move.split(",")[1])); // Perform the move on my board

                                    pieces.remove(d2guest);
                                    Piece p2 = new Piece(d2guest);
                                    p2.setCordinates(Integer.parseInt(p2move.split(",")[0]), Integer.parseInt(p2move.split(",")[1]));
                                    pieces.put(d2guest, p2);

                                    ArrayList<String> myGemOptions = new ArrayList<>();
                                    for (String moveGuest : players.get(checkedPlyPlayerName).possibleGuestNames) {
                                        for (String gemAvail : this.board.rooms[pieces.get(moveGuest).row][pieces.get(moveGuest).col].availableGems) {
                                            if (!myGemOptions.contains(gemAvail)) {
                                                myGemOptions.add(gemAvail);
                                            } else {
                                                break;
                                            }
                                        }
                                    }
//                                    calculatedScore = 1000000;

//                                for (String card1 : remainingCards) {
                                    if (card1.contains("move")) {
//                                        for (String guest : allCharacters) {
//                                        String guest = allCharacters[r.nextInt(allCharacters.length)];
//                                            for (int i = 0; i < 3; i++) {
//                                                for (int j = 0; j < 4; j++) {
//                                        int moverow = pieces.get(guest).row;
//                                        int movecol = pieces.get(guest).col;
                                        int[][] midBoardRow = new int[][]{{2,0},{2,1},{2,2},{2,3}};
                                        for (String guest : allCharacters) {
//                                String guest = allCharacters[r.nextInt(allCharacters.length)];
                                            int moverow = pieces.get(guest).row;
                                            int movecol = pieces.get(guest).col;
                                            for (int[] moveMidBoard : midBoardRow) {
                                                int tempi = moveMidBoard[0];
                                                int tempj = moveMidBoard[1];
//                                                    if (i != moverow || j != movecol) {
//                                        int tempi = r.nextInt(3);
//                                        int tempj = r.nextInt(4);
                                                while (tempi == moverow && tempj == movecol) {
                                                    tempi = r.nextInt(3);
                                                    tempj = r.nextInt(4);
                                                }
                                                this.board.movePlayer(pieces.get(guest), tempi, tempj); // Perform the move on my board
                                                pieces.remove(guest);
                                                Piece p3 = new Piece(guest);
                                                p3.setCordinates(tempi, tempj);
                                                pieces.put(guest, p3);
                                                if (card1.split(":")[0].startsWith("move,")) {
                                                    String whotoask = card1.split(":")[1].split(",")[1];
                                                    double cansee = 0.0;
                                                    double cantsee = 0.0;
                                                    double total = 0.0;
                                                    for (String moveGuest : players.get(checkedPlyPlayerName).possibleGuestNamesofMe) {
                                                        if (pieces.get(moveGuest).row == pieces.get(whotoask).row || pieces.get(moveGuest).col == pieces.get(whotoask).col) {
                                                            cansee++;
                                                            total++;
                                                        } else {
                                                            cantsee++;
                                                            total++;
                                                        }
                                                    }
                                                    if (cansee != 0 && cantsee != 0) {
                                                        double entropy = -(((cansee / total) * (Math.log(cansee / total) / Math.log(2))) + ((cantsee / total) * (Math.log(cantsee / total) / Math.log(2))));
                                                        calculatedScore = entropy * informationGain;
                                                    }
                                                    if (calculatedScore < maxPointGain) {
                                                        MaxP1Move = p1move;
                                                        MaxP2Move = p2move;
                                                        finalD1 = d1guest;
                                                        finalD2 = d2guest;
                                                        maxPointGain = calculatedScore;
                                                    }
                                                } else {
                                                    double gemPointsPossible = extraGemForSet;
                                                    for (String gem : myGemOptions) {
                                                        if (gemsneeded.contains(gem)) {
                                                            if (gemsneeded.size() == 1) {
                                                                gemPointsPossible = lastGemForSet;
                                                            } else if (gemsneeded.size() == 2)
                                                                gemPointsPossible = secondGemForSet;
                                                            else
                                                                gemPointsPossible = firstGemForSet;
                                                        }
                                                        double infoloss = 0;
                                                        ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem(gem);
                                                        for (String playersgems : players.get(checkedPlyPlayerName).possibleGuestNames) {
                                                            if (!possibleGuests.contains(playersgems)) {
                                                                infoloss += (1.0 / players.get(checkedPlyPlayerName).possibleGuestNames.size());
                                                            }
                                                        }
                                                        calculatedScore = (infoloss * informationLost) + gemPointsPossible;
                                                        if (calculatedScore < maxPointGain) {
                                                            MaxP1Move = p1move;
                                                            MaxP2Move = p2move;
                                                            finalD1 = d1guest;
                                                            finalD2 = d2guest;
                                                            maxPointGain = calculatedScore;
                                                        }
                                                    }
                                                }
                                                this.board.movePlayer(pieces.get(guest), moverow, movecol); // Perform the move on my board
                                                pieces.remove(guest);
                                                Piece p4 = new Piece(guest);
                                                p4.setCordinates(moverow, movecol);
                                                pieces.put(guest, p4);
                                            }
                                        }
                                    } else if (card1.split(":")[0].startsWith("viewDeck")) { //view deck is first action on card 1
                                        String whotoask = card1.split(":")[1].split(",")[1];
                                        double cansee = 0;
                                        double cantsee = 0;
                                        double total = 0;
                                        for (String moveGuest : players.get(checkedPlyPlayerName).possibleGuestNamesofMe) {
                                            if (pieces.get(moveGuest).row == pieces.get(whotoask).row || pieces.get(moveGuest).col == pieces.get(whotoask).col) {
                                                cansee++;
                                                total++;
                                            } else {
                                                cantsee++;
                                                total++;
                                            }
                                        }
                                        if (cansee != 0 && cantsee != 0) {
                                            double entropy = -(((cansee / total) * (Math.log(cansee / total) / Math.log(2))) + ((cantsee / total) * (Math.log(cantsee / total) / Math.log(2))));
                                            calculatedScore = entropy * informationGain + valueOfViewCared;
                                        }
                                        if (calculatedScore < maxPointGain) {
                                            MaxP1Move = p1move;
                                            MaxP2Move = p2move;
                                            finalD1 = d1guest;
                                            finalD2 = d2guest;
                                            maxPointGain = calculatedScore;
                                        }
                                    } else if (card1.split(":")[0].startsWith("get,yellow") || card1.split(":")[0].startsWith("get,red")
                                            || card1.split(":")[0].startsWith("get,green")) {   // get given gem is the first action on card 1
                                        double gemPointsPossible = extraGemForSet;
                                        if (gemsneeded.contains(card1.split(":")[0].split(",")[1])) {
                                            if (gemsneeded.size() == 1) {
                                                gemPointsPossible = lastGemForSet;
                                            } else if (gemsneeded.size() == 2)
                                                gemPointsPossible = secondGemForSet;
                                            else
                                                gemPointsPossible = firstGemForSet;
                                        }
                                        if (card1.split(":")[1].startsWith("viewDeck")) { //view deck is first action on card 1
                                            calculatedScore = valueOfViewCared + gemPointsPossible;

                                            if (calculatedScore < maxPointGain) {
                                                MaxP1Move = p1move;
                                                MaxP2Move = p2move;
                                                finalD1 = d1guest;
                                                finalD2 = d2guest;
                                                maxPointGain = calculatedScore;
                                            }
                                        } else {
                                            String whotoask = card1.split(":")[1].split(",")[1];
                                            double cansee = 0;
                                            double cantsee = 0;
                                            double total = 0;
                                            for (String moveGuest : players.get(checkedPlyPlayerName).possibleGuestNamesofMe) {
                                                if (pieces.get(moveGuest).row == pieces.get(whotoask).row || pieces.get(moveGuest).col == pieces.get(whotoask).col) {
                                                    cansee++;
                                                    total++;
                                                } else {
                                                    cantsee++;
                                                    total++;
                                                }
                                            }
                                            if (cansee != 0 && cantsee != 0) {
                                                double entropy = -(((cansee / total) * (Math.log(cansee / total) / Math.log(2))) + ((cantsee / total) * (Math.log(cantsee / total) / Math.log(2))));
                                                calculatedScore = entropy * informationGain + gemPointsPossible;
                                            }
                                            if (calculatedScore < maxPointGain) {
                                                MaxP1Move = p1move;
                                                MaxP2Move = p2move;
                                                finalD1 = d1guest;
                                                finalD2 = d2guest;
                                                maxPointGain = calculatedScore;
                                            }
                                        }
                                    } else {
                                        if (card1.split(":")[1].startsWith("viewDeck")) {
                                            double gemPointsPossible = extraGemForSet;
                                            for (String gem : myGemOptions) {
                                                if (gemsneeded.contains(gem)) {
                                                    if (gemsneeded.size() == 1) {
                                                        gemPointsPossible = lastGemForSet;
                                                    } else if (gemsneeded.size() == 2)
                                                        gemPointsPossible = secondGemForSet;
                                                    else
                                                        gemPointsPossible = firstGemForSet;
                                                }
                                                double infoloss = 0;
                                                ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem(gem);
                                                for (String playersgems : players.get(checkedPlyPlayerName).possibleGuestNamesofMe) {
                                                    if (!possibleGuests.contains(playersgems)) {
                                                        infoloss += (1.0 / players.get(checkedPlyPlayerName).possibleGuestNamesofMe.size());
                                                    }
                                                }
                                                calculatedScore = (infoloss * informationLost) + gemPointsPossible + valueOfViewCared;

                                                if (calculatedScore < maxPointGain) {
                                                    maxPointGain = calculatedScore;
                                                    MaxP1Move = p1move;
                                                    MaxP2Move = p2move;
                                                    finalD1 = d1guest;
                                                    finalD2 = d2guest;
                                                }
                                            }
                                        } else {
                                            String whotoask = card1.split(":")[1].split(",")[1];
                                            double gemPointsPossible = extraGemForSet;
                                            for (String gem : myGemOptions) {
                                                if (gemsneeded.contains(gem)) {
                                                    if (gemsneeded.size() == 1) {
                                                        gemPointsPossible = lastGemForSet;
                                                    } else if (gemsneeded.size() == 2)
                                                        gemPointsPossible = secondGemForSet;
                                                    else
                                                        gemPointsPossible = firstGemForSet;
                                                }
                                                double infoloss = 0;
                                                ArrayList<String> possibleGuests = getPiecesNameInRoomWithGem(gem);
                                                for (String playersgems : players.get(checkedPlyPlayerName).possibleGuestNamesofMe) {
                                                    if (!possibleGuests.contains(playersgems)) {
                                                        infoloss += (1.0 / players.get(checkedPlyPlayerName).possibleGuestNamesofMe.size());
                                                    }
                                                }
                                                double cansee = 0.0;
                                                double cantsee = 0.0;
                                                double total = 0.0;
                                                for (String moveGuest : players.get(checkedPlyPlayerName).possibleGuestNamesofMe) {
                                                    if (pieces.get(moveGuest).row == pieces.get(whotoask).row || pieces.get(moveGuest).col == pieces.get(whotoask).col) {
                                                        cansee++;
                                                        total++;
                                                    } else {
                                                        cantsee++;
                                                        total++;
                                                    }
                                                }

                                                if (cansee != 0 && cantsee != 0) {
                                                    double entropy = -(((cansee / total) * (Math.log(cansee / total) / Math.log(2))) + ((cantsee / total) * (Math.log(cantsee / total) / Math.log(2))));
                                                    calculatedScore = entropy * informationGain + (infoloss * informationLost) + gemPointsPossible;
                                                } else {
                                                    calculatedScore = (infoloss * informationLost) + gemPointsPossible;

                                                }
                                                if (calculatedScore < maxPointGain) {
                                                    maxPointGain = calculatedScore;
                                                    MaxP1Move = p1move;
                                                    MaxP2Move = p2move;
                                                    finalD1 = d1guest;
                                                    finalD2 = d2guest;
                                                }
                                            }
                                        }
                                    }
//                                    System.out.println("calculated score: "+ calculatedScore +"\nmaxPoints: "+maxPointGain);
                                    this.board.movePlayer(pieces.get(d2guest), p2OGrow, p2OGcol); // Perform the move on my board
                                    pieces.remove(d2guest);
                                    Piece p4 = new Piece(d2guest);
                                    p4.setCordinates(p2OGrow, p2OGcol);
                                    pieces.put(d2guest, p4);
                                }
                            }
                            this.board.movePlayer(pieces.get(d1guest), p1OGrow, p1OGcol); // Perform the move on my board
                            pieces.remove(d1guest);
                            Piece p4 = new Piece(d1guest);
                            p4.setCordinates(p1OGrow, p1OGcol);
                            pieces.put(d1guest, p4);
                        }
                    }
        if (plyplayer < otherPlayerNames.length - 1) {
            int OGpiece1Row = pieces.get(finalD1).row;
            int OGpiece1Col = pieces.get(finalD1).col;
            int OGpiece2Row = pieces.get(finalD2).row;
            int OGpiece2Col = pieces.get(finalD2).col;

            this.board.movePlayer(pieces.get(finalD1), Integer.parseInt(MaxP1Move.split(",")[0]), Integer.parseInt(MaxP1Move.split(",")[1])); // Perform the move on my board
            pieces.remove(finalD1);
            Piece p2 = new Piece(finalD1);
            p2.setCordinates(Integer.parseInt(MaxP1Move.split(",")[0]), Integer.parseInt(MaxP1Move.split(",")[1]));
            pieces.put(finalD1, p2);
            this.board.movePlayer(pieces.get(finalD2), Integer.parseInt(MaxP2Move.split(",")[0]), Integer.parseInt(MaxP2Move.split(",")[1])); // Perform the move on my board
            pieces.remove(finalD2);
            p2 = new Piece(finalD2);
            p2.setCordinates(Integer.parseInt(MaxP2Move.split(",")[0]), Integer.parseInt(MaxP2Move.split(",")[1]));
            pieces.put(finalD2, p2);


            ArrayList<String> newRemainingCards = new ArrayList<>();
            for (String cpyplayer : remainingCards) {
                if (!cpyplayer.equals(card1)) {
                    newRemainingCards.add(cpyplayer);
                }
            }
            ArrayList<String> newPlyPlayerName = new ArrayList<>();
            for (String cpyPlyPlayerName : plyPlayerName) {
                if (!cpyPlyPlayerName.equals(checkedPlyPlayerName)) {
                    newPlyPlayerName.add(cpyPlyPlayerName);
                }
            }
            double nextLayersWorstRes = exploreOtherPlayersOptions(newRemainingCards, (plyplayer + 1), newPlyPlayerName, playedCard, nextCardToPlay);
            if ((maxPointGain + nextLayersWorstRes) < returnMaxPoint) {
                returnMaxPoint = (maxPointGain + nextLayersWorstRes);
            }

//            System.out.println("\nnexlayer: "+nextLayersWorstRes);
            this.board.movePlayer(pieces.get(finalD1), OGpiece1Row, OGpiece1Col); // Perform the move on my board
            pieces.remove(finalD1);
            p2 = new Piece(finalD1);
            p2.setCordinates(OGpiece1Row, OGpiece1Col);
            pieces.put(finalD1, p2);
            this.board.movePlayer(pieces.get(finalD2), OGpiece2Row, OGpiece2Col); // Perform the move on my board
            pieces.remove(finalD2);
            p2 = new Piece(finalD2);
            p2.setCordinates(OGpiece2Row, OGpiece2Col);
            pieces.put(finalD2, p2);

        }
        else{
            return maxPointGain;
        }
//        System.out.println("ply player: "+ plyplayer+"\nreturnPoints: "+returnMaxPoint +"\n maxpointsgain: "+maxPointGain);
        return returnMaxPoint;
    }

    public String getPlayerActions(String d1, String d2, String card1, String card2, String board) {
        //new board
        this.board = new Board(board, pieces, gemLocations);
        //remove card from remaining list
        if (cardsRemaining.length < otherPlayerNames.length + 1) {
            cardsRemaining = cardActions.clone();
            cardInHand1 = "";
            cardInHand2 = "";
            cardsInHands = cardsRemaining;
        }
        String[] temp = new String[0];
        int j = 0;
        if (!cardInHand1.equals(card1)) {
            cardInHand1 = card1;
            int onecard = 0;
            temp = new String[cardsRemaining.length - 1];

            for (String card : cardsRemaining) {
                if (!card.equals(card1) || onecard != 0) {
                    if (j < temp.length)
                        temp[j] = card;
                    j++;
                } else {
                    onecard++;
                }
            }
            cardsRemaining = temp;
        }
        if (!cardInHand2.equals(card2)) {
            j = 0;
            cardInHand2 = card2;
            int onecard = 0;
            temp = new String[cardsRemaining.length - 1];
            for (String card : cardsRemaining) {
                if (!card.equals(card2) || onecard != 0) {
                    if (j < temp.length)
                        temp[j] = card;
                    j++;
                } else {
                    onecard++;
                }
            }
            cardsRemaining = temp;
        }
        //state the number of ply searches wanted at the last parameter
//        return diveCards(d1, d2, card1, card2, 2, 2); //multiply (2 ply)
        return diveCards(d1, d2, card1, card2, 1, 1); //singleply (1 ply)
//        return diveCards(d1, d2, card1, card2, -1, -1); //noply search (-1 ply)


    }

    private int countGems(String gem) {
        if (gem.equals("red")) return gemCounts[Suspicion.RED];
        else if (gem.equals("green")) return gemCounts[Suspicion.GREEN];
        else return gemCounts[Suspicion.YELLOW];
    }

    private ArrayList<String> getGuestsInRoomWithGem(String board, String gemcolor) {
        Board b = new Board(board, pieces, gemLocations);
        int gem = -1;
        if (gemcolor.equals("yellow")) gem = Suspicion.YELLOW;
        else if (gemcolor.equals("green")) gem = Suspicion.GREEN;
        else if (gemcolor.equals("red")) gem = Suspicion.RED;
        ArrayList<String> possibleGuests = new ArrayList<String>();

        int y = 0, x = 0;
        for (String guests : board.trim().split(":")) {
            //only get people from rooms with the gem
            if (b.rooms[y][x].gems[gem] && guests.trim().length() > 0) {
                for (String guest : guests.trim().split(",")) {
                    possibleGuests.add(guest.trim());
                }
            }
            x++;
            y += x / 4;
            x %= 4;
        }

        return possibleGuests;
    }

    public void reportPlayerActions(String player, String d1, String d2, String cardPlayed, String board, String actions) {
//        System.out.println("\n\n\n"+actions+"\n\n\n");

    }

    public void reportPlayerActions(String player, String d1, String d2, String cardPlayed, String[] board, String actions) {

//        System.out.println("\n\n\n"+actions+"\n"+cardPlayed+"\n\n\n");
//        for (String sdf : players.keySet()){
//            System.out.println(players.get(sdf).playerName);
//        }

        if (player.equals(this.playerName)) {

            return; // If player is me, return
        }

        int onecard = 0;
        int j = 0;
        String[] temp = new String[cardsRemaining.length - 1];
        for (String card : cardsRemaining) {
            if (!card.equals(cardPlayed) || onecard != 0) {
//                System.out.println(cardPlayed);

//                System.out.println(card);
                if (j < temp.length)
                    temp[j] = card;
                j++;

            } else {
                onecard++;
            }
        }
        cardsRemaining = temp;

        if (cardPlayed.split(":")[0].split(",").length == 2) {
            switch (cardPlayed.split(":")[0].split(",")[1]) {
                case "red":
                    gemsRemaining[0]--;
                    players.get(player).gemCounts[0]++;
                    break;
                case "green":
                    gemsRemaining[1]--;
                    players.get(player).gemCounts[1]++;
                    break;
                case "yellow":
                    gemsRemaining[2]--;
                    players.get(player).gemCounts[2]++;
                    break;
            }
        } else if (cardPlayed.split(":")[1].split(",").length == 2) {
            switch (cardPlayed.split(":")[1].split(",")[1]) {
                case "red":
                    gemsRemaining[0]--;
                    players.get(player).gemCounts[0]++;
                    break;
                case "green":
                    gemsRemaining[1]--;
                    players.get(player).gemCounts[1]++;
                    break;
                case "yellow":
                    gemsRemaining[2]--;
                    players.get(player).gemCounts[2]++;
                    break;
            }
        }

        // Check for a get action and use the info to update player knowledge
//        System.out.println("\n\ncheck name: "+cardPlayed.split(":")[0].split(",")[0]+"\n"+"\n");
        if (cardPlayed.split(":")[0].equals("get,") || cardPlayed.split(":")[1].equals("get,")) {
            int splitindex;
            String[] split = actions.split(":");
            String get;
            if (split[3].indexOf("get") >= 0) splitindex = 3;
            else splitindex = 4;
            get = split[splitindex];
            String gem = get.split(",")[1];
            // board[splitIndex+1] will have the state of the board when the gem was taken
            if (board[splitindex] != null) // This would indicate an error in the action
            {
                ArrayList<String> possibleGuests = getGuestsInRoomWithGem(board[splitindex], gem);
                players.get(player).adjustKnowledge(possibleGuests);
            }
        }

        if (cardPlayed.split(":")[0].split(",")[0].equals("ask") || cardPlayed.split(":")[1].split(",")[0].equals("ask")) {
            int splitindex;
            String[] split = actions.split(":");
            String get;
            splitindex = split[3].contains("ask") ? 3 : 4;
            get = split[splitindex];
            String guest = get.split(",")[1];
            String playerasked = get.split(",")[2];
//            System.out.println("\n\ncheck name: "+playerasked+"\n"+guest+"\n");
            if (playerasked.equals(myName)) {
//                System.out.println("\n\ncheck: "+players.get(player).possibleGuestNamesofMe);
                if (canSee(me, pieces.get(guest))) {
//                    System.out.println("CAN SEE");
                    othersAnswerAsk(guest, player, board[splitindex], true);
                } else {
//                    System.out.println("CAN'T SEE");
                    othersAnswerAsk(guest, player, board[splitindex], false);
                }
//                System.out.println("\n\ncheck: "+players.get(player).possibleGuestNamesofMe);
            }
        }
    }

    private boolean canSee(Piece p1, Piece p2) // returns whether or not these two pieces see each
    {
        return (p1.row == p2.row || p1.col == p2.col);
    }


    public void answerAsk(String guest, String player, String board, boolean canSee) {
        Board b = new Board(board, pieces, gemLocations);
        ArrayList<String> possibleGuests = new ArrayList<String>();
        Piece p1 = pieces.get(guest);  // retrieve the guest
        for (String k : pieces.keySet()) {
            Piece p2 = pieces.get(k);
            if ((canSee && canSee(p1, p2)) || (!canSee && !canSee(p1, p2))) possibleGuests.add(p2.name);
        }
//        System.out.println("Adjusting knowledge about " + player + " to : " + possibleGuests);
        players.get(player).adjustKnowledge(possibleGuests);
    }

    public void othersAnswerAsk(String guest, String player, String board, boolean canSee) {
        Board b = new Board(board, pieces, gemLocations);
        ArrayList<String> possibleGuests = new ArrayList<String>();
        Piece p1 = pieces.get(guest);  // retrieve the guest
        for (String k : pieces.keySet()) {
            Piece p2 = pieces.get(k);
            if ((canSee && canSee(p1, p2)) || (!canSee && !canSee(p1, p2))) possibleGuests.add(p2.name);
        }
//        System.out.println(possibleGuests);
//        System.out.println("Adjusting knowledge about " + player + " to : " + possibleGuests);
//        players.get(player).adjustKnowledgeAboutMe(possibleGuests);
    }

    public void answerViewDeck(String player) {
        for (String k : players.keySet()) {
            players.get(k).adjustKnowledge(player);
        }
    }


    private Map<String, String> mapGuessProb() {
        Map<String, String> bestPlay = new HashMap<>();
        Map<String, Integer> topRes = new HashMap<>();
        Map<String, List<String>> bestPlaySet = new HashMap<>();
        Builder<Collection<String>> strBuild = Stream.builder();
        Collection<List<String>> permMap = new ArrayList<>();
        Collection<List<String>> perms;
        Stream<Collection<List<String>>> listProb;
//      Add all players possible guest names
        for (String k : players.keySet()) {
            Player player = players.get(k);
            strBuild.add(player.possibleGuestNames);
//          if ther is a known guest name for a player then it is impossible for
//          any other player to be that guest and so we remove it here
            if (player.possibleGuestNames.size() == 1) {
                for (String t : players.keySet()) {
                    if (!k.equals(t)) {
                        players.get(t).adjustKnowledge(player.possibleGuestNames.get(0));
                    }
                }
            }
        }

//      convert the string builder into a double array containing each player's possible guest name
        listProb = strBuild.build().map(split -> split.stream().map(problist ->
                new ArrayList<>(Collections.singletonList(problist))).collect(Collectors.toList()));
//      add all the possible combinations
        perms = listProb.reduce((input1, input2) -> {
            Collection<List<String>> combinedPerm = new ArrayList<>();
            for (List<String> perm1 : input1) {
                for (List<String> perm2 : input2) {
                    List<String> comb = new ArrayList<>();
                    comb.addAll(perm1);
                    comb.addAll(perm2);
                    combinedPerm.add(comb);
                }
            }
            return combinedPerm;
        }).orElse(new HashSet<>());
//      remove all the duplicate collections (only one name per player)
        for (List<String> perm : perms) {
            Set<String> temp = new HashSet<>(perm);
            if (perm.size() == temp.size())
                permMap.add(perm);
        }
//      find the highest permutation for each players guess
//        int index = 0;
        Boolean fin = true;
//        Boolean loop = false;
//        Boolean cloop = false;
//        Boolean floop = false;
//        String loopcheckKey = "";
//        String loopcheck = "";
//        String nopecheck = "";

        while (fin) {
            fin = false;
            int index = 0;
//            Boolean loop = false;
//            String loopcheckKey = "";
            outerloop:
            for (String k : players.keySet()) {

//                int loopcheck = 0;
                int prev = 0;
                if (bestPlay.containsKey(k) && topRes.containsKey(k)) {
                    prev = topRes.get(k);
                }
                List<String> tempKey = new ArrayList<>();

                for (String guess : allCharacters) {
                    int curr = 0;
                    for (List<String> perm : permMap) {
                        if (guess.equals(perm.get(index))) {
                            tempKey.add(guess);
                            curr++;
                        }
                    }

                    if (curr > prev) {
                        if (bestPlay.containsValue(guess)) {
                            for (String findKey : bestPlay.keySet()) {
                                if (bestPlay.get(findKey).equals(guess) && !findKey.equals(k)) {
                                    if (curr > topRes.get(findKey)) {
                                        bestPlay.remove(findKey);
                                        topRes.remove(findKey);
                                        bestPlay.put(k, guess);
                                        topRes.put(k, curr);
                                        fin = true;
                                        break outerloop;
                                    }
                                }
                            }
                        } else {
                            bestPlay.put(k, guess);
                            topRes.put(k, curr);
                            prev = curr;
                        }
                    }
                }
                if (!bestPlay.containsKey(k)) {
                    for (String randGuess : tempKey) {
                        if (!bestPlay.containsValue(randGuess)) {
                            bestPlay.put(k, randGuess);
                            topRes.put(k, 500);
                        } else {
                            for (String findKey : bestPlay.keySet()) {
                                if (bestPlay.get(findKey).equals(randGuess)) {
                                    bestPlay.remove(findKey);
                                    topRes.remove(findKey);
                                    bestPlay.put(k, randGuess);
                                    topRes.put(k, 500);
                                    fin = true;
                                    break outerloop;
                                }
                            }
                        }
                    }
                }
                index++;

            }
        }
        return bestPlay;
    }

    public String reportGuesses() {
        StringBuilder rval2 = new StringBuilder();
        Map<String, String> plyrToGuessMap = mapGuessProb();
        rval2 = new StringBuilder();
        for (String plyrName : players.keySet()) {
            rval2.append(plyrName);
            rval2.append(",").append(plyrToGuessMap.get(plyrName));
            rval2.append(":");
        }
        return rval2.substring(0, rval2.length() - 1);
    }

    public String reportGuessesWprob() {
        String rval = "";
        return "random results: ";
    }


    public bestPlayer9(String playerName, String guestName, int numStartingGems, String gemLocations, String[] playerNames, String[] guestNames) {
        super(playerName, guestName, numStartingGems, gemLocations, playerNames, guestNames);
        display = new TextDisplay(gemLocations);
        pieces = new HashMap<String, Piece>();
        ArrayList<String> possibleGuests = new ArrayList<String>();
        for (String name : guestNames) {
            pieces.put(name, new Piece(name));
            if (!name.equals(guestName)) possibleGuests.add(name);
        }
        me = pieces.get(guestName);

        players = new HashMap<String, Player>();
        for (String str : playerNames) {
            if (!str.equals(playerName)) {
                players.put(str, new Player(str, possibleGuests.toArray(new String[possibleGuests.size()])));
            }
        }

        otherPlayerNames = players.keySet().toArray(new String[players.size()]);

        cardsRemaining = cardActions.clone();
        gemsRemaining[0] = gemsRemaining[1] = gemsRemaining[2] = numStartingGems;
        firstLoadOfCardsInHand = true;
        cardInHand1 = "";
        cardInHand2 = "";
        myName = playerName;

    }
}


