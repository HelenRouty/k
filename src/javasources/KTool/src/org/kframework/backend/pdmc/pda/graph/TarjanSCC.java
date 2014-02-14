package org.kframework.backend.pdmc.pda.graph;

import org.kframework.parser.concrete.lib.once_$Memo$My$Tbl$Ground_0_0;

import java.util.*;

/**
 * @author Traian
 *
 * @see <a href="http://en.wikipedia.org/wiki/Tarjan's_strongly_connected_components_algorithm">The rendition on
 * Wikipedia of Tarjan's Strongly Connected Components algorithm.</a>
 */
public class TarjanSCC<Data, Label> {
    Map<Data,TarjanSCCVertex> vertexSet;
    Map<Data, Map<Data, Label>> edgeSet;

    public TarjanSCC() {
        vertexSet = new HashMap<Data, TarjanSCCVertex>();
        edgeSet = new HashMap<Data, Map<Data, Label>>();
        sccs = null;
    }

    public class TarjanSCCVertex {

        TarjanSCCVertex(Data data) {
            this.data = data;
            nextVertex = edgeSet.get(data);
            if (nextVertex == null) {
                nextVertex = new HashMap<Data, Label>();
                edgeSet.put(data, nextVertex);
            }
            index = -1;
            lowlink = -1;
            inStack = false;
        }

        @Override
        public boolean equals(Object o) {
            return (this == o);
        }

        @Override
        public int hashCode() {
            return data.hashCode();
        }

        Data data;
        Map<Data, Label> nextVertex;
        int index;
        int lowlink;
        boolean inStack;

        @Override
        public String toString() {
            return data.toString();
        }
    }

    public boolean addEdge(Data data1, Data data2, Label l) {
        TarjanSCCVertex vertex1 = vertexSet.get(data1);
        if (vertex1 == null) {
            vertex1 = new TarjanSCCVertex(data1);
            vertexSet.put(data1, vertex1);
        }
        TarjanSCCVertex vertex2 = vertexSet.get(data2);
        if (vertex2 == null) {
            vertex2 = new TarjanSCCVertex(data2);
            vertexSet.put(data2, vertex2);
        }
        Label ll = vertex1.nextVertex.put(data2, l);
        return (!l.equals(ll));
    }

    ArrayList<ArrayList<TarjanSCCVertex>> sccs = null;

    public ArrayList<ArrayList<TarjanSCCVertex>> stronglyConnectedComponents() {
        if (sccs == null) computeSCC();
        return sccs;
    }

    int index;
    Stack<TarjanSCCVertex> sccStack;
    private void computeSCC() {
        index = 0;
        sccs = new ArrayList<>();
        sccStack = new Stack<TarjanSCCVertex>();
        for (TarjanSCCVertex v : vertexSet.values()) {
            if (v.index == -1) {
                strongConnect(v);
            }
        }
    }

    private void strongConnect(TarjanSCCVertex v) {
        v.index = index;
        v.lowlink = index;
        index = index + 1;
        v.inStack = true;
        sccStack.push(v);
        for (Data dw : v.nextVertex.keySet()) {
            TarjanSCCVertex w = vertexSet.get(dw);
            if (w.index == -1) {
                strongConnect(w);
                v.lowlink = Math.min(v.lowlink, w.lowlink);
            } else if (w.inStack) {
                v.lowlink = Math.min(v.lowlink, w.index);
            }
        }

        if (v.lowlink == v.index) {
            ArrayList<TarjanSCCVertex> scc = new ArrayList<TarjanSCCVertex>();
            TarjanSCCVertex w = null;
            while (w != v) {
                w = sccStack.pop();
                w.inStack = false;
                scc.add(w);
            }
            sccs.add(scc);
        }

    }

    public String getSCCSString() {
        StringBuilder result = new StringBuilder();
        ArrayList<ArrayList<TarjanSCCVertex>> sccs = stronglyConnectedComponents();
        for (ArrayList<TarjanSCCVertex> scc : sccs) {
            result.append("{ ");
            for (TarjanSCCVertex v : scc) {
                result.append(v.toString());
                result.append(" ");
            }
            result.append("}");
            result.append(";\n");
        }
        return result.toString();
    }


    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (TarjanSCCVertex vertex : vertexSet.values()) {
            if (vertex.nextVertex.isEmpty()) continue;
            result.append(vertex.toString());
            result.append(" |->");
            for(Map.Entry<Data,Label> next : vertex.nextVertex.entrySet()) {
                result.append(" (");
                result.append(next.getKey().toString());
                result.append(",");
                result.append(next.getValue().toString());
                result.append(") ;");
            }
            result.append("\n");
        }
        return result.toString();
    }
}
