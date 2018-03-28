package tracks.levelGeneration.patternConstructive;


import core.game.Game;
import core.game.GameDescription;
import core.vgdl.VGDLFactory;
import core.vgdl.VGDLParser;
import core.vgdl.VGDLRegistry;
import tools.GameAnalyzer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class LevelCreator {

    // 1. Reads from pattern folder and grabs patterns
    // 2. Make a level from these patterns, random size between 9x9 and 12x12
    // 3. Decode the newly made level into game-specific characters for sprites

    GameAnalyzer gameAnalyzer;
    GameDescription gameDescription;
    Game game;

    LevelCreator(){

        VGDLFactory.GetInstance().init();
        VGDLRegistry.GetInstance().init();
        game = new VGDLParser().parseGame("examples/gridphysics/aliens.txt");
        gameDescription = new GameDescription(game);
        gameAnalyzer = new GameAnalyzer(gameDescription);
    }

    private HashMap<Character, Character> getCodeMapping(HashMap<Character, ArrayList<String>> charMap){

        HashMap<Character, Character> codeMap = new HashMap<Character, Character>();

        ArrayList<Character> chars = (ArrayList<Character>) charMap.keySet();

        for(int i = 0; i < chars.size(); i++) {
            char code = convertMappingToCode(chars.get(i), charMap);
            codeMap.put(code, chars.get(i));

        }

        return codeMap;
    }

    private char convertMappingToCode(char lookupChar, HashMap<Character, ArrayList<String>> charMap) {

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
