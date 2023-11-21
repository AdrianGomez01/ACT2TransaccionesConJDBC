package org.example;

import java.sql.*;

public class Main {

    private static final String urlServidorLocal = "jdbc:mysql://localhost:3306/";
    private static final String user = "root";
    private static final String password = "1234";
    //    private static final String bbdd = "coches";
    private static final String bbdd = "banca";
    private static final String urlConexion = urlServidorLocal + bbdd;
    //    private static final String urlConexion = urlServidorLocal;

    public static void main(String[] args) {

        try (Connection cn = DriverManager.getConnection(urlServidorLocal, user, password)) {
            Statement st = cn.createStatement();
            Statement stUser1 = cn.createStatement();
            Statement stUser2 = cn.createStatement();
            Statement stNameUser = cn.createStatement();

            String q1 = "CREATE DATABASE banca";
            String q2 = "USE banca";

            String c1 = "CREATE TABLE clientes (idCliente int auto_increment primary key, nombre varchar(45) not null, apellidos varchar(45) not null, antiguedad DATETIME not null);";
            String c2 = "CREATE TABLE cuentas (idCuenta int auto_increment primary key, idCliente int not null, FOREIGN KEY (idCliente) REFERENCES clientes(idCliente) ON DELETE CASCADE ON UPDATE CASCADE, saldo FLOAT not null);";
            String c3 = "CREATE TABLE transacciones (idTransaccion int auto_increment primary key, tipo varchar(45), idCuentaOrigen int not null, FOREIGN KEY (idCuentaOrigen) REFERENCES cuentas(idCuenta) ON DELETE CASCADE ON UPDATE CASCADE,  idCuentaDestino int not null, FOREIGN KEY (idCuentaDestino) REFERENCES cuentas(idCuenta) ON DELETE CASCADE ON UPDATE CASCADE, fecha DATETIME not null, saldoMovido FLOAT not null;";

            String in1 = "INSERT INTO Usuarios (nombre, apellidos) VALUES ('Adrian', 'Gómez'), ('Enrique', 'López');";
            String in2 = "INSERT INTO Videojuegos (nombre, genero, plataformas, PEGI, precio) VALUES ('The Legend of Zelda: Breath of the Wild', 'Aventura', 'Nintendo Switch', '12', 59.99), ('FIFA 22', 'Deportes', 'PlayStation 5, Xbox Series X', '3', 69.99);";
            // Observaciones del videojuego 1
            String o1 = "INSERT INTO Observaciones (idVideojuego, duracion, puntuacion, vecesJugado, idUsuario) VALUES (1, 50.5, 9.5, 100, 1), (1, 60.2, 8.0, 80, 2);";
            // Observaciones del videojuego 2
            String o2 = "INSERT INTO Observaciones (idVideojuego, duracion, puntuacion, vecesJugado, idUsuario) VALUES (2, 25.1, 7.5, 120, 1), (2, 30.0, 6.8, 90, 2);";


// ------------------Una vez ejecutado por primera vez hay que comentar toda esta parte --------------------------------
            st.execute(q1);
// ---------------------------------------------------------------------------------------------------------------------
            //Esta debe ejecutarse siempre para que seleccione la bbdd que hemos creado
            st.execute(q2);

// ------------------Una vez ejecutado por primera vez hay que comentar toda esta parte --------------------------------
            st.execute(c1);
            st.execute(c2);
            st.execute(c3);
            st.execute(in1);
            st.execute(in2);
            st.execute(o1);
            st.execute(o2);



        } catch
        (SQLException e) {
            e.printStackTrace();
        }


        Connection cn = null;
        try {
            cn = DriverManager.getConnection(urlConexion, user, password);
            //Anula el commit automático al realizar una sentencia sql
            cn.setAutoCommit(false);
            int id = 1;
            Float saldo = 1000f;
            String propietario = "miguel";

            CallableStatement cs = cn.prepareCall("{call insertarRegistro (?,?)}");
//            cs.setFloat(1, saldo);
//            cs.setString(2, propietario);
//            cs.executeUpdate();

            //Transaccion bancaria
            int saldoactual = 0;
            Statement st = cn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM cuentabancaria WHERE idcuentabancaria = 2");
            while (rs.next()) {
                saldoactual = rs.getInt(2);
            }
            int saldoATransferir = 300;
            cs = cn.prepareCall("{CALL actualizarSaldo(?,?)}");
            cs.setInt(1, 2);
            cs.setInt(2, saldoactual - saldoATransferir);
            cs.executeUpdate();


            rs = st.executeQuery("SELECT * FROM cuentabancaria WHERE idcuentabancaria = 1");
            while (rs.next()) {
                saldoactual = rs.getInt(2);
            }
            cs = cn.prepareCall("CALL actualizarSaldo(?,?)");
            cs.setInt(1, 1);
            cs.setInt(2, saldoactual + saldoATransferir);
            cs.executeUpdate();

            cn.commit();

//            Statement st = cn.createStatement();

//            String q1 = "CREATE DATABASE videojuegos";
//            String q2 = "USE videojuegos";
//            String q3 = "CREATE TABLE juegos (id int not null primary key, nombre varchar(30) not null, desarrolladora varchar(30), plataformas varchar(30))";
//            String q4 = "INSERT INTO juegos (id,nombre,desarrolladora,plataformas) VALUES (1515,\"Hollow Knight\",\"Team Cherry\", \"PS4, PC\" ) ";

//            st.execute(q1);
//            st.execute(q2);
//            st.execute(q3);
//            st.execute(q4);

//            String q = "INSERT INTO cuentabancaria VALUES (?,?,?) ";
//            PreparedStatement ps;
//            ps = cn.prepareStatement(q);
////            ps.setInt(1, 1414);
////            ps.setString(2, "Elden Ring");
////            ps.setString(3, "From Software");
////            ps.setString(4, "PS5,XBOX,PC");
////            ps.execute();
//            ps.setInt(1, 1);
//            ps.setString(2, "saldo");
//            ps.setString(3, "cuentabancaria");
//            ps.execute();
//
//
//            String query = "SELECT * FROM cuentabancaria";
//            ResultSet rs = st.executeQuery(query);
//
//            while (rs.next()) {
//                System.out.println(rs.getString(1));
//                System.out.println(rs.getString("saldo"));
//                System.out.println(rs.getString("cuentabancaria"));
//            }

        } catch (SQLException e) {
            try {
                cn.rollback();
                System.out.println("Se ha lanzado la excepción");
                e.printStackTrace();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

    }


}

