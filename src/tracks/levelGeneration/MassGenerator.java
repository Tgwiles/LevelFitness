package tracks.levelGeneration;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class MassGenerator {



    public static void main(String[] args) {
        String geneticGenerator = "tracks.levelGeneration.geneticLevelGenerator.LevelGenerator";
        String patternGenerator = "tracks.levelGeneration.patternConstructive.LevelGenerator";
        String patternSearchGenerator = "tracks.levelGeneration.patternSearch.LevelGenerator";

        String games[] = new String[] {"bomberman"};

        String gamesPath = "examples/gridphysics/";

        for(int i = 0; i < games.length; i++){
            for (int j = 0; j < 10; j++){
                String levelPath = gamesPath + games[i] +"_pslevel" + j + ".txt"; //change this for proper directory

                LevelGenMachine.generateOneLevel(gamesPath + games[i] + ".txt", patternSearchGenerator, levelPath);
            }
        }
    }
}