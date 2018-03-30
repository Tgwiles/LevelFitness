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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.*;

import static core.competition.GVGReplayer.game;

public class LevelGenerator extends AbstractLevelGenerator{

    // 1. Reads from pattern folder and grabs patterns
    // 2. Make a level from these patterns, random size between 9x9 and 12x12
    // 3. Decode the newly made level into game-specific characters for sprites

    GameAnalyzer gameAnalyzer;
    GameDescription gameDescription;
    //Game game;
    HashMap<Character, ArrayList<Character>> codeMapping;


    public LevelGenerator(GameDescription game, ElapsedCpuTimer elapsedCpuTimer){
        gameAnalyzer = new GameAnalyzer(game);
        gameDescription = game;
    }

//    LevelGenerator(String gamePath){
//
//        VGDLFactory.GetInstance().init();
//        VGDLRegistry.GetInstance().init();
//        game = new VGDLParser().parseGame(gamePath);
//        gameDescription = new GameDescription(game);
//        gameAnalyzer = new GameAnalyzer(gameDescription);
//
//        codeMapping = getCodeMapping();
//    }

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

        //populate unknown translations TODO: REDO THE TIE BREAKER TO RECOGNIZE PERFECT TIES AND MAKE THEM ADD BOTH TO THE CODE MAP
        for (int i = 0; i <codes.size(); i++){
            if (codeMap.get(codes.get(i)).size() == 0){
                codeMap.get(codes.get(i)).addAll(bestMapping(codes.get(i)));
            }
        }

        return codeMap;
    }

    private ArrayList<Character> bestMapping(char code){
        ArrayList<Character> rankedMappings = new ArrayList<Character>();

        ArrayList<Character> chars = new ArrayList<Character>();
        chars.addAll(gameDescription.getLevelMapping().keySet()); //contains all level mappings

        ArrayList<Character> answerMappings = new ArrayList<Character>();

        int bestScore = Integer.MIN_VALUE;
        char bestMapping = chars.get(0);
        for(int i = 0; i < chars.size(); i++){ //for each level mapping
            int currentMapScore = 0;
            for(int j = 0; j < gameDescription.getLevelMapping().get(chars.get(i)).size(); j++){ //for each sprite in level mapping
                if (spriteCodeMatch(gameDescription.getLevelMapping().get(chars.get(i)).get(j), code)){ //count type for each sprite
                    currentMapScore++;
                }else currentMapScore--;
            }
            if (currentMapScore > bestScore){
                answerMappings.clear();
                answerMappings.add(chars.get(i));
                bestScore = currentMapScore;

            }else if(currentMapScore == bestScore){
                if(breakTie(answerMappings.get(0), chars.get(i)) == '?'){
                    answerMappings.add(chars.get(i));
                }else if (breakTie(answerMappings.get(0), chars.get(i)) == chars.get(i)){
                    answerMappings.clear();
                    answerMappings.add(chars.get(i));
                }
            }
        }

        return answerMappings;
    }

    // Determine if a sprite exists in the game and in the sprite code
    private boolean spriteCodeMatch(String sprite, char code) {
        if (gameAnalyzer.getAvatarSprites().contains(sprite) && (code == 'A' || code == '2' || code == '8')){
            return true;
        } else if(gameAnalyzer.getSolidSprites().contains(sprite) && (code == 'S' || code == '5' || code == '9')){
            return true;
        } else if(gameAnalyzer.getCollectableSprites().contains(sprite) && (code == 'C' || code == '4')){
            return true;
        } else if(gameAnalyzer.getHarmfulSprites().contains(sprite) && (code == 'H' || code == '3' || code == '6' || code == '7' || code == '9')) {
            return true;
        } else if(gameAnalyzer.getOtherSprites().contains(sprite) && (code == 'O' || code == '1' || code == '2' || code == '3' || code == '4' || code == '5' || code == '6' || code == '7')) {
            return true;
        } else
            return false;
    }

    private char breakTie(char mapOne, char mapTwo){

        if (mappingHasSolid(mapOne) && !mappingHasSolid(mapTwo)) return mapOne;
        else if(mappingHasSolid(mapTwo) && !mappingHasSolid(mapOne)) return mapTwo;

        else if(mappingHasOther(mapOne) && !mappingHasOther(mapTwo)) return mapOne;
        else if(mappingHasOther(mapTwo) && !mappingHasOther(mapOne)) return mapTwo;

        else if(mappingHasHarmful(mapOne) && !mappingHasHarmful(mapTwo)) return mapOne;
        else if(mappingHasHarmful(mapTwo) && !mappingHasHarmful(mapOne)) return mapTwo;

        else if(mappingHasCollectable(mapOne) && !mappingHasCollectable(mapTwo)) return mapOne;
        else if(mappingHasCollectable(mapTwo) && !mappingHasCollectable(mapOne)) return mapTwo;

        return '?';
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

    private String assemblePatterns() throws FileNotFoundException {

        Scanner readFile = new Scanner(new FileReader("src\\tracks\\levelGeneration\\patternConstructive\\patternFile.txt"));
        StringBuilder sb = new StringBuilder();

        while(readFile.hasNext()){
            sb.append(readFile.next());
        }
        readFile.close();
        String outString = sb.toString();

        int stringLength = outString.length();

        Random randGen = new Random();

        ArrayList<Integer> patternsChosen = new ArrayList<Integer>();
        int height = pickLevelSize();
        int width = pickLevelSize();

        for(int i = 0; i < ((height * width)/9); i++){
            patternsChosen.add(randGen.nextInt(stringLength/9));
        }

        StringBuilder stringBuilder = new StringBuilder();

        char[][] levelArray = new char[width][height];

        //System.out.println("Height: " + height);
        //System.out.println("Width: " + width);

        //System.out.println("Patterns Chosen: " + patternsChosen.toString());

        // Reformat the pattern strings back into level layout
        for(int i = 0; i < height; i += 3) {
            for (int j = 0; j < width; j += 3) {
                //System.out.println("j = " + j + ", i = " + i);
                levelArray[j][i] = outString.charAt(patternsChosen.get(j + (i / 3)));
                levelArray[j + 1][i] = outString.charAt(patternsChosen.get(j + (i / 3)) + 1);
                levelArray[j + 2][i] = outString.charAt(patternsChosen.get(j + (i / 3)) + 2);

                levelArray[j][i + 1] = outString.charAt(patternsChosen.get(j + (i / 3)) + 3);
                levelArray[j + 1][i + 1] = outString.charAt(patternsChosen.get(j + (i / 3)) + 4);
                levelArray[j + 2][i + 1] = outString.charAt(patternsChosen.get(j + (i / 3)) + 5);

                levelArray[j][i + 2] = outString.charAt(patternsChosen.get(j + (i / 3)) + 6);
                levelArray[j + 1][i + 2] = outString.charAt(patternsChosen.get(j + (i / 3)) + 7);
                levelArray[j + 2][i + 2] = outString.charAt(patternsChosen.get(j + (i / 3)) + 8);
            }

        }

        for( int i = 0; i < height; i++){
            for(int j = 0; j < width; j++) {
                stringBuilder.append(levelArray[j][i]);
            }
            stringBuilder.append('\n');
        }

        return stringBuilder.toString();
    }


    private int pickLevelSize(){
        Random randGen = new Random();
        if(randGen.nextInt(2) == 1){
            return 12;
        } else {
            return 9;
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

//    public static void main(String[] args) throws FileNotFoundException {
//        LevelGenerator c = new LevelGenerator("\\examples\\gridphysics\\zelda.txt");
//        System.out.println(c.getCodeMapping().toString());
//        System.out.println(c.convertTypeLevelToGameLevel(c.assemblePatterns()));
//    }

    @Override
    public String generateLevel(GameDescription game, ElapsedCpuTimer elapsedTimer) {
        try{
            codeMapping = getCodeMapping();
            return convertTypeLevelToGameLevel(assemblePatterns());
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "Error";
    }

    @Override
    public HashMap<Character, ArrayList<String>> getLevelMapping(){return gameDescription.getLevelMapping();}
}

