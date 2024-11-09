import java.util.Arrays;
import java.util.Scanner;

public class TransProblem {
    int[] supply;
    int[] demand;
    int[][] cost;
    int[][] solution;

    public TransProblem(int[] supply, int[] demand, int[][] cost) {
        this.supply = Arrays.copyOf(supply, supply.length);
        this.demand = Arrays.copyOf(demand, demand.length);
        this.cost = cost;
        this.solution = new int[supply.length][demand.length];
    }

    public void printMatrix() {
        System.out.println("Initial matrix:");
        System.out.print("    |");
        for (int j = 0; j < demand.length; j++) System.out.printf("  D%d  |", j);
        System.out.println("  Sup |");
        System.out.println("----+" + "------+".repeat(demand.length) + "------|");
        for (int i = 0; i < supply.length; i++) {
            System.out.printf(" S%d |", i);
            for (int j = 0; j < demand.length; j++) {
                System.out.printf("  %2d  |", cost[i][j]);
            }
            System.out.printf(" %3d |\n", supply[i]);
        }
        System.out.print(" Dem |");
        for (int d : demand) System.out.printf(" %3d |", d);
        System.out.println();
        System.out.println("----+" + "------+".repeat(demand.length));
    }


    public void nwCorner() {
        int[] s = Arrays.copyOf(supply, supply.length);
        int[] d = Arrays.copyOf(demand, demand.length);
        solution = new int[supply.length][demand.length];
        int i = 0, j = 0;
        while (i < supply.length && j < demand.length) {
            int allocation = Math.min(s[i], d[j]);
            solution[i][j] = allocation;
            s[i] -= allocation;
            d[j] -= allocation;
            if (s[i] == 0) i++;
            if (d[j] == 0) j++;
        }
        printSolution("North-West Method");
    }


    public void vogel() {
        int[] s = Arrays.copyOf(supply, supply.length);
        int[] d = Arrays.copyOf(demand, demand.length);
        solution = new int[supply.length][demand.length];

        while (Arrays.stream(s).sum() > 0 && Arrays.stream(d).sum() > 0) {
            // Calculate row and column penalties
            int[] rowPenalty = calRPen(s, d);
            int[] colPenalty = calCPen(s, d);

            // Find the highest penalty and allocate in the lowest-cost cell
            int maxRowPenalty = -1, rowWithMaxPenalty = -1;
            for (int i = 0; i < rowPenalty.length; i++) {
                if (s[i] > 0 && rowPenalty[i] > maxRowPenalty) {
                    maxRowPenalty = rowPenalty[i];
                    rowWithMaxPenalty = i;
                }
            }
            int maxColPenalty = -1, colWithMaxPenalty = -1;
            for (int j = 0; j < colPenalty.length; j++) {
                if (d[j] > 0 && colPenalty[j] > maxColPenalty) {
                    maxColPenalty = colPenalty[j];
                    colWithMaxPenalty = j;
                }
            }

            int selectedRow, selectedCol;
            if (maxRowPenalty >= maxColPenalty) {
                selectedRow = rowWithMaxPenalty;
                selectedCol = RLowestCell(selectedRow, d);
            } else {
                selectedCol = colWithMaxPenalty;
                selectedRow = CLowestCell(selectedCol, s);
            }

            int allocation = Math.min(s[selectedRow], d[selectedCol]);
            solution[selectedRow][selectedCol] = allocation;
            s[selectedRow] -= allocation;
            d[selectedCol] -= allocation;
        }
        printSolution("Vogel's Method");
    }

    private int[] calRPen(int[] s, int[] d) {
        int[] penalties = new int[s.length];
        for (int i = 0; i < s.length; i++) {
            if (s[i] > 0) {
                int min1 = Integer.MAX_VALUE, min2 = Integer.MAX_VALUE;
                for (int j = 0; j < d.length; j++) {
                    if (d[j] > 0) {
                        if (cost[i][j] < min1) {
                            min2 = min1;
                            min1 = cost[i][j];
                        } else if (cost[i][j] < min2) {
                            min2 = cost[i][j];
                        }
                    }
                }
                penalties[i] = min2 == Integer.MAX_VALUE ? 0 : min2 - min1;
            }
        }
        return penalties;
    }

    private int[] calCPen(int[] s, int[] d) {
        int[] penalties = new int[d.length];
        for (int j = 0; j < d.length; j++) {
            if (d[j] > 0) {
                int min1 = Integer.MAX_VALUE, min2 = Integer.MAX_VALUE;
                for (int i = 0; i < s.length; i++) {
                    if (s[i] > 0) {
                        if (cost[i][j] < min1) {
                            min2 = min1;
                            min1 = cost[i][j];
                        } else if (cost[i][j] < min2) {
                            min2 = cost[i][j];
                        }
                    }
                }
                penalties[j] = min2 == Integer.MAX_VALUE ? 0 : min2 - min1;
            }
        }
        return penalties;
    }

    private int RLowestCell(int row, int[] d) {
        int minCost = Integer.MAX_VALUE, col = -1;
        for (int j = 0; j < d.length; j++) {
            if (d[j] > 0 && cost[row][j] < minCost) {
                minCost = cost[row][j];
                col = j;
            }
        }
        return col;
    }

    private int CLowestCell(int col, int[] s) {
        int minCost = Integer.MAX_VALUE, row = -1;
        for (int i = 0; i < s.length; i++) {
            if (s[i] > 0 && cost[i][col] < minCost) {
                minCost = cost[i][col];
                row = i;
            }
        }
        return row;
    }


    public void russell() {

        int[] s = Arrays.copyOf(supply, supply.length);
        int[] d = Arrays.copyOf(demand, demand.length);
        solution = new int[supply.length][demand.length];

        int[] u = new int[s.length];
        int[] v = new int[d.length];
        Arrays.fill(u, Integer.MIN_VALUE);
        Arrays.fill(v, Integer.MIN_VALUE);

        while (Arrays.stream(s).sum() > 0 && Arrays.stream(d).sum() > 0) {
            updMaxVal(s, d, u, v);


            int[] maxPosition = findMaxPos(u, v, s, d);
            int row = maxPosition[0];
            int col = maxPosition[1];
            int allocation = Math.min(s[row], d[col]);
            solution[row][col] = allocation;
            s[row] -= allocation;
            d[col] -= allocation;
        }

        printSolution("Russell's Method");
    }


    private void updMaxVal(int[] s, int[] d, int[] u, int[] v) {
        for (int i = 0; i < s.length; i++) {
            if (s[i] > 0) {
                u[i] = Arrays.stream(cost[i]).max().orElse(Integer.MIN_VALUE);
            }
        }
        for (int j = 0; j < d.length; j++) {
            if (d[j] > 0) {
                int maxCost = Integer.MIN_VALUE;
                for (int i = 0; i < s.length; i++) {
                    maxCost = Math.max(maxCost, cost[i][j]);
                }
                v[j] = maxCost;
            }
        }
    }


    private int[] findMaxPos(int[] u, int[] v, int[] s, int[] d) {
        int maxRussellValue = Integer.MIN_VALUE;
        int[] position = {-1, -1};

        for (int i = 0; i < s.length; i++) {
            for (int j = 0; j < d.length; j++) {
                if (s[i] > 0 && d[j] > 0) {
                    int russellValue = u[i] + v[j] - cost[i][j];
                    if (russellValue > maxRussellValue) {
                        maxRussellValue = russellValue;
                        position[0] = i;
                        position[1] = j;
                    }
                }
            }
        }
        return position;
    }


    private void printSolution(String method) {
        System.out.println(method);
        int totalCost = 0;
        System.out.println("Solution matrix:");
        System.out.print("    |");
        for (int j = 0; j < demand.length; j++) System.out.printf("  D%d  |", j);
        System.out.println();
        System.out.println("----+" + "------+".repeat(demand.length));
        for (int i = 0; i < supply.length; i++) {
            System.out.printf(" S%d |", i);
            for (int j = 0; j < demand.length; j++) {
                System.out.printf("  %3d |", solution[i][j]);
                totalCost += solution[i][j] * cost[i][j];
            }
            System.out.println();
        }
        System.out.println("Total cost = " + totalCost);
    }

    public static void test(int[] supply, int[] demand, int[][] cost) {

        System.out.println("Output: ");

        int totalSupply = Arrays.stream(supply).sum();
        int totalDemand = Arrays.stream(demand).sum();

        TransProblem tp = new TransProblem(supply, demand, cost);
        tp.printMatrix();

        if (totalSupply != totalDemand) {
            System.out.println("The problem is not balanced!");
            return;
        }


        System.out.println("Applying North-West Corner Method:");
        tp.nwCorner();


        System.out.println("\nApplying Vogel's Approximation Method:");
        tp.vogel();


        System.out.println("\nApplying Russell's Approximation Method:");
        tp.russell();
    }

    public static void main(String[] args) {
        System.out.println("Test Case 1");
        int[] supply1 = {100, 200, 300};
        int[] demand1 = {150, 250, 200};
        int[][] cost1 = {
                {4, 3, 8},
                {2, 7, 3},
                {5, 6, 2}
        };
        test(supply1, demand1, cost1);

        System.out.println("\nTest Case 2");
        int[] supply2 = {300, 500, 700};
        int[] demand2 = {250, 300, 400};
        int[][] cost2 = {
                {8, 6, 10},
                {3, 7, 4},
                {9, 5, 2}
        };
        test(supply2, demand2, cost2);

        System.out.println("\nTest Case 3");
        int[] supply3 = {100};
        int[] demand3 = {100};
        int[][] cost3 = {{5}};
        test(supply3, demand3, cost3);

        System.out.println("\nTest Case 4");
        int[] supply4 = {100, 200, 150};
        int[] demand4 = {80, 120, 150, 100};
        int[][] cost4 = {
                {8, 6, 10, 9},
                {9, 7, 4, 3},
                {5, 2, 8, 6}
        };
        test(supply4, demand4, cost4);
        
        Scanner sc = new Scanner(System.in);
        System.out.println("enter supply and demand count (n, m)");
        int n = sc.nextInt();
        int m = sc.nextInt();
        int[] supply = new int[n];
        int[] demand = new int[m];
        System.out.println("enter a vector of coefficients of supply");
        int[][] cost = new int[n][m];
        for (int i = 0; i < n; i++) {
            supply[i] = sc.nextInt();
        }
        System.out.println("enter a matrix of coefficients of costs");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                cost[i][j] = sc.nextInt();
            }
        }
        System.out.println("enter a vector of coefficients of demand");
        for (int i = 0; i < m; i++) {
            demand[i] = sc.nextInt();
        }

        test(supply, demand, cost);
    }
    
}
