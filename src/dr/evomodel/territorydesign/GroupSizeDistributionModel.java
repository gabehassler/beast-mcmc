package dr.evomodel.territorydesign;

import dr.inference.distribution.ParametricDistributionModel;
import dr.inference.model.AbstractModel;
import dr.inference.model.Model;
import dr.inference.model.Parameter;
import dr.inference.model.Variable;
import dr.math.UnivariateFunction;
import dr.xml.*;

public class GroupSizeDistributionModel extends AbstractModel implements ParametricDistributionModel {

    private final double plateauStart;
    private final double plateauFinish;
    private final double alpha;
    private final double beta;

    /**
     * @param name Model Name
     */
    public GroupSizeDistributionModel(String name, double start, double finish, double alpha, double beta) {
        super(name);

        this.alpha = alpha;
        this.beta = beta;
        this.plateauStart = start;
        this.plateauFinish = finish;
    }

    private double normalizingConstant() {
        return plateauStart / (alpha + 1) + plateauFinish - plateauStart + 1 / beta;
    }

    @Override
    public Variable<Double> getLocationVariable() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public double logPdf(double[] x) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    protected void handleModelChangedEvent(Model model, Object object, int index) {
        // do nothing
    }

    @Override
    protected void storeState() {
        // do nothing
    }

    @Override
    protected void restoreState() {
        // do nothing
    }

    @Override
    protected void acceptState() {
        // do nothing
    }

    @Override
    protected void handleVariableChangedEvent(Variable variable, int index, Parameter.ChangeType type) {
        // do nothing
    }

    @Override
    public double pdf(double x) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public double logPdf(double x) {
        double logC = Math.log(normalizingConstant());
        if (x <= 0) {
            return Double.NEGATIVE_INFINITY;
        } else if (x < plateauStart) {
            return Math.log(alpha) * (Math.log(x) - Math.log(plateauStart)) - logC;
        } else if (x < plateauFinish) {
            return -logC;
        } else {
            return -beta * (x - plateauFinish) - logC;
        }
    }

    @Override
    public double cdf(double x) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public double quantile(double y) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public double mean() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public double variance() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public UnivariateFunction getProbabilityDensityFunction() {
        throw new UnsupportedOperationException("Not implemented");
    }

    private static final String START = "start";
    private static final String END = "end";
    private static final String ALPHA = "alpha";
    private static final String BETA = "beta";
    private static final String GROUP_DISTRIBUTION = "groupSizeDistribution";


    public static AbstractXMLObjectParser PARSER = new AbstractXMLObjectParser() {
        @Override
        public Object parseXMLObject(XMLObject xo) throws XMLParseException {
            double start = xo.getDoubleAttribute(START);
            double end = xo.getDoubleAttribute(END);
            double alpha = xo.getDoubleAttribute(ALPHA);
            double beta = xo.getDoubleAttribute(BETA);
            return new GroupSizeDistributionModel(GROUP_DISTRIBUTION, start, end, alpha, beta);
        }

        @Override
        public XMLSyntaxRule[] getSyntaxRules() {
            return new XMLSyntaxRule[] {
                    AttributeRule.newDoubleRule(START),
                    AttributeRule.newDoubleRule(END),
                    AttributeRule.newDoubleRule(ALPHA),
                    AttributeRule.newDoubleRule(BETA)
            };
        }

        @Override
        public String getParserDescription() {
            return "Test";
        }

        @Override
        public Class getReturnType() {
            return GroupSizeDistributionModel.class;
        }

        @Override
        public String getParserName() {
            return GROUP_DISTRIBUTION;
        }
    };
}
