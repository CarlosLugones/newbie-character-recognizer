/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import rna.CharMatrix;
import rna.MultiLayerPerceptron;
import rna.TrainingSet;

/**
 *
 * @author Carlos Ariel
 */
public class main extends javax.swing.JFrame {
        
    private CharMatrix matrix;
    private MultiLayerPerceptron mlp;
    private TrainingSet trainingSet;
    private final String trainingSetFilename = "trainingSet.ser";
    private final String mlpFilename = "mlp.ser";
    private static main mainFrame;
    private String mode = "paint";
    private final int inputAmout = 32*32;
    
    private Thread trainThread;
    
    /**
     * Creates new form main
     */
    public main() {
        initComponents();        
        matrix = new CharMatrix(canvas.getGraphics());
        resetMLP();
        trainingSet = new TrainingSet(inputAmout,2);
        progressBar1.setVisible(false);
        progressBar2.setVisible(false);
        lbl1.setText("");
        lblRecognized.setText("");        
        refreshTrainingSetTable();
    }
    
    private void resetMLP() {
        mlp = new MultiLayerPerceptron(inputAmout, 28, 2, 1, 0.001);        
    }
    
    public void save() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream(trainingSetFilename) );
            oos.writeObject(trainingSet);
            oos.close();
            JOptionPane.showMessageDialog(this, "Guardado satisfactoriamente.");
        } catch (IOException ex) {
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void load() {
        try {            
            ObjectInputStream ois = new ObjectInputStream( new FileInputStream(trainingSetFilename) );
            trainingSet = (TrainingSet) ois.readObject();
            ois.close();
        } catch (IOException ex) {
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
        }
        refreshTrainingSetTable();
    }
    
    public void saveMLP() {
        Thread t = new Thread( new Runnable() {

            @Override
            public void run() {
                menuRNA.setEnabled(false);
                try {
                    ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream(mlpFilename) );
                    oos.writeObject(mlp);
                    oos.close();
                    JOptionPane.showMessageDialog(mainFrame, "RNA guardada satisfactoriamente.");
                } catch (IOException ex) {
                    Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                }
                menuRNA.setEnabled(true);
            }
        
        });
        t.start();        
    }
    
    public void loadMLP() {
        Thread t = new Thread( new Runnable() {

            @Override
            public void run() {
                menuRNA.setEnabled(false);
                try {
                    ObjectInputStream ois = new ObjectInputStream( new FileInputStream(mlpFilename) );
                    mlp = (MultiLayerPerceptron) ois.readObject();
                    ois.close();
                    JOptionPane.showMessageDialog(mainFrame, "RNA cargada desde archivo "+mlpFilename+".");
                } catch (IOException ex) {
                    Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                }
                menuRNA.setEnabled(true);
            }
        
        });
        t.start();       
    }
    
    public void refreshTrainingSetTable() {        
        // Making model
        DefaultTableModel model = new DefaultTableModel(trainingSet.names(), trainingSet.size());
        for (int i=0; i<trainingSet.size(); i++) {
            // inputs
            int j=0;
            for (j=0; j<trainingSet.getInputs().get(i).length; j++) {
                model.setValueAt(trainingSet.getInputs().get(i)[j], i, j);
            }
            // outputs
            for (int q=0; q<trainingSet.getOutputs().get(i).length; q++) {
                model.setValueAt(trainingSet.getOutputs().get(i)[q], i, j);
                j++;
            }
        }
        // Refresh model in table
        trainingSetTable.setModel(model);
        int size = trainingSet.size();
        if (size == 0) {
            lblTrainingSetSize.setText( "(Sin elementos)");
            btnEntrenar.setEnabled(false);
            btnLimpiar.setEnabled(false);
        } else {
            lblTrainingSetSize.setText( "(" + size + " elementos)");
            btnEntrenar.setEnabled(true);
            btnLimpiar.setEnabled(true);
        }
    }
    
    private void matrixPaint(MouseEvent evt) {
        // Toma la coordenada real y la transforma en la coordenada dentro de la matriz de 10x10        
        int x = evt.getX() / CharMatrix.scale;
        int y = evt.getY() / CharMatrix.scale;
        // Paints the pixel and render
        if (x>=0 && y>=0 && x<CharMatrix.width && y<CharMatrix.width) {
            if (mode.equals("paint")) {
                matrix.set(x, y, true);
            } else {
                matrix.set(x, y, false);
            }
            matrix.render();
        }        
    }
    
    @Override
    public void paint(Graphics g) {        
        super.paint(g);
        matrix.render();        
    }
    
    private void clear() {
        matrix.clear();
        matrix.render();
    }
    
    private void turnColor() {
        if (mode.equals("paint")) {
            mode = "erase";
            color.setBackground(Color.white);
        } else {
            mode = "paint";
            color.setBackground(Color.black);
        }
    }
    
    public void guiBeginTraining() {
        progressBar1.setVisible(true);
        progressBar1.setValue(0);
        progressBar2.setVisible(true);
        progressBar2.setValue(0);
        btnGuardar.setEnabled(false);
        btnCargar.setEnabled(false);
        btnLimpiar.setEnabled(false);
        btnLimpiarImagen.setEnabled(false);        
        btnReconocer.setEnabled(false);
        btnAgregar.setEnabled(false);
        brushSize.setEnabled(false);
        color.setEnabled(false);
        menuRNA.setEnabled(false);
        menuCE.setEnabled(false);
        trainingSetTable.setEnabled(false);
        btnEntrenar.setText("Detener");        
    }
    
    public void guiEndTraining() {
        progressBar1.setVisible(false);
        progressBar2.setVisible(false);
        btnGuardar.setEnabled(true);
        btnCargar.setEnabled(true);
        btnLimpiar.setEnabled(true);
        btnLimpiarImagen.setEnabled(true);
        btnReconocer.setEnabled(true);
        btnEntrenar.setEnabled(true);
        btnAgregar.setEnabled(true);
        brushSize.setEnabled(true);
        color.setEnabled(true);
        menuRNA.setEnabled(true);
        menuCE.setEnabled(true);
        trainingSetTable.setEnabled(true);
        btnEntrenar.setText("Entrenar");        
    }
    
    private void train() {        
        trainThread = new Thread( new Runnable() {

            @Override
            public void run() {
                // GUI                
                guiBeginTraining();
                
                // Train
                int porcent1 = 0;
                int size = trainingSet.size();
                final int iterations = 500;
                for (int c=0; c<iterations; c++) {
                    for (int i=0; i<size; i++) {
                        // Show current set
                        matrix.set(trainingSet.getInputs().get(i));
                        matrix.render();                                        
                        // Train the current set
                        mlp.train(trainingSet.getInputs().get(i), trainingSet.getOutputs().get(i));
                        int porcent2 = (i * 100) / size;
                        progressBar2.setValue(porcent2);
                        progressBar2.setString(porcent2 + "%");
                    }
                    // Update progress bar
                    porcent1 = (c * 100) / iterations;
                    progressBar1.setValue(porcent1);
                    progressBar1.setString(porcent1 + "%");
                }          
                
                // GUI                
                guiEndTraining();
                JOptionPane.showMessageDialog(mainFrame, "Entrenamiento terminado.");
            }
        
        });
        trainThread.start();
    }
    
    private void clearTrainingSet() {
        trainingSet.clear();
        refreshTrainingSetTable();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPane = new javax.swing.JTabbedPane();
        trainPanel = new javax.swing.JPanel();
        lbl1 = new javax.swing.JLabel();
        lblRecognized = new javax.swing.JLabel();
        btnReconocer = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        btnEntrenar = new javax.swing.JButton();
        canvas = new java.awt.Canvas();
        salidaEsperada = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        trainingSetTable = new javax.swing.JTable();
        btnGuardar = new javax.swing.JButton();
        btnCargar = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        btnAgregar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        btnLimpiarImagen = new javax.swing.JButton();
        lblTrainingSetSize = new javax.swing.JLabel();
        progressBar1 = new javax.swing.JProgressBar();
        brushSize = new javax.swing.JSlider();
        color = new javax.swing.JPanel();
        progressBar2 = new javax.swing.JProgressBar();
        panelVistaGraficaRNA = new javax.swing.JPanel();
        rnaPanel = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuRNA = new javax.swing.JMenu();
        menuTrain = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        menuRNA_cargar = new javax.swing.JMenuItem();
        menuRNA_guardar = new javax.swing.JMenuItem();
        menuCE = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Proyecto de RNA");
        setName("mainFrame"); // NOI18N

        lbl1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbl1.setText("Caracter reconocido:");

        lblRecognized.setBackground(new java.awt.Color(204, 255, 255));
        lblRecognized.setFont(new java.awt.Font("Tahoma", 0, 48)); // NOI18N
        lblRecognized.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblRecognized.setText("B");
        lblRecognized.setToolTipText("");

        btnReconocer.setText("Reconocer");
        btnReconocer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReconocerActionPerformed(evt);
            }
        });

        jLabel1.setText("Salida esperada");

        btnEntrenar.setText("Entrenar");
        btnEntrenar.setEnabled(false);
        btnEntrenar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEntrenarActionPerformed(evt);
            }
        });

        canvas.setBackground(new java.awt.Color(255, 255, 255));
        canvas.setPreferredSize(new java.awt.Dimension(200, 200));
        canvas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                canvasMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                canvasMousePressed(evt);
            }
        });
        canvas.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                canvasMouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                canvasMouseMoved(evt);
            }
        });
        canvas.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                canvasFocusGained(evt);
            }
        });

        salidaEsperada.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "B", "D" }));

        trainingSetTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        trainingSetTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        trainingSetTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                trainingSetTableMouseMoved(evt);
            }
        });
        trainingSetTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                trainingSetTableMouseClicked(evt);
            }
        });
        trainingSetTable.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                trainingSetTablePropertyChange(evt);
            }
        });
        jScrollPane1.setViewportView(trainingSetTable);

        btnGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/images/save.png"))); // NOI18N
        btnGuardar.setText("Guardar");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        btnCargar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/images/open.png"))); // NOI18N
        btnCargar.setText("Cargar");
        btnCargar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCargarActionPerformed(evt);
            }
        });

        jLabel2.setText("Conjunto de entrenamiento");

        btnAgregar.setText(">>");
        btnAgregar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarActionPerformed(evt);
            }
        });

        btnLimpiar.setText("Limpiar");
        btnLimpiar.setEnabled(false);
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
            }
        });

        btnLimpiarImagen.setText("Limpiar");
        btnLimpiarImagen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarImagenActionPerformed(evt);
            }
        });

        progressBar1.setToolTipText("");
        progressBar1.setStringPainted(true);

        brushSize.setMaximum(5);
        brushSize.setMinimum(1);
        brushSize.setValue(3);
        brushSize.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                brushSizePropertyChange(evt);
            }
        });

        color.setBackground(new java.awt.Color(0, 0, 0));
        color.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        color.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                colorMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout colorLayout = new javax.swing.GroupLayout(color);
        color.setLayout(colorLayout);
        colorLayout.setHorizontalGroup(
            colorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 26, Short.MAX_VALUE)
        );
        colorLayout.setVerticalGroup(
            colorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        progressBar2.setStringPainted(true);

        javax.swing.GroupLayout trainPanelLayout = new javax.swing.GroupLayout(trainPanel);
        trainPanel.setLayout(trainPanelLayout);
        trainPanelLayout.setHorizontalGroup(
            trainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(trainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(trainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(trainPanelLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(salidaEsperada, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(trainPanelLayout.createSequentialGroup()
                        .addComponent(btnLimpiarImagen, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnReconocer, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(progressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(canvas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnAgregar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(trainPanelLayout.createSequentialGroup()
                        .addComponent(color, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(brushSize, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addComponent(lblRecognized, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lbl1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(progressBar2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(trainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, trainPanelLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblTrainingSetSize))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, trainPanelLayout.createSequentialGroup()
                        .addComponent(btnCargar, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 189, Short.MAX_VALUE)
                        .addComponent(btnEntrenar, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        trainPanelLayout.setVerticalGroup(
            trainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(trainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(trainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(salidaEsperada, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(lblTrainingSetSize))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(trainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(trainPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(trainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnEntrenar)
                            .addComponent(btnGuardar)
                            .addComponent(btnCargar)
                            .addComponent(btnLimpiar)))
                    .addGroup(trainPanelLayout.createSequentialGroup()
                        .addComponent(canvas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAgregar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(trainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnReconocer)
                            .addComponent(btnLimpiarImagen))
                        .addGap(6, 6, 6)
                        .addGroup(trainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(brushSize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(color, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(progressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(progressBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lbl1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblRecognized, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        tabbedPane.addTab("Entrenar", trainPanel);

        panelVistaGraficaRNA.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                panelVistaGraficaRNAFocusGained(evt);
            }
        });
        panelVistaGraficaRNA.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                panelVistaGraficaRNAPropertyChange(evt);
            }
        });

        rnaPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                rnaPanelMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout rnaPanelLayout = new javax.swing.GroupLayout(rnaPanel);
        rnaPanel.setLayout(rnaPanelLayout);
        rnaPanelLayout.setHorizontalGroup(
            rnaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 877, Short.MAX_VALUE)
        );
        rnaPanelLayout.setVerticalGroup(
            rnaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 481, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout panelVistaGraficaRNALayout = new javax.swing.GroupLayout(panelVistaGraficaRNA);
        panelVistaGraficaRNA.setLayout(panelVistaGraficaRNALayout);
        panelVistaGraficaRNALayout.setHorizontalGroup(
            panelVistaGraficaRNALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelVistaGraficaRNALayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rnaPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelVistaGraficaRNALayout.setVerticalGroup(
            panelVistaGraficaRNALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelVistaGraficaRNALayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rnaPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab("Vista gráfica de la RNA", panelVistaGraficaRNA);

        menuRNA.setText("RNA");

        menuTrain.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        menuTrain.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/images/train.png"))); // NOI18N
        menuTrain.setText("Entrenar");
        menuTrain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuTrainActionPerformed(evt);
            }
        });
        menuRNA.add(menuTrain);

        jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/images/new.png"))); // NOI18N
        jMenuItem5.setText("Resetear");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        menuRNA.add(jMenuItem5);
        menuRNA.add(jSeparator1);

        menuRNA_cargar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        menuRNA_cargar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/images/open.png"))); // NOI18N
        menuRNA_cargar.setText("Cargar");
        menuRNA_cargar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuRNA_cargarActionPerformed(evt);
            }
        });
        menuRNA.add(menuRNA_cargar);

        menuRNA_guardar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_MASK));
        menuRNA_guardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/images/save.png"))); // NOI18N
        menuRNA_guardar.setText("Guardar");
        menuRNA_guardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuRNA_guardarActionPerformed(evt);
            }
        });
        menuRNA.add(menuRNA_guardar);

        jMenuBar1.add(menuRNA);

        menuCE.setText("CE");

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/images/open.png"))); // NOI18N
        jMenuItem2.setText("Cargar");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        menuCE.add(jMenuItem2);

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/images/save.png"))); // NOI18N
        jMenuItem3.setText("Guardar");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        menuCE.add(jMenuItem3);

        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem4.setText("Limpiar");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        menuCE.add(jMenuItem4);

        jMenuBar1.add(menuCE);

        jMenu3.setText("Ayuda");

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        jMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/images/help.png"))); // NOI18N
        jMenuItem1.setText("Acerca de...");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem1);

        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents
    
    private void btnAgregarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarActionPerformed
        // Get training set from CharMatrix
        int[] input = matrix.getLinearVector();
        // Transform output
        String s = (String) salidaEsperada.getItemAt( salidaEsperada.getSelectedIndex() );
        int y1=0, y2=0;
        if (s.equals("B")) {
            y1 = 1;
            y2 = 0;
        } else if (s.equals("D")) {
            y1 = 0;
            y2 = 1;
        }
        int[] output = new int[]{ y1, y2 };
        // Add set
        trainingSet.add(input, output);
        refreshTrainingSetTable();
        clear();
    }//GEN-LAST:event_btnAgregarActionPerformed

    private void btnCargarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCargarActionPerformed
        load();        
    }//GEN-LAST:event_btnCargarActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        save();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        clearTrainingSet();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnEntrenarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEntrenarActionPerformed
        if (btnEntrenar.getText().equals("Entrenar")) {
            train();
        } else {
            trainThread.stop();            
            guiEndTraining();
        }
    }//GEN-LAST:event_btnEntrenarActionPerformed

    private void btnReconocerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReconocerActionPerformed
        int[] l = matrix.getLinearVector();        
        double[] output = mlp.calculate( TrainingSet.transformToArrayDouble(l) );
        //lblB.setText( "B: " + output[0] );
        //lblD.setText( "D: " + output[1] );
        lbl1.setText("Caracter reconocido:");
        String recognized = "";
        if (output[0] > output[1]) {
            recognized = "B";
        } else {
            recognized = "D";
        }        
        lblRecognized.setText( recognized );
    }//GEN-LAST:event_btnReconocerActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void trainingSetTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_trainingSetTableMouseClicked
        int row = trainingSetTable.getSelectedRow();
        if (row != -1) {
            double[] d = trainingSet.getInputs().get(row);
            matrix.set(d);
            matrix.render();
        }
    }//GEN-LAST:event_trainingSetTableMouseClicked

    private void trainingSetTablePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_trainingSetTablePropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_trainingSetTablePropertyChange

    private void btnLimpiarImagenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarImagenActionPerformed
        clear();
    }//GEN-LAST:event_btnLimpiarImagenActionPerformed

    private void trainingSetTableMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_trainingSetTableMouseMoved
        
    }//GEN-LAST:event_trainingSetTableMouseMoved

    private void brushSizePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_brushSizePropertyChange
        if (matrix != null) {
            matrix.setBrushSize( brushSize.getValue() );
        }
    }//GEN-LAST:event_brushSizePropertyChange

    private void canvasFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_canvasFocusGained

    }//GEN-LAST:event_canvasFocusGained

    private void canvasMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_canvasMouseMoved

    }//GEN-LAST:event_canvasMouseMoved

    private void canvasMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_canvasMouseDragged
        matrixPaint(evt);
    }//GEN-LAST:event_canvasMouseDragged

    private void canvasMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_canvasMousePressed

    }//GEN-LAST:event_canvasMousePressed

    private void canvasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_canvasMouseClicked
        matrixPaint(evt);
    }//GEN-LAST:event_canvasMouseClicked

    private void colorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_colorMouseClicked
        turnColor();
    }//GEN-LAST:event_colorMouseClicked

    private void menuTrainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuTrainActionPerformed
        train();
    }//GEN-LAST:event_menuTrainActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        load();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        save();
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        clearTrainingSet();
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        resetMLP();
        JOptionPane.showMessageDialog(this, "La RNA ha sido reseteada. Es necesario entrenarla.");
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void menuRNA_guardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuRNA_guardarActionPerformed
        saveMLP();
    }//GEN-LAST:event_menuRNA_guardarActionPerformed

    private void menuRNA_cargarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuRNA_cargarActionPerformed
        loadMLP();
    }//GEN-LAST:event_menuRNA_cargarActionPerformed

    private void rnaPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rnaPanelMouseClicked
        mlp.render(rnaPanel.getGraphics());
    }//GEN-LAST:event_rnaPanelMouseClicked

    private void panelVistaGraficaRNAFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_panelVistaGraficaRNAFocusGained
        
    }//GEN-LAST:event_panelVistaGraficaRNAFocusGained

    private void panelVistaGraficaRNAPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_panelVistaGraficaRNAPropertyChange
        
    }//GEN-LAST:event_panelVistaGraficaRNAPropertyChange
        
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                mainFrame = new main();
                mainFrame.setVisible(true);
            }
        });
    }      

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSlider brushSize;
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnCargar;
    private javax.swing.JButton btnEntrenar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnLimpiarImagen;
    private javax.swing.JButton btnReconocer;
    private java.awt.Canvas canvas;
    private javax.swing.JPanel color;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JLabel lbl1;
    private javax.swing.JLabel lblRecognized;
    private javax.swing.JLabel lblTrainingSetSize;
    private javax.swing.JMenu menuCE;
    private javax.swing.JMenu menuRNA;
    private javax.swing.JMenuItem menuRNA_cargar;
    private javax.swing.JMenuItem menuRNA_guardar;
    private javax.swing.JMenuItem menuTrain;
    private javax.swing.JPanel panelVistaGraficaRNA;
    private javax.swing.JProgressBar progressBar1;
    private javax.swing.JProgressBar progressBar2;
    private javax.swing.JPanel rnaPanel;
    private javax.swing.JComboBox salidaEsperada;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JPanel trainPanel;
    private javax.swing.JTable trainingSetTable;
    // End of variables declaration//GEN-END:variables
}