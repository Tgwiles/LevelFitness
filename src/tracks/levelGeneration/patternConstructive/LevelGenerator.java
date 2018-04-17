package tracks.levelGeneration.patternConstructive;


import com.sun.javafx.collections.ArrayListenerHelper;
import core.game.Game;
import core.game.GameDescription;
import core.generator.AbstractLevelGenerator;
import core.vgdl.VGDLFactory;
import core.vgdl.VGDLParser;
import core.vgdl.VGDLRegistry;
import tools.ElapsedCpuTimer;
import tools.GameAnalyzer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class LevelGenerator extends AbstractLevelGenerator{

    // 1. Reads from pattern folder and grabs patterns
    // 2. Make a level from these patterns, random size between 9x9 and 12x12
    // 3. Decode the newly made level into game-specific characters for sprites

    GameAnalyzer gameAnalyzer;
    GameDescription gameDescription;
    HashMap<Character, ArrayList<Character>> codeMapping;

    ArrayList<Integer> avatarPatterns;
    ArrayList<Integer> bottomAvatarPatterns;
    ArrayList<Integer> openBottomPatterns;
    ArrayList<Integer> topWallPatterns;
    ArrayList<Integer> bottomWallPatterns;
    ArrayList<Integer> leftWallPatterns;
    ArrayList<Integer> rightWallPatterns;
    ArrayList<Integer> tlPatterns;
    ArrayList<Integer> trPatterns;
    ArrayList<Integer> blPatterns;
    ArrayList<Integer> brPatterns;

    boolean hasAvatar;
    boolean connectivityBail;


    public LevelGenerator(GameDescription game, ElapsedCpuTimer elapsedCpuTimer){
        gameAnalyzer = new GameAnalyzer(game);
        gameDescription = game;

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
        hasAvatar = false;
        connectivityBail = false;

    }


    private HashMap<Character, ArrayList<Character>> getCodeMapping(){

        HashMap<Character, ArrayList<Character>> codeMap = new HashMap<Character, ArrayList<Character>>();

        ArrayList<Character> chars = new ArrayList<Character>();
        chars.addAll(gameDescription.getLevelMapping().keySet());

        codeMap.put('A', new ArrayList<Character>());
        codeMap.put('S', new ArrayList<Character>());
        codeMap.put('H', new ArrayList<Character>());
        codeMap.put('C', new ArrayList<Character>());
        codeMap.put('O', new ArrayList<Character>());
        codeMap.put('1', new ArrayList<Character>());
        codeMap.put('2', new ArrayList<Character>());
        codeMap.put('3', new ArrayList<Character>());
        codeMap.put('4', new ArrayList<Character>());
        codeMap.put('5', new ArrayList<Character>());
        codeMap.put('6', new ArrayList<Character>());
        codeMap.put('7', new ArrayList<Character>());
        codeMap.put('8', new ArrayList<Character>());
        codeMap.put('9', new ArrayList<Character>());

        //populate codes mappings that we have direct translations for
        for(int i = 0; i < chars.size(); i++) {
            char code = convertMappingToCode(chars.get(i));
            codeMap.get(code).add(chars.get(i));
        }

        ArrayList<Character> codes = new ArrayList<Character>();
        codes.addAll(codeMap.keySet());

        //populate unknown translations
        for (int i = 0; i <codes.size(); i++){
            if (codeMap.get(codes.get(i)).size() == 0){
//                System.out.println("Finding a map for " + codes.get(i));
                codeMap.get(codes.get(i)).addAll(bestMapping(codes.get(i)));
            }
        }

        return codeMap;
    }

    private ArrayList<Character> bestMapping(char code){
        ArrayList<Character> rankedMappings = new ArrayList<Character>();

        ArrayList<Character> levelMapChars = new ArrayList<Character>();
        levelMapChars.addAll(gameDescription.getLevelMapping().keySet()); //contains all level mappings

        ArrayList<Character> answerMappings = new ArrayList<Character>();

        int bestScore = Integer.MIN_VALUE;
        for(int i = 0; i < levelMapChars.size(); i++){ //for each level mapping
            int currentMapScore = codeSpriteMatchScore(gameDescription.getLevelMapping().get(levelMapChars.get(i)), code);

            if (currentMapScore > bestScore){
                answerMappings.clear();
                answerMappings.add(levelMapChars.get(i));
                bestScore = currentMapScore;

            }else if(currentMapScore == bestScore){
                if(breakTie(answerMappings.get(0), levelMapChars.get(i)) == '?'){
                    answerMappings.add(levelMapChars.get(i));
                }else if (breakTie(answerMappings.get(0), levelMapChars.get(i)) == levelMapChars.get(i)){
                    answerMappings.clear();
                    answerMappings.add(levelMapChars.get(i));
                }
            }
        }

        return answerMappings;
    }

    private int codeSpriteMatchScore(ArrayList<String> sprites, char code){
        int aCount = 0, sCount = 0, hCount = 0, cCount = 0, oCount = 0;

        for (int i = 0; i < sprites.size(); i++){
            if (gameAnalyzer.getAvatarSprites().contains(sprites.get(i))) aCount++;
            else if (gameAnalyzer.getSolidSprites().contains(sprites.get(i))) sCount++;
            else if (gameAnalyzer.getHarmfulSprites().contains(sprites.get(i))) hCount++;
            else if (gameAnalyzer.getCollectableSprites().contains(sprites.get(i))) cCount++;
            else if (gameAnalyzer.getOtherSprites().contains(sprites.get(i))) oCount++;
        }

        int score = 0;
        if (code == 'A'){
            score += Math.min(aCount, 1);
            score -= (sCount + hCount + cCount + oCount);
            score -= Math.max(aCount - 1, 0);
        }
        else if (code == 'S'){
            score += Math.min(sCount, 1);
            score -= (aCount + hCount + cCount + oCount);
            score -= Math.max(sCount - 1, 0);
        }
        else if (code == 'H'){
            score += Math.min(hCount, 1);
            score -= (aCount + sCount + cCount + oCount);
            score -= Math.max(hCount - 1, 0);
        }
        else if (code == 'C'){
            score += Math.min(cCount, 1);
            score -= (aCount + sCount + hCount + oCount);
            score -= Math.max(cCount - 1, 0);
        }
        else if (code == 'O'){
            score += Math.min(oCount, 1);
            score -= (aCount + sCount + hCount + cCount);
            score -= Math.max(oCount - 1, 0);
        }
        else if (code == '1'){
            score += Math.min(oCount, 2);
            score -= (aCount + sCount + hCount + cCount);
            score -= Math.max(oCount - 2, 0);
        }
        else if (code == '2'){
            score += Math.min(oCount, 1);
            score += Math.min(aCount, 1);
            score -= (sCount + hCount + cCount);
            score -= Math.max(oCount - 1, 0);
            score -= Math.max(aCount - 1, 0);
        }
        else if (code == '3'){
            score += Math.min(oCount, 1);
            score += Math.min(hCount, 1);
            score -= (sCount + aCount + cCount);
            score -= Math.max(oCount - 1, 0);
            score -= Math.max(hCount - 1, 0);
        }
        else if (code == '4'){
            score += Math.min(oCount, 1);
            score += Math.min(cCount, 1);
            score -= (sCount + aCount + hCount);
            score -= Math.max(oCount - 1, 0);
            score -= Math.max(cCount - 1, 0);
        }
        else if (code == '5'){
            score += Math.min(oCount, 1);
            score += Math.min(sCount, 1);
            score -= (cCount + aCount + hCount);
            score -= Math.max(oCount - 1, 0);
            score -= Math.max(sCount - 1, 0);
        }
        else if (code == '6'){
            score += Math.min(oCount, 1);
            score += Math.min(aCount, 1);
            score += Math.min(hCount, 1);
            score -= (cCount + sCount);
            score -= Math.max(oCount - 1, 0);
            score -= Math.max(aCount - 1, 0);
            score -= Math.max(hCount - 1, 0);
        }
        else if (code == '7'){
            score += Math.min(oCount, 1);
            score += Math.min(hCount, 2);
            score -= (cCount + sCount + aCount);
            score -= Math.max(oCount - 1, 0);
            score -= Math.max(hCount - 2, 0);
        }
        else if (code == '8'){
            score += Math.min(aCount, 1);
            score += Math.min(sCount, 1);
            score -= (cCount + oCount + hCount);
            score -= Math.max(aCount - 1, 0);
            score -= Math.max(sCount - 1, 0);
        }
        else if (code == '9'){
            score += Math.min(hCount, 1);
            score += Math.min(sCount, 1);
            score -= (cCount + oCount + aCount);
            score -= Math.max(hCount - 1, 0);
            score -= Math.max(sCount - 1, 0);
        }

        return score;
    }

    private char breakTie(char mapOne, char mapTwo){

        if (mappingHasAvatar(mapOne) && !mappingHasAvatar(mapTwo)) return mapOne;
        else if (mappingHasAvatar(mapTwo) && !mappingHasAvatar(mapOne)) return mapTwo;

        else if (mappingHasSolid(mapOne) && !mappingHasSolid(mapTwo)) return mapOne;
        else if(mappingHasSolid(mapTwo) && !mappingHasSolid(mapOne)) return mapTwo;

        else if(mappingHasOther(mapOne) && !mappingHasOther(mapTwo)) return mapOne;
        else if(mappingHasOther(mapTwo) && !mappingHasOther(mapOne)) return mapTwo;

        else if(mappingHasHarmful(mapOne) && !mappingHasHarmful(mapTwo)) return mapOne;
        else if(mappingHasHarmful(mapTwo) && !mappingHasHarmful(mapOne)) return mapTwo;

        else if(mappingHasCollectable(mapOne) && !mappingHasCollectable(mapTwo)) return mapOne;
        else if(mappingHasCollectable(mapTwo) && !mappingHasCollectable(mapOne)) return mapTwo;

        return '?';
    }

    private boolean mappingHasAvatar(char mapping){

        ArrayList<String> spritesInMap = new ArrayList<String>();
        spritesInMap.addAll(gameDescription.getLevelMapping().get(mapping));


        for (int i = 0; i < spritesInMap.size(); i++){
            if (gameAnalyzer.getAvatarSprites().contains(spritesInMap.get(i))) return true;
        }
        return false;
    }
    private boolean mappingHasSolid(char mapping){

        ArrayList<String> spritesInMap = new ArrayList<String>();
        spritesInMap.addAll(gameDescription.getLevelMapping().get(mapping));


        for (int i = 0; i < spritesInMap.size(); i++){
            if (gameAnalyzer.getSolidSprites().contains(spritesInMap.get(i))) return true;
        }
        return false;
    }
    private boolean mappingHasOther(char mapping){

        ArrayList<String> spritesInMap = new ArrayList<String>();
        spritesInMap.addAll(gameDescription.getLevelMapping().get(mapping));

        for (int i = 0; i < spritesInMap.size(); i++){
            if (gameAnalyzer.getOtherSprites().contains(spritesInMap.get(i))) return true;
        }
        return false;
    }
    private boolean mappingHasHarmful(char mapping){

        ArrayList<String> spritesInMap = new ArrayList<String>();
        spritesInMap.addAll(gameDescription.getLevelMapping().get(mapping));

        for (int i = 0; i < spritesInMap.size(); i++){
            if (gameAnalyzer.getHarmfulSprites().contains(spritesInMap.get(i))) return true;
        }
        return false;
    }
    private boolean mappingHasCollectable(char mapping){

        ArrayList<String> spritesInMap = new ArrayList<String>();
        spritesInMap.addAll(gameDescription.getLevelMapping().get(mapping));

        for (int i = 0; i < spritesInMap.size(); i++){
            if (gameAnalyzer.getCollectableSprites().contains(spritesInMap.get(i))) return true;
        }
        return false;
    }

    private char convertMappingToCode(char lookupChar) {

        ArrayList<String> avatars = new ArrayList<String>();
        ArrayList<String> solids = new ArrayList<String>();
        ArrayList<String> harmfuls = new ArrayList<String>();
        ArrayList<String> collectables = new ArrayList<String>();
        ArrayList<String> others = new ArrayList<String>();

        //Find all sprites of each class
        avatars.addAll(gameAnalyzer.getAvatarSprites());
        solids.addAll(gameAnalyzer.getSolidSprites());
        harmfuls.addAll(gameAnalyzer.getHarmfulSprites());
        collectables.addAll(gameAnalyzer.getCollectableSprites());
        others.addAll(gameAnalyzer.getOtherSprites());

        //System.out.println(lookupChar);
        ArrayList<String> spritesInLocation = new ArrayList<String>();

        for (int j = 0; j < gameDescription.getLevelMapping().get(lookupChar).size(); j++) {
            spritesInLocation.add(gameDescription.getLevelMapping().get(lookupChar).get(j));
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


    private String construct() throws IOException {
        float time = System.nanoTime();
        connectivityBail = false;

        loadPatternIndices();

//        System.out.println(System.nanoTime() - time);

        Scanner readFile = new Scanner(new FileReader("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\patternFile.txt"));
        StringBuilder sb = new StringBuilder();


        while(readFile.hasNext()){
            sb.append(readFile.next());
        }
        readFile.close();
        String pString = sb.toString();

        int height = pickLevelSize();
        int width = pickLevelSize();
        int[][] patternsChosen = new int[width/3][height/3];
        hasAvatar = false;

        //simulate fully empty board, filling it in as patterns are added, after each addition check connectivity
        for (int i = 0; i < width/3; i++){
            for (int j = 0; j < height/3; j++){
                patternsChosen[i][j] = -1;
            }
        }



        //build borders
        patternsChosen = fillBorders(patternsChosen, width, height, pString);

        //if horizontal moving avatar, put it at the bottom and remove any existing avatars
        if (gameAnalyzer.horzAvatar.contains(gameDescription.getAvatar().get(0).type)){
            Random randGen = new Random();
            int randNum = randGen.nextInt(width/3);
            for (int i = 0; i < (width / 3); i++){
                if (i == randNum){
                    if (hasAvatar){ //if an avatar has been placed in the border already, remake the border(hasAvatar will be true resulting in none being placed)
                        patternsChosen = fillBorders(patternsChosen, width, height, pString);
                    }
                    //place avatar
                    patternsChosen[i][(height/3)-1] = bottomAvatarPatterns.get(randGen.nextInt(bottomAvatarPatterns.size()));
                    while(!checkConnectivity(patternsChosen, width, height, pString)){
                        patternsChosen[i][(height/3)-1] = bottomAvatarPatterns.get(randGen.nextInt(bottomAvatarPatterns.size()));
                    }
                    hasAvatar = true;
                }
                else{
                    patternsChosen[i][(height/3)-1] = randomDissimilarNumber(openBottomPatterns, avatarPatterns);
                    while(!checkConnectivity(patternsChosen, width, height, pString)){
                        patternsChosen[i][(height/3)-1] = randomDissimilarNumber(openBottomPatterns, avatarPatterns);
                    }
                }
            }
        }
        //Fill middle randomly
        for(int i = 1; i < (width/3) - 1; i++){
            for(int j = 1; j < (height/3) - 1; j++){
                patternsChosen[i][j] = findRandomValidPattern(pString);
                while(!checkConnectivity(patternsChosen, width, height, pString)){
                    if (avatarPatterns.contains(patternsChosen[i][j])) hasAvatar = false;
                    patternsChosen[i][j] = findRandomValidPattern(pString);
                }
            }
        }
        //Ensure avatar is placed, if none place randomly in middle
        if (!hasAvatar){
            Random randGen = new Random();
            int randomX = randGen.nextInt((width/3)-2) + 1;
            int randomY = randGen.nextInt((height/3)-2) + 1;
//            System.out.println("Avatar manually placed at pattern " + randomX + ", " + randomY);
            patternsChosen[randomX][randomY] = avatarPatterns.get(randGen.nextInt(avatarPatterns.size()));
            while(!checkConnectivity(patternsChosen, width, height, pString)){
                patternsChosen[randomX][randomY] = avatarPatterns.get(randGen.nextInt(avatarPatterns.size()));
            }

        }

        //Convert pattern index array to string
        StringBuilder outputBuilder = new StringBuilder();
        for(int i = 0; i < height / 3; i++) {
            //go over on each row and add the three characters from the appropriate pattern
            for (int j = 0; j < width / 3; j++) {
                outputBuilder.append(pString.substring(patternsChosen[j][i]*9, (patternsChosen[j][i]*9)+3));
            }
            outputBuilder.append("\n");
            for (int j = 0; j < width / 3; j++) {
                outputBuilder.append(pString.substring((patternsChosen[j][i]*9)+3, (patternsChosen[j][i]*9)+6));
            }
            outputBuilder.append("\n");
            for (int j = 0; j < width / 3; j++) {
                outputBuilder.append(pString.substring((patternsChosen[j][i]*9)+6, (patternsChosen[j][i]*9)+9));
            }
            outputBuilder.append("\n");
        }

        //Fix goals

//        System.out.println("Patterns chosen");
//        for (int i = 0; i < height/3; i++) {
//            for (int j = 0; j < width / 3; j++) {
//                System.out.println("Pattern Number: " + patternsChosen[j][i] + ", " + pString.substring(patternsChosen[j][i]*9, (patternsChosen[j][i]*9) +9));
//            }
//        }
//        System.out.println("Typed Level");
//        System.out.println(outputBuilder.toString());


        return outputBuilder.toString();
    }

    private String construct(int width, int height) throws IOException {

        loadPatternIndices();
        connectivityBail = false;


        Scanner readFile = new Scanner(new FileReader("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\patternFile.txt"));
        StringBuilder sb = new StringBuilder();

//        System.out.println("Reading from file and making pString...");
        while(readFile.hasNext()){
            sb.append(readFile.next());
        }
        readFile.close();
        String pString = sb.toString();

        int[][] patternsChosen = new int[width/3][height/3];
        hasAvatar = false;

//        System.out.println("Simulating board and filling in patterns...");
        //simulate fully empty board, filling it in as patterns are added, after each addition check connectivity
        for (int i = 0; i < width/3; i++){
            for (int j = 0; j < height/3; j++){
                patternsChosen[i][j] = -1;
            }
        }


//        System.out.println("filling in borders...");
        //build borders
        patternsChosen = fillBorders(patternsChosen, width, height, pString);

//        System.out.println("Accounting for horizontal avatars...");
        //if horizontal moving avatar, put it at the bottom and remove any existing avatars
        if (gameAnalyzer.horzAvatar.contains(gameDescription.getAvatar().get(0).type)){
            Random randGen = new Random();
            int randNum = randGen.nextInt(width/3);
            for (int i = 0; i < (width / 3); i++){
                if (i == randNum){
                    if (hasAvatar){ //if an avatar has been placed in the border already, remake the border(hasAvatar will be true resulting in none being placed)
                        patternsChosen = fillBorders(patternsChosen, width, height, pString);
                    }
                    //place avatar
                    patternsChosen[i][(height/3)-1] = bottomAvatarPatterns.get(randGen.nextInt(bottomAvatarPatterns.size()));
                    while(!checkConnectivity(patternsChosen, width, height, pString)){
                        patternsChosen[i][(height/3)-1] = bottomAvatarPatterns.get(randGen.nextInt(bottomAvatarPatterns.size()));
                    }
                    hasAvatar = true;
                }
                else{
                    patternsChosen[i][(height/3)-1] = randomDissimilarNumber(openBottomPatterns, avatarPatterns);
                    while(!checkConnectivity(patternsChosen, width, height, pString)){
                        patternsChosen[i][(height/3)-1] = randomDissimilarNumber(openBottomPatterns, avatarPatterns);
                    }
                }
            }
        }
        //Fill middle randomly
        for(int i = 1; i < (width/3) - 1; i++){
            for(int j = 1; j < (height/3) - 1; j++){
                patternsChosen[i][j] = findRandomValidPattern(pString);
                while(!checkConnectivity(patternsChosen, width, height, pString)){
                    if (avatarPatterns.contains(patternsChosen[i][j])) hasAvatar = false;
                    patternsChosen[i][j] = findRandomValidPattern(pString);
                }
            }
        }
        //Ensure avatar is placed, if none place randomly in middle
        if (!hasAvatar){
            Random randGen = new Random();
            int randomX = randGen.nextInt((width/3)-2) + 1;
            int randomY = randGen.nextInt((height/3)-2) + 1;
//            System.out.println("Avatar manually placed at pattern " + randomX + ", " + randomY);
            patternsChosen[randomX][randomY] = avatarPatterns.get(randGen.nextInt(avatarPatterns.size()));
            while(!checkConnectivity(patternsChosen, width, height, pString)){
                patternsChosen[randomX][randomY] = avatarPatterns.get(randGen.nextInt(avatarPatterns.size()));
            }

        }

//        System.out.println("converting pattern index to string...");
        //Convert pattern index array to string
        StringBuilder outputBuilder = new StringBuilder();
        for(int i = 0; i < height / 3; i++) {
            //go over on each row and add the three characters from the appropriate pattern
            for (int j = 0; j < width / 3; j++) {
                outputBuilder.append(pString.substring(patternsChosen[j][i]*9, (patternsChosen[j][i]*9)+3));
            }
            outputBuilder.append("\n");
            for (int j = 0; j < width / 3; j++) {
                outputBuilder.append(pString.substring((patternsChosen[j][i]*9)+3, (patternsChosen[j][i]*9)+6));
            }
            outputBuilder.append("\n");
            for (int j = 0; j < width / 3; j++) {
                outputBuilder.append(pString.substring((patternsChosen[j][i]*9)+6, (patternsChosen[j][i]*9)+9));
            }
            outputBuilder.append("\n");
        }

        //Fix goals

//        System.out.println("Patterns chosen");
//        for (int i = 0; i < height/3; i++) {
//            for (int j = 0; j < width / 3; j++) {
//                System.out.println("Pattern Number: " + patternsChosen[j][i] + ", " + pString.substring(patternsChosen[j][i]*9, (patternsChosen[j][i]*9) +9));
//            }
//        }
//        System.out.println("Typed Level");
//        System.out.println(outputBuilder.toString());


        return outputBuilder.toString();
    }

    private int findRandomValidPattern(String pString){
        Random randGen = new Random();
        if (hasAvatar){
            int randNum = randGen.nextInt(pString.length()/9);
            while(avatarPatterns.contains(randNum)){
                randNum = randGen.nextInt(pString.length()/9);
            }
            return randNum;
        }
        else {
            int randNum = randGen.nextInt(pString.length()/9);
            hasAvatar = avatarPatterns.contains(randNum);
            return randNum;
        }
    }

    private boolean checkConnectivity(int[][] patternsChosen, int width, int height, String pString){
        //build simulated board
        char[][] simulatedBoard = new char[width][height];

        if(connectivityBail){
            return true;
        }

        //fill with O's if pattern = -1
        //fill will proper pattern if not
        for (int i = 0; i < width/3; i++) {
            for (int j = 0; j < height / 3; j++) {
                if (patternsChosen[i][j] == -1){ //if unselected pattern fill with empty
                    simulatedBoard[(i*3)][(j*3)] = 'O';
                    simulatedBoard[(i*3)+1][(j*3)] = 'O';
                    simulatedBoard[(i*3)+2][(j*3)] = 'O';

                    simulatedBoard[(i*3)][(j*3)+1] = 'O';
                    simulatedBoard[(i*3)+1][(j*3)+1] = 'O';
                    simulatedBoard[(i*3)+2][(j*3)+1] = 'O';

                    simulatedBoard[(i*3)][(j*3)+2] = 'O';
                    simulatedBoard[(i*3)+1][(j*3)+2] = 'O';
                    simulatedBoard[(i*3)+2][(j*3)+2] = 'O';
                }
                else{ //fill with the pattern selected
                    simulatedBoard[(i*3)][(j*3)] = pString.charAt(patternsChosen[i][j]*9);
                    simulatedBoard[(i*3)+1][(j*3)] = pString.charAt((patternsChosen[i][j]*9)+1);
                    simulatedBoard[(i*3)+2][(j*3)] = pString.charAt((patternsChosen[i][j]*9)+2);

                    simulatedBoard[(i*3)][(j*3)+1] = pString.charAt((patternsChosen[i][j]*9)+3);
                    simulatedBoard[(i*3)+1][(j*3)+1] = pString.charAt((patternsChosen[i][j]*9)+4);
                    simulatedBoard[(i*3)+2][(j*3)+1] = pString.charAt((patternsChosen[i][j]*9)+5);

                    simulatedBoard[(i*3)][(j*3)+2] = pString.charAt((patternsChosen[i][j]*9)+6);
                    simulatedBoard[(i*3)+1][(j*3)+2] = pString.charAt((patternsChosen[i][j]*9)+7);
                    simulatedBoard[(i*3)+2][(j*3)+2] = pString.charAt((patternsChosen[i][j]*9)+8);
                }
            }
        }

        //store list of all non Solid coordinates
        ArrayList<Coordinate> unVisited = new ArrayList<Coordinate>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (!(simulatedBoard[i][j] == 'S' || simulatedBoard[i][j] == '5' || simulatedBoard[i][j] == '8')){
                    unVisited.add(new Coordinate(i,j));
                }
            }
        }

        //Add the first empty coord to frontier and remove it
        ArrayList<Coordinate> frontier = new ArrayList<Coordinate>();

        frontier.add(unVisited.remove(0));

        //Traverse level and remove each visited node, until no unvisted node can be reached
        while(frontier.size() > 0){
            for(int i = 0; i < unVisited.size(); i++){
                if (frontier.get(0).adjacent(unVisited.get(i))){
                    frontier.add(unVisited.remove(i));
                }
            }
            frontier.remove(0);
        }

        //if there is no more unvisted then it is fully connected
        return unVisited.size() == 0;

    }

    private int[][] fillBorders(int[][] patternsChosen, int width, int height, String pString){
        if (gameAnalyzer.getSolidSprites().size() > 0){
            //fill non corner tops and bottoms
            System.out.println("Step 1");
            for(int i = 1; i < (width/3) -1 ; i++){
                patternsChosen[i][0] = findValidPattern(topWallPatterns);
                while(!checkConnectivity(patternsChosen, width, height, pString)){
                    if (avatarPatterns.contains(patternsChosen[i][0])) hasAvatar = false;
                    patternsChosen[i][0] = findValidPattern(topWallPatterns);
                }
                patternsChosen[i][(height / 3) - 1] = findValidPattern(bottomWallPatterns);
                while(!checkConnectivity(patternsChosen, width, height, pString)){
                    if (avatarPatterns.contains(patternsChosen[i][(height / 3) - 1])) hasAvatar = false;
                    patternsChosen[i][(height / 3) - 1] = findValidPattern(bottomWallPatterns);
                }
            }
            System.out.println("Step 2");
            //fill non corner sides
            for(int i = 1; i < (height/3) -1 ; i++){
                patternsChosen[0][i] = findValidPattern(leftWallPatterns);
                while(!checkConnectivity(patternsChosen, width, height, pString)){
                    if (avatarPatterns.contains(patternsChosen[0][i])) hasAvatar = false;
                    patternsChosen[0][i] = findValidPattern(leftWallPatterns);
                }
                patternsChosen[(width / 3) - 1][i] = findValidPattern(rightWallPatterns);
                while(!checkConnectivity(patternsChosen, width, height, pString)){
                    if (avatarPatterns.contains(patternsChosen[(width / 3) - 1][i])) hasAvatar = false;
                    patternsChosen[(width / 3) - 1][i] = findValidPattern(rightWallPatterns);
                }
            }
            //fill in corners
            System.out.println("Step 3");
            patternsChosen[0][0] = findValidPattern(tlPatterns);
            ArrayList<Integer> validPatterns = tlPatterns;
            while(!checkConnectivity(patternsChosen, width, height, pString)){
                validPatterns.remove(validPatterns.indexOf(patternsChosen[0][0]));
                if (avatarPatterns.contains(patternsChosen[0][0])) hasAvatar = false;
                if(validPatterns.size() < 10){
                    connectivityBail = true;
                    break;
                }
                System.out.println(validPatterns.size());
                patternsChosen[0][0] = findValidPattern(validPatterns);
                System.out.println("looking for tl patterns");
            }
            patternsChosen[(width/3)-1][0] = findValidPattern(trPatterns);
            validPatterns = trPatterns;
            while(!checkConnectivity(patternsChosen, width, height, pString)){
                validPatterns.remove(validPatterns.indexOf(patternsChosen[(width/3)-1][0]));
                if (avatarPatterns.contains(patternsChosen[(width/3)-1][0])) hasAvatar = false;
                if(validPatterns.size() < 10){
                    connectivityBail = true;
                    break;
                }
                System.out.println(validPatterns.size());
                patternsChosen[(width/3)-1][0] = findValidPattern(validPatterns);
                System.out.println("looking for tr patterns");
            }
            patternsChosen[0][(height/3)-1] = findValidPattern(blPatterns);
            validPatterns = blPatterns;
            while(!checkConnectivity(patternsChosen, width, height, pString)){
                validPatterns.remove(validPatterns.indexOf(patternsChosen[0][(height/3)-1]));
                if (avatarPatterns.contains(patternsChosen[0][(height/3)-1])) hasAvatar = false;
                if(validPatterns.size() < 10){
                    connectivityBail = true;
                    break;
                }
                System.out.println(validPatterns.size());
                patternsChosen[0][(height/3)-1] = findValidPattern(validPatterns);
                System.out.println("looking for bl patterns");
            }
            patternsChosen[(width/3)-1][(height/3)-1] = findValidPattern(brPatterns);
            validPatterns = brPatterns;
            while(!checkConnectivity(patternsChosen, width, height, pString)){
                validPatterns.remove(validPatterns.indexOf(patternsChosen[(width/3)-1][(height/3)-1]));
                if (avatarPatterns.contains(patternsChosen[(width/3)-1][(height/3)-1])) hasAvatar = false;
                if(validPatterns.size() < 10){
                    connectivityBail = true;
                    break;
                }
                System.out.println(validPatterns.size());
                patternsChosen[(width/3)-1][(height/3)-1] = findValidPattern(validPatterns);
                System.out.println("looking for br patterns");
            }
            System.out.println("Found one!");

        } else {
            //fill in borders with random patterns
            for( int i = 0; i < (width/3) ; i++ ) {
                patternsChosen[i][0] = findRandomValidPattern(pString);
                while(!checkConnectivity(patternsChosen, width, height, pString)){
                    if (avatarPatterns.contains(patternsChosen[i][0])) hasAvatar = false;
                    patternsChosen[i][0] = findRandomValidPattern(pString);
                }
                patternsChosen[i][(height/3)-1] = findRandomValidPattern(pString);
                while(!checkConnectivity(patternsChosen, width, height, pString)){
                    if (avatarPatterns.contains(patternsChosen[i][(height/3)-1])) hasAvatar = false;
                    patternsChosen[i][(height/3)-1] = findRandomValidPattern(pString);
                }
            }

            for(int i = 1; i < (height/3) -1 ; i++){
                patternsChosen[0][i] = findRandomValidPattern(pString);
                while(!checkConnectivity(patternsChosen, width, height, pString)){
                    if (avatarPatterns.contains(patternsChosen[0][i])) hasAvatar = false;
                    patternsChosen[0][i] = findRandomValidPattern(pString);
                }
                patternsChosen[(width/3)-1][i] = findRandomValidPattern(pString);
                while(!checkConnectivity(patternsChosen, width, height, pString)){
                    if (avatarPatterns.contains(patternsChosen[(width/3)-1][i])) hasAvatar = false;
                    patternsChosen[(width/3)-1][i] = findRandomValidPattern(pString);
                }
            }
        }

        System.out.println("Actually chose a pattern");
        return patternsChosen;
    }


    private int findValidPattern(ArrayList<Integer> searchList){
        if (hasAvatar) {
            return randomDissimilarNumber(searchList, avatarPatterns);
        }
        else{
            Random randGen = new Random();
            int randNum = randGen.nextInt(searchList.size());
            hasAvatar = avatarPatterns.contains(searchList.get(randNum));
            return searchList.get(randNum);
        }
    }


    //helper to find matching index between two files
    private int randomDissimilarNumber(ArrayList<Integer> a1, ArrayList<Integer> a2 ){
        int foundNum = Integer.MIN_VALUE;

        ArrayList<Integer> A1 = new ArrayList<Integer>(a1);
        Collections.shuffle(A1, new Random(System.nanoTime()));

        for(int i = 0; i < A1.size(); i ++){
            for(int j = 0; j < a2.size(); j++){
                if(A1.get(i) != a2.get(j)){
                    return A1.get(i);
                }
            }
        }

        return foundNum;
    }

    private void loadPatternIndices() throws IOException {
        Scanner readFile;

        avatarPatterns.clear();
        bottomAvatarPatterns.clear();
        bottomWallPatterns.clear();
        openBottomPatterns.clear();
        topWallPatterns.clear();
        bottomWallPatterns.clear();
        leftWallPatterns.clear();
        rightWallPatterns.clear();
        trPatterns.clear();
        tlPatterns.clear();
        brPatterns.clear();
        blPatterns.clear();

        readFile = new Scanner(new FileReader("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\avatarPatterns.txt"));
        while(readFile.hasNextLine()){
            avatarPatterns.add(Integer.parseInt(readFile.nextLine()));
        }
        readFile.close();

        readFile = new Scanner(new FileReader("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\bottomAvatarPatterns.txt"));
        while(readFile.hasNextLine()){
            bottomAvatarPatterns.add(Integer.parseInt(readFile.nextLine()));
        }
        readFile.close();

        readFile = new Scanner(new FileReader("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\openBottomPatterns.txt"));
        while(readFile.hasNextLine()){
            openBottomPatterns.add(Integer.parseInt(readFile.nextLine()));
        }
        readFile.close();

        readFile = new Scanner(new FileReader("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\topWallPatterns.txt"));
        while(readFile.hasNextLine()){
            topWallPatterns.add(Integer.parseInt(readFile.nextLine()));
        }
        readFile.close();

        readFile = new Scanner(new FileReader("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\bottomWallPatterns.txt"));
        while(readFile.hasNextLine()){
            bottomWallPatterns.add(Integer.parseInt(readFile.nextLine()));
        }
        readFile.close();

        readFile = new Scanner(new FileReader("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\leftWallPatterns.txt"));
        while(readFile.hasNextLine()){
            leftWallPatterns.add(Integer.parseInt(readFile.nextLine()));
        }
        readFile.close();

        readFile = new Scanner(new FileReader("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\rightWallPatterns.txt"));
        while(readFile.hasNextLine()){
            rightWallPatterns.add(Integer.parseInt(readFile.nextLine()));
        }
        readFile.close();

        readFile = new Scanner(new FileReader("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\tlPatterns.txt"));
        while(readFile.hasNextLine()){
            tlPatterns.add(Integer.parseInt(readFile.nextLine()));
        }
        readFile.close();

        readFile = new Scanner(new FileReader("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\trPatterns.txt"));
        while(readFile.hasNextLine()){
            trPatterns.add(Integer.parseInt(readFile.nextLine()));
        }
        readFile.close();

        readFile = new Scanner(new FileReader("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\blPatterns.txt"));
        while(readFile.hasNextLine()){
            blPatterns.add(Integer.parseInt(readFile.nextLine()));
        }
        readFile.close();

        readFile = new Scanner(new FileReader("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\brPatterns.txt"));
        while(readFile.hasNextLine()){
            brPatterns.add(Integer.parseInt(readFile.nextLine()));
        }
        readFile.close();
    }




    private int pickLevelSize(){
        Random randGen = new Random();
        if(randGen.nextInt(2) == 1){
            return 15;
        } else {
            return 12;
        }
    }

    private char convertCodeToMap(char code){
        if (code == '\n') return '\n';
        else{
            Random randGen = new Random();
            int index = randGen.nextInt(codeMapping.get(code).size());
            return codeMapping.get(code).get(index);
        }
    }


    private String convertTypeLevelToGameLevel(String tLevel){
        StringBuilder stringBuilder = new StringBuilder();

        for(int i = 0; i < tLevel.length(); i++){
            stringBuilder.append(convertCodeToMap(tLevel.charAt(i)));
        }

        return stringBuilder.toString();
    }

    private ArrayList<Coordinate> calculateFreePositions(char[][] level, int width, int height, ArrayList<String> allTerminationSprites){

        ArrayList<Coordinate> freePositions = new ArrayList<>();

        for(int i = 0; i < width; i ++){
            for(int j = 0; j < height; j ++){
                boolean freeSpace = true;
                for(int k = 0; k < gameDescription.getLevelMapping().get(level[i][j]).size(); k ++){
                    if(gameAnalyzer.getAvatarSprites().contains(gameDescription.getLevelMapping().get(level[i][j]).get(k))){
                        freeSpace = false;
                    } else if(gameAnalyzer.getSolidSprites().contains(gameDescription.getLevelMapping().get(level[i][j]).get(k))){
                        freeSpace = false;
                    } else if(allTerminationSprites.contains(gameDescription.getLevelMapping().get(level[i][j]).get(k))){
                        freeSpace = false;
                    }
                }
                if(freeSpace){
                    freePositions.add(new Coordinate(i, j));
                }
            }
        }

        return freePositions;
    }

    private char[][] stringToArray(String s){
        String[] test = s.split("\n");

        int width = test[0].length();
        int height = test.length;

        char[][] levelArray = new char[width][height];

        for(int i = 0; i < test.length; i ++){
            for(int j = 0; j < test[i].length(); j++){
                levelArray[j][i] = test[i].charAt(j);
            }
        }

        return levelArray;

    }

    private String fixGoals(String level) {

        char[][] levelArray = stringToArray(level);
        // Data of all termination conditions in the current game
        ArrayList<GameDescription.TerminationData> terminationData = gameDescription.getTerminationConditions();
        // Map to store number of objects in the given game level
        HashMap<String, Integer> numObjects = new HashMap<>();
        // List of all areas that are not solid/can place a goal/termination
        // List of available termination sprites
        ArrayList<String> currentSprites = new ArrayList<>();
        ArrayList<Character> gameMapping = new ArrayList<>();
        ArrayList<String> allTerminationSprites = new ArrayList<>();
        Random rand = new Random();

        gameMapping.addAll(gameDescription.getLevelMapping().keySet());

        // Populate the HashMap with available objects in the level

        for(int i = 0; i < level.length(); i ++){
            for(int j = 0; j < gameMapping.size(); j ++){
                if(level.charAt(i) == gameMapping.get(j)){
                    for(int k = 0; k < gameDescription.getLevelMapping().get(level.charAt(i)).size(); k++){
                        if(numObjects.containsKey(gameDescription.getLevelMapping().get(level.charAt(i)).get(k))){
                            numObjects.replace(gameDescription.getLevelMapping().get(level.charAt(i)).get(k),(numObjects.get(gameDescription.getLevelMapping().get(level.charAt(i)).get(k)) + 1));
                        } else {
                            numObjects.put(gameDescription.getLevelMapping().get(level.charAt(i)).get(k), 1);
                        }
                    }
                }
            }
        }
        /* For each set of termination data,
            - Count how many non avatars are on the board
            - Find the difference between the expected # and the current #
            - add sprites to random free positions until correct # is reached
         */
//        System.out.println(numObjects.keySet());
        for(GameDescription.TerminationData ter: terminationData){
            int desiredNum = ter.limit;
            int currentNum = 0;
            int increase = 0;

            for(String spriteName: ter.sprites){
//                System.out.println(spriteName);
                if(!gameAnalyzer.getAvatarSprites().contains(spriteName)){
                    if(numObjects.keySet().contains(spriteName)){
                        currentNum += numObjects.get(spriteName);
                    }
                    currentSprites.add(spriteName);
                    allTerminationSprites.add(spriteName);
                }
            }

            ArrayList<Coordinate> freePositions = calculateFreePositions(levelArray, levelArray.length, levelArray[0].length, allTerminationSprites);

            increase = desiredNum + 1 - currentNum;

            if(currentSprites.size() > 0){
                ArrayList<Character> foundMappings = new ArrayList<>();
                ArrayList<Character> allMappings = new ArrayList<>();
                allMappings.addAll(gameDescription.getLevelMapping().keySet());

                for(int i = 0; i < gameDescription.getLevelMapping().keySet().size(); i++ ){
                    for(int j = 0; j < currentSprites.size(); j++){
                        if(gameDescription.getLevelMapping().get(allMappings.get(i)).contains(currentSprites.get(j))){
                            foundMappings.add(allMappings.get(i));
                        }
                    }

                }
                for(int i = 0; i < increase; i ++){
                    int index = rand.nextInt(freePositions.size());
                    Coordinate coord = freePositions.remove(index);
                    levelArray[coord.xPos][coord.yPos] = foundMappings.get(rand.nextInt(foundMappings.size()));
//                    System.out.println("Added: " + levelArray[coord.xPos][coord.yPos] + " at " + coord.xPos + " " + coord.yPos);
                }
            }
        }

        StringBuilder output = new StringBuilder();
        for(int i = 0; i < levelArray[0].length; i ++){
            for(int j = 0; j < levelArray.length; j ++){
                output.append(levelArray[j][i]);
            }
            output.append("\n");
        }
        return output.toString();
    }

    @Override
    public String generateLevel(GameDescription game, ElapsedCpuTimer elapsedTimer) {
        try{
            codeMapping = getCodeMapping();
//            System.out.println(codeMapping);
            return fixGoals(convertTypeLevelToGameLevel(construct())); // construct the level, then convert it from its type to a playable level, the fix any termination values
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Error";
    }

    public String generateLevel(GameDescription game, ElapsedCpuTimer elapsedTimer, int width, int height) {
        try{
            codeMapping = getCodeMapping();
//            System.out.println(codeMapping);
            return fixGoals(convertTypeLevelToGameLevel(construct(width, height))); // construct the level, then convert it from its type to a playable level, the fix any termination values
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Error";
    }

    @Override
    public HashMap<Character, ArrayList<String>> getLevelMapping(){return gameDescription.getLevelMapping();}
}