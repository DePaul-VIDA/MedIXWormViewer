
package object;

/**
 *
 * @author MSHAO1
 */


public abstract class FilePathConfiguration {
    private String directoryPath = "";
    private String masterFilePath = "";
    private String kylePath = "";
    private String ronPath = "";
    private String logPath = "";
    private String outputPath = "";
    private String inputPath = "";
    private int offset = 7;
    private String catagoryName = "";
    
    public void setAllPaths(String dPath, String cName){
        this.directoryPath = dPath;
        this.catagoryName = cName;
        this.masterFilePath = this.directoryPath + "\\data\\masterFile.csv";
        this.kylePath = this.directoryPath + "\\data";
        this.ronPath = this.directoryPath + "\\matlab";
        this.logPath = this.directoryPath + "\\log";
        this.outputPath = this.directoryPath + "\\dbtables";
        this.inputPath = this.directoryPath + "\\input";
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public String getMasterFilePath() {
        return masterFilePath;
    }

    public String getKylePath() {
        return kylePath;
    }

    public String getRonPath() {
        return ronPath;
    }

    public String getLogPath() {
        return logPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public String getInputPath() {
        return inputPath;
    }

    public int getOffset() {
        return offset;
    }

    public String getCatagoryName() {
        return catagoryName;
    }
}
