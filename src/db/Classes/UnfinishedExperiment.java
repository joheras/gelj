package db.Classes;

/**
 *
 * @author joheras
 */
public class UnfinishedExperiment {
    
    private int id;
    private String name;
    private int step;
    private Study study;

    public UnfinishedExperiment(int id, String name, int step, Study study) {
        this.id = id;
        this.name = name;
        this.study = study;
        this.step = step;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "UnfinishedExperiment{" + "name=" + name + '}';
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getStep() {
        return step;
    }
    
    
    

}
