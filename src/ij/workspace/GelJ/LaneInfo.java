package ij.workspace.GelJ;

/**
 *
 * @author joheras
 */
public class LaneInfo {
    
    private String genus;
    private String species;
    private String country;
    private String MLST;
    private String StrainNumber;

    public LaneInfo(String genus, String species, String country, String MLST, String StrainNumber) {
        this.genus = genus;
        this.species = species;
        this.country = country;
        this.MLST = MLST;
        this.StrainNumber = StrainNumber;
    }
    
    public LaneInfo() {
        this.genus = "";
        this.species = "";
        this.country = "";
        this.MLST = "";
        this.StrainNumber = "";
    }

    public String getGenus() {
        return genus;
    }

    public void setGenus(String genus) {
        this.genus = genus;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getMLST() {
        return MLST;
    }

    public void setMLST(String MLST) {
        this.MLST = MLST;
    }

    public String getStrainNumber() {
        return StrainNumber;
    }

    public void setStrainNumber(String StrainNumber) {
        this.StrainNumber = StrainNumber;
    }
    
    
    
    

}
