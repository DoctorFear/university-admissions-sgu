package com.xettuyen2026.ui.common;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;

/**
 * Dialog chọn file .xlsx/.csv, preview 5 dòng đầu, confirm import.
 */
public class ImportDialog extends JDialog {

    private File selectedFile;
    private boolean confirmed = false;
    private JLabel lblFileName;
    private JTable previewTable;
    private DefaultTableModel previewModel;

    public ImportDialog(Window owner) {
        super(owner, "Import dữ liệu", ModalityType.APPLICATION_MODAL);
        setSize(650, 420);
        setLocationRelativeTo(owner);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        main.setBackground(Color.WHITE);

        // Top: file selection
        JPanel topPanel = new JPanel(new BorderLayout(8, 0));
        topPanel.setOpaque(false);

        lblFileName = new JLabel("Chưa chọn file");
        lblFileName.setFont(UIConstants.FONT_REGULAR);
        lblFileName.setForeground(UIConstants.TEXT_SECONDARY);

        RoundedButton btnChoose = new RoundedButton("Chọn file...", UIConstants.PRIMARY);
        btnChoose.setPreferredSize(new Dimension(130, 34));
        btnChoose.addActionListener(e -> chooseFile());

        topPanel.add(lblFileName, BorderLayout.CENTER);
        topPanel.add(btnChoose, BorderLayout.EAST);
        main.add(topPanel, BorderLayout.NORTH);

        // Center: preview table
        JLabel lblPreview = new JLabel("Preview (5 dòng đầu):");
        lblPreview.setFont(UIConstants.FONT_BOLD);

        previewModel = new DefaultTableModel();
        previewTable = new JTable(previewModel);
        previewTable.setFont(UIConstants.FONT_SMALL);
        previewTable.setRowHeight(26);
        previewTable.setEnabled(false);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 6));
        centerPanel.setOpaque(false);
        centerPanel.add(lblPreview, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(previewTable), BorderLayout.CENTER);
        main.add(centerPanel, BorderLayout.CENTER);

        // Bottom: buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottomPanel.setOpaque(false);

        RoundedButton btnCancel = new RoundedButton("Hủy", new Color(0x757575));
        btnCancel.addActionListener(e -> dispose());

        RoundedButton btnImport = new RoundedButton(UIConstants.ICON_IMPORT + " Import", UIConstants.SUCCESS);
        btnImport.addActionListener(e -> { confirmed = true; dispose(); });

        bottomPanel.add(btnCancel);
        bottomPanel.add(btnImport);
        main.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Excel / CSV Files", "xlsx", "xls", "csv"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            lblFileName.setText(selectedFile.getName());
            lblFileName.setForeground(UIConstants.TEXT_PRIMARY);
            // Preview loading can be implemented with Apache POI
            previewModel.setRowCount(0);
            previewModel.setColumnCount(0);
            previewModel.addColumn("File đã chọn: " + selectedFile.getName());
            previewModel.addRow(new Object[]{"(Preview sẽ hiển thị khi import thực tế)"});
        }
    }

    public File getSelectedFile() { return selectedFile; }
    public boolean isConfirmed() { return confirmed; }
}
