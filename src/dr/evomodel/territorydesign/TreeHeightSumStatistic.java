package dr.evomodel.territorydesign;

import dr.evolution.tree.NodeRef;
import dr.evomodel.tree.TreeModel;
import dr.inference.model.*;
import dr.xml.*;

import java.util.ArrayList;

public class TreeHeightSumStatistic extends Statistic.Abstract {
    private final TreeModel treeModel;
    private final Parameter minimumSize;
    private final Parameter taxonValues;
    private final double[] groupCounts;

    public TreeHeightSumStatistic(TreeModel treeModel, Parameter taxonValues, Parameter minimumSize) {
        super();
        this.treeModel = treeModel;
        this.taxonValues = taxonValues;
        this.minimumSize = minimumSize;
        this.groupCounts = new double[treeModel.getTaxonCount()];
    }


    private static class Accumulator {
        public final int nChildren;
        public final double sum;

        Accumulator(int nChildren, double sum) {
            this.nChildren = nChildren;
            this.sum = sum;
        }
    }


    private void updateGroupCounts() {
        ArrayList<Accumulator> accumulators = updateGroupCounts(treeModel.getRoot());
        int offset = 0;
        for (int i = 0; i < accumulators.size(); i++) {
            Accumulator accumulator = accumulators.get(i);
            for (int j = offset; j < accumulator.nChildren + offset; j++) {
                groupCounts[j] = accumulator.sum;
            }
            offset += accumulator.nChildren;
        }
    }


    private Accumulator mergeAccumulators(ArrayList<Accumulator> accumulators) {
        int nChildren = 0;
        double sum = 0;
        for (Accumulator a : accumulators) {
            nChildren += a.nChildren;
            sum += a.sum;
        }
        return new Accumulator(nChildren, sum);
    }

    private ArrayList<Accumulator> updateGroupCounts(NodeRef node) {
        ArrayList<Accumulator> accumulators = new ArrayList<>();
        if (treeModel.getChildCount(node) == 0) {
            accumulators.add(new Accumulator(1, taxonValues.getParameterValue(node.getNumber())));
            return accumulators;
        }

        NodeRef c0 = treeModel.getChild(node, 0);
        NodeRef c1 = treeModel.getChild(node, 1);
        ArrayList<Accumulator> a0 = updateGroupCounts(c0);
        ArrayList<Accumulator> a1 = updateGroupCounts(c1);
        accumulators.addAll(a0);
        accumulators.addAll(a1);

        boolean merge = false;
        if (a0.size() == 1 && a0.get(0).sum < minimumSize.getParameterValue(0)) {
            merge = true;
        }
        if (a1.size() == 1 && a1.get(0).sum < minimumSize.getParameterValue(0)) {
            merge = true;
        }
        if (merge) {
            Accumulator merged = mergeAccumulators(accumulators);
            accumulators.clear();
            accumulators.add(merged);
        }
//
//        if (treeModel.getNodeHeight(node) < height.getParameterValue(0)) {
//            Accumulator b0 = a0.get(0);
//            Accumulator b1 = a1.get(0);
//            accumulators.add(new Accumulator(b0.nChildren + b1.nChildren, b0.sum + b1.sum));
//            return accumulators;
//        }
//
//        accumulators.addAll(a0);
//        accumulators.addAll(a1);
        return accumulators;
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

    private static final String MIN_SIZE = "minimumSize";
    private static final String TRAIT = "traitParameter";

    public static AbstractXMLObjectParser PARSER = new AbstractXMLObjectParser() {
        @Override
        public Object parseXMLObject(XMLObject xo) throws XMLParseException {
            TreeModel treeModel = (TreeModel) xo.getChild(TreeModel.class);
            Parameter minSize = (Parameter) xo.getChild(MIN_SIZE).getChild(Parameter.class);
            Parameter trait = (Parameter) xo.getChild(TRAIT).getChild(Parameter.class);

            return new TreeHeightSumStatistic(treeModel, trait, minSize);
        }

        @Override
        public XMLSyntaxRule[] getSyntaxRules() {
            return new XMLSyntaxRule[]{
                    new ElementRule(TreeModel.class),
                    new ElementRule(MIN_SIZE, new XMLSyntaxRule[]{
                            new ElementRule(Parameter.class)
                    }),
                    new ElementRule(TRAIT, new XMLSyntaxRule[]{
                            new ElementRule(Parameter.class)
                    })
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
