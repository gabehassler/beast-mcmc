package dr.evomodelxml.treedatalikelihood;

import dr.evolution.alignment.PatternList;
import dr.evomodel.treedatalikelihood.TreeDataLikelihood;
import dr.evomodel.treedatalikelihood.HDPPolyaUrn;
import dr.evomodel.treedatalikelihood.GenPolyaUrnProcessPrior;
import dr.evomodel.treedatalikelihood.HDPDataSquashingOperator;
import dr.inference.operators.MCMCOperator;
import dr.xml.*;

public class HDPDataSquashingOperatorParser extends AbstractXMLObjectParser {

    public static final String HDP_DATA_SQUASHING_OPERATOR= "hdpDataSquashingOperator";
    public static final String DATA_LOG_LIKELIHOOD = "dataLogLikelihood";
    public static final String MH_STEPS = "mhSteps";
    public static final String CATEGORIES = "categories";
    public static final String CYCLICAL = "cyclical";
    public static final String DIST_METHOD = "distMethod";
    public static final String FIXED_NUMBER = "fixedNumber";
    public static final String STRICT_CUTOFF = "strictCutoff";
    public static final String EPSILON = "epsilon";
    public static final String SAMPLE_PROPORTION = "sampleProportion";
    public static final String MAX_NEW_CAT = "maxNewCat";

    @Override
    public Object parseXMLObject(XMLObject xo) throws XMLParseException {

        HDPPolyaUrn hdp = (HDPPolyaUrn) xo.getChild(HDPPolyaUrn.class);

        PatternList patternList = (PatternList) xo.getChild(PatternList.class);

        TreeDataLikelihood tdl = (TreeDataLikelihood) xo.getElementFirstChild(DATA_LOG_LIKELIHOOD);

        boolean cyclical = xo.getBooleanAttribute(CYCLICAL);

        int distMethod = xo.getIntegerAttribute(DIST_METHOD);

        if(distMethod != 1 && distMethod != 2){
            throw new XMLParseException("Must specify valid distMethod: 1 or 2");
        }

        int fixedNumber = 0;
        if(xo.hasAttribute(FIXED_NUMBER)){
            fixedNumber = xo.getIntegerAttribute(FIXED_NUMBER);
        }

        boolean strictCutoff = false;
        if(xo.hasAttribute(STRICT_CUTOFF)) {
            strictCutoff = xo.getBooleanAttribute(STRICT_CUTOFF);
        }

        //if(fixedNumber > 0){
        //    strictCutoff = true;
        //}

        double epsilon = xo.getDoubleAttribute(EPSILON);

        if(epsilon >= 1 || epsilon <= 0){
            throw new XMLParseException("epsilon must be between grater than 0 and less than or equal to 1");
        }

        double sampleProportion = 0.25;
        if(xo.hasAttribute(SAMPLE_PROPORTION)) {
            sampleProportion = xo.getDoubleAttribute(SAMPLE_PROPORTION);
        }

        int maxNewCat = 1;
        if(xo.hasAttribute(MAX_NEW_CAT)){
            maxNewCat = xo.getIntegerAttribute(MAX_NEW_CAT);
        }

        if(sampleProportion >= 1 || sampleProportion <= 0){
            throw new XMLParseException("sampleProportion must be between greater than 0 and less than or equal to 1");
        }

        int M = xo.getIntegerAttribute(MH_STEPS);

        final double weight = xo.getDoubleAttribute(MCMCOperator.WEIGHT);

        return new dr.evomodel.treedatalikelihood.HDPDataSquashingOperator(hdp,
                tdl,
                patternList,
                M,
                weight,
                cyclical,
                distMethod,
                epsilon,
                sampleProportion,
                fixedNumber,
                strictCutoff,
                maxNewCat
        );

    }// END: parseXMLObject

    @Override
    public XMLSyntaxRule[] getSyntaxRules() {
        return new XMLSyntaxRule[] {
                new ElementRule(GenPolyaUrnProcessPrior.class, false),
                AttributeRule.newDoubleRule(MCMCOperator.WEIGHT),
                AttributeRule.newIntegerRule(DIST_METHOD, false),
                AttributeRule.newBooleanRule(STRICT_CUTOFF, true),
                AttributeRule.newBooleanRule(CYCLICAL, false),
                AttributeRule.newDoubleRule(EPSILON, false),
                AttributeRule.newDoubleRule(SAMPLE_PROPORTION, true),
                AttributeRule.newIntegerRule(FIXED_NUMBER, true),
                AttributeRule.newIntegerRule(MAX_NEW_CAT, true)
        };

    }// END: getSyntaxRules

    @Override
    public String getParserName() {
        return HDP_DATA_SQUASHING_OPERATOR;
    }

    @Override
    public String getParserDescription() {
        return HDP_DATA_SQUASHING_OPERATOR;
    }

    @Override
    public Class getReturnType() {
        return HDPDataSquashingOperator.class;
    }

}// END: class