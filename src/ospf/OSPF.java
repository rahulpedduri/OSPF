/*
 * Name: Phani Rahul Pedduri
 * ID: 800803441
 */
package ospf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
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
    private static boolean quit = false;

    /*Consructors*/
    public OSPF() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String graphPath = args[0];
        String queriespath = args[1];
        String outputPath = args[2];

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
            trimAll();

            while ((line = queries.readLine()) != null) {
                String words[] = line.split(" ");
                if (!quit) {
                    executeCommand(words[0], words, output);
                }
                else{
                    break;
                }

            }

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
    
    /*Since we are using a dynamic array, if we trim the array, the space complexity is good*/
    private static void trimAll() {
        master.trimToSize();
        for (Vertex v : master) {
            v.outEdges.trimToSize();
        }
    }

    /*This method executes a specific query*/
    private static void executeCommand(String command, String[] args, BufferedWriter w)
            throws IOException {

        switch (command) {
            case "print":
            case "Print":
                printVertices(w);
                break;

            case "path":
            case "Path":
                computePath(args[1], args[2], w);
                break;

            case "vertexup":
            case "Vertexup":
                setVertexUp(args[1]);
                break;

            case "vertexdown":
            case "Vertexdown":
                setVertexDown(args[1]);
                break;

            case "edgeup":
            case "Edgeup":
                setEdgeUp(args[1], args[2]);
                break;

            case "edgedown":
            case "Edgedown":
                setEdgeDown(args[1], args[2]);
                break;

            case "addedge":
            case "Addedge":
                addEdge(args[1], args[2], args[3]);
                break;

            case "deleteedge":
            case "Deleteedge":
                deleteEdge(args[1], args[2]);
                break;
            case "reachable":
            case "Reachable":
                computeReach(w);
                break;
            case "quit":
            case "Quit":
                quit = true;
                break;
            default:

        }
    }
    
    /*Prints all the vertices and edges to the output file*/
    private static void printVertices(BufferedWriter w) throws IOException {

        Collections.sort(master, new Comparator<Vertex>() {
            @Override
            public int compare(Vertex o1, Vertex o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        Iterator<Vertex> i = master.iterator();
        while (i.hasNext()) {
            Vertex v = (Vertex) i.next();
            w.append(v.name);
            if (v.status == DOWN) {
                w.append(" DOWN");
            }
            w.append("\n");
            Collections.sort(v.outEdges, new Comparator<Edge>() {
                @Override
                public int compare(Edge o1, Edge o2) {
                    return o1.toVertex.name.compareTo(o2.toVertex.name);
                }
            });
            Iterator<Edge> ei = v.outEdges.iterator();
            while (ei.hasNext()) {
                Edge e = ei.next();
                w.append("     ").append(e.toVertex.name);
                w.append(" ");
                w.append(e.time + "");
                if (e.status == DOWN) {
                    w.append(" DOWN");
                }
                w.append("\n");
            }
        }
        w.append("\n");
    }
    
    /*Computes and prints a shortest path from source to destination*/
    private static void computePath(String from, String to, BufferedWriter w) throws IOException {
        Vertex fromVertex = findVertexByName(from);
        Vertex toVertex = findVertexByName(to);
        Iterator<Vertex> i = shortestPath(fromVertex, toVertex).iterator();

        Vertex v = null;
        Vertex prev = null;
        double time = toVertex.minDistTemp;
        while (i.hasNext()) {
            prev = v;
            v = i.next();
            w.append(v.name);
            w.append(" ");            
        }
        /*decimal formatting is done so that round up until 2 decimal points is done.
         Otherwise 0.9 is displayed as 0.899999999999 which is mistaken for wrong result*/
        DecimalFormat f = new DecimalFormat("0.00");
        w.append(f.format(time));
        w.append("\n \n");
    }
    
    /*sets the status of vertex as UP*/
    private static String setVertexUp(String name) {
        Vertex v = findVertexByName(name);
        v.setStatus(UP);
        return "";
    }
    
    /*sets the status of vertex as DOWN*/
    private static String setVertexDown(String name) {
        Vertex v = findVertexByName(name);
        v.setStatus(DOWN);
        return "";
    }
    /*sets the status of a directed edge as UP*/

    private static String setEdgeUp(String from, String to) {
        Vertex v1 = findVertexByName(from);
        Vertex v2 = findVertexByName(to);
        Edge e = v1.getEdgeByVertices(v1, v2);
        if (e != null) {
            v1.updateEdge(e, UP);
        }
        return "";
    }
    /*sets the status of a directed edge as DOWN*/

    private static String setEdgeDown(String from, String to) {
        Vertex v1 = findVertexByName(from);
        Vertex v2 = findVertexByName(to);
        Edge e = v1.getEdgeByVertices(v1, v2);
        if (e != null) {
            v1.updateEdge(e, DOWN);
        }
        return "";
    }
    /*adds a directed edge between two vertices and creates the vertices if they dont exist*/

    private static String addEdge(String from, String to, String time) {
        Vertex v1 = findVertexByName(from);
        Vertex v2 = findVertexByName(to);
        Edge e = new Edge(v1, Double.valueOf(time), v2);
        v1.addEdge(e);
        return "";
    }
    /*deletes a directed edge from the graph*/

    private static String deleteEdge(String from, String to) {
        Vertex v1 = findVertexByName(from);
        Vertex v2 = findVertexByName(to);
        Edge e = v1.getEdgeByVertices(v1, v2);
        if (e != null) {
            v1.deleteEdge(e);
        }
        return "";
    }
    /*computes and prints the reachable vertices from any given vertex*/

    private static void computeReach(BufferedWriter w) throws IOException {
        /* algorithm to traverse through the entire graph It traverses through
         all the vertices in O(V+E) time, where V is the number of vertices
         and E is the number of edges*/
        Collections.sort(master, new Comparator<Vertex>() {
            @Override
            public int compare(Vertex o1, Vertex o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        Iterator<Vertex> i = master.iterator();
        HashSet<Vertex> coll = new HashSet<>();

        while (i.hasNext()) {
            Vertex v = i.next();
            addToReachable(coll, v);

            coll.remove(v);
            w.append(v.name);
            w.append("\n");

            ArrayList<Vertex> l = new ArrayList(coll);
            l.trimToSize();
            Collections.sort(l, new Comparator<Vertex>() {
                @Override
                public int compare(Vertex o1, Vertex o2) {
                    return o1.name.compareTo(o2.name);
                }
            });
            for (Vertex ob : l) {
                w.append("  ");
                w.append(ob.name);
                w.append("\n");
            }
            coll.clear();
        }
        w.append("\n");
    }
    /*to compute the reachability of a vertex*/

    private static void addToReachable(HashSet h, Vertex v) {

        if (v.status == DOWN || h.contains(v)) {
            return;
        } else {
            h.add(v);
        }
        for (Edge e : v.outEdges) {
            if (e.status != DOWN && e.toVertex.status != DOWN) {
                addToReachable(h, e.toVertex);
            }
        }
    }
    /*makes the adjacency list, the list is a global "master" */

    private static void makeAdjList(String vertex1, String vertex2, double time) {

        Vertex v1 = findVertexByName(vertex1);
        Vertex v2 = findVertexByName(vertex2);

        Edge e1 = new Edge(v1, time, v2);
        v1.outEdges.add(e1);
        Edge e2 = new Edge(v2, time, v1);
        v2.outEdges.add(e2);
    }
    /*finds a vertex by its name and creates a new vertex if it doesn't find one*/

    private static Vertex findVertexByName(String name) {
        Vertex v;
        for (Vertex i : master) {
            if (i.name.equalsIgnoreCase(name)) {
                return i;
            }
        }
        v = new Vertex(name);
        master.add(v);
        return v;
    }
    /*Computes the shortest path using dijkstra's algorithm and returns the arraylist of it*/

    private static ArrayList shortestPath(Vertex source, Vertex dest) {

        ArrayList<Vertex> path = new ArrayList<>();
        initialize(source);
        if (!heap.isEmpty()) {
            heap.clear();
        }
        heap.addAll(master);
        computeAuxillary();
        for (Vertex v = dest; v != null; v = v.last) {
            path.add(v);
        }
        path.trimToSize();
        /*The path has to be reversed because we are traversing in the reverse direction*/
        Collections.reverse(path);
        return path;
    }
    /*computes the auxillary array, which is crucial in dijkstra's algorithm*/

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
    /*relaxes the nodes, part of dijkstra's algorithm*/

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
    /*initializes for dijkstra's algorithm*/

    private static void initialize(Vertex source) {

        for (Vertex v : master) {
            v.minDistTemp = Double.POSITIVE_INFINITY;
            v.last = null;
        }
        source.minDistTemp = 0.0;
    }
}
/*Vertex data structure to hold all the data of a vertex*/

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
        this.outEdges = new ArrayList<>(1);
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
        if (outEdges.contains(edge)) {
            Edge e = outEdges.get(outEdges.indexOf(edge));
            e.status = status;
        }
    }

    public void addEdge(Edge e) {
        if (!this.outEdges.contains(e)) {
            this.outEdges.add(e);
        }
        else{
            this.outEdges.remove(e);
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
/*Edge data structure to hold the info of an edge*/

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