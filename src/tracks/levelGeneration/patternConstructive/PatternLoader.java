package tracks.levelGeneration.patternConstructive;

import com.sun.jmx.remote.internal.ArrayQueue;
import com.sun.org.apache.xpath.internal.SourceTree;
import core.game.Game;
import core.game.GameDescription;
import core.vgdl.VGDLFactory;
import core.vgdl.VGDLParser;
import core.vgdl.VGDLRegistry;
import tools.GameAnalyzer;

import javax.xml.bind.SchemaOutputResolver;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class PatternLoader {

    ArrayList<String> games;

    String patternFilePath;
    String gamesFolderPath;

    ArrayList<String> avatars = new ArrayList<String>();
    ArrayList<String> solids = new ArrayList<String>();
    ArrayList<String> harmfuls = new ArrayList<String>();
    ArrayList<String> collectables = new ArrayList<String>();
    ArrayList<String> others = new ArrayList<String>();

    PatternLoader() {
        games = new ArrayList<String>();
        games.add("aliens");
        games.add("brainman");
        games.add("catapults");
        games.add("fireman");
        games.add("frogs");
        games.add("pacman");
        games.add("sokoban");
        games.add("solarfox");
        games.add("surround");
        games.add("zelda");

        patternFilePath = "src/tracks/levelGeneration/patternConstructive/patternFile.txt";
        gamesFolderPath = "examples/gridphysics/";

        avatars = new ArrayList<String>();
        solids = new ArrayList<String>();
        harmfuls = new ArrayList<String>();
        collectables = new ArrayList<String>();
        others = new ArrayList<String>();
    }

    //Copy original game levels into our constructor's package to operate on
    public static void copy(String sourcePath, String destinationPath) throws IOException {
        Files.copy(Paths.get(sourcePath), new FileOutputStream(destinationPath));
    }

    //Helper to find all sprites associated with mapped character
    private char convertMappingToCode(char lookupChar, HashMap<Character, ArrayList<String>> charMap) {

        //System.out.println(lookupChar);
        ArrayList<String> spritesInLocation = new ArrayList<String>();
        if (lookupChar == '\n') {
            return lookupChar;
        }
        if (lookupChar == '\r') {
            return '\n';
        } else {
            for (int j = 0; j < charMap.get(lookupChar).size(); j++) {
                spritesInLocation.add(charMap.get(lookupChar).get(j));
            }

            //Convert the list of sprites into a list of their types
            ArrayList<Character> typesInLocation = new ArrayList<Character>();
            for (int k = 0; k < spritesInLocation.size(); k++) {
                if (avatars.contains(spritesInLocation.get(k))) {
                    typesInLocation.add('A');
                } else if (solids.contains(spritesInLocation.get(k))) {
                    typesInLocation.add('S');
                } else if (harmfuls.contains(spritesInLocation.get(k))) {
                    typesInLocation.add('H');
                } else if (collectables.contains(spritesInLocation.get(k))) {
                    typesInLocation.add('C');
                } else if (others.contains(spritesInLocation.get(k))) {
                    typesInLocation.add('O');
                }
            }

            char answer = 'Z';
            //Convert List of sprite types into a character code THIS WILL HAVE TO BE EXTENDED IF ADDED GAMES HAVE UNKNOWN MAPPING COMBOS
            if (typesInLocation.contains('O')) {
                if (typesInLocation.contains('A')) {
                    if (typesInLocation.contains('H')) {
                        answer = '6';  //O,A,H
                    } else answer = '2';  //O,A
                } else if (typesInLocation.contains('H')) {
                    if (typesInLocation.size() == 2) {
                        answer = '3'; //O,H
                    } else answer = '7';  //O,H,H
                } else if (typesInLocation.contains('C')) {
                    answer = '4'; //O,C
                } else if (typesInLocation.contains('S')) {
                    answer = '5'; //O,S
                } else if (typesInLocation.size() == 2) {
                    answer = '1'; //O,O
                } else answer = 'O'; //O
            } else if (typesInLocation.contains('A')) {
                if (typesInLocation.contains('S')) {
                    answer = '8'; //A,S
                } else answer = 'A'; //A
            } else if (typesInLocation.contains('S')) {
                if (typesInLocation.contains('H')) {
                    answer = '9'; //S,H
                } else answer = 'S'; //S
            } else if (typesInLocation.contains('H')) {
                answer = 'H'; //H
            } else if (typesInLocation.contains('C')) {
                answer = 'C'; //C
            } else answer = 'Z';

            return answer;
        }

    }

    //Function to convert all levels for each designated game into a level with codified types instead of sprites and save them in the folder
    private void convertLevelsToCodes() throws IOException {

        for (int i = 0; i < games.size(); i++) {
            VGDLFactory.GetInstance().init(); // This always first thing to do.
            VGDLRegistry.GetInstance().init();


            //Set up the game file and necessarry tools to use it
            String gamePath = gamesFolderPath + games.get(i) + ".txt";
            Game myGame = new VGDLParser().parseGame(gamePath);
            GameDescription myDescription = new GameDescription(myGame);
            GameAnalyzer gameAnalyzer = new GameAnalyzer(myDescription);


            //Find all sprites of each class
            avatars.addAll(gameAnalyzer.getAvatarSprites());
            solids.addAll(gameAnalyzer.getSolidSprites());
            harmfuls.addAll(gameAnalyzer.getHarmfulSprites());
            collectables.addAll(gameAnalyzer.getCollectableSprites());
            others.addAll(gameAnalyzer.getOtherSprites());

            //test
            System.out.println(gamePath);
            System.out.println("Avatars: " + avatars);
            System.out.println("Solids: " + solids);
            System.out.println("Harmfuls: " + harmfuls);
            System.out.println("Collectables: " + collectables);
            System.out.println("Others: " + others);
            System.out.println(myGame.getCharMapping());

            for (int j = 0; j < 5; j++) {
                copy("examples\\gridphysics\\" + games.get(i) + "_lvl" + j + ".txt", "src\\tracks\\levelGeneration\\patternConstructive\\ConvertedLevels\\" + games.get(i) + j);

                String levelString = new Scanner(new File("src\\tracks\\levelGeneration\\patternConstructive\\ConvertedLevels\\" + games.get(i) + j)).useDelimiter("\\Z").next();
                char[] levelChars = levelString.toCharArray();
                ArrayList<Character> levelCodes = new ArrayList<Character>();
                for (int k = 0; k < levelString.length(); k++) {
                    levelCodes.add(convertMappingToCode(levelChars[k], myGame.getCharMapping()));
                }
                String codedLevel = levelCodes.toString().replace(",", "").replace(" ", "").replace("[", "").replace("]", "");
                System.out.println("--------------Before-----------");
                System.out.println(levelString);
                System.out.println("--------------After-----------");
                System.out.println(codedLevel);

                PrintWriter out = new PrintWriter("src\\tracks\\levelGeneration\\patternConstructive\\ConvertedLevels\\" + games.get(i) + j);
                out.print(codedLevel);
                out.close();
            }
        }
    }

    //Function to generate all 3x3 patterns contained in


    public static void main(String[] args) throws IOException {

        PatternLoader myLoader = new PatternLoader();
        myLoader.convertLevelsToCodes();


    }


}