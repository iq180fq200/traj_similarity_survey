package measures;

import entity.Point;
import entity.Trajectory;

import java.util.Arrays;


public class EDWP_DP implements CalDistance{
    public double GetDistance(Trajectory s, Trajectory t) {
//        Point[] t1Point = getAllPoints(s);
//        Point[] t2Point = getAllPoints(t);

//        STrajectory t1 = new STrajectory(0, -1, null, Arrays.asList(t1Point));
//        STrajectory t2 = new STrajectory(0, -1, null, Arrays.asList(t2Point));
        Matrix matrix = new Matrix(s.points.size(), t.points.size());

        initializeMatrix(matrix);

        Trajectory t1 = s;
        Trajectory t2 = t;

//        double totalLength = (Distance.getDistance(t1Point, distFunc) + Distance.getDistance(t2Point, distFunc));
        double totalLength = s.getTotalLength() + t.getTotalLength();

        for (int i = 1; i < matrix.numRows(); i++) {
            for (int j = 1; j < matrix.numCols(); j++) {
                double rowDelta = Double.MAX_VALUE;
                double colDelta = Double.MAX_VALUE;
                double rowCoverage1 = Double.MAX_VALUE;
                double rowCoverage2 = Double.MAX_VALUE;
                double colCoverage1 = Double.MAX_VALUE;
                double colCoverage2 = Double.MAX_VALUE;
                double rowSpatialScore = Double.MAX_VALUE;
                double colSpatialScore = Double.MAX_VALUE;
                Point t2Insert = null;
                Point t1Insert = null;
                Point t2Edit = null;
                Point t1Edit = null;
                if (i > 1) {
                    t1Edit = matrix.rowEdits[(i - 1)][j];
                    t2Edit = matrix.colEdits[(i - 1)][j];
//                    double prevPointEdge = distFunc.distance(t1Edit, t1Point[i - 1]);
                    double prevPointEdge = t1Edit.minus(t1.points.get(i - 1)).norm2();

//                    t2Insert = lineMap((Point) t2Edit, t2Point[j], t1Point[i - 1]);
                    t2Insert = Point.project(t2Edit, t2.points.get(j), t1.points.get(i - 1));

//                    double rowEditDistance = distFunc.distance(t2Insert, t1Point[i - 1]);
//                    double rowEditEdge = distFunc.distance(t2Edit, t2Insert);
                    double rowEditDistance = t2Insert.minus(t1.points.get(i - 1)).norm2();
                    double rowEditEdge = t2Insert.minus(t2Edit).norm2();
//                    t2Insert.setTime(0.0D);
                    rowCoverage1 = (rowEditEdge + prevPointEdge) / totalLength;
//                    rowCoverage2 = (distFunc.distance(t2Point[j], t2Insert) + t1.edgeLength(i - 1)) / totalLength;
                    rowCoverage2 = (Point.getLength(t2.points.get(j), t2Insert) + Point.getLength(t1.points.get(i), t1.points.get(i - 1))) / totalLength;

//                    rowDelta = matrix.value[(i - 1)][j] - matrix.delta[(i - 1)][j]
//                            + (rowEditDistance + distFunc.distance(t1Edit, t2Edit)) * rowCoverage1;
//                    rowSpatialScore = rowDelta
//                            + (rowEditDistance + distFunc.distance(t2Point[j], t1Point[i])) * rowCoverage2;
                    rowDelta = matrix.value[(i - 1)][j] - matrix.delta[(i - 1)][j]
                            + (rowEditDistance + Point.getLength(t1Edit, t2Edit)) * rowCoverage1;
                    rowSpatialScore = rowDelta
                            + (rowEditDistance + Point.getLength(t2.points.get(j), t1.points.get(i))) * rowCoverage2;
                }
                if (j > 1) {
                    t1Edit = matrix.rowEdits[i][(j - 1)];
                    t2Edit = matrix.colEdits[i][(j - 1)];
                    if (t1Edit == null) {
                        break;
                    }
                    double prevPointEdge = Point.getLength(t2Edit, t2.points.get(j - 1));

                    t1Insert = Point.project(t1Edit, t1.points.get(i), t2.points.get(j - 1));
//                    t1Insert = lineMap((Point) t1Edit, t1Point[i], t2Point[j - 1]);
                    double colEditDistance = Point.getLength(t1Insert, t2.points.get(j - 1));
//                    double colEditDistance = distFunc.distance(t1Insert, t2Point[j - 1]);
                    double colEditEdge = Point.getLength(t1Edit, t1Insert);
//                    double colEditEdge = distFunc.distance(t1Edit, t1Insert);

//                    t1Insert.setTime(0.0D);

                    colCoverage1 = (colEditEdge + prevPointEdge) / totalLength;
                    colCoverage2 = (Point.getLength(t1.points.get(i), t1Insert) + Point.getLength(t2.points.get(j), t2.points.get(j - 1))) / totalLength;
//                    colCoverage2 = (distFunc.distance(t1Point[i], t1Insert) + t2.edgeLength(j - 1)) / totalLength;
                    colDelta = matrix.value[i][(j - 1)] - matrix.delta[i][(j - 1)]
                            + (colEditDistance + Point.getLength(t1Edit, t2Edit)) * colCoverage1;
//                    colDelta = matrix.value[i][(j - 1)] - matrix.delta[i][(j - 1)]
//                            + (colEditDistance + distFunc.distance(t1Edit, t2Edit)) * colCoverage1;
                    colSpatialScore = colDelta
                            + (colEditDistance + Point.getLength((t1.points.get(i)), t2.points.get(j))) * colCoverage2;
//                    colSpatialScore = colDelta
//                            + (colEditDistance + distFunc.distance(t1Point[i], t2Point[j])) * colCoverage2;
                }
                double diagCoverage = (Point.getLength(t1.points.get(i), t1.points.get(i - 1))
                    + Point.getLength(t2.points.get(j), t2.points.get(j - 1))) / totalLength;
//                double diagCoverage = (t1.edgeLength(i - 1) + t2.edgeLength(j - 1)) / totalLength;

                double subScore = (Point.getLength(t2.points.get(j), t1.points.get(i))
                        + Point.getLength(t2.points.get(j - 1), t1.points.get(i - 1))) * diagCoverage;
//                double subScore = (distFunc.distance(t2Point[j], t1Point[i])
//                        + distFunc.distance(t2Point[j - 1], t1Point[i - 1])) * diagCoverage;

                double diagScore = matrix.value[(i - 1)][(j - 1)] + subScore;
                if ((diagScore <= colSpatialScore) && (diagScore <= rowSpatialScore)) {
                    matrix.add(i, j, diagScore, (byte) 1, t2.points.get(j - 1), t1.points.get(i - 1));
//                    matrix.add(i, j, diagScore, (byte) 1, t2Point[j - 1], t1Point[i - 1]);
                    matrix.delta[i][j] = (diagScore - matrix.value[(i - 1)][(j - 1)]);
                } else if ((colSpatialScore < rowSpatialScore)
                        || ((colSpatialScore == rowSpatialScore) && (t2.points.size() > t1.points.size()))) {
                    matrix.add(i, j, colSpatialScore, (byte) 2, t2.points.get(j - 1), t1Insert);
                    matrix.delta[i][j] = (colSpatialScore - colDelta);
                } else {
                    matrix.add(i, j, rowSpatialScore, (byte) 3, t2Insert, t1.points.get(i - 1));
                    matrix.delta[i][j] = (rowSpatialScore - rowDelta);
                }
            }
        }
        // double[] answer = { matrix.score(), this.time };
        return matrix.score();
    }

    private void initializeMatrix(Matrix matrix) {
        for (int i = 1; i < matrix.value.length; i++) {
            matrix.value[i][0] = Double.MAX_VALUE;
        }
        for (int j = 1; j < matrix.value[0].length; j++) {
            matrix.value[0][j] = Double.MAX_VALUE;
        }
        matrix.value[0][0] = 0.0D;
    }

    public static class Matrix {

        double[][] value;
        double[][] delta;
        public byte[][] parent;
        public Point[][] colEdits;
        public Point[][] rowEdits;

        public Matrix(int rowNum, int colNum) {
            this.value = new double[rowNum][colNum];
            this.delta = new double[rowNum][colNum];
            this.parent = new byte[rowNum][colNum];
            this.colEdits = new Point[rowNum][colNum];
            this.rowEdits = new Point[rowNum][colNum];
        }

        public void add(int i, int j, double val, byte parent, Point colEdit, Point rowEdit) {
            this.value[i][j] = val;
            this.parent[i][j] = parent;
            this.colEdits[i][j] = colEdit;
            this.rowEdits[i][j] = rowEdit;
        }

        public int numRows() {
            return this.value.length;
        }

        public int numCols() {
            return this.value[0].length;
        }

        public double score() {
            return this.value[(this.value.length - 1)][(this.value[0].length - 1)];
        }

        public void printPath() {
            int i = numRows() - 1;
            int j = numCols() - 1;
            System.out.println(i + " " + j + " " + this.value[i][j] + " " + this.delta[i][j]);
            if (this.parent[i][j] == 1) {
                i--;
                j--;
            } else if (this.parent[i][j] == 2) {
                j--;
            } else {
                i--;
            }
            printPath(i, j);
        }

        public void getSubMatchLength() {
            int j = -1;
            double min = Double.MAX_VALUE;
            for (int i = 1; i < this.value.length; i++) {
                if (this.value[i][(this.value[0].length - 1)] < min) {
                    min = this.value[i][(this.value[0].length - 1)];
                    j = i;
                }
            }
            int i = numRows() - 1;
            System.out.println(i + " " + j + " " + this.value[i][j] + " " + this.delta[i][j]);
            if (this.parent[i][j] == 1) {
                i--;
                j--;
            } else if (this.parent[i][j] == 2) {
                j--;
            } else {
                i--;
            }
            printPath(i, j);
        }

        private void printPath(int i, int j) {
            System.out.println(i + " " + j + " " + this.value[i][j] + " " + this.delta[i][j]);
            if ((i == 0) || (j == 0)) {
                return;
            }
            if (this.parent[i][j] == 1) {
                i--;
                j--;
            } else if (this.parent[i][j] == 2) {
                j--;
            } else {
                i--;
            }
            printPath(i, j);
        }

        public double subScore() {
            double min = Double.MAX_VALUE;
            for (int i = 1; i < this.value.length; i++) {
                if (this.value[i][(this.value[0].length - 1)] < min) {
                    min = this.value[i][(this.value[0].length - 1)];
                }
            }
            return min;
        }

        public void print() {
            System.out.println("=======================");
            for (int i = 0; i < this.value.length; i++) {
                System.out.println(Arrays.toString(this.value[i]));
            }
            System.out.println("=======================");
        }

        public int getSubPoint() {
            double min = Double.MAX_VALUE;
            int minI = -1;
            for (int i = 1; i < this.value.length; i++) {
                if (this.value[i][(this.value[0].length - 1)] < min) {
                    min = this.value[i][(this.value[0].length - 1)];
                    minI = i;
                }
            }
            return minI;
        }
    }
}
