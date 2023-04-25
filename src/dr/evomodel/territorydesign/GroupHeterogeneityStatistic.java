package dr.evomodel.territorydesign;

import dr.inference.model.MatrixParameterInterface;
import dr.inference.model.Statistic;
import dr.xml.*;

import java.util.ArrayList;

public class GroupHeterogeneityStatistic extends Statistic.Abstract {

    private final MatrixParameterInterface traits;
    private final TaxonGroupsProvider taxonGroups;

    public GroupHeterogeneityStatistic(MatrixParameterInterface traits, TaxonGroupsProvider taxonGroups) {
        this.traits = traits;
        this.taxonGroups = taxonGroups;
//        super("groupHeterogeneity");
    }

    @Override
    public int getDimension() {
        return 1;
    }

    @Override
    public double getStatisticValue(int dim) {
        ArrayList<TaxonGroupsProvider.Accumulator> groups = taxonGroups.getGroups();
        int n = groups.size();
        int p = traits.getRowDimension();
        double sum = 0;

        for (int i = 0; i < n; i++) {
            TaxonGroupsProvider.Accumulator group = groups.get(i);
            ArrayList<Integer> taxa = group.descendants;
            int m = taxa.size();
            for (int j = 0; j < p; j++) {
                double mean = 0;
                for (int k = 0; k < m; k++) {
                    mean += traits.getParameterValue(j, taxa.get(k));
                }
                mean /= m;
                double sse = 0;
                for (int k = 0; k < m; k++) {
                    double diff = traits.getParameterValue(j, taxa.get(k)) - mean;
                    sse += diff * diff;
                }
                sum += sse;
            }
        }
        return sum / traits.getColumnDimension();
    }


    private static final String GROUP_HETEROGENEITY_STATISTIC = "groupHeterogeneityStatistic";


    public static AbstractXMLObjectParser PARSER = new AbstractXMLObjectParser() {
        @Override
        public Object parseXMLObject(XMLObject xo) throws XMLParseException {
            TaxonGroupsProvider taxonGroups = (TaxonGroupsProvider) xo.getChild(TaxonGroupsProvider.class);
            MatrixParameterInterface traits = (MatrixParameterInterface) xo.getChild(MatrixParameterInterface.class);
            return new GroupHeterogeneityStatistic(traits, taxonGroups);
        }

        @Override
        public XMLSyntaxRule[] getSyntaxRules() {
            return new XMLSyntaxRule[]{
                    new ElementRule(TaxonGroupsProvider.class),
                    new ElementRule(MatrixParameterInterface.class),
            };
        }

        @Override
        public String getParserDescription() {
            return "Sum of squared differences between group mean and individual values";
        }

        @Override
        public Class getReturnType() {
            return GroupHeterogeneityStatistic.class;
        }

        @Override
        public String getParserName() {
            return GROUP_HETEROGENEITY_STATISTIC;
        }
    };

}
