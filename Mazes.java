import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.HashMap;
import javalib.impworld.*;

import java.applet.Applet;
import java.awt.Color;
import javalib.worldimages.*;

//main class
public class Mazes {
  
  public static void main(String[] args) {
    Examples e = new Examples();
    e.testGame();
  }
  
}

/* HOW TO PLAY
 * Arrow Keys - move up right left down
 * n or r - new game/reset game (both do the same)
 * d - depth first search
 * b - breadth first search
 * NOTE:
 * When the maze has been solved, press any n or r to reset. All other keys are locked.
 */

// class to represent a node in the graph
class Node {
  
  //unique name of this node "x,y"
  String name;
  //neighbors names
  ArrayList<String> neighbors;
  //utils class
  Utils u;
  //which node is the representative
  int parentNode;
  //the location of this node on the grid
  Posn p;
  
  //booleans representing whether each side of the node has an edge
  //true means there is NOT an edge (so draw the wall)
  //false means there is an edge (so don't draw the wall)
  boolean top;
  boolean bottom;
  boolean right;
  boolean left;
  
  //the node to the left, right, bottom, or top of of this node
  //only added if there is an edge (no wall)
  Node leftNode;
  Node rightNode;
  Node bottomNode;
  Node topNode;
    
  //a list of the edges coming out of this tree
  ArrayList<Edge> edges;
  
  //constructor used for testing
  Node(String name) {
    this.name = name;
    neighbors = new ArrayList<String>();
    u = new Utils();
    this.edges = new ArrayList<Edge>();
    this.top = true;
    this.bottom = true;
    this.right = true;
    this.left = true;
  }
  
  //constructor
  Node(String name, Posn p) {
    this.name = name;
    neighbors = new ArrayList<String>();
    u = new Utils();
    this.p = p;
    this.edges = new ArrayList<Edge>();
    this.top = true;
    this.bottom = true;
    this.right = true;
    this.left = true;
  }
  
  //EFFECT: adds the given node to this node's list of neighbors
  void addNeighbor(Node anode) {
    if (!(u.contains(neighbors, anode.name))) {
      this.neighbors.add(anode.name);
      anode.addNeighbor(this);
    }
  }
  
  //EFFECT: adds an edge to the this list of edges
  void addEdge(Edge e) {
    edges.add(e);
  }
  
  
  //EFFECT: changes the booleans that say if there is an edge or not
  //EFFECT: sets leftNode, rightNode, topNode, bottomNode to node if there is one
  void setSides() {
    for (Edge e : this.edges) {
      Node connection;
      if (e.first == this) { //if first is this, then second is what the edge is connected to
        connection = e.second;
      } else { //otherwise, second is this, first is what the edge is connected to
        connection = e.first;
      }
      int xdiff = this.p.x - connection.p.x;
      int ydiff = this.p.y - connection.p.y;

      if (xdiff == 1) {
        this.left = false;
        connection.right = false;
        this.leftNode = connection;
      } 
      if (xdiff == -1) {
        this.right = false;
        connection.left = false;
        this.rightNode = connection;
      }
      if (ydiff == 1) {
        this.top = false;
        connection.bottom = false;
        this.topNode = connection;
      }
      if (ydiff == -1) {
        this.bottom = false;
        connection.top = false;
        this.bottomNode = connection;
      }
    }
  }
  
}

// class to represent an edge in a graph
class Edge implements Comparable<Edge> {
  
  //node that edge is coming from
  Node first;
  //node that edge is going to
  Node second;
  //weight of the edge
  int weight;
  //location of the edge 
  Posn loc;
  //if the edge is vertical or not
  boolean vertical;
  //unique name of this edge
  String name;
  
  //constructor
  Edge(Node first, Node second, Posn loc, boolean vertical) {
    this.first = first;
    this.second = second;
    this.loc = loc;
    this.vertical = vertical;
    this.name = this.first.name + this.second.name;
  }
  
  //constructor for testing
  Edge(int weight) {
    this.weight = weight; 
    this.first = new Node("");
    this.second = new Node("");
    this.loc = new Posn(0,0);
    this.vertical = false;    
  }
  
  //EFFECT: sets the weight of this edge
  void setWeight(int weight) {
    this.weight = weight;
  }
  
  //produces an image of this edge
  WorldImage draw() {
    return new RectangleImage(8, 10, OutlineMode.SOLID, Color.WHITE);
  }

  //overrides the compareTo for the collection sorting to sort by weight
  public int compareTo(Edge o) {
    return (this.weight - o.weight);
  }

}


//utility class
class Utils {
  
  //determines if the given arrayList<String> contains the given target string
  public boolean contains(ArrayList<String> alist, String target) {    
    for (String s : alist) {
      if (s.equals(target)) {
        return true;
      }
    }
    return false;
  }
 
  //returns the value of the parent of the given node
  int find(ArrayList<Integer> map, Node target) {
    return map.get(target.parentNode);
  }
  
  //creates a union between the two given int locations in map
  //and then updates the given map and list of nodes
  void union(ArrayList<Integer> map, ArrayList<Node> nodes, int first, int second) {
    for (Node n : nodes) {
      if (n.parentNode == second) {
        n.parentNode = first;
      }
    }
    for (Integer i : map) {
      if (i == second) {
        i = first;
      }
    }
  }
  
}

//class to represent the world for the Maze game
class MazeWorld extends World {
  
  //edges of the graph
  ArrayList<Edge> graph;
  //the nodes
  ArrayList<ArrayList<Node>> nodes;
  //size of the graph
  int xsize;
  int ysize;
  //seed
  int seed = 0;
  
  //USED FOR PLAYER VERSION
  //current location of the user
  Node current;
  //node in the top left
  Node start;
  //node in the bottom right
  Node destination;
  //tells if the user has won the game or not
  boolean gameOver;
  //the path that the user took to get to the end
  ArrayList<Node> solutionPath;
  
  //USED FOR DFS/BFS VERSION
  //all the nodes passed through in the DFS/BFS
  ArrayList<Node> solutionPathGenerated;
  //the found path with DFS/BFS
  ArrayList<Node> solutionPathShortest;
  //tells if a path has been found
  boolean foundPath;
  //gives the current location that is being animated
  int animate;
  
  //convenience constructor for testing
  MazeWorld(int xsize, int ysize, int seed) {
    this.xsize = xsize;
    this.ysize = ysize;
    this.seed = seed;
    
    generateNodes();
    generateEdges();
    generateWeights();
    this.current = nodes.get(0).get(0);
    this.start = nodes.get(0).get(0);
    this.destination = nodes.get(xsize - 1).get(ysize - 1);
    this.gameOver = false;
    this.solutionPath = new ArrayList<Node>();
    this.solutionPathGenerated = new ArrayList<Node>();
    this.solutionPathShortest = new ArrayList<Node>();
    this.animate = 0;
    this.foundPath = false;
  }
  
  //regular constructor
  MazeWorld(int xsize, int ysize) {
    this.xsize = xsize;
    this.ysize = ysize;
    
    generateNodes();
    generateEdges();
    generateWeights();
    this.current = nodes.get(0).get(0);
    this.start = nodes.get(0).get(0);
    this.destination = nodes.get(xsize - 1).get(ysize - 1);
    this.gameOver = false;
    this.solutionPath = new ArrayList<Node>();
    this.solutionPathGenerated = new ArrayList<Node>();
    this.solutionPathShortest = new ArrayList<Node>();
    this.animate = 0;
    this.foundPath = false;
  }
  
  //EFFECT: creates a new game
  void reset() {
    this.graph.clear();
    this.nodes.clear();
    generateNodes();
    generateEdges();
    generateWeights();
    this.current = nodes.get(0).get(0);
    this.start = nodes.get(0).get(0);
    this.destination = nodes.get(xsize - 1).get(ysize - 1);
    this.gameOver = false;
    this.getMST();
    this.solutionPath = new ArrayList<Node>();
    this.solutionPathGenerated = new ArrayList<Node>();
    this.solutionPathShortest = new ArrayList<Node>();
    this.foundPath = false;
    this.animate = 0;
  }
  
  //EFFECT: generates the edges in the graph from the list of nodes
  void generateEdges() {
    graph = new ArrayList<Edge>();
    
    int nextColNum;
    
    for (int i = 0; i < this.xsize; i++) {
      ArrayList<Node> col = nodes.get(i);
      
      ArrayList<Node> nextCol;
      
      nextColNum = i + 1;
      if (i >= this.xsize - 1) {
        nextCol = new ArrayList<Node>();
      } else {
        nextCol = nodes.get(nextColNum);
      }
            
      for (int j = 0; j < this.ysize; j++) {
        
        if (j < this.ysize - 1) {
          col.get(j).addNeighbor(col.get(j + 1));
          graph.add(new Edge(col.get(j), col.get(j + 1), new Posn(i, j), true));
        }
        if (i < this.xsize - 1) {
          col.get(j).addNeighbor(nextCol.get(j));
          graph.add(new Edge(col.get(j), nextCol.get(j), new Posn(i, j), false));
        }
           
      }
    }  
  }

  //EFFECT: creates random weights for each of the edges
  void generateWeights() {
    int numberEdges = graph.size();
    Random r;
    
    if (seed != 0) {
      r = new Random(seed);
    } else {
      r = new Random();
    }
    
    for (int i = 0; i < numberEdges; i++) {
      this.graph.get(i).setWeight((r.nextInt(20) + 1));
    }
    
    this.graph.get(0).setWeight(0);
  }
  
  //EFFECT: creates the nodes in the graph
  void generateNodes() {
    this.nodes = new ArrayList<ArrayList<Node>>();
    
    for (int i = 0; i < this.xsize; i++) {
      ArrayList<Node> col = new ArrayList<Node>();
      for (int j = 0; j < this.ysize; j++) { 
        String nodeString = new Integer(i).toString() + "," + new Integer(j).toString();
        
        col.add(new Node(nodeString, new Posn(i, j)));         
      }
      nodes.add(col);
    }
  }
  
  //EFFECT: finds the minimum spanning tree of the edges
  void getMST() {
    
    Utils u = new Utils();
   
    int nodeloc = 0;
    
    ArrayList<Node> listofNodes = new ArrayList<Node>();
    for (int i = 0; i < this.xsize; i++) {
      ArrayList<Node> col = this.nodes.get(i);
      for (int j = 0; j < this.ysize; j++) {
        listofNodes.add(col.get(j));
        // set parent of each node to itself
        col.get(j).parentNode = nodeloc;
        nodeloc += 1;
      }
    }
    
    Collections.sort(graph);
    
    int totalNodes = xsize * ysize;
    int totalEdges = totalNodes - 1;
    ArrayList<Integer> map = new ArrayList<Integer>(totalNodes);    
    
    if (totalNodes != 0) {
      for (int i = 0; i < totalNodes; i++) {
        map.add(i);
      } 
    }
    
    ArrayList<Edge> finalGraph = new ArrayList<Edge>();
    
    while (finalGraph.size() < totalEdges) {
      Edge e = graph.remove(0);

      if (u.find(map, e.first) == (u.find(map, e.second))) {
        // do nothing if the node creates a loop
      } else {
        finalGraph.add(e);
        e.first.addEdge(e);
        e.second.addEdge(e);
        u.union(map, listofNodes, u.find(map, e.first), u.find(map, e.second));
      }
    }
    
    graph.clear();
    graph.addAll(finalGraph);
    
    for (ArrayList<Node> n1 : this.nodes) {
      for (Node n : n1) {
        n.setSides();
      }
    }
  }
    
  //draws the maze onto the screen
  public WorldScene makeScene() {
    WorldScene temp = this.getEmptyScene();

    //draws the graph
    for (int i = 0; i < nodes.size(); i++) {
      for (int j = 0; j < nodes.get(i).size(); j++) {
        Node node = nodes.get(i).get(j);
        temp.placeImageXY(new RectangleImage(10, 10, OutlineMode.OUTLINE, Color.BLACK),
            (((node.p.x + 2) * 10)), (((node.p.y + 2) * 10)));
      }
    }

    //draws the MST
    int xpos = 0;
    int ypos = 0;
    for (int i = 0; i < graph.size(); i++) {
      xpos = (graph.get(i).loc.x + 2) * 10;
      ypos = (graph.get(i).loc.y + 2) * 10;
      if (graph.get(i).vertical) {
        temp.placeImageXY(graph.get(i).draw().movePinhole(0, -5), xpos, ypos);
      } else {
        temp.placeImageXY(new RotateImage(graph.get(i).draw(), 90).movePinhole(-5, 0), xpos, ypos);
      }
    }  

    //draws the green square in the upper left
    temp.placeImageXY(new RectangleImage(8, 8, OutlineMode.SOLID, Color.GREEN), 
        (start.p.x + 2) * 10, (start.p.y + 2) * 10);
    //draws the purple square in the bottom right
    temp.placeImageXY(new RectangleImage(8, 8, OutlineMode.SOLID, Color.MAGENTA), 
        (destination.p.x + 2) * 10, (destination.p.y + 2) * 10);


    //display you win if you win
    if (this.gameOver) {
      temp.placeImageXY(new TextImage("You Win!", Color.GREEN), 30, 10);
    }
    
    //display the solution path as the user is playing
    for (Node n : this.solutionPath) {
      temp.placeImageXY(new RectangleImage(6, 6, OutlineMode.SOLID, Color.ORANGE),  
          (((n.p.x + 2) * 10)), (((n.p.y + 2) * 10)));
    }

    //draws the user's current location
    temp.placeImageXY(new RectangleImage(6, 6, OutlineMode.SOLID, Color.BLUE), 
        (current.p.x + 2) * 10, (current.p.y + 2) * 10);

    //animates the DFS/BFS search
    if (this.foundPath) {
      //if animating is not done, then draw the next point
      if (animate != this.solutionPathGenerated.size()) {
        for (int i = 0; i < this.animate; i++) {
          Node n = this.solutionPathGenerated.get(i);
          temp.placeImageXY(new RectangleImage(6, 6, OutlineMode.SOLID, Color.CYAN),  
              (((n.p.x + 2) * 10)), (((n.p.y + 2) * 10)));
        }
        this.animate++;
      }

      //if animating is done, then draw the entire search and the path
      if (this.animate == this.solutionPathGenerated.size()) {
        for (Node n : this.solutionPathGenerated) {
          temp.placeImageXY(new RectangleImage(6, 6, OutlineMode.SOLID, Color.CYAN),  
              (((n.p.x + 2) * 10)), (((n.p.y + 2) * 10)));
        }
        for (Node n : this.solutionPathShortest) {
          temp.placeImageXY(new RectangleImage(5, 5, OutlineMode.SOLID, Color.ORANGE),  
              (((n.p.x + 2) * 10)), (((n.p.y + 2) * 10)));
        }
      }
    }

    //return the scene
    return temp;
  }
  
  //EFFECT: changes the world based on the given key event
  public void onKeyEvent(String key) {
    //if the user won the game, then press n or r to reset
    //if the user presses anything else, input is discarded
    if (this.gameOver || this.foundPath) {
      if (key.equals("n") || key.equals("r")) {
        this.reset();
      }
      return;
    }
    //otherwise, switch on the key
    switch (key) {
      case "left": //user move left
        if (!current.left && !this.foundPath) {
          this.solutionPath.add(this.current);
          this.current = this.current.leftNode;
        }
        break;
      case "right": //user move right
        if (!current.right && !this.foundPath) {
          this.solutionPath.add(this.current);
          this.current = this.current.rightNode;
        }
        break;
      case "down": //user move down
        if (!current.bottom && !this.foundPath) {
          this.solutionPath.add(this.current);
          this.current = this.current.bottomNode;
        }
        break;
      case "up": //user move up
        if (!current.top && !this.foundPath) {
          this.solutionPath.add(this.current);
          this.current = this.current.topNode;
        }
        break;
      case "n": //creates a new maze
      case "r": //fall-through
        this.reset();
        break;
      case "d": //depth first search
        this.solutionPath.clear();
        this.current = this.start;
        this.getPath(new Stack<Node>());
        break;
      case "b": //breadth first search
        this.solutionPath.clear();
        this.current = this.start;
        this.getPath(new Queue<Node>());
        break;
      default:
        break;
    }
    
  }
  
  //EFFECT: each tick checks if the user is at the destination, sets variable if so
  public void onTick() {
    if (this.current == this.destination) {
      this.gameOver = true;
    }
  }
  
  //EFFECT: searches the graph with the given worklist
  //this code was adapted from code given in lecture
  void getPath(ICollection<Node> worklist) {
    //map containing parent nodes so it is possible to trace the path back
    HashMap<Node, Node> cameFromEdge = new HashMap<Node, Node>();
    //start and end
    Node from = this.start;
    Node to = this.destination;

    //nodes seen so far
    ArrayList<Node> seen = new ArrayList<Node>();
    worklist.add(from);
    
    while (worklist.size() > 0) {
      Node next = worklist.remove();
      this.solutionPathGenerated.add(next);
      
      if (next.equals(to)) {
        this.foundPath = true;
        Node node1 = to;
        while (cameFromEdge.get(node1) != from) {
          this.solutionPathShortest.add(node1);
          node1 = cameFromEdge.get(node1);
        }
        this.solutionPathShortest.add(node1);
        this.solutionPathShortest.add(from);
        return;
      } else if (seen.contains(next)) {
        //discard
      } else {
        for (Edge e : next.edges) {
          Node n = null;
          if (e.first == next) {
            n = e.second;
          } else {
            n = e.first;
          }
          //make sure there isn't a cycle by going back and forth between two nodes on same edge
          if (cameFromEdge.get(next) != n) {
            cameFromEdge.put(n, next);
          }
          worklist.add(n);
        }
      }
      seen.add(next);
    }
  }
  
}

//a way to abstract by using different data types (stack/queue)
interface ICollection<T> {
  //adds an item to this collection
  void add(T item);
  
  //removes an item
  T remove();
  
  //gets size of this collection
  int size();
}

//class representing a stack
class Stack<T> implements ICollection<T> {

  //uses our implementation of a Deque
  Deque<T> items;

  //constructor
  Stack() {
    this.items = new Deque<T>();
  }

  //EFFECT: pushes an item
  public void add(T item) {
    this.items.addAtHead(item);
  }

  //pops an item and returns it
  public T remove() {
    return this.items.removeFromHead();
  }

  //gets the size of this stack
  public int size() {
    return this.items.size();
  }

}

//class representing a queue
class Queue<T> implements ICollection<T> {

  //uses our implementation of a Deque
  Deque<T> items;

  //constructor
  Queue() {
    this.items = new Deque<T>();
  }

  //EFFECT: enqueues given item
  public void add(T item) {
    this.items.addAtTail(item);
  }

  //dequeues item and returns it
  public T remove() {
    return this.items.removeFromHead();
  }

  //gets the size of the stack
  public int size() {
    return this.items.size();
  }

}

//examples class
class Examples {
  
  //runs the game
  void testGame() { 
    //***** CHANGE THE SIZE HERE *****
    //constructor takes xsize, ysize
    MazeWorld test = new MazeWorld(20, 20);
    
    test.getMST();
    
    //***** CHANGE THE SPEED HERE *****
    //increasing the last argument of big-bang will slow down the game
    test.bigBang(test.xsize * 12 + 100, test.ysize * 12 + 100, 0.05);
  }
  
}

abstract class ANode<T> {

  //fields of the next and prev are shared in the Sentinel and Node classes
  ANode<T> next;
  ANode<T> prev;

  //constructor
  ANode(ANode<T> next, ANode<T> prev) {
    this.next = next;
    this.prev = prev;
  }

  //EFFECT: updates this.prev with the given node
  void updatePrev(ANode<T> that) {
    this.prev = that;
  }

  //EFFECT: updates this.next with the given node
  void updateNext(ANode<T> that) {
    this.next = that;
  }

  //helper to get the size, by default returns 0, but overridden in the Node class
  int sizeH(ANode<T> end) {
    return 0;
  }

  //helper to find that applies the predicate to the data to check it
  abstract ANode<T> findH(IPred<T> pred);

  //helper to remove the given node from the deque 
  //--does nothing by default, overridden in the Node class
  void removeH(ANode<T> n) { 
    return;
  }

}

//a node is something in the list that contains data
class DNode<T> extends ANode<T> {

  //data in this node
  T data;

  //first constructor: initializes the data field to what's given
  //--and sets the next and prev to null
  DNode(T data) {
    super(null, null);
    this.data = data;
  }

  //second constructor: initializes the data to what's given
  //--calls super constructor with the given next and prev
  //--then updates the next and prev in their nodes if not null
  DNode(T data, ANode<T> next, ANode<T> prev) {
    super(next, prev);
    this.data = data;
    if (next == null || prev == null) {
      throw new IllegalArgumentException("Node with null next or prev");
    } else {
      this.next.updatePrev(this);
      this.prev.updateNext(this);
    }
  }

  //helper for size that checks if end of the list is this, ends recursion if so
  //---if not adds 1 and recurs
  int sizeH(ANode<T> end) {
    if (this.equals(end)) {
      return 1;
    } else {
      return 1 + this.next.sizeH(end);
    }
  }

  //helper for find that uses the predicate to check if this is the correct node to return
  //--if not, recur on the next
  ANode<T> findH(IPred<T> pred) {
    if (pred.apply(this.data)) {
      return this;
    } else {
      return this.next.findH(pred);
    }
  }

  //helper that checks if the given node is equal to this node
  //--if so, "remove" this node by fixing up the links
  //--if not, recur on the rest
  void removeH(ANode<T> n) {
    if (this.equals(n)) {
      this.next.updatePrev(this.prev);
      this.prev.updateNext(this.next);
    } else {
      this.next.removeH(n);
    }
  }

}

//sentinel class always has the head as next, always has the tail (last) as prev
class Sentinel<T> extends ANode<T> {

  //constructor takes 0 args and sets prev and next to itself
  Sentinel() {
    super(null, null);
    this.next = this; //the first thing in the list
    this.prev = this; //the last thing in the list
  }

  //gets the size of this list by delegating to a helper with the front of the list
  //---while passing the end of the list
  int size() {
    return this.next.sizeH(this.prev);
  }

  //adds something to the head of the list
  void addAtHead(T t) {
    //creates a node with t
    DNode<T> n = new DNode<T>(t);
    //updates n's prev to be the sentinel
    n.updatePrev(this);
    //updates n's next to be the current first of the list
    n.updateNext(this.next);
    //updates current next's prev to be the new node
    this.next.updatePrev(n);
    //makes this node the beginning of the list by changing the head to n
    this.next = n;
  }

  //add something to the tail of the list
  void addAtTail(T t) {
    DNode<T> n = new DNode<T>(t);
    //sets the next in n to be the sentinel
    n.updateNext(this);
    //sets the previous in n to be the current tail
    n.updatePrev(this.prev);
    //updates the current prev's next to be the new node
    this.prev.updateNext(n);
    //makes the new node the tail of the list
    this.prev = n;
  }

  //removes the head of this list
  T removeFromHead() {
    if (this.next.equals(this)) {
      throw new RuntimeException("can't remove from an empty list");
    } else {
      //we know next is not a sentinel, so by safe casting
      //this allows us to return the T but fix the links first
      //...we were told that we could use field of field, so I hope this is ok
      T save = ((DNode<T>)this.next).data;
      //sets the head to the next node
      this.updateNext(this.next.next);
      //updates that node's previous to this (sentinel)
      this.next.updatePrev(this);
      //return the data that was in the old head
      return save;
    }
  }

  //removes the tail from this list
  T removeFromTail() {
    if (this.next.equals(this)) {
      throw new RuntimeException("can't remove from an empty list");
    } else {
      //we know next is not a sentinel, so by safe casting
      //this allows us to return the T but fix the links first
      //...we were told that we could use field of field, so I hope this is ok
      T save = ((DNode<T>)this.prev).data;
      //sets the tail of the node to the next one to the left
      this.updatePrev(this.prev.prev);
      //sets that node's next to this (the sentinel)
      this.prev.updateNext(this);
      //return the data that was in the old tail
      return save;
    }
  }

  //finds the first thing in this list that passes the predicate
  ANode<T> find(IPred<T> pred) {
    return this.next.findH(pred);
  }

  //if it reaches the sentinel again (empty case), return the sentinel
  ANode<T> findH(IPred<T> pred) {
    return this;
  }

  //removes the given node from this list
  void remove(ANode<T> n) {
    this.next.removeH(n);
  }

}

//a Deque refers to a list
class Deque<T> {

  //the sentinel always has 
  Sentinel<T> header;

  //initializes header to a new Sentinel
  Deque() {
    this.header = new Sentinel<T>();
  }

  //sets the header to a given sentinel
  Deque(Sentinel<T> sent) {
    this.header = sent;
  }

  //get the size of this deque
  int size() {
    return this.header.size();
  }

  //adds a node to the head
  void addAtHead(T n) {
    this.header.addAtHead(n);
  }

  //adds a node to the tail
  void addAtTail(T n) {
    this.header.addAtTail(n);
  }

  //removes the head from this deque
  T removeFromHead() {
    return this.header.removeFromHead();
  }

  //removes the tail from this deque
  T removeFromTail() {
    return this.header.removeFromTail();
  }

  //finds the first node in this list that passes the predicate
  ANode<T> find(IPred<T> pred) {
    return this.header.find(pred);
  }

  //removes the given node from the list
  void removeNode(ANode<T> n) {
    this.header.remove(n);
  }


}

//Represents a boolean-valued question over values of type T
interface IPred<T> {

  //apply function that returns true if the given T passes the predicate
  boolean apply(T t);

}