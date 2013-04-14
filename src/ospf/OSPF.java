/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ospf;

import java.util.HashSet;
import java.util.Objects;

/**
 *
 * @author Phani Rahul
 */
public class OSPF {

    /*Constants..*/
    int UP = 1;
    int DOWN = 2;
    
    /*Globals*/
    HashSet<Vertex> master; //This stores all the Vertex-es in the graph

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        System.out.println(".......:)");
    }

    class Vertex {

        int status;
        String name;
        HashSet<Edge> outEdges;

        Vertex(String name) {
            this.name = name;
            this.outEdges = new HashSet<>();
            status = UP;
        }
        void updateEdge(Edge edge){
            //TODO
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
        
    }

    class Edge implements Comparable<Edge> {

        int status;
        double time;
        Vertex toVertex;
        Vertex fromVertex;

        public Edge(Vertex fromVertex, double time, Vertex toVertex) {
            this.time = time;
            this.toVertex = toVertex;
            this.fromVertex=fromVertex;
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
            if(this.status==UP){
            ret = (new Double(this.time)).compareTo(other.time);
            }
            else ret = Integer.MAX_VALUE;
            return ret;
        }
        
    }
}
