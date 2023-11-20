package org.example;

import java.sql.*;

public class Main {

    private static final String urlServidorLocal = "jdbc:mysql://localhost:3306/";
    private static final String user = "root";
    private static final String password = "1234";
    //    private static final String bbdd = "coches";
    private static final String bbdd = "banco";
    private static final String urlConexion = urlServidorLocal + bbdd;
    //    private static final String urlConexion = urlServidorLocal;

    public static void main(String[] args) {


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

            if (args[0] == "1") {
                throw new SQLException();
            }

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

