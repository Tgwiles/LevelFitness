package tracks.levelGeneration;

import tools.Utils;
import tracks.ArcadeMachine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class SurveyTester {

    public static void main(String[] args) {

        String baseGamePath = "examples/gridphysics/";

        String zelda = "zelda";
        String bomberman = "bomberman";
        String frogs = "frogs";

        ArrayList<String> games = new  ArrayList<String>();
        games.add(zelda);
        games.add(bomberman);
        games.add(frogs);
        Collections.shuffle(games); //pick a random order for the games

        String baseLevelPath = "examples/surveyLevels/";

        String newConstructive = "newConstructive";
        String newSearch = "newSearch";
        String oldSearch = "oldSearch";

        ArrayList<String> newCnewS = new ArrayList<String>();
        newCnewS.add(newConstructive);
        newCnewS.add(newSearch);
        Collections.shuffle(newCnewS);

        ArrayList<String> newColdS = new ArrayList<String>();
        newColdS.add(newConstructive);
        newColdS.add(oldSearch);
        Collections.shuffle(newColdS);

        ArrayList<String> newSoldS = new ArrayList<String>();
        newSoldS.add(newSearch);
        newSoldS.add(oldSearch);
        Collections.shuffle(newSoldS);

        ArrayList<ArrayList<String>> comparisonsOrder = new ArrayList<ArrayList<String>>();
        comparisonsOrder.add(newCnewS);
        comparisonsOrder.add(newColdS);
        comparisonsOrder.add(newSoldS);
        Collections.shuffle(comparisonsOrder);

        for (int i = 0; i < 3; i++){
            System.out.println("Playing game: " + games.get(i));
            int levelNumOne = new Random().nextInt(5);
            int levelNumTwo = new Random().nextInt(5);
            System.out.println("With comparison: " + comparisonsOrder.get(i).get(0) + " level " + levelNumOne + " vs " + comparisonsOrder.get(i).get(1) + " level " + levelNumTwo);
            ArcadeMachine.playOneGame(baseGamePath+games.get(i)+".txt", baseLevelPath+comparisonsOrder.get(i).get(0)+"/"+games.get(i)+"_level" + levelNumOne +".txt", null, new Random().nextInt());
            ArcadeMachine.playOneGame(baseGamePath+games.get(i)+".txt", baseLevelPath+comparisonsOrder.get(i).get(1)+"/"+games.get(i)+"_level" + levelNumTwo +".txt", null, new Random().nextInt());
        } // System.out.println("End my life");
    }
}