package tracks.levelGeneration.patternSearch;

import core.game.Event;
import core.game.GameDescription.SpriteData;
import core.game.GameDescription.TerminationData;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.WINNER;
import tools.ElapsedCpuTimer;
import tools.LevelMapping;
import tools.StepController;
import tracks.levelGeneration.constraints.CombinedConstraints;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;

public class Chromosome implements Comparable<Chromosome>{

	/**
	 * current level described by the chromosome
	 */
	private ArrayList<String>[][] level;
	/**
	 * current chromosome fitness if its a feasible
	 */
	private ArrayList<Double> fitness;
	/**
	 * current chromosome fitness if its an infeasible
	 */
	private double constrainFitness;
	/**
	 * if the fitness is calculated before (no need to recalculate)
	 */
	private boolean calculated;
	/**
	 * the best automated agent
	 */
	private AbstractPlayer automatedAgent;
	/**
	 * the naive automated agent
	 */
	private AbstractPlayer naiveAgent;
	/**
	 * the do nothing automated agent
	 */
	private AbstractPlayer doNothingAgent;
	/**
	 * The current stateObservation of the level
	 */
	private StateObservation stateObs;

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

    String pString;
    HashMap<Character, ArrayList<Character>> codeMapping;
	
	/**
	 * initialize the chromosome with a certain length and width
	 * @param width
	 * @param height
	 */
	@SuppressWarnings("unchecked")
	public Chromosome(int width, int height) throws IOException {
//        System.out.println("Chromosome()");
		this.level = new ArrayList[height][width];
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				this.level[y][x] = new ArrayList<String>();
			}
		}
		this.fitness = new ArrayList<Double>();
		this.calculated = false;
		this.stateObs = null;

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

        loadPatternIndices();

        Scanner readFile = new Scanner(new FileReader("src\\tracks\\levelGeneration\\patternConstructive\\PatternData\\patternFile.txt"));
        StringBuilder sb = new StringBuilder();


        while(readFile.hasNext()){
            sb.append(readFile.next());
        }
        readFile.close();
        pString = sb.toString();
        codeMapping = getCodeMapping();
	}
	

	/**
	 * clone the chromosome data
	 */
	public Chromosome clone(){
//        System.out.println("Clone()");
        Chromosome c = null;
        try {
            c = new Chromosome(level[0].length, level.length);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(int y = 0; y < level.length; y++){
			for(int x = 0; x < level[y].length; x++){
				c.level[y][x].addAll(level[y][x]);
			}
		}
		
		c.constructAgent();
		return c;
	}
	

	/**
	 * initialize the agents used during evaluating the chromosome
	 */
	@SuppressWarnings("unchecked")
	private void constructAgent(){
//        System.out.println("constructAgent()");
		try{
			Class agentClass = Class.forName(SharedData.AGENT_NAME);
			Constructor agentConst = agentClass.getConstructor(new Class[]{StateObservation.class, ElapsedCpuTimer.class});
			automatedAgent = (AbstractPlayer)agentConst.newInstance(getStateObservation().copy(), null);
		}
		catch(Exception e){
			e.printStackTrace();
		}

		try{
			Class agentClass = Class.forName(SharedData.NAIVE_AGENT_NAME);
			Constructor agentConst = agentClass.getConstructor(new Class[]{StateObservation.class, ElapsedCpuTimer.class});
			naiveAgent = (AbstractPlayer)agentConst.newInstance(getStateObservation().copy(), null);
		}
		catch(Exception e){
			e.printStackTrace();
		}

		try{
			Class agentClass = Class.forName(SharedData.NAIVE_AGENT_NAME);
			Constructor agentConst = agentClass.getConstructor(new Class[]{StateObservation.class, ElapsedCpuTimer.class});
			doNothingAgent = (AbstractPlayer)agentConst.newInstance(getStateObservation().copy(), null);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}


	/**
	 * initialize the chromosome with random sprites
	 */
	public void InitializeRandom(){
//        System.out.println("Initialize Random()");
		for(int i = 0; i < SharedData.RANDOM_INIT_AMOUNT; i++){
			this.mutate();
		}

		constructAgent();
	}

	/**
	 * initialize the chromosome using the contructive level generator
	 */
	public void InitializeConstructive(){
//        System.out.println("initializeConstructive()");
		String[] levelString = SharedData.patternGen.generateLevel(SharedData.gameDescription, null, level[0].length, level.length).split("\n");
		HashMap<Character, ArrayList<String>> charMap = SharedData.patternGen.getLevelMapping();

		for(int y=0; y<levelString.length; y++){
			for(int x=0; x<levelString[y].length(); x++){
				if(levelString[y].charAt(x) != ' '){
					this.level[y][x].addAll(charMap.get(levelString[y].charAt(x)));
				}
			}
		}

		FixLevel();
		constructAgent();
	}


	/**
	 * crossover the current chromosome with the input chromosome
	 * @param c	the other chromosome to crossover with
	 * @return	the current children from the crossover process
	 */
	public ArrayList<Chromosome> crossOver(Chromosome c) throws IOException {
//        System.out.println("crossOver()");
		ArrayList<Chromosome> children = new ArrayList<Chromosome>();
		children.add(new Chromosome(level[0].length, level.length));
		children.add(new Chromosome(level[0].length, level.length));

		//crossover point
		int pointY = SharedData.random.nextInt(level.length);
		int pointX = SharedData.random.nextInt(level[0].length);

		//swap the two chromosomes around this point
		for(int y = 0; y < level.length; y++){
			for(int x = 0; x < level[y].length; x++){
				if(y < pointY){
					children.get(0).level[y][x].addAll(this.level[y][x]);
					children.get(1).level[y][x].addAll(c.level[y][x]);
				}
				else if(y == pointY){
					if(x <= pointX){
						children.get(0).level[y][x].addAll(this.level[y][x]);
						children.get(1).level[y][x].addAll(c.level[y][x]);
					}
					else{
						children.get(0).level[y][x].addAll(c.level[y][x]);
						children.get(1).level[y][x].addAll(this.level[y][x]);
					}
				}
				else{
					children.get(0).level[y][x].addAll(c.level[y][x]);
					children.get(1).level[y][x].addAll(this.level[y][x]);
				}
			}
		}

		children.get(0).FixLevel();
		children.get(1).FixLevel();

		children.get(0).constructAgent();
		children.get(1).constructAgent();

		return children;
	}


    private int randomDissimilarNumber(ArrayList<Integer> a1, ArrayList<Integer> a2 ){
//        System.out.println("RandomDissimilarNumber()");
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

    private int randomSimilarNumber(ArrayList<Integer> a1, ArrayList<Integer> a2 ){
//        System.out.println("RandomSimilarNumber");
        int foundNum = Integer.MIN_VALUE;

        ArrayList<Integer> A1 = new ArrayList<Integer>(a1);
        Collections.shuffle(A1, new Random(System.nanoTime()));

        for(int i = 0; i < A1.size(); i ++){
            for(int j = 0; j < a2.size(); j++){
                if(A1.get(i) == a2.get(j)){
                    return A1.get(i);
                }
            }
        }

        return foundNum;
    }

    private void loadPatternIndices() throws IOException {
//        System.out.println("loadPatternindices");

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

        Scanner readFile;

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

    private char convertMappingToCode(char lookupChar) {
//        System.out.println("convertmappingtocode");
        ArrayList<String> avatars = new ArrayList<String>();
        ArrayList<String> solids = new ArrayList<String>();
        ArrayList<String> harmfuls = new ArrayList<String>();
        ArrayList<String> collectables = new ArrayList<String>();
        ArrayList<String> others = new ArrayList<String>();

        //Find all sprites of each class
        avatars.addAll(SharedData.gameAnalyzer.getAvatarSprites());
        solids.addAll(SharedData.gameAnalyzer.getSolidSprites());
        harmfuls.addAll(SharedData.gameAnalyzer.getHarmfulSprites());
        collectables.addAll(SharedData.gameAnalyzer.getCollectableSprites());
        others.addAll(SharedData.gameAnalyzer.getOtherSprites());

        //System.out.println(lookupChar);
        ArrayList<String> spritesInLocation = new ArrayList<String>();

        for (int j = 0; j < SharedData.gameDescription.getLevelMapping().get(lookupChar).size(); j++) {
            spritesInLocation.add(SharedData.gameDescription.getLevelMapping().get(lookupChar).get(j));
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

    private int codeSpriteMatchScore(ArrayList<String> sprites, char code){
//        System.out.println("codeSpriteMatchScore");
        int aCount = 0, sCount = 0, hCount = 0, cCount = 0, oCount = 0;

        for (int i = 0; i < sprites.size(); i++){
            if (SharedData.gameAnalyzer.getAvatarSprites().contains(sprites.get(i))) aCount++;
            else if (SharedData.gameAnalyzer.getSolidSprites().contains(sprites.get(i))) sCount++;
            else if (SharedData.gameAnalyzer.getHarmfulSprites().contains(sprites.get(i))) hCount++;
            else if (SharedData.gameAnalyzer.getCollectableSprites().contains(sprites.get(i))) cCount++;
            else if (SharedData.gameAnalyzer.getOtherSprites().contains(sprites.get(i))) oCount++;
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

    private boolean mappingHasAvatar(char mapping){

        ArrayList<String> spritesInMap = new ArrayList<String>();
        spritesInMap.addAll(SharedData.gameDescription.getLevelMapping().get(mapping));


        for (int i = 0; i < spritesInMap.size(); i++){
            if (SharedData.gameAnalyzer.getAvatarSprites().contains(spritesInMap.get(i))) return true;
        }
        return false;
    }
    private boolean mappingHasSolid(char mapping){

        ArrayList<String> spritesInMap = new ArrayList<String>();
        spritesInMap.addAll(SharedData.gameDescription.getLevelMapping().get(mapping));


        for (int i = 0; i < spritesInMap.size(); i++){
            if (SharedData.gameAnalyzer.getSolidSprites().contains(spritesInMap.get(i))) return true;
        }
        return false;
    }
    private boolean mappingHasOther(char mapping){

        ArrayList<String> spritesInMap = new ArrayList<String>();
        spritesInMap.addAll(SharedData.gameDescription.getLevelMapping().get(mapping));

        for (int i = 0; i < spritesInMap.size(); i++){
            if (SharedData.gameAnalyzer.getOtherSprites().contains(spritesInMap.get(i))) return true;
        }
        return false;
    }
    private boolean mappingHasHarmful(char mapping){

        ArrayList<String> spritesInMap = new ArrayList<String>();
        spritesInMap.addAll(SharedData.gameDescription.getLevelMapping().get(mapping));

        for (int i = 0; i < spritesInMap.size(); i++){
            if (SharedData.gameAnalyzer.getHarmfulSprites().contains(spritesInMap.get(i))) return true;
        }
        return false;
    }
    private boolean mappingHasCollectable(char mapping){

        ArrayList<String> spritesInMap = new ArrayList<String>();
        spritesInMap.addAll(SharedData.gameDescription.getLevelMapping().get(mapping));

        for (int i = 0; i < spritesInMap.size(); i++){
            if (SharedData.gameAnalyzer.getCollectableSprites().contains(spritesInMap.get(i))) return true;
        }
        return false;
    }

    private char breakTie(char mapOne, char mapTwo){
//        System.out.println("breakTie");

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

    private ArrayList<Character> bestMapping(char code){
//        System.out.println("bestMapping");
        ArrayList<Character> rankedMappings = new ArrayList<Character>();

        ArrayList<Character> levelMapChars = new ArrayList<Character>();
        levelMapChars.addAll(SharedData.gameDescription.getLevelMapping().keySet()); //contains all level mappings

        ArrayList<Character> answerMappings = new ArrayList<Character>();

        int bestScore = Integer.MIN_VALUE;
        for(int i = 0; i < levelMapChars.size(); i++){ //for each level mapping
            int currentMapScore = codeSpriteMatchScore(SharedData.gameDescription.getLevelMapping().get(levelMapChars.get(i)), code);

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

    private HashMap<Character, ArrayList<Character>> getCodeMapping(){
//        System.out.println("getCodeMapping");
        HashMap<Character, ArrayList<Character>> codeMap = new HashMap<Character, ArrayList<Character>>();

        ArrayList<Character> chars = new ArrayList<Character>();
        chars.addAll(SharedData.gameDescription.getLevelMapping().keySet());

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

    private char convertCodeToMap(char code){
//        System.out.println("convertCodeToMap");
	    Random randGen = new Random();
	    int index = randGen.nextInt(codeMapping.get(code).size());
	    return codeMapping.get(code).get(index);
    }

	/**
	 * mutate the current chromosome
	 */
	public void mutate(){
        System.out.println("MUTATE");

		for(int i = 0; i < SharedData.MUTATION_AMOUNT; i++) {
			Random rand = new Random();
			int CHANGE_PROB = 8;		// probability integer

			if(rand.nextInt(10) < CHANGE_PROB){	// pick a pattern and swap it with a random chunk (80% probability)
				int x = rand.nextInt(level[0].length / 3);
				int y = rand.nextInt(level.length / 3);

				String borderType = "none";

				if (SharedData.gameAnalyzer.getSolidSprites().size() > 0 ) {
					if(x == 0 ){
						if (y == 0){
							borderType = "tl";
						} else if (y == (level.length / 3) - 1) {
							borderType = "bl";
						} else {
							borderType = "leftWall";
						}
					} else if (x == (level[0].length / 3) - 1) {
						if (y == 0) {
							borderType = "tr";
						} else if (y == (level.length / 3) - 1){
							borderType = "br";
						} else {
							borderType = "rightWall";
						}
					} else if (y == 0) {
						borderType = "topWall";
					} else if (y == (level.length / 3) - 1){
						borderType = "bottomWall";
					}
				}

				boolean hasAvatar = false;

				if(SharedData.gameAnalyzer.getAvatarSprites().contains(level[y*3][x*3])){
				    hasAvatar = true;
                } else if (SharedData.gameAnalyzer.getAvatarSprites().contains(level[(y*3)+1][(x*3)])){
                    hasAvatar = true;
                } else if (SharedData.gameAnalyzer.getAvatarSprites().contains(level[(y*3)+2][(x*3)])){
                    hasAvatar = true;
                } else if (SharedData.gameAnalyzer.getAvatarSprites().contains(level[(y*3)][(x*3)+1])){
                    hasAvatar = true;
                } else if (SharedData.gameAnalyzer.getAvatarSprites().contains(level[(y*3)][(x*3)+2])){
                    hasAvatar = true;
                } else if (SharedData.gameAnalyzer.getAvatarSprites().contains(level[(y*3)+1][(x*3)+1])){
                    hasAvatar = true;
                } else if (SharedData.gameAnalyzer.getAvatarSprites().contains(level[(y*3)+2][(x*3)+1])){
                    hasAvatar = true;
                } else if (SharedData.gameAnalyzer.getAvatarSprites().contains(level[(y*3)+1][(x*3)+2])){
                    hasAvatar = true;
                } else if (SharedData.gameAnalyzer.getAvatarSprites().contains(level[(y*3)+2][(x*3)+2])){
                    hasAvatar = true;
                }

                int patternIndex = 0;

                if(borderType.equals("none") && hasAvatar == false){
                    Random randGen = new Random();
                    patternIndex = randGen.nextInt(pString.length()/9);

                    while(avatarPatterns.contains(patternIndex)){
                        patternIndex = randGen.nextInt(pString.length()/9);
                    }
                } else if (borderType.equals("none") && hasAvatar){
                    patternIndex = avatarPatterns.get(new Random().nextInt(avatarPatterns.size()));
                } else if (borderType.equals("tl") && hasAvatar){
                    patternIndex = randomSimilarNumber(tlPatterns, avatarPatterns);
                } else if (borderType.equals("tl") && !hasAvatar){
                    patternIndex = randomDissimilarNumber(tlPatterns, avatarPatterns);
                } else if (borderType.equals("tr") && hasAvatar){
                    patternIndex = randomSimilarNumber(trPatterns, avatarPatterns);
                } else if (borderType.equals("tr") && !hasAvatar){
                    patternIndex = randomDissimilarNumber(trPatterns, avatarPatterns);
                } else if (borderType.equals("bl") && hasAvatar){
                    patternIndex = randomSimilarNumber(blPatterns, avatarPatterns);
                } else if (borderType.equals("bl") && !hasAvatar){
                    patternIndex = randomDissimilarNumber(blPatterns, avatarPatterns);
                } else if (borderType.equals("br") && hasAvatar){
                    patternIndex = randomSimilarNumber(brPatterns, avatarPatterns);
                } else if (borderType.equals("br") && !hasAvatar){
                    patternIndex = randomDissimilarNumber(brPatterns, avatarPatterns);
                } else if (borderType.equals("topWall") && hasAvatar){
                    patternIndex = randomSimilarNumber(topWallPatterns, avatarPatterns);
                } else if (borderType.equals("topWall") && !hasAvatar){
                    patternIndex = randomDissimilarNumber(topWallPatterns, avatarPatterns);
                } else if (borderType.equals("bottomWall") && hasAvatar){
                    patternIndex = randomSimilarNumber(bottomWallPatterns, avatarPatterns);
                } else if (borderType.equals("bottomWall") && !hasAvatar){
                    patternIndex = randomDissimilarNumber(bottomWallPatterns, avatarPatterns);
                } else if (borderType.equals("rightWall") && hasAvatar){
                    patternIndex = randomSimilarNumber(rightWallPatterns, avatarPatterns);
                } else if (borderType.equals("rightWall") && !hasAvatar){
                    patternIndex = randomDissimilarNumber(rightWallPatterns, avatarPatterns);
                } else if (borderType.equals("leftWall") && hasAvatar){
                    patternIndex = randomSimilarNumber(leftWallPatterns, avatarPatterns);
                } else if (borderType.equals("leftWall") && !hasAvatar){
                    patternIndex = randomDissimilarNumber(leftWallPatterns, avatarPatterns);
                }

                String patternString = pString.substring(patternIndex * 9, (patternIndex * 9) + 9);

                level[y*3][x*3] = SharedData.gameDescription.getLevelMapping().get(convertCodeToMap(patternString.charAt(0)));
                level[(y*3)][(x*3)+1] = SharedData.gameDescription.getLevelMapping().get(convertCodeToMap(patternString.charAt(1)));
                level[(y*3)][(x*3)+2] = SharedData.gameDescription.getLevelMapping().get(convertCodeToMap(patternString.charAt(2)));
                level[(y*3)+1][x*3] = SharedData.gameDescription.getLevelMapping().get(convertCodeToMap(patternString.charAt(3)));
                level[(y*3)+1][(x*3)+1] = SharedData.gameDescription.getLevelMapping().get(convertCodeToMap(patternString.charAt(4)));
                level[(y*3)+1][(x*3)+2] = SharedData.gameDescription.getLevelMapping().get(convertCodeToMap(patternString.charAt(5)));
                level[(y*3)+2][x*3] = SharedData.gameDescription.getLevelMapping().get(convertCodeToMap(patternString.charAt(6)));
                level[(y*3)+2][(x*3)+1] = SharedData.gameDescription.getLevelMapping().get(convertCodeToMap(patternString.charAt(7)));
                level[(y*3)+2][(x*3)+2] = SharedData.gameDescription.getLevelMapping().get(convertCodeToMap(patternString.charAt(8)));


			} else {
				int x = rand.nextInt(level[0].length / 3);
				int y = rand.nextInt(level.length / 3);

				int x2 = rand.nextInt(level[0].length / 3);
				int y2 = rand.nextInt(level.length / 3);

				if(SharedData.gameAnalyzer.getSolidSprites().size() > 0){
					x = rand.nextInt((level[0].length / 3) - 2) + 1;
					y = rand.nextInt((level.length / 3) - 2) + 1;
					x2 = rand.nextInt((level[0].length / 3) - 2) + 1;
					y2 = rand.nextInt((level.length / 3) - 2) + 1;
				}

				ArrayList<String> topLeft = level[y * 3][x * 3];
				ArrayList<String> topMiddle = level[y * 3][(x * 3) + 1];
				ArrayList<String> topRight = level[y * 3][(x * 3) + 2];
				ArrayList<String> middleLeft = level[(y * 3)+ 1][x * 3];
				ArrayList<String> middleMiddle = level[(y * 3)+ 1][(x * 3) + 1];
				ArrayList<String> middleRight = level[(y * 3)+ 1][(x * 3) + 2];
				ArrayList<String> bottomLeft = level[(y * 3)+ 2][x * 3];
				ArrayList<String> bottomMiddle = level[(y * 3)+ 2][(x * 3) + 1];
				ArrayList<String> bottomRight = level[(y * 3)+ 2][(x * 3) + 2];

				level[y * 3][x * 3] =  level[y2 * 3][x2 * 3];
				level[y * 3][(x * 3) + 1] = level[y2 * 3][(x2 * 3) + 1];
				level[y * 3][(x * 3) + 2] = level[y2 * 3][(x2 * 3) + 2];
				level[(y * 3)+ 1][x * 3] = level[(y2 * 3)+ 1][x2 * 3];
				level[(y * 3)+ 1][(x * 3) + 1] = level[(y2 * 3)+ 1][(x2 * 3) + 1];
				level[(y * 3)+ 1][(x * 3) + 2]= level[(y2 * 3)+ 1][(x2 * 3) + 2];
				level[(y * 3)+ 2][x * 3] = level[(y2 * 3)+ 2][x2 * 3];
				level[(y * 3)+ 2][(x * 3) + 1] = level[(y2 * 3)+ 2][(x2 * 3) + 1];
				level[(y * 3)+ 2][(x * 3) + 2] = level[(y2 * 3)+ 2][(x2 * 3) + 2];

				level[y2 * 3][x2 * 3] =  topLeft;
				level[y2 * 3][(x2 * 3) + 1] = topMiddle;
				level[y2 * 3][(x2 * 3) + 2] = topRight;
				level[(y2 * 3)+ 1][x2 * 3] = middleLeft;
				level[(y2 * 3)+ 1][(x2 * 3) + 1] = middleMiddle;
				level[(y2 * 3)+ 1][(x2 * 3) + 2]= middleRight;
				level[(y2 * 3)+ 2][x2 * 3] = bottomLeft;
				level[(y2 * 3)+ 2][(x2 * 3) + 1] = bottomMiddle;
				level[(y2 * 3)+ 2][(x2 * 3) + 2] = bottomRight;
			}
		}
		FixLevel();
	}


	/**
	 * get the free positions in the current level (that doesnt contain solid or object from the input list)
	 * @param sprites	list of sprites names to test them
	 * @return			list of all free position points
	 */
	private ArrayList<SpritePointData> getFreePositions(ArrayList<String> sprites){
//        System.out.println("getFreePositions");
		ArrayList<SpritePointData> positions = new ArrayList<SpritePointData>();

		for(int y = 0; y < level.length; y++){
			for(int x = 0; x < level[y].length; x++){
				ArrayList<String> tileSprites = level[y][x];
				boolean found = false;
				for(String stype:tileSprites){
					found = found || sprites.contains(stype);
					found = found || SharedData.gameAnalyzer.getSolidSprites().contains(stype);
				}

				if(!found){
					positions.add(new SpritePointData("", x, y));
				}
			}
		}

		return positions;
	}


	/**
	 * get all the positions of all the sprites found in the input list
	 * @param sprites	list of sprites
	 * @return			list of points that contains the sprites in the list
	 */
	private ArrayList<SpritePointData> getPositions(ArrayList<String> sprites){
//        System.out.println("getPositions");
		ArrayList<SpritePointData> positions = new ArrayList<SpritePointData>();

		for(int y = 0; y < level.length; y++){
			for(int x = 0; x < level[y].length; x++){
				ArrayList<String> tileSprites = level[y][x];
				for(String stype:tileSprites){
					for(String s:sprites){
						if(s.equals(stype)){
							positions.add(new SpritePointData(stype, x, y));
						}
					}
				}
			}
		}

		return positions;
	}


	/**
	 * Fix the player in the level (there must be only one player no more or less)
	 */
	private void FixPlayer(){
//        System.out.println("FixPlayer");
		//get the list of all the avatar names
		ArrayList<SpriteData> avatar = SharedData.gameDescription.getAvatar();
		ArrayList<String> avatarNames = new ArrayList<String>();
		for(SpriteData a:avatar){
			avatarNames.add(a.name);
		}

		//get list of all the avatar positions in the level
		ArrayList<SpritePointData> avatarPositions = getPositions(avatarNames);

		// if not avatar insert a new one
		if(avatarPositions.size() == 0){
			ArrayList<SpritePointData> freePositions = getFreePositions(avatarNames);

			int index = SharedData.random.nextInt(freePositions.size());
			level[freePositions.get(index).y][freePositions.get(index).x].add(avatarNames.get(SharedData.random.nextInt(avatarNames.size())));
		}

		//if there is more than one avatar remove all of them except one
		else if(avatarPositions.size() > 1){
			int notDelete = SharedData.random.nextInt(avatarPositions.size());
			int index = 0;
			for(SpritePointData point:avatarPositions){
				if(index != notDelete){
					level[point.y][point.x].remove(point.name);
				}
				index += 1;
			}
		}
	}


	/**
	 * Fix the level by fixing the player number
	 */
	private void FixLevel(){
		FixPlayer();
	}


	/**
	 * get the current used level mapping to parse the level string
	 * @return	Level mapping object that can help to construct the
	 * 			level string and parse the level string
	 */
	public LevelMapping getLevelMapping(){
//        System.out.println("getLevelMapping");
		LevelMapping levelMapping = new LevelMapping(SharedData.gameDescription);
		levelMapping.clearLevelMapping();
		char c = 'a';
		for(int y = 0; y < level.length; y++){
			for(int x = 0; x < level[y].length; x++){
				if(levelMapping.getCharacter(level[y][x]) == null){
					levelMapping.addCharacterMapping(c, level[y][x]);
					c += 1;
				}
			}
		}

		return levelMapping;
	}


	/**
	 * get the current level string
	 * @param levelMapping	level mapping object to help constructing the string
	 * @return				string of letters defined in the level mapping
	 * 						that represent the level
	 */
	public String getLevelString(LevelMapping levelMapping){
//        System.out.println("getLevelString");
		String levelString = "";
		for(int y = 0; y < level.length; y++){
			for(int x = 0; x < level[y].length; x++){
				levelString += levelMapping.getCharacter(level[y][x]);
			}
			levelString += "\n";
		}

		levelString = levelString.substring(0, levelString.length() - 1);

		return levelString;
	}


	/**
	 * get the percentage of the level covered by objects excluding the borders
	 * @return	percentage with respect to the size of the level
	 */
	private double getCoverPercentage(){
//        System.out.println("getCoverPercentage");
		int objects = 0;
		int borders = 0;
		if(SharedData.gameAnalyzer.getSolidSprites().size() > 0){
			borders = 1;
		}
		for (int y = borders; y < level.length - borders; y++) {
			for (int x = borders; x < level[y].length - borders; x++) {
				objects += Math.min(1, level[y][x].size());
			}
		}

		return 1.0 * objects / (level.length * level[0].length);
	}


	/**
	 * get game state observation for the current level
	 * @return	StateObservation for the current level
	 */
	private StateObservation getStateObservation(){
//        System.out.println("getStateObservation");
		if(stateObs != null){
			return stateObs;
		}

		LevelMapping levelMapping = getLevelMapping();
		String levelString = getLevelString(levelMapping);
		stateObs = SharedData.gameDescription.testLevel(levelString, levelMapping.getCharMapping());
		return stateObs;
	}


	/**
	 * calculate the number of objects in the level by sprite names
	 * @return	a hashmap of the number of each object based on its name
	 */
	private HashMap<String, Integer> calculateNumberOfObjects(){
//        System.out.println("calculateNumberOfObjects");
		HashMap<String, Integer> objects = new HashMap<String, Integer>();
		ArrayList<SpriteData> allSprites = SharedData.gameDescription.getAllSpriteData();


		//initialize the hashmap with all the sprite names
		for(SpriteData sprite:allSprites){
			objects.put(sprite.name, 0);
		}


		//modify the hashmap to reflect the number of objects found in this level
		for(int y = 0; y < level.length; y++){
			for(int x = 0; x < level[y].length; x++){
				ArrayList<String> sprites = level[y][x];
				for(String stype:sprites){
					if(objects.containsKey(stype)){
						objects.put(stype, objects.get(stype) + 1);
					}
					else{
						objects.put(stype, 1);
					}
				}
			}
		}

		return objects;
	}


	/**
	 * Get fitness value for the current score difference between
	 * the best player and the naive player
	 * @param scoreDiff	difference between the best player score and the naive player score
	 * @param maxScore	maximum score required to approach it
	 * @return			value between 0 to 1 which is almost 1 near the maxScore.
	 */
	private double getGameScore(double scoreDiff, double maxScore){
//        System.out.println("getGameScore");
		if(maxScore == 0){
			return 1;
		}
		if(scoreDiff <= 0){
			return 0;
		}
		double result = (3 * scoreDiff / maxScore);
		return 2 / (1 + Math.exp(-result)) - 1;
	}

	/**
	 * check if the player death terminates the game
	 * player ID used is 0, default for single player games.
	 * @return	true if the player death terminates the game and false otherwise
	 */
	private boolean isPlayerCauseDeath(){

		for(TerminationData t: SharedData.gameDescription.getTerminationConditions()){
			String[] winners = t.win.split(",");
			Boolean win = Boolean.parseBoolean(winners[0]);

			for(String s:t.sprites){
				if(!win & SharedData.gameDescription.getAvatar().contains(s)){
					return true;
				}
			}

		}

		return false;
	}


	/**
	 * get a fitness value for the number of unique rules satisfied during playing the game
	 * @param gameState		the current level after playing using the best player
	 * @param minUniqueRule		minimum amount of rules needed to reach 1
	 * @return			near 1 when its near to minUniqueRule
	 */
	private double getUniqueRuleScore(StateObservation gameState, double minUniqueRule){
//        System.out.println("getUniqueRuleScore");
		double unique = 0;
		HashMap<Integer, Boolean> uniqueEvents = new HashMap<Integer, Boolean>();
		for(Event e:gameState.getEventsHistory()){
			int code = e.activeTypeId + 10000 * e.passiveTypeId;
			if(!uniqueEvents.containsKey(code)){
				unique += 1;
				uniqueEvents.put(code, true);
			}
		}


		/**
		 * Remove the player death from the unique rules
		 */
		if(isPlayerCauseDeath() && gameState.getGameWinner() == WINNER.PLAYER_LOSES){
			unique -= 1;
		}

		return 2 / (1 + Math.exp(-3 * unique / minUniqueRule)) - 1;
	}


	/**
	 * Play the current level using the naive player
	 * @param stateObs	the current stateObservation object that represent the level
	 * @param steps		the maximum amount of steps that it shouldn't exceed it
	 * @param agent		current agent to play the level
	 * @return			the number of steps that the agent stops playing after (<= steps)
	 */
	private int getNaivePlayerResult(StateObservation stateObs, int steps, AbstractPlayer agent){
//        System.out.println("getnaiveplayerresult");
		int i =0;
		for(i=0;i<steps;i++){
			if(stateObs.isGameOver()){
				break;
			}
			Types.ACTIONS bestAction = agent.act(stateObs, null);
			stateObs.advance(bestAction);
		}

		return i;
	}


	/**
	 * Calculate the current fitness of the chromosome
	 * @param time	amount of time to evaluate the chromosome
	 * @return		current fitness of the chromosome
	 */
	public ArrayList<Double> calculateFitness(long time){
//        System.out.println("calculateFitness");
		if(!calculated){
			calculated = true;
			StateObservation stateObs = getStateObservation();


			//Play the game using the best agent
			StepController stepAgent = new StepController(automatedAgent, SharedData.EVALUATION_STEP_TIME);
			ElapsedCpuTimer elapsedTimer = new ElapsedCpuTimer();
			elapsedTimer.setMaxTimeMillis(time);
			stepAgent.playGame(stateObs.copy(), elapsedTimer);

			StateObservation bestState = stepAgent.getFinalState();
			ArrayList<Types.ACTIONS> bestSol = stepAgent.getSolution();

			StateObservation doNothingState = null;
			int doNothingLength = Integer.MAX_VALUE;
			//playing the game using the donothing agent and naive agent
			for(int i = 0; i< SharedData.REPETITION_AMOUNT; i++){
				StateObservation tempState = stateObs.copy();
				int temp = getNaivePlayerResult(tempState, bestSol.size(), doNothingAgent);
				if(temp < doNothingLength){
					doNothingLength = temp;
					doNothingState = tempState;
				}
			}
			double coverPercentage = getCoverPercentage();

			//calculate the maxScore need to be satisfied based on the difference
			//between the score of different collectible objects
			double maxScore = 0;
			if(SharedData.gameAnalyzer.getMinScoreUnit() > 0){
				double numberOfUnits = SharedData.gameAnalyzer.getMaxScoreUnit() / (SharedData.MAX_SCORE_PERCENTAGE * SharedData.gameAnalyzer.getMinScoreUnit());
				maxScore = numberOfUnits * SharedData.gameAnalyzer.getMinScoreUnit();
			}


			//calculate the constrain fitness by applying all different constraints
			HashMap<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("solutionLength", bestSol.size());
			parameters.put("minSolutionLength", SharedData.MIN_SOLUTION_LENGTH);
			parameters.put("doNothingSteps", doNothingLength);
			parameters.put("doNothingState", doNothingState.getGameWinner());
			parameters.put("bestPlayer", bestState.getGameWinner());
			parameters.put("minDoNothingSteps", SharedData.MIN_DOTHING_STEPS);
			parameters.put("coverPercentage", coverPercentage);
			parameters.put("minCoverPercentage", SharedData.MIN_COVER_PERCENTAGE);
			parameters.put("maxCoverPercentage", SharedData.MAX_COVER_PERCENTAGE);
			parameters.put("numOfObjects", calculateNumberOfObjects());
			parameters.put("gameAnalyzer", SharedData.gameAnalyzer);
			parameters.put("gameDescription", SharedData.gameDescription);

			CombinedConstraints constraint = new CombinedConstraints();
			constraint.addConstraints(new String[]{"SolutionLengthConstraint", "DeathConstraint",

					"CoverPercentageConstraint", "SpriteNumberConstraint", "GoalConstraint", "AvatarNumberConstraint", "WinConstraint"});
			constraint.setParameters(parameters);
			constrainFitness = constraint.checkConstraint();

			System.out.println("SolutionLength:" + bestSol.size() + " doNothingSteps:" + doNothingLength + " coverPercentage:" + coverPercentage + " bestPlayer:" + bestState.getGameWinner());


			//calculate the fitness if it satisfied all the constraints
			if(constrainFitness >= 1){
				StateObservation naiveState = null;
				for(int i = 0; i< SharedData.REPETITION_AMOUNT; i++){
					StateObservation tempState = stateObs.copy();
					getNaivePlayerResult(tempState, bestSol.size(), naiveAgent);
					if(naiveState == null || tempState.getGameScore() > naiveState.getGameScore()){
						naiveState = tempState;
					}
				}
				
				double scoreDiffScore = getGameScore(bestState.getGameScore() - naiveState.getGameScore(), maxScore);
				double ruleScore = getUniqueRuleScore(bestState, SharedData.MIN_UNIQUE_RULE_NUMBER);
				
				fitness.add(scoreDiffScore);
				fitness.add(ruleScore);
			}

			this.automatedAgent = null;
			this.naiveAgent = null;
			this.stateObs = null;
		}
		
		return fitness;
	}

	/**
	 * Get the current chromosome fitness
	 * @return	array contains all fitness values
	 */
	public ArrayList<Double> getFitness(){
		return fitness;
	}
	

	/**
	 * Get the average value of the fitness
	 * @return	average value of the fitness array
	 */
	public double getCombinedFitness(){
		double result = 0;
		for(double v: this.fitness){
			result += v;
		}
		return result / this.fitness.size();
	}
	
	/**
	 * Get constraint fitness for infeasible chromosome
	 * @return	1 if its feasible and less than 1 if not
	 */
	public double getConstrainFitness(){
		return constrainFitness;
	}
	

	/**
	 * helpful data structure to hold information about certain points in the level
	 * @author AhmedKhalifa
	 */
	public class SpritePointData{
		public String name;
		public int x;
		public int y;
		
		public SpritePointData(String name, int x, int y){
			this.name = name;
			this.x = x;
			this.y = y;
		}
	}


	/**
	 * Compare two chromosome with each other based on their 
	 * constrained fitness and normal fitness
	 */
	@Override
	public int compareTo(Chromosome o) {
		if(this.constrainFitness < 1 || o.constrainFitness < 1){
			if(this.constrainFitness < o.constrainFitness){
				return 1;
			}
			if(this.constrainFitness > o.constrainFitness){
				return -1;
			}
			return 0;
		}
		
		double firstFitness = 0;
		double secondFitness = 0;
		for(int i=0; i<this.fitness.size(); i++){
			firstFitness += this.fitness.get(i);
			secondFitness += o.fitness.get(i);
		}
		
		if(firstFitness > secondFitness){
			return -1;
		}
		
		if(firstFitness < secondFitness){
			return 1;
		}
		
		return 0;
	}
}
