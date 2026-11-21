package db.Classes;

/**
 *
 * @author joheras
 */
public class LaneDBwithsimilarity {
    
    
    private LaneDB lane;
    private double similarity;

    public LaneDBwithsimilarity(LaneDB lane, double similarity) {
        this.lane = lane;
        this.similarity = similarity;
    }

    public LaneDB getLane() {
        return lane;
    }

    public void setLane(LaneDB lane) {
        this.lane = lane;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }
    

}
