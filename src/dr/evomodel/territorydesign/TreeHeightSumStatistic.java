package dr.evomodel.territorydesign;


import dr.inference.model.*;
import dr.xml.*;

import java.util.ArrayList;

public class TreeHeightSumStatistic extends Statistic.Abstract {

    private final TaxonGroupsProvider provider;
    private final double maxSize;


    public TreeHeightSumStatistic(TaxonGroupsProvider provider, double maxSize) {
        super();
        this.provider = provider;
        this.maxSize = maxSize;
    }


    private double getExcessGroupCounts() {
        ArrayList<TaxonGroupsProvider.Accumulator> accumulators = provider.getGroups();
        double sum = 0;
        for (int i = 0; i < accumulators.size(); i++) {
            TaxonGroupsProvider.Accumulator accumulator = accumulators.get(i);
            if (accumulator.sum > maxSize) {
                sum += accumulator.sum - maxSize;
            }
        }
        return sum;
    }


    @Override
    public int getDimension() {
        return 1;
    }

    @Override
    public double getStatisticValue(int dim) {
        return getExcessGroupCounts();
    }


    private static final String GROUP_SUM_STATISTIC = "groupSumStatistic";
    private static final String MAX_SIZE = "maxSize";

    public static AbstractXMLObjectParser PARSER = new AbstractXMLObjectParser() {
        @Override
        public Object parseXMLObject(XMLObject xo) throws XMLParseException {
            TaxonGroupsProvider taxonGroupsProvider = (TaxonGroupsProvider) xo.getChild(TaxonGroupsProvider.class);
            double maxSize = xo.getDoubleAttribute(MAX_SIZE);
            return new TreeHeightSumStatistic(taxonGroupsProvider, maxSize);
        }

        @Override
        public XMLSyntaxRule[] getSyntaxRules() {
            return new XMLSyntaxRule[]{
                    new ElementRule(TaxonGroupsProvider.class),
                    AttributeRule.newDoubleRule(MAX_SIZE)
            };
        }

        @Override
        public String getParserDescription() {
            return "Statistic that captures the number of individuals in the same group as a taxon if the tree were " +
                    "cut at a certain height";
        }

        @Override
        public Class getReturnType() {
            return TreeHeightSumStatistic.class;
        }

        @Override
        public String getParserName() {
            return GROUP_SUM_STATISTIC;
        }
    };
}
