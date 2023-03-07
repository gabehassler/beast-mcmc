package dr.evomodel.territorydesign;

import dr.evolution.tree.NodeRef;
import dr.evomodel.tree.TreeModel;
import dr.inference.model.*;
import dr.xml.*;

import java.util.ArrayList;

public class ContiguityStatistic extends Statistic.Abstract {

    private final TaxaAdjacencyMatrix adjacencyMatrix;
    private final Parameter threshold;

    public ContiguityStatistic(TaxaAdjacencyMatrix adjacencyMatrix, Parameter threshold) {
        this.adjacencyMatrix = adjacencyMatrix;
        this.threshold = threshold;
    }

    private int countDiscontiguities() {
        TreeModel treeModel = adjacencyMatrix.getTreeModel();
        AdjacencyAccumulator accumulator = countDiscontiguities(treeModel, treeModel.getRoot());
        return accumulator.nDiscongiguities;
    }
    private AdjacencyAccumulator countDiscontiguities(TreeModel treeModel, NodeRef node) {
        ArrayList<Integer> descendants = new ArrayList<>();
        if (treeModel.getChildCount(node) == 0) {
            descendants.add(node.getNumber());
            return new AdjacencyAccumulator(0, descendants);
        }
        AdjacencyAccumulator a0 = countDiscontiguities(treeModel, treeModel.getChild(node, 0));
        AdjacencyAccumulator a1 = countDiscontiguities(treeModel, treeModel.getChild(node, 1));

        descendants.addAll(a0.descendants);
        descendants.addAll(a1.descendants);
        int n = a0.nDiscongiguities + a1.nDiscongiguities;

        if (treeModel.getNodeHeight(node) < threshold.getParameterValue(0)) {
            for (int i0 = 0; i0 < a0.descendants.size(); i0++) {
                int taxon0 = descendants.get(i0);
                for (int i1 = 0; i1 < a1.descendants.size(); i1++) {
                    int taxon1 = descendants.get(i1);
                    if (adjacencyMatrix.areAdjacent(taxon0, taxon1)) {
                        return new AdjacencyAccumulator(n, descendants);
                    }
                }
            }
            return new AdjacencyAccumulator(n + 1, descendants);
        }

        return new AdjacencyAccumulator(n, descendants);
    }

    @Override
    public int getDimension() {
        return 1;
    }

    @Override
    public double getStatisticValue(int dim) {
        return countDiscontiguities();
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

    public static AbstractXMLObjectParser PARSER = new AbstractXMLObjectParser() {
        @Override
        public Object parseXMLObject(XMLObject xo) throws XMLParseException {
            TaxaAdjacencyMatrix adjacencyMatrix = (TaxaAdjacencyMatrix) xo.getChild(TaxaAdjacencyMatrix.class);
            Parameter threshold = (Parameter) xo.getChild(THRESHOLD).getChild(Parameter.class);
            return new ContiguityStatistic(adjacencyMatrix, threshold);
        }

        @Override
        public XMLSyntaxRule[] getSyntaxRules() {
            return new XMLSyntaxRule[]{
                    new ElementRule(TaxaAdjacencyMatrix.class),
                    new ElementRule(THRESHOLD, new XMLSyntaxRule[] {
                            new ElementRule(Parameter.class)
                    })
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
            return "contiguityStatistic";
        }
    };
}
