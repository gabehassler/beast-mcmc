/*
 * BeastVersion.java
 *
 * Copyright (c) 2002-2015 Alexei Drummond, Andrew Rambaut and Marc Suchard
 *
 * This file is part of BEAST.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership and licensing.
 *
 * BEAST is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 *  BEAST is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BEAST; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */

package dr.app.beast;

import dr.util.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class provides a mechanism for returning the version number of the
 * dr software.
 *
 * This is manually updated as required. The REVISION string is no longer used
 * since switching to GitHub.
 *
 * @author Alexei Drummond
 * @author Andrew Rambaut
 *
 * $Id$
 */
public class BeastVersion implements Version, Citable {

    public static final BeastVersion INSTANCE = new BeastVersion();

    /**
     * Version string: assumed to be in format x.x.x
     */
//    private static final String VERSION = "1.10.5";

    private static final String DATE_STRING = "2002-2019";

//    private static final boolean IS_PRERELEASE = true;

    // this is now being manually updated since the move to GitHub. 7 digits of GitHub hash.
    private static final String REVISION = "23570d1";

    public String getVersion() {
        //TODO update with BEAUTI version too
        return readVersion()
                .get("version");
    }

    public String getVersionString() {
        return readVersion().get("tag") + " commit: " + readVersion().get("commit");
    }

    public String getDateString() {
        return DATE_STRING;
    }

    public String[] getCredits() {
        return new String[]{
                "Designed and developed by",
                "Alexei J. Drummond, Andrew Rambaut and Marc A. Suchard",
                "",
                "Department of Computer Science",
                "University of Auckland",
                "alexei@cs.auckland.ac.nz",
                "",
                "Institute of Evolutionary Biology",
                "University of Edinburgh",
                "a.rambaut@ed.ac.uk",
                "",
                "David Geffen School of Medicine",
                "University of California, Los Angeles",
                "msuchard@ucla.edu",
                "",
                "Downloads, Help & Resources:",

                "\thttp://beast.community",
                "",
                "Source code distributed under the GNU Lesser General Public License:",
                "\thttp://github.com/beast-dev/beast-mcmc",
                "",
                "BEAST developers:",
                "\tAlex Alekseyenko, Guy Baele, Trevor Bedford, Filip Bielejec, Erik Bloomquist, Matthew Hall,",
                "\tJoseph Heled, Sebastian Hoehna, Denise Kuehnert, Philippe Lemey, Wai Lok Sibon Li,",
                "\tGerton Lunter, Sidney Markowitz, Vladimir Minin, Michael Defoin Platel,",
                "\tOliver Pybus, Chieh-Hsi Wu, Walter Xie",
                "",
                "Thanks to:",
                "\tRoald Forsberg, Beth Shapiro and Korbinian Strimmer"};
    }

    public String getHTMLCredits() {
        return
                "<p>Designed and developed by<br>" +
                        "Alexei J. Drummond, Andrew Rambaut and Marc A. Suchard</p>" +
                        "<p>Department of Computer Science, University of Auckland<br>" +
                        "<a href=\"mailto:alexei@cs.auckland.ac.nz\">alexei@cs.auckland.ac.nz</a></p>" +
                        "<p>Institute of Evolutionary Biology, University of Edinburgh<br>" +
                        "<a href=\"mailto:a.rambaut@ed.ac.uk\">a.rambaut@ed.ac.uk</a></p>" +
                        "<p>David Geffen School of Medicine, University of California, Los Angeles<br>" +
                        "<a href=\"mailto:msuchard@ucla.edu\">msuchard@ucla.edu</a></p>" +
                        "<p><a href=\"http://beast.community\">http://beast.community</a></p>" +
                        "<p>Source code distributed under the GNU LGPL:<br>" +
                        "<a href=\"http://github.com/beast-dev/beast-mcmc\">http://github.com/beast-dev/beast-mcmc</a></p>" +
                        "<p>BEAST developers:<br>" +
                        "Alex Alekseyenko, Guy Baele, Trevor Bedford, Filip Bielejec, Erik Bloomquist, Matthew Hall,<br>"+
                        "Joseph Heled, Sebastian Hoehna, Denise Kuehnert, Philippe Lemey, Wai Lok Sibon Li,<br>"+
                        "Gerton Lunter, Sidney Markowitz, Vladimir Minin, Michael Defoin Platel,<br>"+
                        "Oliver Pybus, Chieh-Hsi Wu, Walter Xie</p>" +
                        "<p>Thanks to Roald Forsberg, Beth Shapiro and Korbinian Strimmer</p>";
    }

    public String getBuildString() {

        return "https://github.com/beast-dev/beast-mcmc/commit/" + readVersion().get("commit");
    }

    @Override
    public Citation.Category getCategory() {
        return Citation.Category.FRAMEWORK;
    }

    @Override
    public String getDescription() {
        return "BEAST primary citation";
    }

    @Override
    public List<Citation> getCitations() {
        return Arrays.asList(CITATIONS);
    }

    public static Citation[] CITATIONS = new Citation[] {
            new Citation(
                    new Author[]{
                            new Author("MA", "Suchard"),
                            new Author("P", "Lemey"),
                            new Author("G", "Baele"),
                            new Author("DL", "Ayres"),
                            new Author("AJ", "Drummond"),
                            new Author("A", "Rambaut")
                    },
                    "Bayesian phylogenetic and phylodynamic data integration using BEAST 1.10",
                    2018,
                    "Virus Evolution",
                    4, "vey016",
                    "10.1093/ve/vey016"),
//            new Citation(
//                    new Author[]{
//                            new Author("AJ", "Drummond"),
//                            new Author("MA", "Suchard"),
//                            new Author("Dong", "Xie"),
//                            new Author("A", "Rambaut")
//                    },
//                    "Bayesian phylogenetics with BEAUti and the BEAST 1.7",
//                    2012,
//                    "Mol Biol Evol",
//                    29, 1969, 1973,
//                    "10.1093/molbev/mss075")
    };
    public static void main(String[] args) {
        System.out.println(readVersion());
    }
    public static Map<String,String> readVersion() {
        Map<String, String> version = new HashMap<>();
        Pattern versionPattern = Pattern.compile("v([\\d.]+)[^\\d.].*"); //v1.10.5ANYTHING_ELSE_IS_IGNORED_FOR_VERSION
        try {
            try (InputStream in = BeastVersion.class.getResourceAsStream("/version.txt")) {
                assert in != null;

                List<String> lines =  new BufferedReader(
                        new InputStreamReader(in, StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.toList());
                Matcher versionMatcher = versionPattern.matcher(lines.get(1));
                if(!versionMatcher.find()){
                    throw new RuntimeException("Last tag does not match semantic versioning please use the format v([\\d.]+)[^\\d.].* which will capture the version number");
                }
                version.put("version",versionMatcher.group(1));
                version.put("tag",lines.get(1).replaceAll("-\\d+-g[a-zA-Z0-9]+","")); //"tag-commit" -commit is not provided if at tag
                version.put("commit",lines.get(2)); //"commit-dirty" -dirty is only output if there are uncommited changes
                version.put("branch", lines.get(3));
            }
            return version;
        } catch (IOException e) {
            throw new RuntimeException("Cannot report the present tag and commit. No version file found. Try running `ant version` to make it");
        }
    }
}
