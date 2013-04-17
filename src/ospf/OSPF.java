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
import java.util.HashSet;
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
    ArrayList<Vertex> master; //This stores all the Vertex-es in the graph
    PriorityQueue<Vertex> heap;

    /*Consructors*/
    public OSPF() {
        master = new ArrayList<>();

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

                makeAdjList(inst, vertex1, vertex2, time);
            }
System.out.println(inst.shortestPath(inst.findVertexByName("Education"), inst.findVertexByName("Belk")));
           
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

    private static void makeAdjList(OSPF inst, String vertex1, String vertex2, double time) {

        Vertex v1 = inst.findVertexByName(vertex1);
        Vertex v2 = inst.findVertexByName(vertex2);
       
        Edge e1 = new Edge(v1, time, v2);
        v1.outEdges.add(e1);
        Edge e2 = new Edge(v2, time, v1);
        v2.outEdges.add(e2);
    }

    private Vertex findVertexByName(String name){
        Vertex v;
        v = new Vertex(name);
        if(master.contains(v)){
            v=master.get( master.indexOf(v) );
        }else{
            master.add(v);
        }
        return v;
    }
    public ArrayList shortestPath(Vertex source, Vertex dest) {
        ArrayList<Vertex> path = new ArrayList<>();
        initialize(source);
        heap = new PriorityQueue<>(master);
        computeAuxillary();
        for (Vertex v = dest; v != null; v = v.last) {
            path.add(v);
        }
        /*TODO : path to be populated*/
        Collections.reverse(path);
        return path;
    }

    private void computeAuxillary() {

        while (!heap.isEmpty()) {
            Vertex tempVertex = heap.poll();

            for (Edge next : tempVertex.outEdges) {
                if (heap.contains(next.toVertex)) {
                    relaxNodes(tempVertex, next.toVertex);
                }
            }
        }
    }

    private void relaxNodes(Vertex now, Vertex next) {
        double min = now.minDistTemp + now.getEdgeTime(now, next);
        if (min < next.minDistTemp) {
            next.minDistTemp = min;
            next.last = now;

            /*Adding and removing the vertex would ensure that the vertex reorders in the queue*/
            heap.remove(next);
            heap.add(next);
        }
    }

    private void initialize(Vertex source) {

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
    HashSet<Edge> outEdges;
    /* will be used as a temporary area to compute the shortest distance */
    double minDistTemp;
    /*the vertex last visited before this node*/
    Vertex last;

    Vertex(String name) {
        this.name = name;
        this.outEdges = new HashSet<>();
//            distances = new HashMap<>();
        status = UP;
        last = null;
    }

    void updateEdge(Edge edge) {
        //TODO update edges if the specified edge exists in the outEdges
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