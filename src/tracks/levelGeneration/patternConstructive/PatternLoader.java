package tracks.levelGeneration.patternConstructive;

import core.game.Game;
import core.game.GameDescription;
import core.vgdl.VGDLFactory;
import core.vgdl.VGDLParser;
import core.vgdl.VGDLRegistry;
import tools.GameAnalyzer;
import tools.Utils;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class PatternLoader {

    private String gamesFolderPath;

    private ArrayList<String> avatars;
    private ArrayList<String> solids;
    private ArrayList<String> harmfuls;
    private ArrayList<String> collectables;
    private ArrayList<String> others;

    private ArrayList<Integer> avatarPatterns;
    private ArrayList<Integer> bottomAvatarPatterns;
    private ArrayList<Integer> openBottomPatterns;
    private ArrayList<Integer> topWallPatterns;
    private ArrayList<Integer> bottomWallPatterns;
    private ArrayList<Integer> leftWallPatterns;
    private ArrayList<Integer> rightWallPatterns;
    private ArrayList<Integer> tlPatterns;
    private ArrayList<Integer> trPatterns;
    private ArrayList<Integer> blPatterns;
    private ArrayList<Integer> brPatterns;

    private GameDescription gameDescription;
    private GameAnalyzer gameAnalyzer;

    private int patternCount;

    PatternLoader(){
        gamesFolderPath = "examples/gridphysics/";

        avatars = new ArrayList<String>();
        solids = new ArrayList<String>();
        harmfuls = new ArrayList<String>();
        collectables = new ArrayList<String>();
        others = new ArrayList<String>();

        avatarPatterns = new ArrayList<Integer>();
        bottomAvatarPatterns = new ArrayList<Integer>();
        openBottomPatterns = new ArrayList<Integer>();
        topWallPatterns = new ArrayList<Integer>();
        bottomWallPatterns = new ArrayList<Integer>();
        leftWallPatterns = new ArrayList<Integer>();
        rightWallPatterns = new ArrayList<Integer>();
        tlPatterns = new ArrayList<Integer>();
        trPatterns = new ArrayList<Integer>();
        blPatterns = new ArrayList<Integer>();
        brPatterns = new ArrayList<Integer>();

        patternCount = 0;
    }

    //Copy original game levels into our constructor's package to operate on
    public static void copy(String sourcePath, String destinationPath) throws IOException{
        Files.copy(Paths.get(sourcePath), new FileOutputStream(destinationPath));
    }

    //Helper to find all sprites associated with mapped character
    private char convertMappingToCode(char lookupChar, HashMap<Character, ArrayList<String>> charMap){

        //System.out.println(lookupChar);
        ArrayList<String> spritesInLocation = new ArrayList<String>();
        if (lookupChar == '\n'){
            return lookupChar;
        }
        if (lookupChar == '\r'){ //This is to fix the newline character problems with the pacman levels
            return ' ';
        }
        else{
            for (int j = 0; j < charMap.get(lookupChar).size(); j++){
                spritesInLocation.add(charMap.get(lookupChar).get(j));
            }

            //Convert the list of sprites into a list of their types
            ArrayList<Character> typesInLocation = new ArrayList<Character>();
            for (int k = 0; k < spritesInLocation.size(); k++){
                if (avatars.contains(spritesInLocation.get(k))){
                    typesInLocation.add('A');
                }
                else if (solids.contains(spritesInLocation.get(k))){
                    typesInLocation.add('S');
                }
                else if (harmfuls.contains(spritesInLocation.get(k))){
                    typesInLocation.add('H');
                }
                else if (collectables.contains(spritesInLocation.get(k))){
                    typesInLocation.add('C');
                }
                else if (others.contains(spritesInLocation.get(k))){
                    typesInLocation.add('O');
                }
            }

            char answer = 'Z';
            //Convert List of sprite types into a character code THIS WILL HAVE TO BE EXTENDED IF ADDED GAMES HAVE UNKNOWN MAPPING COMBOS
            if (typesInLocation.contains('O')){
                if (typesInLocation.contains('A')){
                    if (typesInLocation.contains('H')){
                        answer = '6';  //O,A,H
                    } else answer = '2';  //O,A
                }
                else if(typesInLocation.contains('H')){
                    if (typesInLocation.size() == 2){
                        answer = '3'; //O,H
                    } else answer = '7';  //O,H,H
                }
                else if(typesInLocation.contains('C')){
                    answer = '4'; //O,C
                }
                else if(typesInLocation.contains('S')){
                    answer = '5'; //O,S
                }
                else if (typesInLocation.size() == 2) {
                    answer = '1'; //O,O
                } else answer = 'O'; //O
            }
            else if (typesInLocation.contains('A')){
                if (typesInLocation.contains('S')){
                    answer = '8'; //A,S
                } else answer = 'A'; //A
            }
            else if (typesInLocation.contains('S')){
                if (typesInLocation.contains('H')){
                    answer = '9'; //S,H
                } else answer = 'S'; //S
            }
            else if (typesInLocation.contains('H')){
                answer = 'H'; //H
            }
            else if(typesInLocation.contains('C')){
                answer = 'C'; //C
            }
            else{
                return '?';
            }

            return answer;
        }

    }

    //Function to convert all levels for each designated game into a level with codified types instead of sprites and save them in the folder
    private void convertLevelsToCodes() throws IOException {

        //Clear the prior patternFile
        new File("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\patternFile.txt").delete();

        //Load available games
        String spGamesCollection =  "examples/all_games_sp.csv";
        String[][] games = Utils.readGames(spGamesCollection);

        ArrayList<String> allGames = new ArrayList<String>();

        for(int i = 0; i < 102; i++){ //102 is the last game in the gridphysics folder
            allGames.add(games[i][1]);
        }
        allGames.remove("eggomania");
        allGames.remove("eighthpassenger");
        allGames.remove("jaws");
        allGames.remove("realsokoban");
        allGames.remove("thecitadel");
        allGames.remove("painter");



        for(int i = 0; i < allGames.size(); i++){
            VGDLFactory.GetInstance().init(); // This always first thing to do.
            VGDLRegistry.GetInstance().init();

            System.out.println("Game Name: " + allGames.get(i));
            //Set up the game file and necessarry tools to use it
            String gamePath = gamesFolderPath + allGames.get(i) + ".txt";
            Game myGame = new VGDLParser().parseGame(gamePath);
            gameDescription = new GameDescription(myGame);
            gameAnalyzer = new GameAnalyzer(gameDescription);

            avatars.clear();
            solids.clear();
            harmfuls.clear();
            collectables.clear();
            others.clear();

            //Find all sprites of each class
            avatars.addAll(gameAnalyzer.getAvatarSprites());
            solids.addAll(gameAnalyzer.getSolidSprites());
            harmfuls.addAll(gameAnalyzer.getHarmfulSprites());
            collectables.addAll(gameAnalyzer.getCollectableSprites());
            others.addAll(gameAnalyzer.getOtherSprites());


            //test
            System.out.println(gamePath);
            System.out.println("Keyset: " + myGame.getCharMapping().keySet());
            System.out.println("Avatars: " + avatars);
            System.out.println("Solids: " + solids);
            System.out.println("Harmfuls: " + harmfuls);
            System.out.println("Collectables: " + collectables);
            System.out.println("Others: " + others);
            System.out.println(myGame.getCharMapping());

            for(int j = 0; j < 5; j ++){
                System.out.println("Level Number: " + j);
                copy("examples\\gridphysics\\" + allGames.get(i) + "_lvl" + j + ".txt", "src\\tracks\\levelGeneration\\patternConstructive\\ConvertedLevels\\" + allGames.get(i) + j);

                String levelString = new Scanner(new File("src\\tracks\\levelGeneration\\patternConstructive\\ConvertedLevels\\" + allGames.get(i) + j)).useDelimiter("\\Z").next();
                char[] levelChars = levelString.toCharArray();
                ArrayList<Character> levelCodes = new ArrayList<Character>();
                for (int k = 0; k < levelString.length(); k++){
                    levelCodes.add(convertMappingToCode(levelChars[k], myGame.getCharMapping()));
                    if (convertMappingToCode(levelChars[k], myGame.getCharMapping()) == '?') System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"); //warning for unidentified mapping type
                }
                String codedLevel = levelCodes.toString().replace(",", "").replace(" ", "").replace("[", "").replace("]", "");
                System.out.println("--------------Before-----------");
                System.out.println(levelString);
                System.out.println("--------------After-----------");
                System.out.println(codedLevel);

                PrintWriter out = new PrintWriter("src\\tracks\\levelGeneration\\patternConstructive\\ConvertedLevels\\" + allGames.get(i) + j);
                out.print(codedLevel);
                out.close();

                generate3by3Patterns(codedLevel);
            }
        }
    }

    private void classifyPattern(ArrayList<Character> pattern){
        //if contains avatar
        for(int i = 0; i < 9; i++){
            if(codeHasAvatar(pattern.get(i))){
                avatarPatterns.add(patternCount);
            }
        }
        //if bottom avatar
        if(codeHasAvatar(pattern.get(6)) || (!codeHasSolid(pattern.get(6)) && !codeHasHarmful(pattern.get(6)))){
            if(codeHasAvatar(pattern.get(7)) || (!codeHasSolid(pattern.get(7)) && !codeHasHarmful(pattern.get(7)))){
                if(codeHasAvatar(pattern.get(8)) || (!codeHasSolid(pattern.get(8)) && !codeHasHarmful(pattern.get(8)))){
                    if(codeHasAvatar(pattern.get(6)) || codeHasAvatar(pattern.get(7)) || codeHasAvatar(pattern.get(8))){
                        bottomAvatarPatterns.add(patternCount);
                    }
                }
            }
        }
        //if open bottom
        if(!codeHasSolid(pattern.get(6)) && !codeHasHarmful(pattern.get(6))){
            if(!codeHasSolid(pattern.get(7)) && !codeHasHarmful(pattern.get(7))){
                if(!codeHasSolid(pattern.get(8)) && !codeHasHarmful(pattern.get(8))){
                    if(!avatarPatterns.contains(patternCount)){
                        openBottomPatterns.add(patternCount);
                    }
                }
            }
        }
        //if t wall
        if(codeHasSolid(pattern.get(0)) && codeHasSolid(pattern.get(1)) && codeHasSolid(pattern.get(2))){
            topWallPatterns.add(patternCount);
        }
        //if b wall
        if(codeHasSolid(pattern.get(6)) && codeHasSolid(pattern.get(7)) && codeHasSolid(pattern.get(8))){
            bottomWallPatterns.add(patternCount);
        }
        //if l wall
        if(codeHasSolid(pattern.get(0)) && codeHasSolid(pattern.get(3)) && codeHasSolid(pattern.get(6))){
            leftWallPatterns.add(patternCount);
        }
        //if r wall
        if(codeHasSolid(pattern.get(2)) && codeHasSolid(pattern.get(5)) && codeHasSolid(pattern.get(8))){
            rightWallPatterns.add(patternCount);
        }
        //if tl corner
        if(codeHasSolid(pattern.get(0)) && codeHasSolid(pattern.get(1)) && codeHasSolid(pattern.get(2)) && codeHasSolid(pattern.get(3)) && codeHasSolid(pattern.get(6))){
            tlPatterns.add(patternCount);
        }
        //if tr corner
        if(codeHasSolid(pattern.get(0)) && codeHasSolid(pattern.get(1)) && codeHasSolid(pattern.get(2)) && codeHasSolid(pattern.get(5)) && codeHasSolid(pattern.get(8))){
            trPatterns.add(patternCount);
        }
        //if bl corner
        if(codeHasSolid(pattern.get(0)) && codeHasSolid(pattern.get(3)) && codeHasSolid(pattern.get(6)) && codeHasSolid(pattern.get(7)) && codeHasSolid(pattern.get(8))){
            blPatterns.add(patternCount);
        }
        //if br corner
        if(codeHasSolid(pattern.get(2)) && codeHasSolid(pattern.get(5)) && codeHasSolid(pattern.get(6)) && codeHasSolid(pattern.get(7)) && codeHasSolid(pattern.get(8))){
            brPatterns.add(patternCount);
        }
    }

    private boolean codeHasAvatar(char code){
        return code == 'A' || code == '2' || code == '6' || code == '8';
    }
    private boolean codeHasSolid(char code){
        return code == 'S' || code == '5' || code == '8';
    }
    private boolean codeHasHarmful(char code){
        return code == 'H' || code == '3' || code == '6' || code == '7' || code == '9';
    }

    //Function to generate all 3x3 patterns contained in a single level
    private void generate3by3Patterns(String level) throws IOException {
        int width = level.indexOf("\n");
        int height = level.split("\n", -1).length;
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\patternFile.txt", true)));

        //build it into a double array
        ArrayList<String> levelArray = new ArrayList<String>();
        for (int i = 0; i < height; i++){
            levelArray.add(level.split("\n")[i]);
        }

        //Print pattern to file
        for (int i = 0; i < height-2; i++){
            for(int j = 0; j < width-2; j++){

                ArrayList<Character> pattern = new ArrayList<Character>();
                pattern.add(levelArray.get(i).charAt(j));
                pattern.add(levelArray.get(i).charAt(j+1));
                pattern.add(levelArray.get(i).charAt(j+2));
                pattern.add(levelArray.get(i+1).charAt(j));
                pattern.add(levelArray.get(i+1).charAt(j+1));
                pattern.add(levelArray.get(i+1).charAt(j+2));
                pattern.add(levelArray.get(i+2).charAt(j));
                pattern.add(levelArray.get(i+2).charAt(j+1));
                pattern.add(levelArray.get(i+2).charAt(j+2));

                classifyPattern(pattern);
                patternCount++;

                out.print(levelArray.get(i).charAt(j));
                out.print(levelArray.get(i).charAt(j+1));
                out.print(levelArray.get(i).charAt(j+2));

                out.print(levelArray.get(i+1).charAt(j));
                out.print(levelArray.get(i+1).charAt(j+1));
                out.print(levelArray.get(i+1).charAt(j+2));

                out.print(levelArray.get(i+2).charAt(j));
                out.print(levelArray.get(i+2).charAt(j+1));
                out.print(levelArray.get(i+2).charAt(j+2));
            }
        }
        out.close();
    }


    private void removeAllSingleOccurrences() throws FileNotFoundException {

        String patternString = new Scanner(new File("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\patternFile.txt")).useDelimiter("\\Z").next();
        new File("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\patternFile.txt").delete();
        PrintWriter writer = new PrintWriter("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\patternFile.txt");

        HashMap<String, Integer> patternCounts = new HashMap<String, Integer>();
        for (int i = 0; i < patternString.length(); i+=9){

            if (patternCounts.containsKey(patternString.substring(i, i+9))){
                patternCounts.replace(patternString.substring(i, i+9), patternCounts.get(patternString.substring(i, i+9)) +1);
            }else patternCounts.put(patternString.substring(i, i+9), 1);

        }

        ArrayList<String> keySetVals = new ArrayList<String>();
        keySetVals.addAll(patternCounts.keySet());
        ArrayList<String> singleAppearance = new ArrayList<String>();

        for(int j = 0; j < patternCounts.keySet().size(); j ++){
            if(patternCounts.get(keySetVals.get(j)) == 1 ){
                singleAppearance.add(keySetVals.get(j));
            }
        }

        for (int i = 0; i < patternString.length(); i+=9) {
            if(singleAppearance.contains(patternString.substring(i, i + 9))){
                patternString = patternString.substring(0, i).concat(patternString.substring(i + 9));
                i -= 9;
            }
        }

        writer.print(patternString);
        writer.close();

    }

    private void printPatternStatistics() throws FileNotFoundException {
        String patternString = new Scanner(new File("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\patternFile.txt")).useDelimiter("\\Z").next();

        HashMap<Character, Integer> codeCounts = new HashMap<Character, Integer>();
        codeCounts.put('A',0);
        codeCounts.put('S',0);
        codeCounts.put('H',0);
        codeCounts.put('C',0);
        codeCounts.put('O',0);
        codeCounts.put('1',0);
        codeCounts.put('2',0);
        codeCounts.put('3',0);
        codeCounts.put('4',0);
        codeCounts.put('5',0);
        codeCounts.put('6',0);
        codeCounts.put('7',0);
        codeCounts.put('8',0);
        codeCounts.put('9',0);


        for (int i = 0; i < patternString.length(); i++){
            codeCounts.replace(patternString.charAt(i), codeCounts.get(patternString.charAt(i)) + 1);
        }


//        System.out.println("Number of patterns: " + patternString.length()/9);
//        System.out.println("Codes: " + codeCounts.keySet());
//        System.out.println("Counts: " + codeCounts.values());

        HashMap<String, Integer> patternCounts = new HashMap<String, Integer>();
        for (int i = 0; i < patternString.length(); i+=9){

            if (patternCounts.containsKey(patternString.substring(i, i+9))){
                patternCounts.replace(patternString.substring(i, i+9), patternCounts.get(patternString.substring(i, i+9)) +1);
            }else patternCounts.put(patternString.substring(i, i+9), 1);

        }

        ArrayList<Integer> allPatternCounts = new ArrayList<Integer>();
        allPatternCounts.addAll(patternCounts.values());

        Collections.sort(allPatternCounts);
        Collections.reverse(allPatternCounts);
        //System.out.println("Count per pattern: " + allPatternCounts);

        HashMap<Integer, Integer> countFrequency = new HashMap<Integer, Integer>();
        for (int i = 0; i < allPatternCounts.size(); i ++){
            if(countFrequency.containsKey(allPatternCounts.get(i))){
                countFrequency.replace(allPatternCounts.get(i), countFrequency.get(allPatternCounts.get(i)) + 1);
            }else countFrequency.put(allPatternCounts.get(i), 1);
        }

        System.out.println("Frequency : number of strings with that frequency");
        ArrayList<Integer> uniqueCounts = new ArrayList<Integer>();
        uniqueCounts.addAll(countFrequency.keySet());
        Collections.sort(uniqueCounts);
        Collections.reverse(uniqueCounts);
        for (int i = 0; i < uniqueCounts.size(); i++){
            System.out.println(uniqueCounts.get(i) + ":" + countFrequency.get(uniqueCounts.get(i)));
        }



    }

    private void writePatternsIndexesToFile() throws IOException {

        new File("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\avatarPatterns.txt").delete();
        new File("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\bottomAvatarPatterns.txt").delete();
        new File("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\openBottomPatterns.txt").delete();
        new File("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\topWallPatterns.txt").delete();
        new File("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\bottomWallPatterns.txt").delete();
        new File("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\leftWallPatterns.txt").delete();
        new File("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\rightWallPatterns.txt").delete();
        new File("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\tlPatterns.txt").delete();
        new File("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\trPatterns.txt").delete();
        new File("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\blPatterns.txt").delete();
        new File("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\brPatterns.txt").delete();

        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\avatarPatterns.txt", true)));
        for(int i = 0; i < avatarPatterns.size(); i++){
            out.println(avatarPatterns.get(i));
        }
        out.close();

        out = new PrintWriter(new BufferedWriter(new FileWriter("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\bottomAvatarPatterns.txt", true)));
        for(int i = 0; i < bottomAvatarPatterns.size(); i++){
            out.println(bottomAvatarPatterns.get(i));
        }
        out.close();

        out = new PrintWriter(new BufferedWriter(new FileWriter("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\openBottomPatterns.txt", true)));
        for(int i = 0; i < openBottomPatterns.size(); i++){
            out.println(openBottomPatterns.get(i));
        }
        out.close();

        out = new PrintWriter(new BufferedWriter(new FileWriter("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\topWallPatterns.txt", true)));
        for(int i = 0; i < topWallPatterns.size(); i++){
            out.println(topWallPatterns.get(i));
        }
        out.close();

        out = new PrintWriter(new BufferedWriter(new FileWriter("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\bottomWallPatterns.txt", true)));
        for(int i = 0; i < bottomWallPatterns.size(); i++){
            out.println(bottomWallPatterns.get(i));
        }
        out.close();

        out = new PrintWriter(new BufferedWriter(new FileWriter("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\leftWallPatterns.txt", true)));
        for(int i = 0; i < leftWallPatterns.size(); i++){
            out.println(leftWallPatterns.get(i));
        }
        out.close();

        out = new PrintWriter(new BufferedWriter(new FileWriter("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\rightWallPatterns.txt", true)));
        for(int i = 0; i < rightWallPatterns.size(); i++){
            out.println(rightWallPatterns.get(i));
        }
        out.close();

        out = new PrintWriter(new BufferedWriter(new FileWriter("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\tlPatterns.txt", true)));
        for(int i = 0; i < tlPatterns.size(); i++){
            out.println(tlPatterns.get(i));
        }
        out.close();

        out = new PrintWriter(new BufferedWriter(new FileWriter("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\trPatterns.txt", true)));
        for(int i = 0; i < trPatterns.size(); i++){
            out.println(trPatterns.get(i));
        }
        out.close();

        out = new PrintWriter(new BufferedWriter(new FileWriter("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\blPatterns.txt", true)));
        for(int i = 0; i < blPatterns.size(); i++){
            out.println(blPatterns.get(i));
        }
        out.close();

        out = new PrintWriter(new BufferedWriter(new FileWriter("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\brPatterns.txt", true)));
        for(int i = 0; i < brPatterns.size(); i++){
            out.println(brPatterns.get(i));
        }
        out.close();
        System.out.println(patternCount);
    }

    public static void main(String[] args) throws IOException {

        PatternLoader myLoader = new PatternLoader();
        myLoader.convertLevelsToCodes();
        myLoader.writePatternsIndexesToFile();
//        myLoader.removeAllSingleOccurrences();    TODO: Figure out if removing singles will affect the output meaningfully enough to merit rearranging all the code
//        myLoader.printPatternStatistics();
    }

}
