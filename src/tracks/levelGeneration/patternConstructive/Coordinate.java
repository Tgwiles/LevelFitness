package tracks.levelGeneration.patternConstructive;

public class Coordinate {

    int xPos;
    int yPos;

    Coordinate(int x, int y){
        xPos = x;
        yPos = y;
    }

    public boolean equals(Coordinate other){
        return xPos == other.xPos && yPos == other.yPos;
    }

    public boolean adjacent(Coordinate other){
        return (xPos == other.xPos-1 && yPos == other.yPos) || (xPos == other.xPos+1 && yPos == other.yPos) || (xPos == other.xPos && yPos == other.yPos-1) || (xPos == other.xPos && yPos == other.yPos+1);
    }

    public String toString(){
        return "(" + xPos + ", " + yPos + ")";
    }
}