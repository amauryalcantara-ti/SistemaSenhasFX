package com.sistemasenhas.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    // Carrega configurações do arquivo database.properties
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        String host = "localhost";
        String port = "3306";
        String dbName = "db_gerasenhas";
        String user = "root";
        String password = "MRFECKBYD";

        try (InputStream input = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("database.properties")) {
            if (input != null) {
                props.load(input);
                host = props.getProperty("db.host", "localhost");
                port = props.getProperty("db.port", "3306");
                dbName = props.getProperty("db.name", "db_gerasenhas");
                user = props.getProperty("db.user", "root");
                password = props.getProperty("db.password", "MRFECKBYD");
                System.out.println("✓ Configurações do banco carregadas de database.properties");
            } else {
                System.out.println("⚠ Arquivo database.properties não encontrado. Usando valores padrão.");
            }
        } catch (IOException e) {
            System.out.println("⚠ Erro ao ler database.properties, usando valores padrão: " + e.getMessage());
        }

        URL = String.format(
                "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8",
                host, port, dbName);
        USER = user;
        PASSWORD = password;
    }

    private static Connection connection;
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver MySQL JDBC carregado com sucesso!");
        } catch (ClassNotFoundException e) {
            System.err.println("ERRO: Driver MySQL JDBC não encontrado!");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Conexão com MySQL estabelecida!");
            } catch (SQLException e) {
                System.err.println("ERRO na conexão com MySQL: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao fechar conexão: " + e.getMessage());
        }
    }
}