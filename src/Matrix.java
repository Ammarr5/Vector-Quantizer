import java.util.ArrayList;
import java.util.Objects;

public class Matrix {
    ArrayList<ArrayList<Integer>> matrix;
    int rows, cols;
    public Matrix(int rows, int cols){
        matrix = new ArrayList<>();
        this.rows = rows;
        this.cols = cols;
        for (int i=0; i<rows; i++) {
            ArrayList<Integer> row = new ArrayList<>();
            for (int j=0; j<cols; j++) {
                row.add(0);
            }
            matrix.add(row);
        }
    }

    public Matrix(int[][] mt) {
        matrix = new ArrayList<>();
        rows = mt.length;
        cols = mt[0].length;
        for (int i=0; i<rows; i++) {
            ArrayList<Integer> row = new ArrayList<>();
            for (int j=0; j<cols; j++) {
                row.add(mt[i][j]);
            }
            matrix.add(row);
        }
    }

    public Matrix(ArrayList<ArrayList<Integer>> matrix) {
        rows = matrix.size();
        cols = matrix.get(0).size();
        this.matrix = matrix;
    }

    public int get(int i, int j) {
        return matrix.get(i).get(j);
    }

    public void insert(int i, int j, int value) {
        matrix.get(i).set(j, value);
    }

    public int getEuclideanDistance(Matrix matrix) {
        int distance = 0;
        for (int i=0; i<rows; i++) {
            for (int j=0; j<cols; j++) {
                int sub = get(i, j) - matrix.get(i, j);
                distance += sub*sub;
            }
        }
        return distance;
    }

    public Matrix[] splitMatrix() {
        Matrix before = new Matrix(rows, cols), after = new Matrix(rows, cols);
        for (int i=0; i<rows; i++) {
            for (int j=0; j<cols; j++) {
                before.insert(i, j, get(i, j)-1);
                after.insert(i, j, get(i, j)+1);
            }
        }
        return new Matrix[]{before, after};
    }

    public int getNearestMatrix(ArrayList<Matrix> matrices) {
        int minIndex = 0;
        int minDistance = getEuclideanDistance(matrices.get(0));
        for (int i=1; i<matrices.size(); i++) {
            int distance = getEuclideanDistance(matrices.get(i));
            if (distance < minDistance) {
                minDistance = distance;
                minIndex = i;
            }
        }
        return minIndex;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public ArrayList<ArrayList<Integer>> getMatrix() {
        return matrix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Matrix matrix1 = (Matrix) o;
        return rows == matrix1.rows && cols == matrix1.cols && Objects.equals(matrix, matrix1.matrix);
    }

    @Override
    public String toString() {
//        return matrix.toString() + "\n";
        return matrix.toString();
    }
}
