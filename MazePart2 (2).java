import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.*;

// represents a mutable collection of items
interface ICollection<T> {
  // determines if an ICollection is empty
  boolean isEmpty();
  
  // removes an item from an ICollection
  T remove();
  
  // adds a given item to an ICollection
  void add(T item);
}

// represents a stack (last in first out)
class Stack<T> implements ICollection<T> {
  Deque<T> contents;
  
  // constructor for Stack which initializes the deque
  Stack() {
    this.contents = new LinkedList<T>();
  }

  // determines if a stack is empty
  @Override
  public boolean isEmpty() {
    return contents.isEmpty();
  }

  // removes an item from an ICollection
  @Override
  public T remove() {
    return contents.removeFirst();
  }

  // adds an item to an ICollection
  @Override
  public void add(T item) {
    contents.addFirst(item);
  }
}

// represents a queue (first in first out)
class Queue<T> implements ICollection<T> {
  Deque<T> contents;
  
  // constructor for Queue which initializes the deque
  Queue() {
    this.contents = new LinkedList<T>();
  }

  // determines whether a queue is empty
  @Override
  public boolean isEmpty() {
    return this.contents.isEmpty();
  }

  // removes an item from a queue
  @Override
  public T remove() {
    return this.contents.removeFirst();
  }

  // adds an item to a queue
  @Override
  public void add(T item) {
    this.contents.addLast(item);
  }
}

// represents a square on the maze
class Vertex {
  ArrayList<Edge> outEdges; // edges from this node
  int x;
  int y;
  Posn position;
  Color color;

  // constructor for a vertex
  Vertex(int x, int y) {
    this.outEdges = new ArrayList<Edge>();
    this.x = x;
    this.y = y;
    this.position = new Posn(x, y);
    this.color = Color.gray;
  }

  // draws the vertex
  WorldImage drawVertex() {
    return new RectangleImage(MazeWorld.VERTEX_SIZE, MazeWorld.VERTEX_SIZE, 
        OutlineMode.SOLID, color);
  }
}

// represents an edge leading to another vertex
class Edge {
  Vertex from;
  Vertex to;
  Random rand;
  int weight;

  // constructor for an edge
  Edge(Vertex from, Vertex to) {
    this.from = from;
    this.to = to;
    this.rand = new Random();
    this.weight = rand.nextInt(1000);
  }

  // constructor for an edge with an assigned weight
  // for testing purposes
  Edge(Vertex from, Vertex to, int weight) {
    this.from = from;
    this.to = to;
    this.rand = new Random();
    this.weight = weight;
  }
}

// represents a maze with vertices and edges
class MazeWorld extends World {
  static int WIDTH = 20;
  static int HEIGHT = 20;
  static int VERTEX_SIZE = 30;

  ArrayList<ArrayList<Vertex>> allVertices;
  ArrayList<Edge> allEdges;
  ArrayList<Vertex> explored;
  ArrayList<Vertex> solution;
  HashMap<Posn, Posn> cameFromEdge;
  boolean found;
  int count;
  int increment;
  int timer;
  int seconds;
  int minutes;

  // constructor for MazeWorld
  MazeWorld() {
    this.timer = 0;
    this.seconds = 0;
    this.minutes = 0;
    ArrayList<ArrayList<Vertex>> vertices = new ArrayList<ArrayList<Vertex>>();
    ArrayList<Edge> edges = new ArrayList<Edge>();
    HashMap<Posn, Posn> map = new HashMap<Posn, Posn>();

    for (int i = 0; i < HEIGHT; i++) {
      ArrayList<Vertex> row = new ArrayList<Vertex>();
      for (int j = 0; j < WIDTH; j++) {
        Vertex newVertex = new Vertex(j, i);
        row.add(newVertex);
        map.put(new Posn(newVertex.x, newVertex.y), 
            new Posn(newVertex.x, newVertex.y));
      }
      vertices.add(row);
    }

    vertices.get(0).get(0).color = Color.blue;
    vertices.get(HEIGHT - 1).get(WIDTH - 1).color = Color.green;

    for (int i = 0; i < HEIGHT; i++) {
      for (int j = 0; j < WIDTH; j++) {
        if (j != 0) {
          edges.add(new Edge(vertices.get(i).get(j), 
              vertices.get(i).get(j - 1)));
        }
        if (j != WIDTH - 1) {
          edges.add(new Edge(vertices.get(i).get(j), 
              vertices.get(i).get(j + 1)));
        }
        if (i != 0) {
          edges.add(new Edge(vertices.get(i).get(j), 
              vertices.get(i - 1).get(j)));
        }
        if (i != HEIGHT - 1) {
          edges.add(new Edge(vertices.get(i).get(j), 
              vertices.get(i + 1).get(j)));
        }
      }
    }

    edges.sort((e1, e2) -> e1.weight - e2.weight);

    this.count = 0;
    this.increment = 0;

    int edgeCount = 0;

    while (edgeCount < (WIDTH * HEIGHT) - 1) {
      for (int i = 0; i < edges.size(); i++) {
        Edge e = edges.get(i);
        if (!(find(map, e.to.position).equals(find(map, e.from.position)))) {
          edgeCount++;
          e.from.outEdges.add(e);
          e.to.outEdges.add(new Edge(e.to, e.from, e.weight));
          Posn p = find(map, e.to.position);
          map.remove(find(map, e.to.position));
          map.put(p, find(map, e.from.position));
        }
      }
    }

    this.allVertices = vertices;
    this.allEdges = edges;
    this.explored = new ArrayList<Vertex>();
    this.solution = new ArrayList<Vertex>();
    this.cameFromEdge = new HashMap<Posn, Posn>();
    this.found = false;
  }

  // MazeWorld constructor for testing purposes
  MazeWorld(ArrayList<ArrayList<Vertex>> allVertices, 
      ArrayList<Vertex> explored, ArrayList<Vertex> solution) {
    this.allVertices = allVertices;
    this.explored = explored;
    this.solution = solution;
    this.allEdges = new ArrayList<Edge>();
    this.cameFromEdge = new HashMap<Posn, Posn>();
    this.found = false;
    this.count = 0;
    this.increment = 0;
    this.timer = 0;
    this.seconds = 0;
    this.minutes = 0;
  }

  // finds the corresponding position in the HashMap
  Posn find(HashMap<Posn, Posn> map, Posn p) {
    if (map.get(p) == p) {
      return p;
    } else {
      return find(map, map.get(p));
    }
  }

  // handles frame rate and animations 
  public void onTick() {
    if (this.count == WIDTH) {
      this.count = 0;
    }

    if (allVertices.get(HEIGHT - 1).get(WIDTH - 1).color == Color.blue) {
      if (this.increment >= solution.size()) {
        this.increment = 0;
      }
      solution.get(increment).color = Color.blue;
    }

    ArrayList<Edge> finalEdges = new ArrayList<Edge>();

    for (Edge e : allEdges) {
      boolean add = true;
      for (int i = 0; i < HEIGHT; i++) {
        for (Edge e2 : allVertices.get(i).get(count).outEdges) {
          if ((e.to == e2.to && e.from == e2.from)
              || (e.to == e2.from && e.from == e2.to)) {
            add = false;
          }
        }
      }
      if (add) {
        finalEdges.add(e);
      }
    }

    if (!explored.isEmpty()) {
      if (this.increment >= explored.size()) {
        this.increment = 0;
      }
      explored.get(increment).color = Color.cyan;
    }

    if (allVertices.get(HEIGHT - 1).get(WIDTH - 1).color == Color.cyan) {
      allVertices.get(HEIGHT - 1).get(WIDTH - 1).color = Color.blue;
      reconstruct(allVertices.get(HEIGHT - 1).get(WIDTH - 1));
    }

    for (Vertex v : solution) {
      explored.remove(v);
    }
    
    if (this.timer == 120) {
      timer = 0;
      seconds++;
    }
    if (this.seconds == 60) {
      minutes++;
      seconds = 0;
    }


    this.allEdges = finalEdges;
    count++;
    increment++;
    timer++;
  }

  // handles keystrokes for the maze
  public void onKeyEvent(String key) {
    if (key.equals("b")) {
      bfs(allVertices.get(0).get(0), 
          allVertices.get(HEIGHT - 1).get(WIDTH - 1));
    }
    if (key.equals("d")) {
      dfs(allVertices.get(0).get(0), 
          allVertices.get(HEIGHT - 1).get(WIDTH - 1));
    }
  }

  // uses breadth first search to highlight the solution for the maze
  void bfs(Vertex from, Vertex to) {
    explored = new ArrayList<Vertex>();
    solution = new ArrayList<Vertex>();
    cameFromEdge = new HashMap<Posn, Posn>();
    increment = 0;
    timer = 0;
    seconds = 0;
    minutes = 0;
    found = false;
    for (ArrayList<Vertex> l : allVertices) {
      for (Vertex v : l) {
        v.color = Color.gray;
      }
    }
    allVertices.get(0).get(0).color = Color.blue;
    allVertices.get(HEIGHT - 1).get(WIDTH - 1).color = Color.green;
    searchHelp(from, to, new Queue<Vertex>());
  }

  // uses depth first search to highlight the solution for the maze
  void dfs(Vertex from, Vertex to) {
    explored = new ArrayList<Vertex>();
    solution = new ArrayList<Vertex>();
    cameFromEdge = new HashMap<Posn, Posn>();
    increment = 0;
    timer = 0;
    seconds = 0;
    minutes = 0;
    found = false;
    for (ArrayList<Vertex> l : allVertices) {
      for (Vertex v : l) {
        v.color = Color.gray;
      }
    }
    allVertices.get(0).get(0).color = Color.blue;
    allVertices.get(HEIGHT - 1).get(WIDTH - 1).color = Color.green;
    searchHelp(from, to, new Stack<Vertex>());
  }

  // helper for search algorithms
  void searchHelp(Vertex from, Vertex to, ICollection<Vertex> workList) {
    ArrayList<Vertex> alreadySeen = new ArrayList<Vertex>();

    workList.add(from);
    explored.add(from);
    while (!workList.isEmpty()) {
      Vertex next = workList.remove();
      if (next.equals(to)) {
        found = true;
        while (!workList.isEmpty()) {
          workList.remove();
        }
      }
      else if (alreadySeen.contains(next)) {
        // do nothing
      }
      else {
        for (Edge e : next.outEdges) {
          workList.add(e.to);
          explored.add(e.to);
          cameFromEdge.putIfAbsent(e.to.position, e.from.position);
        }        
        alreadySeen.add(next);
      }
    }
  }

  // highlights the shortest path after search
  void reconstruct(Vertex v) {
    solution.add(v);
    explored.remove(v);

    if (v != allVertices.get(0).get(0)) {
      for (int i = 0; i < allVertices.size(); i++) {
        for (int j = 0; j < allVertices.get(i).size(); j++) {
          if (allVertices.get(i).get(j).position.equals(cameFromEdge.get(v.position))) {
            reconstruct(allVertices.get(i).get(j));
          }
        }
      }
    }
  }

  // creates the scene
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene((WIDTH * VERTEX_SIZE) + 200, HEIGHT * VERTEX_SIZE);

    for (ArrayList<Vertex> l : allVertices) {
      for (Vertex v : l) {
        scene.placeImageXY(v.drawVertex(), (v.x * VERTEX_SIZE) + (VERTEX_SIZE / 2), 
            (v.y * VERTEX_SIZE) + (VERTEX_SIZE / 2));
      }
    }

    for (Edge e : allEdges) {
      if (e.to.x == e.from.x) {
        scene.placeImageXY(
            new RectangleImage(VERTEX_SIZE, VERTEX_SIZE / 10, OutlineMode.SOLID, Color.black),
            (e.to.x * VERTEX_SIZE) + (VERTEX_SIZE / 2),
            ((e.to.y + e.from.y) * VERTEX_SIZE / 2) + (VERTEX_SIZE / 2));
      }
      else {
        scene.placeImageXY(
            new RectangleImage(VERTEX_SIZE / 10, VERTEX_SIZE, OutlineMode.SOLID, Color.black),
            ((e.to.x + e.from.x) * VERTEX_SIZE / 2) + (VERTEX_SIZE * 1 / 2),
            (e.to.y * VERTEX_SIZE) + (VERTEX_SIZE / 2));
      }
    }
    
    String secondsString = "";

    if (this.seconds < 10) {
      secondsString = "0" + Integer.toString(this.seconds);
    }
    else {
      secondsString = Integer.toString(this.seconds);
    }
    
    scene.placeImageXY(new TextImage("Time : " + Integer.toString(this.minutes) + ":"
        + secondsString, 20, FontStyle.BOLD, Color.black), 
        (WIDTH * VERTEX_SIZE) + 100, HEIGHT * VERTEX_SIZE / 8);

    return scene;
  }
}

// Examples and tests for the maze and ICollection
class ExamplesMyWorldProgram {

  MazeWorld world;

  WorldScene scene;

  HashMap<Posn, Posn> map;

  Vertex v1;
  Vertex v2;
  Vertex v3;
  Vertex v4;

  Edge e1;
  Edge e2;
  Edge e3;
  Edge e4;

  Queue<Vertex> queue1;

  Stack<Vertex> stack1;

  WorldImage r1;
  WorldImage r2;
  WorldImage r3;
  WorldImage r4;

  // Establishes initial conditions for each test
  void initialConditions() {
    world = new MazeWorld();

    scene = new WorldScene((MazeWorld.WIDTH * MazeWorld.VERTEX_SIZE) + 200, 
        MazeWorld.HEIGHT * MazeWorld.VERTEX_SIZE);

    map = new HashMap<Posn, Posn>();
    map.put(new Posn(0, 0), new Posn(0, 0));
    map.put(new Posn(1, 0), new Posn(1, 1));
    map.put(new Posn(1, 1), new Posn(1, 1));

    v1 = new Vertex(0, 0);
    v2 = new Vertex(0, 1);
    v3 = new Vertex(0, 0);
    v4 = new Vertex(1, 1);

    e1 = new Edge(v1, v2);
    e2 = new Edge(v1, v2);
    e3 = new Edge(v2, v1);
    e4 = new Edge(v2, v4);

    queue1 = new Queue<Vertex>();

    r1 = new RectangleImage(MazeWorld.VERTEX_SIZE, MazeWorld.VERTEX_SIZE, 
        OutlineMode.SOLID, Color.blue);
    r2 = new RectangleImage(MazeWorld.VERTEX_SIZE, MazeWorld.VERTEX_SIZE, 
        OutlineMode.SOLID, Color.gray);
    r3 = new RectangleImage(MazeWorld.VERTEX_SIZE, MazeWorld.VERTEX_SIZE, 
        OutlineMode.SOLID, Color.cyan);
    r4 = new RectangleImage(MazeWorld.VERTEX_SIZE, MazeWorld.VERTEX_SIZE, 
        OutlineMode.SOLID, Color.green);
  }

  // Tests the isEmpty method
  void testIsEmpty(Tester t) {
    initialConditions();

    t.checkExpect(queue1.isEmpty(), true);
    queue1.contents.add(v1);
    queue1.contents.add(v2);
    t.checkExpect(queue1.isEmpty(), false);
    queue1.contents.remove();
    queue1.contents.remove();
    t.checkExpect(queue1.isEmpty(), true);
  }

  // Tests the remove method
  void testRemove(Tester t) {
    initialConditions();

    queue1.contents.add(v1);
    queue1.contents.add(v2);
    t.checkExpect(queue1.contents.size(), 2);
    queue1.remove();
    t.checkExpect(queue1.contents.size(), 1);
    t.checkExpect(queue1.contents.contains(v1), false);
    queue1.remove();
    t.checkExpect(queue1.isEmpty(), true);
  }

  // Tests the add method
  void testAdd(Tester t) {
    initialConditions();

    t.checkExpect(queue1.isEmpty(), true);
    queue1.add(v1);
    t.checkExpect(queue1.contents.size(), 1);
    t.checkExpect(queue1.contents.contains(v1), true);
    queue1.add(v2);
    t.checkExpect(queue1.contents.size(), 2);
    t.checkExpect(queue1.contents.contains(v2), true);
  }

  // Tests the find method
  void testFind(Tester t) {
    initialConditions();

    t.checkExpect(world.find(map, new Posn(0, 0)), new Posn(0, 0));
    t.checkExpect(world.find(map, new Posn(1, 1)), new Posn(1, 1));
    t.checkExpect(world.find(map, new Posn(1, 0)), new Posn(1, 1));
  }

  // Tests the onKeyEvent method
  void testOnKeyEvent(Tester t) {
    initialConditions();

    MazeWorld world2 = world;

    world2.bfs(world2.allVertices.get(0).get(0), 
        world2.allVertices.get(MazeWorld.HEIGHT - 1).get(MazeWorld.WIDTH - 1));
    world.onKeyEvent("b");
    t.checkExpect(world, world2);

    world2.dfs(world2.allVertices.get(0).get(0), 
        world2.allVertices.get(MazeWorld.HEIGHT - 1).get(MazeWorld.WIDTH - 1));
    world.onKeyEvent("d");
    t.checkExpect(world, world2);

  }

  // Tests the bfs method
  void testBfs(Tester t) {
    initialConditions();

    MazeWorld world2 = world;
    world.found = false;

    world2.explored = new ArrayList<Vertex>();
    world2.solution = new ArrayList<Vertex>();
    world2.cameFromEdge = new HashMap<Posn, Posn>();
    world2.increment = 0;
    world2.found = false;
    for (ArrayList<Vertex> l : world2.allVertices) {
      for (Vertex v : l) {
        v.color = Color.gray;
      }
    }
    world2.allVertices.get(0).get(0).color = Color.blue;
    world2.allVertices.get(MazeWorld.HEIGHT - 1).get(MazeWorld.WIDTH - 1).color = Color.green;
    world2.searchHelp(world2.allVertices.get(0).get(0), 
        world2.allVertices.get(MazeWorld.HEIGHT - 1).get(MazeWorld.WIDTH - 1), new Queue<Vertex>());

    world.bfs(world.allVertices.get(0).get(0), 
        world.allVertices.get(MazeWorld.HEIGHT - 1).get(MazeWorld.WIDTH - 1));
    t.checkExpect(world.found, true);
    t.checkExpect(world, world2);
  }

  // Tests the dfs method
  void testDfs(Tester t) {
    initialConditions();

    MazeWorld world2 = world;
    world.found = false;

    world2.explored = new ArrayList<Vertex>();
    world2.solution = new ArrayList<Vertex>();
    world2.cameFromEdge = new HashMap<Posn, Posn>();
    world2.increment = 0;
    for (ArrayList<Vertex> l : world2.allVertices) {
      for (Vertex v : l) {
        v.color = Color.gray;
      }
    }
    world2.allVertices.get(0).get(0).color = Color.blue;
    world2.allVertices.get(MazeWorld.HEIGHT - 1).get(MazeWorld.WIDTH - 1).color = Color.green;
    world2.searchHelp(world2.allVertices.get(0).get(0), 
        world2.allVertices.get(MazeWorld.HEIGHT - 1).get(MazeWorld.WIDTH - 1), new Stack<Vertex>());

    world.dfs(world.allVertices.get(0).get(0), 
        world.allVertices.get(MazeWorld.HEIGHT - 1).get(MazeWorld.WIDTH - 1));

    t.checkExpect(world.found, true);
    t.checkExpect(world, world2);
  }

  // Tests the searchHelp method
  void testSearchHelp(Tester t) {
    initialConditions();

    MazeWorld world2 = world;
    world.found = false;

    Queue<Vertex> workList = new Queue<Vertex>();

    ArrayList<Vertex> alreadySeen = new ArrayList<Vertex>();

    workList.add(world2.allVertices.get(0).get(0));
    world2.explored.add(world2.allVertices.get(0).get(0));
    while (!workList.isEmpty()) {
      Vertex next = workList.remove();
      if (next.equals(world2.allVertices.get(MazeWorld.HEIGHT - 1).get(MazeWorld.WIDTH - 1))) {
        world2.found = true;
        while (!workList.isEmpty()) {
          workList.remove();
        }
      }
      else if (alreadySeen.contains(next)) {
        // do nothing
      }
      else {
        for (Edge e : next.outEdges) {
          workList.add(e.to);
          world2.explored.add(e.to);
          world2.cameFromEdge.putIfAbsent(e.to.position, e.from.position);
        }        
        alreadySeen.add(next);
      }
    }

    world.searchHelp(world.allVertices.get(0).get(0), 
        world.allVertices.get(MazeWorld.HEIGHT - 1).get(MazeWorld.WIDTH - 1), new Queue<Vertex>());
    t.checkExpect(world.found, true);
    t.checkExpect(world, world2);
  }

  // Tests the reconstruct method
  void testReconstruct(Tester t) {
    initialConditions();

    ArrayList<Vertex> vertices = new ArrayList<Vertex>(Arrays.asList(v1, v2, v4));
    ArrayList<Vertex> listJawn = new ArrayList<Vertex>();
    ArrayList<ArrayList<Vertex>> allVerts = new ArrayList<ArrayList<Vertex>>();
    allVerts.add(vertices);

    MazeWorld world2 = new MazeWorld(allVerts, vertices, listJawn);

    world2.reconstruct(v4);
    listJawn.add(v4);
    t.checkExpect(world2.solution, listJawn);
    world2.cameFromEdge.put(v4.position, v2.position);
    listJawn.add(v2);
    world.cameFromEdge.put(v2.position, v1.position);
    listJawn.add(v1);
    world.reconstruct(v4);
    t.checkExpect(world2.solution, listJawn);
  }

  // Tests the drawVertex method
  void testDrawVertex(Tester t) {
    initialConditions();

    t.checkExpect(world.allVertices.get(0).get(0).drawVertex(), r1);
    t.checkExpect(v2.drawVertex(), r2);
    v2.color = Color.cyan;
    t.checkExpect(v2.drawVertex(), r3);
    t.checkExpect(world.allVertices.get(MazeWorld.HEIGHT - 1).
        get(MazeWorld.WIDTH - 1).drawVertex(), r4);
  }

  //Tests the makeScene method
  void testMakeScene(Tester t) {
    initialConditions();

    for (ArrayList<Vertex> l : world.allVertices) {
      for (Vertex v : l) {
        scene.placeImageXY(v.drawVertex(), (v.x * MazeWorld.VERTEX_SIZE) 
            + (MazeWorld.VERTEX_SIZE / 2), (v.y * MazeWorld.VERTEX_SIZE) 
            + (MazeWorld.VERTEX_SIZE / 2));
      }
    }

    for (Edge e : world.allEdges) {
      if (e.to.x == e.from.x) {
        scene.placeImageXY(
            new RectangleImage(MazeWorld.VERTEX_SIZE, MazeWorld.VERTEX_SIZE / 8, 
                OutlineMode.SOLID, Color.black), (e.to.x * MazeWorld.VERTEX_SIZE) 
            + (MazeWorld.VERTEX_SIZE / 2),((e.to.y + e.from.y) * MazeWorld.VERTEX_SIZE / 2) 
            + (MazeWorld.VERTEX_SIZE / 2));
      }
      else {
        scene.placeImageXY(
            new RectangleImage(MazeWorld.VERTEX_SIZE / 8, MazeWorld.VERTEX_SIZE, 
                OutlineMode.SOLID, Color.black), ((e.to.x + e.from.x) * MazeWorld.VERTEX_SIZE / 2) 
            + (MazeWorld.VERTEX_SIZE / 2),(e.to.y * MazeWorld.VERTEX_SIZE) 
            + (MazeWorld.VERTEX_SIZE / 2));
      }
    }
    

    
    scene.placeImageXY(new TextImage("Time : 0:00", 20, FontStyle.BOLD, Color.black), 
        (MazeWorld.WIDTH * MazeWorld.VERTEX_SIZE) + 100, 
        MazeWorld.HEIGHT * MazeWorld.VERTEX_SIZE / 8);


  

    t.checkExpect(world.makeScene(), scene);
  }

  // displays the scene
  void testGame(Tester t) {
    initialConditions();

    world.bigBang((MazeWorld.WIDTH * MazeWorld.VERTEX_SIZE) + 200,
        MazeWorld.HEIGHT * MazeWorld.VERTEX_SIZE, 1 / 120.0);
  }
}