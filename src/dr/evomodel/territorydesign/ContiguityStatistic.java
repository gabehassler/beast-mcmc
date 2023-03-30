package dr.evomodel.territorydesign;

import dr.evolution.tree.NodeRef;
import dr.evomodel.tree.TreeModel;
import dr.inference.model.*;
import dr.math.distributions.Distribution;
import dr.xml.*;

import java.util.ArrayList;

public class ContiguityStatistic extends AbstractModelLikelihood {

    private final TaxaAdjacencyMatrix adjacencyMatrix;
    private final Parameter threshold;
    private int nDiscongiguities = 0;
    //    private double[] areas;
    //    private double[] perimeters;
    private double[] groupSizes;
    //    private final Distribution groupSizeDistribution;
    private final Distribution perimeterDistribution;
    private final double discontiguityPenalty;
    private final boolean useThreshold = false;

    public ContiguityStatistic(TaxaAdjacencyMatrix adjacencyMatrix,
                               Parameter threshold,
//                               Distribution groupSizeDistribution,
                               Distribution perimeterDistribution,
                               double discontiguityPenalty) {
        super(CONTIGUITY_LIKELIHOOD);
        this.adjacencyMatrix = adjacencyMatrix;
        this.threshold = threshold;
//        this.groupSizeDistribution = groupSizeDistribution;
        this.perimeterDistribution = perimeterDistribution;
        this.discontiguityPenalty = discontiguityPenalty;
    }

    private void update() {
        TreeModel treeModel = adjacencyMatrix.getTreeModel();
        ArrayList<AdjacencyAccumulator> accumulators = countDiscontiguities(treeModel, treeModel.getRoot());
        int n = accumulators.size();
//        this.perimeters = new double[n];
//        this.areas = new double[n];
        this.groupSizes = new double[n];
        this.nDiscongiguities = 0;

        for (int i = 0; i < n; i++) {
            AdjacencyAccumulator accumulator = accumulators.get(i);
            ArrayList<Integer> taxa = accumulator.descendants;
            int ni = taxa.size();
            groupSizes[i] = ni;
            this.nDiscongiguities += accumulator.nDiscongiguities;
//            for (int j = 0; j < ni; j++) {
//                areas[i] += adjacencyMatrix.getArea(j);
//                perimeters[i] += adjacencyMatrix.getPerimeter(j);
//                for (int k = (j + 1); k < ni; k++) {
//                    perimeters[i] -= 2 * adjacencyMatrix.getSharedPerimeter(j, k);
//                }
//            }
        }

    }

    private ArrayList<AdjacencyAccumulator> countDiscontiguities(TreeModel treeModel, NodeRef node) {
        ArrayList<Integer> descendants = new ArrayList<>();
        ArrayList<AdjacencyAccumulator> accumulators = new ArrayList<>();

        if (treeModel.getChildCount(node) == 0) {
            descendants.add(node.getNumber());
            accumulators.add(new AdjacencyAccumulator(0, descendants));
            return accumulators;
        }
        ArrayList<AdjacencyAccumulator> as0 = countDiscontiguities(treeModel, treeModel.getChild(node, 0));
        ArrayList<AdjacencyAccumulator> as1 = countDiscontiguities(treeModel, treeModel.getChild(node, 1));

        if (useThreshold && treeModel.getNodeHeight(node) > threshold.getParameterValue(0)) {
            accumulators.addAll(as0);
            accumulators.addAll(as1);
            return accumulators;
        }

        AdjacencyAccumulator a0 = as0.get(0);
        AdjacencyAccumulator a1 = as1.get(0);

        descendants.addAll(a0.descendants);
        descendants.addAll(a1.descendants);
        int n = a0.nDiscongiguities + a1.nDiscongiguities;

        for (int i0 = 0; i0 < a0.descendants.size(); i0++) {
            int taxon0 = a0.descendants.get(i0);
            for (int i1 = 0; i1 < a1.descendants.size(); i1++) {
                int taxon1 = a1.descendants.get(i1);
                if (adjacencyMatrix.areAdjacent(taxon0, taxon1)) {
                    accumulators.add(new AdjacencyAccumulator(n, descendants));
                    return accumulators;
                }
            }
        }
        accumulators.add(new AdjacencyAccumulator(n + 1, descendants));
        return accumulators;
    }

    @Override
    protected void handleModelChangedEvent(Model model, Object object, int index) {
        // do nothing
    }

    @Override
    protected void handleVariableChangedEvent(Variable variable, int index, Parameter.ChangeType type) {
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
    public Model getModel() {
        return this;
    }

    @Override
    public double getLogLikelihood() {
        update();
        double ll = -discontiguityPenalty * nDiscongiguities;
//        for (int i = 0; i < areas.length; i++) {
//            double ratio = perimeters[i] * perimeters[i] / areas[i];
//            ll += perimeterDistribution.logPdf(ratio) * groupSizes[i];
//        }
        return ll;
    }

    @Override
    public void makeDirty() {
        // do nothing
    }

    private static class AdjacencyAccumulator {
        public final int nDiscongiguities;
        //        public final ArrayList<Integer> adjacentNodes;
        public final ArrayList<Integer> descendants;

        AdjacencyAccumulator(int nDiscongiguities, ArrayList<Integer> descendants) {
            this.nDiscongiguities = nDiscongiguities;
            this.descendants = descendants;
        }
    }


    private static String THRESHOLD = "threshold";
    private static String PENALTY = "discontiguityPenalty";
    private static String RATIO_DISTRIBUTION = "perimeterAreaRatioDistribution";
    private static String CONTIGUITY_LIKELIHOOD = "contiguityLikelihood";

    public static AbstractXMLObjectParser PARSER = new AbstractXMLObjectParser() {
        @Override
        public Object parseXMLObject(XMLObject xo) throws XMLParseException {
            TaxaAdjacencyMatrix adjacencyMatrix = (TaxaAdjacencyMatrix) xo.getChild(TaxaAdjacencyMatrix.class);
            Parameter threshold = (Parameter) xo.getChild(THRESHOLD).getChild(Parameter.class);
            Distribution ratioDistribution = (Distribution) xo.getChild(RATIO_DISTRIBUTION).getChild(Distribution.class);
            double penalty = xo.getDoubleAttribute(PENALTY);
            return new ContiguityStatistic(adjacencyMatrix, threshold, ratioDistribution, penalty);
        }

        @Override
        public XMLSyntaxRule[] getSyntaxRules() {
            return new XMLSyntaxRule[]{
                    new ElementRule(TaxaAdjacencyMatrix.class),
                    new ElementRule(THRESHOLD, new XMLSyntaxRule[]{
                            new ElementRule(Parameter.class)
                    }),
                    new ElementRule(RATIO_DISTRIBUTION, new XMLSyntaxRule[]{
                            new ElementRule(Distribution.class)
                    }),
                    AttributeRule.newDoubleRule(PENALTY)
            };
        }

        @Override
        public String getParserDescription() {
            return "Calculates the number of discontiguous branches in a tree";
        }

        @Override
        public Class getReturnType() {
            return ContiguityStatistic.class;
        }

        @Override
        public String getParserName() {
            return CONTIGUITY_LIKELIHOOD;
        }
    };
}
