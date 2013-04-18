/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ospf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Phani Rahul
 */
public class OSPF {

    /*Constants..*/
    static int UP = 1;
    static int DOWN = 2;
    /*Globals*/
    private static ArrayList<Vertex> master; //This stores all the Vertex-es in the graph
    private static PriorityQueue<Vertex> heap;

    

    /*Consructors*/
    public OSPF() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String graphPath = "c:\\app\\network.txt";
        String queriespath = "c:\\app\\queries.txt";
        String outputPath = "c:\\app\\output.txt";

        OSPF inst = new OSPF();
        master = new ArrayList<>();
        heap = new PriorityQueue<>();
        File graphFile = new File(graphPath);
        File queriesFile = new File(queriespath);
        File outputFile = new File(outputPath);
        BufferedWriter output = null;
        BufferedReader graph = null;
        BufferedReader queries = null;
        try {
            graph = new BufferedReader(new FileReader(graphFile));
            queries = new BufferedReader(new FileReader(queriesFile));
            output = new BufferedWriter(new FileWriter(outputFile));
            String line = "";
            while ((line = graph.readLine()) != null) {
                String words[] = line.split(" ");
                String vertex1 = words[0];
                String vertex2 = words[1];
                double time = Double.valueOf(words[2]);

                makeAdjList(vertex1, vertex2, time);
            }
            graph.close();
            master.trimToSize();
            trimAll();
            StringBuilder out = new StringBuilder();
            while ((line = queries.readLine()) != null) {
                String words[] = line.split(" ");
                String ret = executeCommand(words[0], words);
                out.append(ret);
                System.out.println(ret);
                System.gc();
            }
//            System.out.println(out);
//System.out.println(shortestPath(findVertexByName("Education"), findVertexByName("Belk")));
//            String a[]={"edgedown","Health","Education"};
//            executeCommand("edgedown", a);
//            System.out.println(computePath("Belk", "Education"));
            System.out.println(computeReach());
            //System.out.println(printVertices());


        } catch (FileNotFoundException ex) {
            Logger.getLogger(OSPF.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OSPF.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                graph.close();
                queries.close();
                output.close();
            } catch (IOException ex) {
                Logger.getLogger(OSPF.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    private static void trimAll() {
        master.trimToSize();
        for(Vertex v : master){
            v.outEdges.trimToSize();
        }
    }

    private static String executeCommand(String command, String[] args) {
        String output = "";
        switch (command) {
            case "print":
            case "Print":
                output = printVertices();
                break;

            case "path":
            case "Path":
                output = computePath(args[1], args[2]);
                break;

            case "vertexup":
            case "Vertexup":
                output = setVertexUp(args[1]);
                break;

            case "vertexdown":
            case "Vertexdown":
                output = setVertexDown(args[1]);
                break;

            case "edgeup":
            case "Edgeup":
                output = setEdgeUp(args[1], args[2]);
                break;

            case "edgedown":
            case "Edgedown":
                output = setEdgeDown(args[1], args[2]);
                break;

            case "addedge":
            case "Addedge":
                output = addEdge(args[1], args[2], args[3]);
                break;

            case "deleteedge":
            case "Deleteedge":
                output = deleteEdge(args[1], args[2]);
                break;
            case "reachable":
            case "Reachable":
                output = computeReach();
                break;
            default:
                output = "The command given is unrecognized. /n /n";
        }

        return output;
    }

    private static String printVertices() {
        StringBuilder output = new StringBuilder();
        Collections.sort(master, new Comparator<Vertex>() {
            @Override
            public int compare(Vertex o1, Vertex o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        Iterator<Vertex> i = master.iterator();
        while (i.hasNext()) {
            Vertex v = (Vertex) i.next();
            output.append(v.name);
            if (v.status == DOWN) {
                output.append(" DOWN");
            }
            output.append("\n");
            Collections.sort(v.outEdges, new Comparator<Edge>() {
                @Override
                public int compare(Edge o1, Edge o2) {
                    return o1.toVertex.name.compareTo(o2.toVertex.name);
                }
            });
            Iterator<Edge> ei = v.outEdges.iterator();
            while (ei.hasNext()) {
                Edge e = ei.next();
                output.append("     ").append(e.toVertex.name);
                output.append(" ").append(e.time);
                if (e.status == DOWN) {
                    output.append(" DOWN");
                }
                output.append("\n");
            }
        }
        output.append("\n");
        return output.toString();
    }

    private static String computePath(String from, String to) {
        Vertex fromVertex = findVertexByName(from);
        Vertex toVertex = findVertexByName(to);
        Iterator<Vertex> i = shortestPath(fromVertex, toVertex).iterator();
        StringBuilder output = new StringBuilder();
        Vertex v = null;
        Vertex prev = null;
        double time = 0;
        while (i.hasNext()) {
            prev = v;
            v = i.next();
            output.append(v.name);
            output.append(" ");
            if (prev != null) {
                double res = prev.getEdgeTime(prev, v);
                time = res + time;
            }
        }
        output.append(time);
        output.append("\n \n");
        return output.toString();
    }

    private static String setVertexUp(String name) {
        Vertex v = findVertexByName(name);
        v.setStatus(UP);
        return "";
    }

    private static String setVertexDown(String name) {
        Vertex v = findVertexByName(name);
        v.setStatus(DOWN);
        return "";
    }

    private static String setEdgeUp(String from, String to) {
        Vertex v1 = findVertexByName(from);
        Vertex v2 = findVertexByName(to);
        Edge e = v1.getEdgeByVertices(v1, v2);
        if (e != null) {
            v1.updateEdge(e, UP);
        }
        return "";
    }

    private static String setEdgeDown(String from, String to) {
        Vertex v1 = findVertexByName(from);
        Vertex v2 = findVertexByName(to);
        Edge e = v1.getEdgeByVertices(v1, v2);
        if (e != null) {
            v1.updateEdge(e, DOWN);
        }
        return "";
    }

    private static String addEdge(String from, String to, String time) {
        Vertex v1 = findVertexByName(from);
        Vertex v2 = findVertexByName(to);
        Edge e = new Edge(v1, Double.valueOf(time), v2);
        v1.addEdge(e);
        return "";
    }

    private static String deleteEdge(String from, String to) {
        Vertex v1 = findVertexByName(from);
        Vertex v2 = findVertexByName(to);
        Edge e = v1.getEdgeByVertices(v1, v2);
        if (e != null) {
            v1.deleteEdge(e);
        }
        return "";
    }

    private static String computeReach() {
        /*DFS algorithm to traverse through the entire graph*/
        Collections.sort(master, new Comparator<Vertex>() {
            @Override
            public int compare(Vertex o1, Vertex o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        Iterator<Vertex> i = master.iterator();
        HashSet<Vertex> coll = new HashSet<>();
        StringBuilder output = new StringBuilder();
        while (i.hasNext()) {
            Vertex v = i.next();
            addToReachable(coll, v);
            coll.remove(v);
            output.append(v.name);
            output.append("\n");
           
            ArrayList<Vertex> l = new ArrayList(coll);
            l.trimToSize();
            Collections.sort(l, new Comparator<Vertex>() {
                @Override
                public int compare(Vertex o1, Vertex o2) {
                    return o1.name.compareTo(o2.name);
                }
            });
            for (Vertex ob : l) {
                output.append("  ");
                output.append(ob.name);
                output.append("\n");
            }
            coll.clear();
        }
        output.append("\n");
        return output.toString();
    }

    private static void addToReachable(HashSet h, Vertex v) {

        if (v.status == DOWN || h.contains(v)) {
            return;
        } else {
            h.add(v);
        }
        for (Edge e : v.outEdges) {
            if (e.status != DOWN) {
                addToReachable(h, e.toVertex);
            }
        }
    }

    private static void makeAdjList(String vertex1, String vertex2, double time) {

        Vertex v1 = findVertexByName(vertex1);
        Vertex v2 = findVertexByName(vertex2);

        Edge e1 = new Edge(v1, time, v2);
        v1.outEdges.add(e1);
        Edge e2 = new Edge(v2, time, v1);
        v2.outEdges.add(e2);
    }

    private static Vertex findVertexByName(String name) {
        Vertex v;
        //v = new Vertex(name);
        for (Vertex i : master) {
            if (i.name.equalsIgnoreCase(name)) {
                return i;
            }
        }
        v = new Vertex(name);
        master.add(v);
        return v;
//        if (master.contains(v)) {
//            v = master.get(master.indexOf(v));
//        } else {
//            master.add(v);
//        }
//        return v;
    }

    private static ArrayList shortestPath(Vertex source, Vertex dest) {
        ArrayList<Vertex> path = new ArrayList<>();
//        if (source.minDistTemp != 0) {
            initialize(source);
            if (!heap.isEmpty()) {
                heap.clear();
            }
            heap.addAll(master);
            computeAuxillary();
//        }
        for (Vertex v = dest; v != null; v = v.last) {
            path.add(v);
        }
        path.trimToSize();
        /*TODO : path to be populated*/
        Collections.reverse(path);
        return path;
    }

    private static void computeAuxillary() {

        while (!heap.isEmpty()) {
            Vertex tempVertex = heap.poll();

            for (Edge next : tempVertex.outEdges) {
                if (next.status != DOWN) {
                    if (heap.contains(next.toVertex)) {
                        relaxNodes(tempVertex, next.toVertex);
                    }
                }
            }
        }
    }

    private static void relaxNodes(Vertex now, Vertex next) {

        if (next.status != DOWN) {
            double min = now.minDistTemp + now.getEdgeTime(now, next);
            if (min < next.minDistTemp) {
                next.minDistTemp = min;
                next.last = now;

                /*Adding and removing the vertex would ensure that the vertex reorders in the queue*/
                heap.remove(next);
                heap.add(next);
            }
        }
    }

    private static void initialize(Vertex source) {

        for (Vertex v : master) {
            v.minDistTemp = Double.POSITIVE_INFINITY;
        }
        source.minDistTemp = 0.0;
    }
}
class Vertex implements Comparable<Vertex> {

    int UP = OSPF.UP;
    int DOWN = OSPF.DOWN;
    int status;
    String name;
    ArrayList<Edge> outEdges;
    /* will be used as a temporary area to compute the shortest distance */
    double minDistTemp;
    /*the vertex last visited before this node*/
    Vertex last;

    Vertex(String name) {
        this.name = name;
        this.outEdges = new ArrayList<>();
//            distances = new HashMap<>();
        status = UP;
        last = null;
    }

    public Edge getEdgeByVertices(Vertex v1, Vertex v2) {

        for (Edge e : this.outEdges) {
            if (e.fromVertex.equals(v1) && e.toVertex.equals(v2)) {
                return e;
            }
        }
        return null;
    }

    public void updateEdge(Edge edge, int status) {
        //TODO update edges if the specified edge exists in the outEdges
        if (outEdges.contains(edge)) {
            Edge e = outEdges.get(outEdges.indexOf(edge));
            e.status = status;
        }
    }

    public void addEdge(Edge e) {
        if (!this.outEdges.contains(e)) {
            this.outEdges.add(e);
        }
    }

    public void deleteEdge(Edge e) {
        if (this.outEdges.contains(e)) {
            this.outEdges.remove(e);
        }
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Vertex other = (Vertex) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    public double getEdgeTime(Vertex now, Vertex next) {
        for (Edge e : now.outEdges) {
            if ((e.toVertex).equals(next)) {
                if (e.status != DOWN) {
                    return e.time;
                }
            }
        }
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public int compareTo(Vertex other) {
        int ret;
        if (this.status == UP) {
            ret = (new Double(this.minDistTemp)).compareTo(other.minDistTemp);
        } else {
            ret = Integer.MAX_VALUE;
        }
        return ret;
    }

    @Override
    public String toString() {
        return "Vertex{" + "name=" + name + '}';
    }
}

class Edge implements Comparable<Edge> {

    int UP = OSPF.UP;
    int DOWN = OSPF.DOWN;
    int status;
    Vertex toVertex;
    double time;
    Vertex fromVertex;

    public Edge(Vertex fromVertex, double time, Vertex toVertex) {
        this.time = time;
        this.toVertex = toVertex;
        this.fromVertex = fromVertex;
        this.status = UP;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setTime(double time) {
        this.time = time;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.toVertex);
        hash = 17 * hash + Objects.hashCode(this.fromVertex);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Edge other = (Edge) obj;
        if (!Objects.equals(this.toVertex, other.toVertex)) {
            return false;
        }
        if (!Objects.equals(this.fromVertex, other.fromVertex)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Edge other) {
        int ret;
        if (this.status == UP) {
            ret = (new Double(this.time)).compareTo(other.time);
        } else {
            ret = Integer.MAX_VALUE;
        }
        return ret;
    }

    @Override
    public String toString() {
        return "Edge{" + "toVertex=" + toVertex + ", time=" + time + ", fromVertex=" + fromVertex + '}';
    }
}