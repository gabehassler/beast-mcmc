package dr.evomodel.territorydesign;


import dr.inference.model.*;
import dr.xml.*;

import java.util.ArrayList;

public class TreeHeightSumStatistic extends Statistic.Abstract {

    private final double[] groupCounts;
    private final TaxonGroupsProvider provider;


    public TreeHeightSumStatistic(TaxonGroupsProvider provider) {
        super();
        this.provider = provider;
        this.groupCounts = new double[provider.getMaximumNumGroups()];
    }


    private void updateGroupCounts() {
        ArrayList<TaxonGroupsProvider.Accumulator> accumulators = provider.getGroups();
//        int offset = 0;
        for (int i = 0; i < accumulators.size(); i++) {
            TaxonGroupsProvider.Accumulator accumulator = accumulators.get(i);
            groupCounts[i] = accumulator.sum;
//            for (int j = offset; j < accumulator.nChildren + offset; j++) {
//                groupCounts[j] = accumulator.sum;
//            }
//            offset += accumulator.nChildren;
        }
        for (int i = accumulators.size(); i < groupCounts.length; i++) {
            groupCounts[i] = 0;
        }
    }


    @Override
    public int getDimension() {
        return groupCounts.length;
    }

    @Override
    public double getStatisticValue(int dim) {
        if (dim == 0) {
            updateGroupCounts();
        }
        return groupCounts[dim];
    }

    public static AbstractXMLObjectParser PARSER = new AbstractXMLObjectParser() {
        @Override
        public Object parseXMLObject(XMLObject xo) throws XMLParseException {
            TaxonGroupsProvider taxonGroupsProvider = (TaxonGroupsProvider) xo.getChild(TaxonGroupsProvider.class);
            return new TreeHeightSumStatistic(taxonGroupsProvider);
        }

        @Override
        public XMLSyntaxRule[] getSyntaxRules() {
            return new XMLSyntaxRule[]{
                    new ElementRule(TaxonGroupsProvider.class)
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
            return "groupSumStatistic";
        }
    };
}
