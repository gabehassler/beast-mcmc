package dr.evomodel.territorydesign;

import dr.inference.model.Model;
import dr.inference.model.ModelListener;
import dr.inference.model.Parameter;
import dr.inference.model.Statistic;
import dr.xml.*;

import java.util.ArrayList;

public class PerimeterAreaRatioProvider extends Statistic.Abstract implements ModelListener {

    private final TaxonGroupsProvider taxonGroups;
    private final TaxaAdjacencyMatrix adjacency;
    private final Parameter perimeter;
    private final Parameter area;
    private double meanRatio;

    private boolean needsUpdate = true;

    PerimeterAreaRatioProvider(TaxonGroupsProvider taxonGroups, TaxaAdjacencyMatrix adjacency, Parameter perimeter, Parameter area) {
        this.taxonGroups = taxonGroups;
        this.adjacency = adjacency;
        this.perimeter = perimeter;
        this.area = area;
        taxonGroups.addModelListener(this);
    }

    @Override
    public int getDimension() {
        return 1;
    }

    @Override
    public double getStatisticValue(int dim) {
        if (needsUpdate) {
            updateMeanRatio();
            needsUpdate = false;
        }

        return meanRatio;
    }

    private void updateMeanRatio() {
        ArrayList<TaxonGroupsProvider.Accumulator> groups = taxonGroups.getGroups();

        double sum = 0;
        int n = groups.size();

        for (int i = 0; i < n; i++) {
            TaxonGroupsProvider.Accumulator group = groups.get(i);
            ArrayList<Integer> taxa = group.descendants;
            int m = taxa.size();
            double perimeter = 0;
            double area = 0;
            for (int j = 0; j < m; j++) {
                area += this.area.getParameterValue(taxa.get(j));
                perimeter += this.perimeter.getParameterValue(taxa.get(j));
                for (int k = j + 1; k < m; k++) {
                    perimeter -= 2 * adjacency.getSharedPerimeter(taxa.get(j), taxa.get(k));
                }
            }
            sum += perimeter * perimeter / area;
        }

        meanRatio = sum / n;
    }

    @Override
    public void modelChangedEvent(Model model, Object object, int index) {
        needsUpdate = true;
    }

    @Override
    public void modelRestored(Model model) {
        needsUpdate = true; // TODO: not sure if this is necessary, but better safe than sorry
    }

    public static AbstractXMLObjectParser PARSER = new AbstractXMLObjectParser() {

        private static final String AREA = "area";
        private static final String PERIMETER = "perimeter";
        @Override
        public Object parseXMLObject(XMLObject xo) throws XMLParseException {
            TaxonGroupsProvider taxonGroups = (TaxonGroupsProvider) xo.getChild(TaxonGroupsProvider.class);
            TaxaAdjacencyMatrix adjacency = (TaxaAdjacencyMatrix) xo.getChild(TaxaAdjacencyMatrix.class);
            Parameter perimeter = (Parameter) xo.getElementFirstChild(PERIMETER);
            Parameter area = (Parameter) xo.getElementFirstChild(AREA);

            return new PerimeterAreaRatioProvider(taxonGroups, adjacency, perimeter, area);
        }

        @Override
        public XMLSyntaxRule[] getSyntaxRules() {
            return new XMLSyntaxRule[]{
                    new ElementRule(TaxonGroupsProvider.class),
                    new ElementRule(TaxaAdjacencyMatrix.class),
                    new ElementRule(PERIMETER, Parameter.class),
                    new ElementRule(AREA, Parameter.class)
            };
        }

        @Override
        public String getParserDescription() {
            return "Calculates the mean perimeter-area ratio of the a group of taxa.";
        }

        @Override
        public Class getReturnType() {
            return PerimeterAreaRatioProvider.class;
        }

        @Override
        public String getParserName() {
            return "perimeterAreaRatio";
        }
    };
}
