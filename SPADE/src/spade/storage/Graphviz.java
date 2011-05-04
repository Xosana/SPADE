/*
--------------------------------------------------------------------------------
SPADE - Support for Provenance Auditing in Distributed Environments.
Copyright (C) 2011 SRI International

This program is free software: you can redistribute it and/or
modify it under the terms of the GNU General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
--------------------------------------------------------------------------------
 */
package spade.storage;

import spade.core.AbstractStorage;
import spade.core.AbstractEdge;
import spade.core.AbstractVertex;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Iterator;

public class Graphviz extends AbstractStorage {

    private FileWriter outputFile;
    private HashSet<Integer> EdgeSet;

    @Override
    public boolean initialize(String arguments) {
        try {
            EdgeSet = new HashSet<Integer>();
            outputFile = new FileWriter(arguments, false);
            outputFile.write("digraph spade_dot {\ngraph [rankdir = \"RL\"];\nnode [fontname=\"Helvetica\" fontsize=\"10\" style=\"filled\" margin=\"0.0,0.0\"];\nedge [fontname=\"Helvetica\" fontsize=\"10\"];\n");
            return true;
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
        }
        return false;
    }

    @Override
    public boolean putVertex(AbstractVertex incomingVertex) {
        try {
            String vertexString = "";
            Map<String, String> annotations = incomingVertex.getAnnotations();
            for (Iterator iterator = annotations.keySet().iterator(); iterator.hasNext();) {
                String key = (String) iterator.next();
                String value = (String) annotations.get(key);
                if ((key.equalsIgnoreCase("type")) || (key.equalsIgnoreCase("storageId"))) {
                    continue;
                }
                vertexString = vertexString + key + ":" + value + "\\n";
            }
            vertexString = vertexString.substring(0, vertexString.length() - 2);
            String shape = "";
            String color = "";
            String type = incomingVertex.getAnnotation("type");
            if (type.equalsIgnoreCase("Agent")) {
                shape = "octagon";
                color = "rosybrown1";
            } else if (type.equalsIgnoreCase("Process")) {
                shape = "box";
                color = "lightsteelblue1";
            } else {
                shape = "ellipse";
                color = "khaki1";
            }
            outputFile.write("\"" + incomingVertex.hashCode() + "\" [label=\"" + vertexString.replace("\"", "'") + "\" shape=\"" + shape + "\" fillcolor=\"" + color + "\"];\n");
            return true;
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
        }
        return false;
    }

    @Override
    public boolean putEdge(AbstractEdge incomingEdge) {
        try {
            if (EdgeSet.add(incomingEdge.hashCode())) {
                String annotationString = "";
                Map<String, String> annotations = incomingEdge.getAnnotations();
                for (Iterator iterator = annotations.keySet().iterator(); iterator.hasNext();) {
                    String key = (String) iterator.next();
                    String value = (String) annotations.get(key);
                    if ((key.equalsIgnoreCase("storageId")) || (key.equalsIgnoreCase("type"))) {
                        continue;
                    }
                    annotationString = annotationString + key + ":" + value + ", ";
                }
                String color = "";
                String type = incomingEdge.getAnnotation("type");
                if (type.equalsIgnoreCase("Used")) {
                    color = "green";
                } else if (type.equalsIgnoreCase("WasGeneratedBy")) {
                    color = "red";
                } else if (type.equalsIgnoreCase("WasTriggeredBy")) {
                    color = "blue";
                } else if (type.equalsIgnoreCase("WasControlledBy")) {
                    color = "purple";
                } else if (type.equalsIgnoreCase("WasDerivedFrom")) {
                    color = "orange";
                }
                if (annotationString.length() > 3) {
                    annotationString = "(" + annotationString.substring(0, annotationString.length() - 2) + ")";
                }
                String edgeString = "\"" + incomingEdge.getSrcVertex().hashCode() + "\" -> \"" + incomingEdge.getDstVertex().hashCode() + "\" [label=\"" + annotationString.replace("\"", "'") + "\" color=\"" + color + "\"];\n";
                outputFile.write(edgeString);
                return true;
            }
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
        }
        return false;
    }

    @Override
    public boolean shutdown() {
        try {
            outputFile.write("}\n");
            outputFile.close();
            return true;
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
        }
        return false;
    }
}
