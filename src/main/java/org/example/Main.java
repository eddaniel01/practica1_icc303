package org.example;

import mpi.MPI;
import mpi.MPIException;

import java.util.Arrays;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws MPIException {

        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        int n = 4;
        int[][] A = new int[n][n];
        int[][] B = new int[n][n];
        int[][] C = new int[n][n];
        int[][] C_secuencial = new int[n][n];

        Random aleatorio = new Random();

        if (rank == 0) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    A[i][j] = aleatorio.nextInt(10);
                    B[i][j] = aleatorio.nextInt(10);
                }
            }
        }

        for (int i = 0; i < n; i++) {
            MPI.COMM_WORLD.Bcast(A[i], 0, n, MPI.INT, 0);
            MPI.COMM_WORLD.Bcast(B[i], 0, n, MPI.INT, 0);
        }

        if (rank == 0) {
            System.out.println("Matriz A:");
            for (int i = 0; i < n; i++) {
                System.out.println(Arrays.toString(A[i]));
            }
            System.out.println("Matriz B:");
            for (int i = 0; i < n; i++) {
                System.out.println(Arrays.toString(B[i]));
            }
        }

        int filasPorProceso = n / size;
        int filaInicio = rank * filasPorProceso;
        int filaFin = (rank == size - 1) ? n : filaInicio + filasPorProceso;

        for (int i = filaInicio; i < filaFin; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = 0;
                for (int k = 0; k < n; k++) {
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }

        if (rank == 0) {
            for (int i = 1; i < size; i++) {
                int inicio = i * filasPorProceso;
                int fin = (i == size - 1) ? n : inicio + filasPorProceso;
                for (int j = inicio; j < fin; j++) {
                    MPI.COMM_WORLD.Recv(C[j], 0, n, MPI.INT, i, 0);
                }
            }
        } else {
            for (int i = filaInicio; i < filaFin; i++) {
                MPI.COMM_WORLD.Send(C[i], 0, n, MPI.INT, 0, 0);
            }
        }

        if (rank == 0) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    C_secuencial[i][j] = 0;
                    for (int k = 0; k < n; k++) {
                        C_secuencial[i][j] += A[i][k] * B[k][j];
                    }
                }
            }

            System.out.println("Resultado de la multiplicacion de matrices (paralela):");
            for (int i = 0; i < n; i++) {
                System.out.println(Arrays.toString(C[i]));
            }

            System.out.println("Resultado de la multiplicacion de matrices (secuencial):");
            for (int i = 0; i < n; i++) {
                System.out.println(Arrays.toString(C_secuencial[i]));
            }

            boolean correcto = true;
            for (int i = 0; i < n; i++) {
                if (!Arrays.equals(C[i], C_secuencial[i])) {
                    correcto = false;
                    break;
                }
            }
            if (correcto) {
                System.out.println("La implementacion paralela es correcta.");
            } else {
                System.out.println("La implementacion paralela es incorrecta.");
            }
        }

        MPI.Finalize();
    }
}
