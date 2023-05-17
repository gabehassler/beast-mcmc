package dr.inference.operators;

import dr.inference.hmc.GradientWrtParameterProvider;
import dr.inference.model.Parameter;
import dr.math.MathUtils;
import dr.xml.*;

import java.util.Arrays;

public class GradientDescentOperator extends SimpleMCMCOperator implements GibbsOperator {

    private final GradientWrtParameterProvider provider;
    private final Parameter parameter;
    private final double tolerance = 1e-5;
    private double stepSize = 0.1;
    private final int maxSteps = 10000;
    private final int coordInterval = 100;
    private final int[] inds;
//    private double coordStepSize = 0.1;

    public static final String GRADIENT_DESCENT_OPERATOR = "gradientDescentOperator";

    private static final Boolean DEBUG = false;

    public GradientDescentOperator(GradientWrtParameterProvider provider, boolean immediate, int[] inds) {
        this.provider = provider;
        this.parameter = provider.getParameter();
        this.inds = inds;
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
        double maxDiff = MathUtils.absMax(gradient, inds);
        double logLikelihood = provider.getLikelihood().getLogLikelihood();
        int steps = 1;
        while (steps < maxSteps && maxDiff > tolerance) {

            if (steps % coordInterval == 0) {
                System.out.println(logLikelihood);
                System.out.println(maxDiff);
                gradient = provider.getGradientLogDensity();
                int ind = MathUtils.findAbsMax(gradient, inds);
                boolean isPos = gradient[ind] > 0;
                double coordStepSize = stepSize;
                int coordSteps = 0;
                while (coordSteps < 100 && Math.abs(gradient[ind]) > tolerance) {
                    coordSteps++;
                    if (DEBUG) {
//                        System.out.println("GradientDescentOperator: " + parameter.getParameterName());
                        System.out.println("\tvalue: " + parameter.getParameterValue(ind));
                        System.out.println("\tgradient: " + gradient[ind]);
                        System.out.println("\tlogDensity: " + provider.getLikelihood().getLogLikelihood());
                        System.out.println("\tsteps: " + steps);
                        System.out.println("\tstepSize: " + coordStepSize);
                    }

                    double originalValue = parameter.getParameterValue(ind);
                    parameter.setParameterValue(ind, originalValue + (isPos ? coordStepSize : -coordStepSize));
//                    parameter.fireParameterChangedEvent();
                    gradient = provider.getGradientLogDensity();
                    if (gradient[ind] > 0 != isPos) {
                        parameter.setParameterValue(ind, originalValue);
                        coordStepSize /= 2;
                    } else {
                        coordStepSize *= 1.05;
                    }


                }
            }

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
            if (newLogLikelihood == logLikelihood) {
                break;
            }
            if (newLogLikelihood > logLikelihood) stepSize *= 1.05;
            while (newLogLikelihood < logLikelihood) {
                stepSize /= 2;
                for (int i = 0; i < gradient.length; i++) {
                    parameter.setParameterValueQuietly(i, parameter.getParameterValue(i) - stepSize * gradient[i]);
                }
                parameter.fireParameterChangedEvent();
                newLogLikelihood = provider.getLikelihood().getLogLikelihood();
            }

            logLikelihood = newLogLikelihood;

            gradient = provider.getGradientLogDensity();
            maxDiff = MathUtils.absMax(gradient, inds);

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
    private static final String INDICES = "indices";

    public static AbstractXMLObjectParser PARSER = new AbstractXMLObjectParser() {
        @Override
        public Object parseXMLObject(XMLObject xo) throws XMLParseException {
            final GradientWrtParameterProvider provider = (GradientWrtParameterProvider) xo.getChild(GradientWrtParameterProvider.class);
            final boolean immediate = xo.getAttribute(IMMEDIATE, false);
            final int[] inds;
            if (xo.hasAttribute(INDICES)) {
                inds = xo.getIntegerArrayAttribute(INDICES);
                for (int i = 0; i < inds.length; i++) {
                    inds[i] = inds[i] - 1;
                }
            } else {
                inds = new int[provider.getDimension()];
                for (int i = 0; i < inds.length; i++) {
                    inds[i] = i;
                }
            }

            return new GradientDescentOperator(provider, immediate, inds);
        }

        @Override
        public XMLSyntaxRule[] getSyntaxRules() {
            return new XMLSyntaxRule[]{
                    AttributeRule.newBooleanRule(IMMEDIATE, true),
                    AttributeRule.newIntegerArrayRule(INDICES, true),
                    new ElementRule(GradientWrtParameterProvider.class)
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
