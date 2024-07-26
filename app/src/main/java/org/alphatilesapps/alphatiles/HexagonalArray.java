package org.alphatilesapps.alphatiles;

import java.util.Iterator;
import java.util.LinkedList;

public class HexagonalArray<T> {

    private int height, width;

    T[][] hexArray;
    @SuppressWarnings("unchecked")
    public HexagonalArray(int width, int height) {
        hexArray = (T[][]) new Object[width][height];
        this.height = height;
        this.width = width;
    }

    @SuppressWarnings("unchecked")
    public HexagonalArray(T[][] array) {
        hexArray = array;
        this.height = array[0].length;
        this.width = array.length;
    }

    int getHeight() { return height; }
    int getWidth() { return width; }

    public T get(int x, int y) {
        return hexArray[x][y];
    }

    public void set(int x, int y, T item) {
        hexArray[x][y] = item;
    }

    /**
     * returns true if the x-column would be drawn higher if it were to be visualized
     */
    public boolean isUp(int x) { return x % 2 == 0; }

    public void swap(int x0, int y0, int x1, int y1) {
        T temp = get(x0, y0);
        set(x0, y0, get(x1, y1));
        set(x1, y1, temp);
    }

    public LinkedList<T> getAdjacents(int x, int y) {
        // make sure the parameters are within bounds
        assert x >= 0 && y >= 0 && x < hexArray.length && y < hexArray[0].length;

        // the linked list holds all of the
        LinkedList<T> adjacents = new LinkedList<>();
        if (x % 2 == 0) { // if x is even
            if (x - 1 >= 0) {
                if (y - 1 >= 0)
                    adjacents.add(get(x - 1, y - 1));
                adjacents.add(get(x - 1, y));
            }
            if (y - 1 >= 0)
                adjacents.add(get(x, y - 1));
            if (y + 1 < hexArray[0].length)
                adjacents.add(get(x, y + 1));
            if (x + 1 < hexArray.length) {
                if (y - 1 >= 0)
                    adjacents.add(get(x + 1, y - 1));
                adjacents.add(get(x + 1, y));
            }
        } else { // if x is odd
            if (x - 1 >= 0) {
                adjacents.add(get(x - 1, y));
                if (y + 1 < hexArray[0].length)
                    adjacents.add(get(x - 1, y + 1));
            }
            if (y - 1 >= 0)
                adjacents.add(get(x, y - 1));
            if (y + 1 < hexArray[0].length)
                adjacents.add(get(x, y + 1));
            if (x + 1 < hexArray.length) {
                adjacents.add(get(x + 1, y));
                if (y + 1 < hexArray[0].length)
                    adjacents.add(get(x + 1, y + 1));
            }
        }
        return adjacents;
    }

    public LinkedList<T> getSwappableAdjacents(int x, int y) {
        // make sure the parameters are within bounds
        assert x >= 0 && y >= 0 && x < hexArray.length && y < hexArray[0].length;

        // the linked list holds all of the
        LinkedList<T> adjacents = new LinkedList<>();
        if (x % 2 == 0) { // if x is even
            if (x - 1 >= 0) {
                if (y - 1 >= 0)
                    adjacents.add(get(x - 1, y - 1));
                adjacents.add(get(x - 1, y));
            }
            if (x + 1 < hexArray.length) {
                if (y - 1 >= 0)
                    adjacents.add(get(x + 1, y - 1));
                adjacents.add(get(x + 1, y));
            }
        } else { // if x is odd
            if (x - 1 >= 0) {
                adjacents.add(get(x - 1, y));
                if (y + 1 < hexArray[0].length)
                    adjacents.add(get(x - 1, y + 1));
            }
            if (x + 1 < hexArray.length) {
                adjacents.add(get(x + 1, y));
                if (y + 1 < hexArray[0].length)
                    adjacents.add(get(x + 1, y + 1));
            }
        }
        return adjacents;
    }

}
