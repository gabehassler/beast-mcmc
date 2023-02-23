package dr.evomodel.territorydesign;

import dr.evolution.util.Taxon;
import dr.evomodel.tree.TreeModel;
import dr.xml.*;

import java.util.List;

public class TaxaAdjacencyMatrix {

    private final TreeModel treeModel;
    private final int[][] adjacency;

    public TaxaAdjacencyMatrix(TreeModel treeModel, int[][] adjacency) {
        this.treeModel = treeModel;
        this.adjacency = adjacency;
    }

    public boolean areAdjacent(int i, int j) {
        return adjacency[i][j] == 1;
    }

    public TreeModel getTreeModel() {
        return treeModel;
    }

    private static final String ADJACENCY = "adjacency";


    public static AbstractXMLObjectParser PARSER = new AbstractXMLObjectParser() {
        @Override
        public Object parseXMLObject(XMLObject xo) throws XMLParseException {
            TreeModel treeModel = (TreeModel) xo.getChild(TreeModel.class);
            int nTaxa = treeModel.getTaxonCount();
            int[][] adjacencyMatrix = new int[nTaxa][nTaxa];
            for (XMLObject cxo : xo.getAllChildren(ADJACENCY)) {
                List<Taxon> taxa = cxo.getAllChildren(Taxon.class);
                int i = treeModel.getTaxonIndex(taxa.get(0));
                int j = treeModel.getTaxonIndex(taxa.get(1));
                adjacencyMatrix[i][j] = 1;
                adjacencyMatrix[j][i] = 1;
            }
            return new TaxaAdjacencyMatrix(treeModel, adjacencyMatrix);
        }

        @Override
        public XMLSyntaxRule[] getSyntaxRules() {
            return new XMLSyntaxRule[]{
                    new ElementRule(TreeModel.class),
                    new ElementRule(ADJACENCY, new XMLSyntaxRule[]{
                            new ElementRule(Taxon.class, 2, 2)
                    }, 1, Integer.MAX_VALUE)

            };
        }

        @Override
        public String getParserDescription() {
            return "Keeps track of which taxa are 'adjacent' to each other";
        }

        @Override
        public Class getReturnType() {
            return TaxaAdjacencyMatrix.class;
        }

        @Override
        public String getParserName() {
            return "adjacentTaxa";
        }
    };
}
