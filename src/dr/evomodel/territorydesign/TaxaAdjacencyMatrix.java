package dr.evomodel.territorydesign;

import dr.evolution.util.Taxon;
import dr.evomodel.tree.TreeModel;
import dr.xml.*;

import java.util.List;

public class TaxaAdjacencyMatrix {

    private final TreeModel treeModel;
    private final int[][] adjacency;
    private final double[][] sharedBorder;
//    private final double[] perimeter;
//    private final double[] area;

    public TaxaAdjacencyMatrix(TreeModel treeModel,
                               int[][] adjacency,
                               double[][] sharedBorder) {
        this.treeModel = treeModel;
        this.adjacency = adjacency;
        this.sharedBorder = sharedBorder;
//        this.perimeter = perimeter;
//        this.area = area;
    }

    public boolean areAdjacent(int i, int j) {
        return adjacency[i][j] == 1;
    }
//    public double getPerimeter(int i) {
//        return perimeter[i];
//    }
//
//    public double getArea(int i) {
//        return area[i];
//    }

    public double getSharedPerimeter(int i, int j) {
        return sharedBorder[i][j];
    }

    public TreeModel getTreeModel() {
        return treeModel;
    }

    private static final String ADJACENCY = "adjacency";
    private static final String SHARED_BORDER = "sharedPerimeter";
    //    private static final String PERIMETER = "perimeter";
//    private static final String AREA = "area";
    private static final String TAXON_DATA = "taxonData";

    public static AbstractXMLObjectParser PARSER = new AbstractXMLObjectParser() {
        @Override
        public Object parseXMLObject(XMLObject xo) throws XMLParseException {
            TreeModel treeModel = (TreeModel) xo.getChild(TreeModel.class);
            int nTaxa = treeModel.getTaxonCount();
            int[][] adjacencyMatrix = new int[nTaxa][nTaxa];
            double[][] sharedBorder = new double[nTaxa][nTaxa];
//            double[] area = new double[nTaxa];
//            double[] perimeter = new double[nTaxa];
            for (XMLObject cxo : xo.getAllChildren(ADJACENCY)) {
                List<XMLObject> taxa = cxo.getAllChildren(TAXON_DATA);
                Taxon taxon0 = (Taxon) taxa.get(0).getChild(Taxon.class);
                Taxon taxon1 = (Taxon) taxa.get(1).getChild(Taxon.class);

                int i = treeModel.getTaxonIndex(taxon0);
                int j = treeModel.getTaxonIndex(taxon1);
                adjacencyMatrix[i][j] = 1;
                adjacencyMatrix[j][i] = 1;
                double sb = cxo.getAttribute(SHARED_BORDER, 0.0);
                sharedBorder[i][j] = sb;
                sharedBorder[j][i] = sb;

//                area[i] = taxa.get(0).getAttribute(AREA, 0.0);
//                area[j] = taxa.get(1).getAttribute(AREA, 0.0);
//                perimeter[i] = taxa.get(0).getAttribute(PERIMETER, 0.0);
//                perimeter[j] = taxa.get(1).getAttribute(PERIMETER, 0.0);

            }
            return new TaxaAdjacencyMatrix(treeModel, adjacencyMatrix, sharedBorder);
        }

        @Override
        public XMLSyntaxRule[] getSyntaxRules() {
            return new XMLSyntaxRule[]{
                    new ElementRule(TreeModel.class),
                    new ElementRule(ADJACENCY, new XMLSyntaxRule[]{
                            new ElementRule(TAXON_DATA, new XMLSyntaxRule[]{
                                    new ElementRule(Taxon.class, 1, 1),
//                                    AttributeRule.newDoubleRule(PERIMETER),
//                                    AttributeRule.newDoubleRule(AREA)
                            }, 2, 2),
                            AttributeRule.newDoubleRule(SHARED_BORDER, true)
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
