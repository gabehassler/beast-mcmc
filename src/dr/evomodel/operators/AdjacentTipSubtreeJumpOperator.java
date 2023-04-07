package dr.evomodel.operators;

import dr.evolution.tree.NodeRef;
import dr.evolution.tree.Tree;
import dr.evomodel.territorydesign.TaxaAdjacencyMatrix;
import dr.evomodel.tree.TreeModel;
import dr.inference.operators.AdaptationMode;
import dr.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class AdjacentTipSubtreeJumpOperator extends SubtreeJumpOperator {
    private final TaxaAdjacencyMatrix adjacency;


    /**
     * Constructor
     *
     * @param tree
     * @param weight
     * @param size    : the variance of a half normal used to compute distance weights (as a rule, larger size, bolder moves)
     * @param accP
     * @param uniform
     * @param mode
     */
    public AdjacentTipSubtreeJumpOperator(TreeModel tree, TaxaAdjacencyMatrix adjacency, double weight, double size, double accP, boolean uniform, AdaptationMode mode) {
        super(tree, weight, size, accP, uniform, mode);
        this.adjacency = adjacency;
    }

    @Override
    protected NodeRef selectTargetNode() {

        final NodeRef root = tree.getRoot();

        NodeRef i;
        do {
            // 1. choose a random child node avoiding root or child of root
            i = tree.getNode(MathUtils.nextInt(tree.getExternalNodeCount()));

        } while (tree.getParent(i) == root);
        return i;
    }


    @Override
    protected List<NodeRef> getIntersectingEdges(Tree tree, double height, NodeRef movedNode) {
        List<NodeRef> possibleEdges = super.getIntersectingEdges(tree, height, movedNode);
        ArrayList<NodeRef> adjacentEdges = new ArrayList<>();
        for (NodeRef node : possibleEdges) {
            if (isAdjacent(tree, node, movedNode)) {
                adjacentEdges.add(node);
            }
        }
        return adjacentEdges;
    }

    private boolean isAdjacent(Tree tree, NodeRef testNode, NodeRef targetNode) {
        if (tree.isExternal(testNode)) {
            if (adjacency.areAdjacent(testNode.getNumber(), targetNode.getNumber())) {
                return true;
            }
            return false;
        }

        if (isAdjacent(tree, tree.getChild(testNode, 0), targetNode)) return true;
        if (isAdjacent(tree, tree.getChild(testNode, 1), targetNode)) return true;
        return false;
    }


}
