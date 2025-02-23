package com.mycompany.sqljava;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RegistroDato extends JFrame {
    private CardLayout cardLayout;
    private JPanel panelContainer;
    private JTextField txtNombre, txtApellido, txtHumedad, txtTemperatura, txtSismo, txtPresion;
    private JComboBox<String> cbPronostico, cbAlerta;
    private JButton btnSiguiente1, btnSiguiente2, btnSiguiente3;
    private int userId;

    public RegistroDato() {
        setTitle("Registro de Datos");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        panelContainer = new JPanel(cardLayout);

        panelContainer.add(panelUsuario(), "Usuario");
        panelContainer.add(panelMedicion(), "Medicion");
        panelContainer.add(panelFinal(), "Final");

        add(panelContainer);
        setVisible(true);
    }

    private JPanel panelUsuario() {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Nombre:"));
        txtNombre = new JTextField();
        panel.add(txtNombre);

        panel.add(new JLabel("Apellido:"));
        txtApellido = new JTextField();
        panel.add(txtApellido);

        btnSiguiente1 = new JButton("Siguiente");
        panel.add(btnSiguiente1);
        btnSiguiente1.addActionListener(e -> registrarUsuario());

        return panel;
    }

    private JPanel panelMedicion() {
        JPanel panel = new JPanel(new GridLayout(7, 2));

        // Campos de medición
        panel.add(new JLabel("Humedad (%):"));
        txtHumedad = new JTextField();
        panel.add(txtHumedad);

        panel.add(new JLabel("Temperatura (°C):"));
        txtTemperatura = new JTextField();
        panel.add(txtTemperatura);

        panel.add(new JLabel("Sismo (Magnitud):"));
        txtSismo = new JTextField();
        panel.add(txtSismo);

        panel.add(new JLabel("Presión Atm (hPa):"));
        txtPresion = new JTextField();
        panel.add(txtPresion);

        // Selección de Pronóstico
        panel.add(new JLabel("Pronóstico:"));
        cbPronostico = new JComboBox<>(new String[]{
            "Lluvias intensas en la zona",
            "Nevadas Extremas en la sierra",
            "Calor extremo en la costa",
            "Posible actividad sísmica leve",
            "Actividad sísmica fuerte"
        });
        panel.add(cbPronostico);

        // Selección de Alerta
        panel.add(new JLabel("Alerta:"));
        cbAlerta = new JComboBox<>(new String[]{"Crítico", "Muy Alto", "Alto", "Moderado", "Bajo"});
        panel.add(cbAlerta);

        // Botón de acción
        btnSiguiente2 = new JButton("Registrar y Alertar");
        panel.add(btnSiguiente2);

        btnSiguiente2.addActionListener(e -> {
            try {
                int idAlerta = cbAlerta.getSelectedIndex() + 1;
                int idPronostico = cbPronostico.getSelectedIndex() + 1;
                double humedad = Double.parseDouble(txtHumedad.getText());
                double temperatura = Double.parseDouble(txtTemperatura.getText());
                double sismo = Double.parseDouble(txtSismo.getText());
                double presion = Double.parseDouble(txtPresion.getText());

                // Insertar datos en la base de datos
                registrarMedicion(userId, idAlerta, idPronostico, humedad, temperatura, sismo, presion);

                // Pasar a la siguiente pantalla
                cardLayout.next(panelContainer);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Ingrese valores numéricos válidos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private JPanel panelFinal() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Autoridades alertadas."));
        JButton btnOk = new JButton("OK");
        panel.add(btnOk);
        btnOk.addActionListener(e -> System.exit(0));
        
        return panel;
    }

    private void registrarUsuario() {
        String nombre = txtNombre.getText();
        String apellido = txtApellido.getText();
        int tipoUsuario = 1; // Siempre será 1

        Connection conn = CConexion.getInstancia().conectar();
        if (conn != null) {
            try {
                String sql = "INSERT INTO DIM_Usuario (usuNombres, usuApellidos, IDTipoUsuario) VALUES (?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                stmt.setString(1, nombre);
                stmt.setString(2, apellido);
                stmt.setInt(3, tipoUsuario); // Se asigna 1 siempre

                stmt.executeUpdate();

                var rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    userId = rs.getInt(1);
                }
                stmt.close();
                cardLayout.next(panelContainer);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }


    private void registrarMedicion(int userId, int idAlerta, int idPronostico, double humedad, double temperatura, double sismo, double presion) {
        Connection conn = CConexion.getInstancia().conectar();
        if (conn != null) {
            try {
                String sql = "INSERT INTO Hechos_Medicion (IDSensor, IDUsuario, IDAlerta, IDPronostico, medFecha, medHumedad, medTemperatura, medSismo, medPresionAtm) " +
                             "VALUES (1, ?, ?, ?, GETDATE(), ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                stmt.setInt(2, idAlerta);
                stmt.setInt(3, idPronostico);
                stmt.setDouble(4, humedad);
                stmt.setDouble(5, temperatura);
                stmt.setDouble(6, sismo);
                stmt.setDouble(7, presion);

                int filasAfectadas = stmt.executeUpdate();

                if (filasAfectadas > 0) {
                    System.out.println("Medicion registrada correctamente.");
                } else {
                    System.out.println("Error: No se inserto ninguna medicion.");
                }

                stmt.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error al registrar medicion: " + ex.getMessage());
            }
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(RegistroDato::new);
    }
}
