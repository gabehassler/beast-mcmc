package dr.evomodelxml.operators;

import dr.evomodel.operators.AdjacentTipSubtreeJumpOperator;
import dr.evomodel.operators.SubtreeJumpOperator;
import dr.evomodel.territorydesign.TaxaAdjacencyMatrix;
import dr.evomodel.tree.TreeModel;
import dr.inference.operators.AdaptationMode;
import dr.xml.*;

public class AdjacentTipSubtreeJumpOperatorParser extends SubtreeJumpOperatorParser {

    public static final String ADJACENT_TIP_SUBTREE_JUMP = "adjacentTipSubtreeJump";
    private TaxaAdjacencyMatrix adjcency;

    @Override
    public Object parseXMLObject(XMLObject xo) throws XMLParseException {
        TaxaAdjacencyMatrix adjacency = (TaxaAdjacencyMatrix) xo.getChild(TaxaAdjacencyMatrix.class);
        this.adjcency = adjacency;
        return super.parseXMLObject(xo);
    }

    @Override
    protected SubtreeJumpOperator factory(TreeModel treeModel, double weight, double size, double targetAcceptance, boolean uniform, AdaptationMode mode) {
        return new AdjacentTipSubtreeJumpOperator(treeModel, adjcency, weight, size, targetAcceptance, uniform, mode);
    }

    @Override
    public String getParserName() {
        return ADJACENT_TIP_SUBTREE_JUMP;
    }

    @Override
    public String getParserDescription() {
        return "An operator that jumps a subtree to another edge at the same height while maintaining contiguity  " +
                "between taxa.";
    }

    @Override
    public Class getReturnType() {
        return AdjacentTipSubtreeJumpOperator.class;
    }


    @Override
    public XMLSyntaxRule[] getSyntaxRules() {
        XMLSyntaxRule[] oldRules = super.getSyntaxRules();
        XMLSyntaxRule[] newRules = new XMLSyntaxRule[oldRules.length + 1];
        System.arraycopy(oldRules, 0, newRules, 1, oldRules.length);
        newRules[0] = new ElementRule(TaxaAdjacencyMatrix.class);
        return newRules;
    }
}
