package com.dimmingEchoes.dungeon;

public class GridBuilder {

    public static int[][] build(int width, int height, boolean outerWalls, boolean doors) {
        int[][] grid = new int[width][height];

        if (outerWalls) {
            for (int x = 0; x < width; x++) {
                grid[x][0] = 1;
                grid[x][height - 1] = 1;
            }
            for (int y = 0; y < height; y++) {
                grid[0][y] = 1;
                grid[width - 1][y] = 1;
            }
        }

        if (doors) {
            if (width >= 5 && height >= 5) {
                grid[1][height / 2] = 2;
                grid[width - 2][height / 2] = 2;
                grid[width / 2][1] = 2;
                grid[width / 2][height - 2] = 2;
            }
        }

        return grid;
    }
}
