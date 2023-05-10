package dr.inference.operators;

import dr.inference.hmc.GradientWrtParameterProvider;
import dr.inference.model.Parameter;
import dr.math.MathUtils;
import dr.xml.*;

import java.util.Arrays;

public class GradientDescentOperator extends SimpleMCMCOperator implements GibbsOperator {

    private final GradientWrtParameterProvider provider;
    private final Parameter parameter;
    private final double tolerance = 1e-3;
    private double stepSize = 0.1;
    private final int maxSteps = 100000;

    public static final String GRADIENT_DESCENT_OPERATOR = "gradientDescentOperator";

    private static final Boolean DEBUG = true;

    public GradientDescentOperator(GradientWrtParameterProvider provider, boolean immediate) {
        this.provider = provider;
        this.parameter = provider.getParameter();
        if (immediate) {
            doOperation();
        }
    }

    @Override
    public String getOperatorName() {
        return GRADIENT_DESCENT_OPERATOR;
    }

    @Override
    public double doOperation() {
        double[] gradient = provider.getGradientLogDensity();
        double maxDiff = MathUtils.absMax(gradient);
        double logLikelihood = provider.getLikelihood().getLogLikelihood();
        int steps = 0;
        while (steps < maxSteps && maxDiff > tolerance) {

            if (DEBUG) {
                System.out.println("GradientDescentOperator: " + parameter.getParameterName());
                System.out.println("\tvalue: " + Arrays.toString(parameter.getParameterValues()));
                System.out.println("\tgradient: " + Arrays.toString(gradient));
                System.out.println("\tlogDensity: " + provider.getLikelihood().getLogLikelihood());
                System.out.println("\tgradMax: " + maxDiff);
                System.out.println("\tsteps: " + steps);
                System.out.println("\tstepSize: " + stepSize);
            }

            for (int i = 0; i < gradient.length; i++) {
                parameter.setParameterValueQuietly(i, parameter.getParameterValue(i) + stepSize * gradient[i]);
            }
            parameter.fireParameterChangedEvent();
            double newLogLikelihood = provider.getLikelihood().getLogLikelihood();
            System.out.println(newLogLikelihood);
            while (newLogLikelihood < logLikelihood) {
                stepSize /= 2;
                for (int i = 0; i < gradient.length; i++) {
                    parameter.setParameterValueQuietly(i, parameter.getParameterValue(i) - stepSize * gradient[i]);
                }
                parameter.fireParameterChangedEvent();
                newLogLikelihood = provider.getLikelihood().getLogLikelihood();
                System.out.println(newLogLikelihood);
            }

            logLikelihood = newLogLikelihood;

            gradient = provider.getGradientLogDensity();
            maxDiff = MathUtils.absMax(gradient);

            steps++;
        }

        if (DEBUG) {
            System.out.println("GradientDescentOperator: " + parameter.getParameterName());
            System.out.println("\tvalue: " + Arrays.toString(parameter.getParameterValues()));
            System.out.println("\tgradient: " + Arrays.toString(gradient));
            System.out.println("\tlogDensity: " + provider.getLikelihood().getLogLikelihood());
            System.out.println("\tgradMax: " + maxDiff);
        }

        return 0;
    }

    private static final String IMMEDIATE = "immediate";

    public static AbstractXMLObjectParser PARSER = new AbstractXMLObjectParser() {
        @Override
        public Object parseXMLObject(XMLObject xo) throws XMLParseException {
            final GradientWrtParameterProvider provider = (GradientWrtParameterProvider) xo.getChild(GradientWrtParameterProvider.class);
            final boolean immediate = xo.getAttribute(IMMEDIATE, false);
            return new GradientDescentOperator(provider, immediate);
        }

        @Override
        public XMLSyntaxRule[] getSyntaxRules() {
            return new XMLSyntaxRule[]{
                    AttributeRule.newBooleanRule(IMMEDIATE, true),

            };
        }

        @Override
        public String getParserDescription() {
            return "Maximizes the (negative log-) likelihood via gradient descent";
        }

        @Override
        public Class getReturnType() {
            return GradientDescentOperator.class;
        }

        @Override
        public String getParserName() {
            return GRADIENT_DESCENT_OPERATOR;
        }
    };
}
