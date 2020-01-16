package dr.inference.distribution;

public class EmpiricalDistributionData {
    final double[] values;
    final double[] density;
    final boolean densityInLogSpace;

    public EmpiricalDistributionData(double[] values, double[] density, boolean densityInLogSpace) {
        this.values = values;
        this.density = density;
        this.densityInLogSpace = densityInLogSpace;
    }
}
