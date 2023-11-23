package org.example;

import java.sql.*;
import java.util.Scanner;

public class Main {

    private static final String urlServidorLocal = "jdbc:mysql://localhost:3306/";
    private static final String user = "root";
    private static final String password = "1234";
    private static final String bbdd = "banca";
    private static final String urlConexion = "jdbc:mysql://localhost:3306/" + bbdd;

    public static void main(String[] args) {

        crearSchema();

        Scanner scanner = new Scanner(System.in);
        int opcion;

        do {
            System.out.println("\nMenú de Operaciones Bancarias:");
            System.out.println("1. Realizar Transacción");
            System.out.println("2. Consultar Saldo de una Cuenta");
            System.out.println("3. Consultar Historial de Transacciones de una Cuenta");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");

            opcion = scanner.nextInt();
            scanner.nextLine();

            switch (opcion) {
                case 1:
                    System.out.print("Ingrese el ID de la cuenta de origen: ");
                    int idCuentaOrigen = scanner.nextInt();
                    System.out.print("Ingrese el ID de la cuenta de destino: ");
                    int idCuentaDestino = scanner.nextInt();
                    System.out.print("Ingrese el monto a transferir: ");
                    double monto = scanner.nextDouble();
                    transferirSaldo(idCuentaOrigen, idCuentaDestino, monto);
                    break;
                case 2:
                    System.out.print("Ingrese el ID de la cuenta: ");
                    int idCuentaConsulta = scanner.nextInt();
                    consultarSaldoCliente(idCuentaConsulta);
                    break;
                case 3:
                    System.out.print("Ingrese el ID de la cuenta: ");
                    int idCuentaHistorial = scanner.nextInt();
                    consultarHistorialTransacciones(idCuentaHistorial);
                    break;
                case 0:
                    System.out.println("Saliendo del programa. ¡Hasta luego!");
                    break;
                default:
                    System.out.println("Opción no válida. Intente de nuevo.");
            }

        } while (opcion != 0);
    }


    //Método para crear la bbdd, las tablas y las inserciones de los datos en estas
    public static void crearSchema() {
        try {
            Connection cn = DriverManager.getConnection(urlServidorLocal, user, password);
            Statement st = cn.createStatement();


            String q1 = "CREATE DATABASE IF NOT EXISTS " + bbdd;
            String q2 = "USE " + bbdd;
            st.executeUpdate(q1);
            st.executeUpdate(q2);

            String c1 = "CREATE TABLE IF NOT EXISTS clientes (idCliente int auto_increment primary key, nombre varchar(45) not null, apellidos varchar(45) not null, antiguedad DATETIME not null);";
            String c2 = "CREATE TABLE IF NOT EXISTS cuentas (idCuenta int auto_increment primary key, idCliente int not null, FOREIGN KEY (idCliente) REFERENCES clientes(idCliente) ON DELETE CASCADE ON UPDATE CASCADE, saldo FLOAT not null);";
            String c3 = "CREATE TABLE IF NOT EXISTS transacciones (idTransaccion int auto_increment primary key, tipo varchar(45), idCuentaOrigen int not null, FOREIGN KEY (idCuentaOrigen) REFERENCES cuentas(idCuenta) ON DELETE CASCADE ON UPDATE CASCADE,  idCuentaDestino int not null, FOREIGN KEY (idCuentaDestino) REFERENCES cuentas(idCuenta) ON DELETE CASCADE ON UPDATE CASCADE, fecha DATETIME not null, saldoMovido FLOAT not null);";


            st.executeUpdate(c1);
            st.executeUpdate(c2);
            st.executeUpdate(c3);

            String in1 = "INSERT INTO clientes (nombre, apellidos, antiguedad) VALUES ('Adrian', 'Gomez', CURRENT_TIMESTAMP);";
            String in2 = "INSERT INTO clientes (nombre, apellidos, antiguedad) VALUES ('Enrique', 'Lopez', CURRENT_TIMESTAMP);";
            String in3 = "INSERT INTO clientes (nombre, apellidos, antiguedad) VALUES ('Miguel', 'Moreno', CURRENT_TIMESTAMP);";

            st.executeUpdate(in1);
            st.executeUpdate(in2);
            st.executeUpdate(in3);


            String inCuenta1 = "INSERT INTO cuentas (idCliente, saldo) VALUES (1, 1000.0);";
            String inCuenta2 = "INSERT INTO cuentas (idCliente, saldo) VALUES (1, 500.0);";
            String inCuenta3 = "INSERT INTO cuentas (idCliente, saldo) VALUES (2, 800.0);";
            String inCuenta4 = "INSERT INTO cuentas (idCliente, saldo) VALUES (3, 1200.0);";

            st.executeUpdate(inCuenta1);
            st.executeUpdate(inCuenta2);
            st.executeUpdate(inCuenta3);
            st.executeUpdate(inCuenta4);


        } catch (SQLException e) {
            System.err.println("Error al ejecutar la consulta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para realizar una transacción entre dos cuentas
    public static void transferirSaldo(int idCuentaOrigen, int idCuentaDestino, double monto) {
        Connection cn = null;

        try {
            cn = DriverManager.getConnection(urlConexion, user, password);
            cn.setAutoCommit(false);

            // Verifica los saldos por si son suficientes en la cuenta de origen
            double saldoOrigen = obtenerSaldoCuenta(cn, idCuentaOrigen);
            if (saldoOrigen < monto) {
                System.out.println("Saldo insuficiente en la cuenta de origen.");
                return;
            }

            // Realiza la transferencia del saldo
            actualizarSaldoCuenta(cn, idCuentaOrigen, saldoOrigen - monto);
            double saldoDestino = obtenerSaldoCuenta(cn, idCuentaDestino);
            actualizarSaldoCuenta(cn, idCuentaDestino, saldoDestino + monto);

            // Registra la transacción
            registrarTransaccion(cn, "TRANSFERENCIA", idCuentaOrigen, idCuentaDestino, monto);

            cn.commit();
        } catch (SQLException e) {
            System.err.println("Error al realizar la transacción: " + e.getMessage());
            try {
                if (cn != null) {
                    cn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                if (cn != null) {
                    cn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    // Método para obtener y actualizar los saldos
    private static double obtenerSaldoCuenta(Connection cn, int idCuenta) throws SQLException {
        String query = "SELECT saldo FROM cuentas WHERE idCuenta = ?";
        try (PreparedStatement ps = cn.prepareStatement(query)) {
            ps.setInt(1, idCuenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("saldo");
                }
            }
        }
        throw new SQLException("Cuenta no encontrada: " + idCuenta);
    }

    //Método para actualizar el saldo de una cuenta al realizar una transacción
    private static void actualizarSaldoCuenta(Connection cn, int idCuenta, double nuevoSaldo) throws SQLException {
        String query = "UPDATE cuentas SET saldo = ? WHERE idCuenta = ?";
        try (PreparedStatement ps = cn.prepareStatement(query)) {
            ps.setDouble(1, nuevoSaldo);
            ps.setInt(2, idCuenta);
            ps.executeUpdate();
        }
    }

    // Método para guardar las transacciones realizadas.
    private static void registrarTransaccion(Connection cn, String tipo, int idCuentaOrigen, int idCuentaDestino, double monto) throws SQLException {
        String query = "INSERT INTO transacciones (tipo, idCuentaOrigen, idCuentaDestino, fecha, saldoMovido) VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?)";
        try (PreparedStatement ps = cn.prepareStatement(query)) {
            ps.setString(1, tipo);
            ps.setInt(2, idCuentaOrigen);
            ps.setInt(3, idCuentaDestino);
            ps.setDouble(4, monto);
            ps.executeUpdate();
        }
    }

    // Método para consultar el saldo de todas las cuentas de un cliente por su idCliente
    public static void consultarSaldoCliente(int idCliente) {
        try (Connection cn = DriverManager.getConnection(urlConexion, user, password)) {
            String query = "SELECT idCuenta, saldo FROM cuentas WHERE idCliente = ?";
            try (PreparedStatement ps = cn.prepareStatement(query)) {
                ps.setInt(1, idCliente);

                //Imprime el resultado por consola:
                try (ResultSet rs = ps.executeQuery()) {
                    System.out.println("Saldo de las cuentas del cliente " + idCliente + ":");
                    while (rs.next()) {
                        int idCuenta = rs.getInt("idCuenta");
                        double saldo = rs.getDouble("saldo");
                        System.out.println("Cuenta " + idCuenta + ": " + saldo);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar el saldo del cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Método para consultar el historial de transacciones de una cuenta por su idCuenta
    public static void consultarHistorialTransacciones(int idCuenta) {
        Connection cn = null;

        try {
            cn = DriverManager.getConnection(urlConexion, user, password);
            String query = "SELECT * FROM transacciones WHERE idCuentaOrigen = ? OR idCuentaDestino = ?";
            try (PreparedStatement ps = cn.prepareStatement(query)) {
                ps.setInt(1, idCuenta);
                ps.setInt(2, idCuenta);

                //Imprimo las transacciones por consola
                try (ResultSet rs = ps.executeQuery()) {
                    System.out.println("Historial de transacciones de la cuenta " + idCuenta + ":");
                    while (rs.next()) {
                        int idTransaccion = rs.getInt("idTransaccion");
                        String tipo = rs.getString("tipo");
                        int idCuentaOrigen = rs.getInt("idCuentaOrigen");
                        int idCuentaDestino = rs.getInt("idCuentaDestino");
                        Timestamp fecha = rs.getTimestamp("fecha");
                        double saldoMovido = rs.getDouble("saldoMovido");

                        System.out.println("Transacción " + idTransaccion + ":");
                        System.out.println("   Tipo: " + tipo);
                        System.out.println("   Cuenta Origen: " + idCuentaOrigen);
                        System.out.println("   Cuenta Destino: " + idCuentaDestino);
                        System.out.println("   Fecha: " + fecha);
                        System.out.println("   Saldo Movido: " + saldoMovido);

                        // Aplicar lógica de transferencia de saldo
                        if ("a favor".equals(tipo)) {
                            transferirSaldo(idCuentaDestino, idCuentaOrigen, saldoMovido);
                        } else if ("en contra".equals(tipo)) {
                            transferirSaldo(idCuentaOrigen, idCuentaDestino, saldoMovido);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar el historial de transacciones: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (cn != null) {
                    cn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
