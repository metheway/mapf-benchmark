package utilities;

import solvers.astar.State;

public interface IClosedList {

    boolean contains(State state);
    void add(State state);
    void clear();

}
