package dr.evomodel.territorydesign;

import dr.evolution.tree.NodeRef;
import dr.evomodel.tree.TreeModel;
import dr.inference.model.*;
import dr.xml.*;

import java.util.ArrayList;

public class TaxonGroupsProvider extends AbstractModel {
    private final TreeModel treeModel;
    private final Parameter minimumSize;
    private final Parameter taxonValues;
    private ArrayList<Accumulator> accumulators;
    private ArrayList<Accumulator> oldAccumulators;
    private boolean needsUpdate = true;

    public TaxonGroupsProvider(TreeModel treeModel, Parameter taxonValues, Parameter minimumSize) {
        super(TAXON_GROUPS_PROVIDER);
        this.treeModel = treeModel;
        this.taxonValues = taxonValues;
        this.minimumSize = minimumSize;
        addModel(treeModel);
        addVariable(taxonValues);
        addVariable(minimumSize);
    }

    @Override
    protected void handleModelChangedEvent(Model model, Object object, int index) {
        needsUpdate = true;
    }

    @Override
    protected void handleVariableChangedEvent(Variable variable, int index, Parameter.ChangeType type) {
        needsUpdate = true;
    }

    @Override
    protected void storeState() {
        oldAccumulators = accumulators;
    }

    @Override
    protected void restoreState() {
        accumulators = oldAccumulators;
    }

    @Override
    protected void acceptState() {
        // do nothing
    }

    public ArrayList<Accumulator> getGroups() {
        if (needsUpdate) {
            updateAccumulators();
            needsUpdate = false;
        }
        return accumulators;
    }

    public int getMaximumNumGroups() {
        return treeModel.getTaxonCount();
    }


    public static class Accumulator {
        public final int nChildren;
        public final double sum;
        public final ArrayList<Integer> descendants;
        public final int parentNode;

        Accumulator(int nChildren, double sum, ArrayList<Integer> descendants, int parentNode) {
            this.nChildren = nChildren;
            this.sum = sum;
            this.descendants = descendants;
            this.parentNode = parentNode;
        }
    }

    private void updateAccumulators() {
        accumulators = updateGroupCounts(treeModel.getRoot());
    }


    private Accumulator mergeAccumulators(ArrayList<Accumulator> accumulators) {
        if (accumulators.size() != 2) {
            throw new RuntimeException("Expected 2 accumulators, got " + accumulators.size());
        }

        int nChildren = 0;
        double sum = 0;
        for (Accumulator a : accumulators) {
            nChildren += a.nChildren;
            sum += a.sum;
        }

        NodeRef parent = treeModel.getNode(accumulators.get(0).parentNode);
        NodeRef parent2 = treeModel.getNode(accumulators.get(1).parentNode);
        if (parent != parent2) {
            throw new RuntimeException("Expected parent nodes to be the same");
        }

        ArrayList<Integer> descendants = new ArrayList<>();
        for (Accumulator a : accumulators) {
            descendants.addAll(a.descendants);
        }


        return new Accumulator(nChildren, sum, descendants, parent.getNumber());
    }

    private ArrayList<Accumulator> updateGroupCounts(NodeRef node) {
        ArrayList<Accumulator> accumulators = new ArrayList<>();
        if (treeModel.getChildCount(node) == 0) {
            int nodeNum = node.getNumber();
            accumulators.add(new Accumulator(1,
                    taxonValues.getParameterValue(nodeNum),
                    new ArrayList<>(nodeNum),
                    nodeNum));
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


    private static final String MIN_SIZE = "minimumSize";
    private static final String TRAIT = "traitParameter";
    private static final String TAXON_GROUPS_PROVIDER = "taxonGroupsProvider";

    public static AbstractXMLObjectParser PARSER = new AbstractXMLObjectParser() {
        @Override
        public Object parseXMLObject(XMLObject xo) throws XMLParseException {
            TreeModel treeModel = (TreeModel) xo.getChild(TreeModel.class);
            Parameter minSize = (Parameter) xo.getChild(MIN_SIZE).getChild(Parameter.class);
            Parameter trait = (Parameter) xo.getChild(TRAIT).getChild(Parameter.class);

            return new TaxonGroupsProvider(treeModel, trait, minSize);
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
            return "Groups taxa by some minimum size";
        }

        @Override
        public Class getReturnType() {
            return TaxonGroupsProvider.class;
        }

        @Override
        public String getParserName() {
            return TAXON_GROUPS_PROVIDER;
        }
    };
}
