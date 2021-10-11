package object;

/**
 *
 * @author mshao1
 */
public class FiveNumberSummary {

    private double median;
    private double firstQuartile;
    private double thirdQuartile;
    private double max;
    private double min;
    private String name;

    public FiveNumberSummary(String name) {
        this.name = name;
    }

    public FiveNumberSummary(String name, double median, double firstQuartile, double thirdQuartile, double max, double min) {
        this.median = median;
        this.firstQuartile = firstQuartile;
        this.thirdQuartile = thirdQuartile;
        this.max = max;
        this.min = min;
        this.name = name;
    }

    public double getMedian() {
        return median;
    }

    public void setMedian(double median) {
        this.median = median;
    }

    public double getFirstQuartile() {
        return firstQuartile;
    }

    public void setFirstQuartile(double firstQuartile) {
        this.firstQuartile = firstQuartile;
    }

    public double getThirdQuartile() {
        return thirdQuartile;
    }

    public void setThirdQuartile(double thirdQuartile) {
        this.thirdQuartile = thirdQuartile;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
