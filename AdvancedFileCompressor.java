
package com.mycompany.advancedfilecompressor;


import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.*;


public class AdvancedFileCompressor extends JFrame {


    private static final Color PRIMARY = new Color(63, 81, 181);
    private static final Color PRIMARY_DARK = new Color(48, 63, 159);
    private static final Color SUCCESS = new Color(52, 168, 83);
    private static final Color WARNING = new Color(251, 140, 0);
    private static final Color DANGER = new Color(220, 53, 69);

    private static final Color BG_LIGHT = new Color(245, 247, 250);
    private static final Color BG_DARK = new Color(28, 30, 36);
    private static final Color CARD_LIGHT = Color.WHITE;
    private static final Color CARD_DARK = new Color(42, 45, 54);
    private static final Color TEXT_LIGHT = new Color(33, 33, 33);
    private static final Color TEXT_DARK = new Color(225, 225, 225);


    private boolean darkMode = false;
    private int defaultCompressionLevel = 6;
    private String defaultOutputFolder = System.getProperty("user.home");
    private final List<HistoryEntry> historyList = new ArrayList<>();
    private final File historyFile = new File(System.getProperty("user.home"), ".afc_history.csv");


    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainContainer = new JPanel(cardLayout);
    private final JLabel statusLabel = new JLabel("Ready");


    private File compressInputFile;
    private JLabel cNameValue, cExtValue, cSizeValue, cCreatedValue, cModifiedValue, cPathValue;
    private JPanel previewContainer;
    private JTextArea textPreviewArea;
    private JLabel imagePreviewLabel;
    private JRadioButton huffmanRadio, gzipRadio, zipRadio, lzwRadio;
    private JSlider levelSlider;
    private JTextField compressOutputFolderField, compressOutputNameField;
    private JButton compressButton;
    private JProgressBar compressProgressBar;
    private JPanel compressResultPanel;
    private JLabel resOriginal, resCompressed, resSaved, resRatio, resPercent, resTime, resSpeed;
    private BarChartPanel compressBarChart;
    private PieChartPanel compressPieChart;


    private File decompressInputFile;
    private JLabel dNameValue, dSizeValue, dAlgoValue, dModifiedValue;
    private JTextField decompressOutputFolderField;
    private JButton decompressButton;
    private JProgressBar decompressProgressBar;
    private JPanel decompressResultPanel;
    private JLabel decResultFile, decResultSize, decResultTime, decResultHash, decResultIntegrity;


    private File compareFile1, compareFile2;
    private JLabel cmp1Label, cmp2Label;
    private JTextArea compareResultArea;


    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private JTextField historySearchField;


    private JRadioButton lightThemeRadio, darkThemeRadio;
    private JSlider defaultLevelSlider;
    private JTextField defaultFolderField;

    private static final Set<String> TEXT_EXTENSIONS = new HashSet<>(Arrays.asList(
            "txt", "csv", "xml", "json", "java", "html", "css", "js", "md", "log", "ini", "properties"));
    private static final Set<String> IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp"));


    public AdvancedFileCompressor() {
        super("Advanced File Compressor & Decompressor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1250, 780);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        loadHistory();

        add(buildHeaderPanel(), BorderLayout.NORTH);

        mainContainer.add(buildDashboardPanel(), "dashboard");
        mainContainer.add(buildCompressPanel(), "compress");
        mainContainer.add(buildDecompressPanel(), "decompress");
        mainContainer.add(buildComparePanel(), "compare");
        mainContainer.add(buildHistoryPanel(), "history");
        mainContainer.add(buildSettingsPanel(), "settings");
        mainContainer.add(buildHelpPanel(), "help");
        add(mainContainer, BorderLayout.CENTER);

        add(buildStatusBar(), BorderLayout.SOUTH);

        showCard("dashboard");
    }

    private JPanel buildHeaderPanel() {
        GradientPanel header = new GradientPanel(PRIMARY_DARK, PRIMARY);
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(100, 64));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Advanced File Compressor & Decompressor");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JButton themeToggle = new JButton("Dark Mode");
        themeToggle.setFocusPainted(false);
        themeToggle.addActionListener(e -> {
            applyTheme(!darkMode);
            themeToggle.setText(darkMode ? "Light Mode" : "Dark Mode");
        });
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);
        right.add(themeToggle);
        header.add(right, BorderLayout.EAST);
        return header;
    }


    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel welcome = new JLabel("Welcome — choose an action below");
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        panel.add(welcome, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 4, 20, 20));
        grid.setOpaque(false);

        grid.add(makeCard("Compress File", "Reduce file size", PRIMARY, () -> showCard("compress")));
        grid.add(makeCard("Decompress File", "Restore original file", new Color(0, 150, 136), () -> showCard("decompress")));
        grid.add(makeCard("Compare Files", "Find differences", new Color(255, 112, 67), () -> showCard("compare")));
        grid.add(makeCard("Compression History", "View past activity", new Color(126, 87, 194), () -> showCard("history")));
        grid.add(makeCard("Settings", "Customize app", new Color(96, 125, 139), () -> showCard("settings")));
        grid.add(makeCard("Help", "User guide & info", new Color(255, 160, 0), () -> showCard("help")));
        grid.add(makeCard("Exit", "Close application", DANGER, this::confirmExit));

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(grid);
        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private CardButton makeCard(String title, String subtitle, Color color, Runnable action) {
        CardButton btn = new CardButton(title, subtitle, color);
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private void confirmExit() {
        int r = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?", "Exit", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) System.exit(0);
    }


    private JPanel buildCompressPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.add(buildBackBar("Compress File"), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // --- 1. File selection ---
        JPanel selectPanel = new JPanel(new BorderLayout(10, 10));
        selectPanel.setBorder(titled("1. Select File"));

        JButton browseBtn = new JButton("Browse File...");
        browseBtn.addActionListener(e -> browseCompressFile());

        JPanel dropArea = new JPanel(new BorderLayout());
        dropArea.setPreferredSize(new Dimension(100, 50));
        dropArea.setBorder(BorderFactory.createDashedBorder(Color.GRAY));
        dropArea.add(new JLabel("   or drag & drop a file here", JLabel.LEFT), BorderLayout.CENTER);
        dropArea.setTransferHandler(new FileDropHandler(this::loadCompressFile));

        JPanel selectTop = new JPanel(new BorderLayout(10, 0));
        selectTop.add(browseBtn, BorderLayout.WEST);
        selectTop.add(dropArea, BorderLayout.CENTER);
        selectPanel.add(selectTop, BorderLayout.NORTH);

        JPanel infoGrid = new JPanel(new GridLayout(3, 4, 10, 8));
        cNameValue = new JLabel("-");
        cExtValue = new JLabel("-");
        cSizeValue = new JLabel("-");
        cCreatedValue = new JLabel("-");
        cModifiedValue = new JLabel("-");
        cPathValue = new JLabel("-");
        infoGrid.add(boldLabel("File Name:"));
        infoGrid.add(cNameValue);
        infoGrid.add(boldLabel("Extension:"));
        infoGrid.add(cExtValue);
        infoGrid.add(boldLabel("Original Size:"));
        infoGrid.add(cSizeValue);
        infoGrid.add(boldLabel("Created:"));
        infoGrid.add(cCreatedValue);
        infoGrid.add(boldLabel("Modified:"));
        infoGrid.add(cModifiedValue);
        infoGrid.add(boldLabel("Path:"));
        infoGrid.add(cPathValue);
        selectPanel.add(infoGrid, BorderLayout.CENTER);

        content.add(selectPanel);
        content.add(Box.createVerticalStrut(10));

        // --- 2. Preview ---
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(titled("2. File Preview (first 500 lines for text files)"));
        previewPanel.setPreferredSize(new Dimension(100, 220));
        textPreviewArea = new JTextArea();
        textPreviewArea.setEditable(false);
        textPreviewArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        imagePreviewLabel = new JLabel("", JLabel.CENTER);
        previewContainer = new JPanel(new BorderLayout());
        previewContainer.add(new JScrollPane(textPreviewArea), BorderLayout.CENTER);
        previewPanel.add(previewContainer, BorderLayout.CENTER);
        content.add(previewPanel);
        content.add(Box.createVerticalStrut(10));

        // --- 3. Options ---
        JPanel optionsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        optionsPanel.setBorder(titled("3. Compression Options"));

        JPanel algoPanel = new JPanel(new GridLayout(4, 1));
        algoPanel.setBorder(BorderFactory.createTitledBorder("Algorithm"));
        huffmanRadio = new JRadioButton("Huffman Coding", true);
        gzipRadio = new JRadioButton("GZIP");
        zipRadio = new JRadioButton("ZIP");
        lzwRadio = new JRadioButton("LZW");
        ButtonGroup algoGroup = new ButtonGroup();
        algoGroup.add(huffmanRadio);
        algoGroup.add(gzipRadio);
        algoGroup.add(zipRadio);
        algoGroup.add(lzwRadio);
        algoPanel.add(huffmanRadio);
        algoPanel.add(gzipRadio);
        algoPanel.add(zipRadio);
        algoPanel.add(lzwRadio);

        JPanel levelPanel = new JPanel(new BorderLayout());
        levelPanel.setBorder(BorderFactory.createTitledBorder("Compression Level (1-9)"));
        levelSlider = new JSlider(1, 9, defaultCompressionLevel);
        levelSlider.setMajorTickSpacing(1);
        levelSlider.setPaintTicks(true);
        levelSlider.setPaintLabels(true);
        levelPanel.add(levelSlider, BorderLayout.CENTER);

        JPanel outputPanel = new JPanel();
        outputPanel.setLayout(new BoxLayout(outputPanel, BoxLayout.Y_AXIS));
        outputPanel.setBorder(BorderFactory.createTitledBorder("Output"));
        compressOutputFolderField = new JTextField(defaultOutputFolder);
        JButton chooseFolderBtn = new JButton("Choose Folder");
        chooseFolderBtn.addActionListener(e -> chooseOutputFolder(compressOutputFolderField));
        compressOutputNameField = new JTextField("compressed_output");
        outputPanel.add(new JLabel("Output Folder:"));
        JPanel folderRow = new JPanel(new BorderLayout());
        folderRow.add(compressOutputFolderField, BorderLayout.CENTER);
        folderRow.add(chooseFolderBtn, BorderLayout.EAST);
        outputPanel.add(folderRow);
        outputPanel.add(Box.createVerticalStrut(8));
        outputPanel.add(new JLabel("File Name (no extension):"));
        outputPanel.add(compressOutputNameField);

        optionsPanel.add(algoPanel);
        optionsPanel.add(levelPanel);
        optionsPanel.add(outputPanel);
        content.add(optionsPanel);
        content.add(Box.createVerticalStrut(10));

        // --- Action ---
        JPanel actionPanel = new JPanel(new BorderLayout(10, 5));
        compressButton = new JButton("Compress Now");
        compressButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        compressButton.setBackground(PRIMARY);
        compressButton.setForeground(Color.WHITE);
        compressButton.addActionListener(e -> startCompression());
        compressProgressBar = new JProgressBar(0, 100);
        compressProgressBar.setStringPainted(true);
        actionPanel.add(compressButton, BorderLayout.WEST);
        actionPanel.add(compressProgressBar, BorderLayout.CENTER);
        content.add(actionPanel);
        content.add(Box.createVerticalStrut(10));

        // --- 4. Result ---
        compressResultPanel = new JPanel(new BorderLayout(10, 10));
        compressResultPanel.setBorder(titled("4. Result"));
        compressResultPanel.setVisible(false);

        JPanel statsGrid = new JPanel(new GridLayout(7, 2, 10, 5));
        resOriginal = new JLabel("-");
        resCompressed = new JLabel("-");
        resSaved = new JLabel("-");
        resRatio = new JLabel("-");
        resPercent = new JLabel("-");
        resTime = new JLabel("-");
        resSpeed = new JLabel("-");
        statsGrid.add(boldLabel("Original Size:"));
        statsGrid.add(resOriginal);
        statsGrid.add(boldLabel("Compressed Size:"));
        statsGrid.add(resCompressed);
        statsGrid.add(boldLabel("Space Saved:"));
        statsGrid.add(resSaved);
        statsGrid.add(boldLabel("Compression Ratio:"));
        statsGrid.add(resRatio);
        statsGrid.add(boldLabel("Percentage Saved:"));
        statsGrid.add(resPercent);
        statsGrid.add(boldLabel("Compression Time:"));
        statsGrid.add(resTime);
        statsGrid.add(boldLabel("Speed:"));
        statsGrid.add(resSpeed);

        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        compressBarChart = new BarChartPanel();
        compressPieChart = new PieChartPanel();
        chartsPanel.add(wrapTitled(compressBarChart, "Before vs After"));
        chartsPanel.add(wrapTitled(compressPieChart, "Size Distribution"));

        compressResultPanel.add(statsGrid, BorderLayout.WEST);
        compressResultPanel.add(chartsPanel, BorderLayout.CENTER);

        content.add(compressResultPanel);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void browseCompressFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select File to Compress");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            loadCompressFile(chooser.getSelectedFile());
        }
    }

    private void loadCompressFile(File f) {
        compressInputFile = f;
        updateFileInfo(f);
        updatePreview(f);
        compressResultPanel.setVisible(false);
        setStatus("Loaded file: " + f.getName());
    }

    private void updateFileInfo(File f) {
        cNameValue.setText(f.getName());
        String ext = getExtension(f);
        cExtValue.setText(ext.isEmpty() ? "(none)" : ext);
        cSizeValue.setText(formatSize(f.length()));
        cPathValue.setText(f.getAbsolutePath());
        try {
            BasicFileAttributes attrs = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            cCreatedValue.setText(sdf.format(new Date(attrs.creationTime().toMillis())));
            cModifiedValue.setText(sdf.format(new Date(attrs.lastModifiedTime().toMillis())));
        } catch (IOException ex) {
            cCreatedValue.setText("Unknown");
            cModifiedValue.setText("Unknown");
        }
    }

    private void updatePreview(File f) {
        String ext = getExtension(f).toLowerCase();
        previewContainer.removeAll();
        if (isImageExtension(ext)) {
            try {
                BufferedImage img = ImageIO.read(f);
                if (img != null) {
                    Image scaled = img.getScaledInstance(400, -1, Image.SCALE_SMOOTH);
                    imagePreviewLabel.setIcon(new ImageIcon(scaled));
                    imagePreviewLabel.setText(null);
                } else {
                    imagePreviewLabel.setIcon(null);
                    imagePreviewLabel.setText("Unable to preview this image format.");
                }
            } catch (IOException ex) {
                imagePreviewLabel.setIcon(null);
                imagePreviewLabel.setText("Unable to preview image: " + ex.getMessage());
            }
            previewContainer.add(new JScrollPane(imagePreviewLabel), BorderLayout.CENTER);
        } else if (isTextExtension(ext)) {
            try {
                textPreviewArea.setText(readPreviewText(f, 500));
            } catch (IOException ex) {
                textPreviewArea.setText("Unable to read file: " + ex.getMessage());
            }
            textPreviewArea.setCaretPosition(0);
            previewContainer.add(new JScrollPane(textPreviewArea), BorderLayout.CENTER);
        } else {
            textPreviewArea.setText("Preview not available for this file type.\n\nFile type: "
                    + (ext.isEmpty() ? "unknown" : "." + ext));
            previewContainer.add(new JScrollPane(textPreviewArea), BorderLayout.CENTER);
        }
        previewContainer.revalidate();
        previewContainer.repaint();
    }

    private void startCompression() {
        if (compressInputFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a file first.", "No File Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!compressInputFile.exists()) {
            JOptionPane.showMessageDialog(this, "Selected file no longer exists.", "Invalid File", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (compressInputFile.length() == 0) {
            JOptionPane.showMessageDialog(this, "Cannot compress an empty file.", "Empty File", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File outDir = new File(compressOutputFolderField.getText().trim());
        if (!outDir.exists() || !outDir.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Please choose a valid output folder.", "Invalid Folder", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!outDir.canWrite()) {
            JOptionPane.showMessageDialog(this, "Permission denied: cannot write to the output folder.", "Permission Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String outName = compressOutputNameField.getText().trim();
        if (outName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an output file name.", "Missing Name", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String algo = huffmanRadio.isSelected() ? "HUFFMAN" : gzipRadio.isSelected() ? "GZIP" : zipRadio.isSelected() ? "ZIP" : "LZW";
        int level = levelSlider.getValue();

        long freeSpace = outDir.getFreeSpace();
        if (freeSpace > 0 && freeSpace < compressInputFile.length() / 2) {
            int r = JOptionPane.showConfirmDialog(this, "Disk space appears low. Continue anyway?",
                    "Low Disk Space", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (r != JOptionPane.YES_OPTION) return;
        }

        compressButton.setEnabled(false);
        compressProgressBar.setValue(0);
        setStatus("Compressing...");
        new CompressWorker(compressInputFile, outDir, outName, algo, level).execute();
    }

    /** SwingWorker that performs compression off the EDT and reports progress. */
    class CompressWorker extends SwingWorker<CompressionService.CompressionResult, Integer> {
        private final File input;
        private final File outDir;
        private final String outName;
        private final String algo;
        private final int level;

        CompressWorker(File input, File outDir, String outName, String algo, int level) {
            this.input = input;
            this.outDir = outDir;
            this.outName = outName;
            this.algo = algo;
            this.level = level;
        }

        @Override
        protected CompressionService.CompressionResult doInBackground() throws Exception {
            return CompressionService.compress(input, outDir, outName, algo, level, percent -> publish(percent));
        }

        @Override
        protected void process(List<Integer> chunks) {
            compressProgressBar.setValue(chunks.get(chunks.size() - 1));
        }

        @Override
        protected void done() {
            compressButton.setEnabled(true);
            try {
                CompressionService.CompressionResult result = get();
                displayCompressionResult(result);
                setStatus("Compression complete: " + result.outputFile.getName());
            } catch (Exception ex) {
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                JOptionPane.showMessageDialog(AdvancedFileCompressor.this,
                        "Compression failed: " + cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                setStatus("Compression failed.");
            }
        }
    }

    private void displayCompressionResult(CompressionService.CompressionResult r) {
        resOriginal.setText(formatSize(r.originalSize));
        resCompressed.setText(formatSize(r.compressedSize));
        long saved = r.originalSize - r.compressedSize;
        resSaved.setText(formatSize(Math.max(saved, 0)));
        double ratio = r.originalSize == 0 ? 0 : (r.compressedSize * 100.0 / r.originalSize);
        resRatio.setText(String.format("%.2f%%", ratio));
        resPercent.setText(String.format("%.2f%%", 100 - ratio));
        resTime.setText(r.timeMillis + " ms");
        double speed = r.timeMillis > 0 ? (r.originalSize / 1024.0 / 1024.0) / (r.timeMillis / 1000.0) : 0;
        resSpeed.setText(String.format("%.2f MB/s", speed));
        compressBarChart.setData(r.originalSize, r.compressedSize);
        compressPieChart.setData(r.compressedSize, Math.max(saved, 0));
        compressResultPanel.setVisible(true);
        compressResultPanel.revalidate();
        compressResultPanel.repaint();

        HistoryEntry entry = new HistoryEntry();
        entry.dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        entry.originalFile = compressInputFile.getAbsolutePath();
        entry.compressedFile = r.outputFile.getAbsolutePath();
        entry.algorithm = r.algorithm;
        entry.originalSize = r.originalSize;
        entry.compressedSize = r.compressedSize;
        entry.durationMillis = r.timeMillis;
        try {
            entry.originalHash = computeHash(compressInputFile, "SHA-256");
        } catch (Exception ex) {
            entry.originalHash = "";
        }
        addHistoryEntry(entry);
    }


    private JPanel buildDecompressPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.add(buildBackBar("Decompress File"), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel selectPanel = new JPanel(new BorderLayout(10, 10));
        selectPanel.setBorder(titled("1. Select Compressed File"));
        JButton browseBtn = new JButton("Browse Compressed File...");
        browseBtn.addActionListener(e -> browseDecompressFile());
        selectPanel.add(browseBtn, BorderLayout.NORTH);

        JPanel infoGrid = new JPanel(new GridLayout(4, 2, 10, 8));
        dNameValue = new JLabel("-");
        dSizeValue = new JLabel("-");
        dAlgoValue = new JLabel("-");
        dModifiedValue = new JLabel("-");
        infoGrid.add(boldLabel("File Name:"));
        infoGrid.add(dNameValue);
        infoGrid.add(boldLabel("Size:"));
        infoGrid.add(dSizeValue);
        infoGrid.add(boldLabel("Detected Algorithm:"));
        infoGrid.add(dAlgoValue);
        infoGrid.add(boldLabel("Modified:"));
        infoGrid.add(dModifiedValue);
        selectPanel.add(infoGrid, BorderLayout.CENTER);
        content.add(selectPanel);
        content.add(Box.createVerticalStrut(10));

        JPanel outputPanel = new JPanel(new BorderLayout(10, 5));
        outputPanel.setBorder(titled("2. Output Folder"));
        decompressOutputFolderField = new JTextField(defaultOutputFolder);
        JButton chooseBtn = new JButton("Choose Folder");
        chooseBtn.addActionListener(e -> chooseOutputFolder(decompressOutputFolderField));
        outputPanel.add(decompressOutputFolderField, BorderLayout.CENTER);
        outputPanel.add(chooseBtn, BorderLayout.EAST);
        content.add(outputPanel);
        content.add(Box.createVerticalStrut(10));

        JPanel actionPanel = new JPanel(new BorderLayout(10, 5));
        decompressButton = new JButton("Decompress Now");
        decompressButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        decompressButton.setBackground(new Color(0, 150, 136));
        decompressButton.setForeground(Color.WHITE);
        decompressButton.addActionListener(e -> startDecompression());
        decompressProgressBar = new JProgressBar(0, 100);
        decompressProgressBar.setStringPainted(true);
        actionPanel.add(decompressButton, BorderLayout.WEST);
        actionPanel.add(decompressProgressBar, BorderLayout.CENTER);
        content.add(actionPanel);
        content.add(Box.createVerticalStrut(10));

        decompressResultPanel = new JPanel(new GridLayout(5, 2, 10, 8));
        decompressResultPanel.setBorder(titled("3. Result"));
        decompressResultPanel.setVisible(false);
        decResultFile = new JLabel("-");
        decResultSize = new JLabel("-");
        decResultTime = new JLabel("-");
        decResultHash = new JLabel("-");
        decResultIntegrity = new JLabel("-");
        decompressResultPanel.add(boldLabel("Output File:"));
        decompressResultPanel.add(decResultFile);
        decompressResultPanel.add(boldLabel("Decompressed Size:"));
        decompressResultPanel.add(decResultSize);
        decompressResultPanel.add(boldLabel("Time Taken:"));
        decompressResultPanel.add(decResultTime);
        decompressResultPanel.add(boldLabel("SHA-256 Hash:"));
        decompressResultPanel.add(decResultHash);
        decompressResultPanel.add(boldLabel("Integrity Check:"));
        decompressResultPanel.add(decResultIntegrity);
        content.add(decompressResultPanel);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void browseDecompressFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Compressed File");
        chooser.setFileFilter(new FileNameExtensionFilter("Compressed Files (*.zip, *.gz, *.huff, *.lzw)", "zip", "gz", "huff", "lzw"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            decompressInputFile = chooser.getSelectedFile();
            dNameValue.setText(decompressInputFile.getName());
            dSizeValue.setText(formatSize(decompressInputFile.length()));
            String ext = getExtension(decompressInputFile).toLowerCase();
            String algo;
            switch (ext) {
                case "zip": algo = "ZIP"; break;
                case "gz": algo = "GZIP"; break;
                case "huff": algo = "Huffman Coding"; break;
                case "lzw": algo = "LZW"; break;
                default: algo = "Unknown / Unsupported";
            }
            dAlgoValue.setText(algo);
            try {
                BasicFileAttributes attrs = Files.readAttributes(decompressInputFile.toPath(), BasicFileAttributes.class);
                dModifiedValue.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(attrs.lastModifiedTime().toMillis())));
            } catch (IOException ex) {
                dModifiedValue.setText("Unknown");
            }
            decompressResultPanel.setVisible(false);
            setStatus("Loaded compressed file: " + decompressInputFile.getName());
        }
    }

    private void startDecompression() {
        if (decompressInputFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a compressed file first.", "No File Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String ext = getExtension(decompressInputFile).toLowerCase();
        if (!ext.equals("zip") && !ext.equals("gz") && !ext.equals("huff") && !ext.equals("lzw")) {
            JOptionPane.showMessageDialog(this, "Unsupported file extension: ." + ext, "Unsupported Extension", JOptionPane.ERROR_MESSAGE);
            return;
        }
        File outDir = new File(decompressOutputFolderField.getText().trim());
        if (!outDir.exists() || !outDir.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Please choose a valid output folder.", "Invalid Folder", JOptionPane.WARNING_MESSAGE);
            return;
        }
        decompressButton.setEnabled(false);
        decompressProgressBar.setValue(0);
        setStatus("Decompressing...");
        new DecompressWorker(decompressInputFile, outDir).execute();
    }

    /** SwingWorker that performs decompression off the EDT and reports progress. */
    class DecompressWorker extends SwingWorker<File, Integer> {
        private final File input;
        private final File outDir;
        private long timeMillis;

        DecompressWorker(File input, File outDir) {
            this.input = input;
            this.outDir = outDir;
        }

        @Override
        protected File doInBackground() throws Exception {
            long start = System.currentTimeMillis();
            File result = CompressionService.decompress(input, outDir, percent -> publish(percent));
            timeMillis = System.currentTimeMillis() - start;
            return result;
        }

        @Override
        protected void process(List<Integer> chunks) {
            decompressProgressBar.setValue(chunks.get(chunks.size() - 1));
        }

        @Override
        protected void done() {
            decompressButton.setEnabled(true);
            try {
                File output = get();
                decResultFile.setText(output.getAbsolutePath());
                decResultSize.setText(formatSize(output.length()));
                decResultTime.setText(timeMillis + " ms");
                String hash = computeHash(output, "SHA-256");
                decResultHash.setText(hash);
                String matched = findOriginalHashForCompressedFile(input.getAbsolutePath());
                if (matched != null && !matched.isEmpty()) {
                    if (matched.equalsIgnoreCase(hash)) {
                        decResultIntegrity.setText("Integrity Verified");
                        decResultIntegrity.setForeground(SUCCESS);
                    } else {
                        decResultIntegrity.setText("Corrupted File (hash mismatch)");
                        decResultIntegrity.setForeground(DANGER);
                    }
                } else {
                    decResultIntegrity.setText("No history record to verify against");
                    decResultIntegrity.setForeground(WARNING);
                }
                decompressResultPanel.setVisible(true);
                decompressResultPanel.revalidate();
                decompressResultPanel.repaint();
                setStatus("Decompression complete: " + output.getName());
            } catch (Exception ex) {
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                JOptionPane.showMessageDialog(AdvancedFileCompressor.this,
                        "Decompression failed: " + cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                setStatus("Decompression failed.");
            }
        }
    }

    private String findOriginalHashForCompressedFile(String compressedPath) {
        for (HistoryEntry e : historyList) {
            if (e.compressedFile.equals(compressedPath)) return e.originalHash;
        }
        return null;
    }

    private JPanel buildComparePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.add(buildBackBar("Compare Files"), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel selectPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        selectPanel.setBorder(titled("Select Two Files"));

        JPanel row1 = new JPanel(new BorderLayout(10, 5));
        JButton btn1 = new JButton("Select File 1");
        cmp1Label = new JLabel("No file selected");
        btn1.addActionListener(e -> browseCompareFile(1));
        row1.add(btn1, BorderLayout.WEST);
        row1.add(cmp1Label, BorderLayout.CENTER);

        JPanel row2 = new JPanel(new BorderLayout(10, 5));
        JButton btn2 = new JButton("Select File 2");
        cmp2Label = new JLabel("No file selected");
        btn2.addActionListener(e -> browseCompareFile(2));
        row2.add(btn2, BorderLayout.WEST);
        row2.add(cmp2Label, BorderLayout.CENTER);

        selectPanel.add(row1);
        selectPanel.add(row2);
        content.add(selectPanel);
        content.add(Box.createVerticalStrut(10));

        JButton compareBtn = new JButton("Compare Files");
        compareBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        compareBtn.addActionListener(e -> startCompare());
        content.add(compareBtn);
        content.add(Box.createVerticalStrut(10));

        compareResultArea = new JTextArea(15, 50);
        compareResultArea.setEditable(false);
        compareResultArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        JScrollPane resultScroll = new JScrollPane(compareResultArea);
        resultScroll.setBorder(titled("Comparison Result"));
        content.add(resultScroll);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void browseCompareFile(int which) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (which == 1) {
                compareFile1 = f;
                cmp1Label.setText(f.getAbsolutePath());
            } else {
                compareFile2 = f;
                cmp2Label.setText(f.getAbsolutePath());
            }
        }
    }

    private void startCompare() {
        if (compareFile1 == null || compareFile2 == null) {
            JOptionPane.showMessageDialog(this, "Please select both files.", "Missing Files", JOptionPane.WARNING_MESSAGE);
            return;
        }
        compareResultArea.setText("Comparing files, please wait...\n");
        setStatus("Comparing files...");
        new CompareWorker(compareFile1, compareFile2).execute();
    }

    /** SwingWorker that performs file comparison (size, hash, byte diff) off the EDT. */
    class CompareWorker extends SwingWorker<String, Void> {
        private final File f1, f2;

        CompareWorker(File f1, File f2) {
            this.f1 = f1;
            this.f2 = f2;
        }

        @Override
        protected String doInBackground() throws Exception {
            StringBuilder sb = new StringBuilder();
            sb.append("File 1: ").append(f1.getAbsolutePath()).append("\n");
            sb.append("File 2: ").append(f2.getAbsolutePath()).append("\n\n");
            long s1 = f1.length(), s2 = f2.length();
            sb.append("Size 1: ").append(formatSize(s1)).append("\n");
            sb.append("Size 2: ").append(formatSize(s2)).append("\n");
            sb.append("Size Match: ").append(s1 == s2 ? "YES" : "NO").append("\n\n");

            String sha1 = computeHash(f1, "SHA-256");
            String sha2 = computeHash(f2, "SHA-256");
            sb.append("SHA-256 (File 1): ").append(sha1).append("\n");
            sb.append("SHA-256 (File 2): ").append(sha2).append("\n");
            boolean identical = sha1.equals(sha2);
            sb.append("Hash Match: ").append(identical ? "YES (Files Identical)" : "NO (Files Differ)").append("\n\n");

            String md5_1 = computeHash(f1, "MD5");
            String md5_2 = computeHash(f2, "MD5");
            sb.append("MD5 (File 1): ").append(md5_1).append("\n");
            sb.append("MD5 (File 2): ").append(md5_2).append("\n\n");

            if (!identical) {
                long diffBytes = 0;
                long comparedBytes = Math.min(s1, s2);
                List<Long> diffOffsets = new ArrayList<>();
                try (BufferedInputStream b1 = new BufferedInputStream(new FileInputStream(f1));
                     BufferedInputStream b2 = new BufferedInputStream(new FileInputStream(f2))) {
                    long pos = 0;
                    int r1, r2;
                    while (true) {
                        r1 = b1.read();
                        r2 = b2.read();
                        if (r1 == -1 || r2 == -1) break;
                        if (r1 != r2) {
                            diffBytes++;
                            if (diffOffsets.size() < 20) diffOffsets.add(pos);
                        }
                        pos++;
                    }
                }
                sb.append("Bytes Compared: ").append(comparedBytes).append("\n");
                sb.append("Differing Bytes: ").append(diffBytes).append("\n");
                if (s1 != s2) sb.append("Extra bytes in larger file: ").append(Math.abs(s1 - s2)).append("\n");
                if (!diffOffsets.isEmpty()) {
                    sb.append("First differing byte offsets: ");
                    for (Long off : diffOffsets) sb.append(off).append(" ");
                    sb.append("\n");
                }
            } else {
                sb.append("Result: Files are byte-for-byte identical.\n");
            }
            return sb.toString();
        }

        @Override
        protected void done() {
            try {
                compareResultArea.setText(get());
                setStatus("Comparison complete.");
            } catch (Exception ex) {
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                compareResultArea.setText("Error comparing files: " + cause.getMessage());
                setStatus("Comparison failed.");
            }
        }
    }

    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.add(buildBackBar("Compression History"), BorderLayout.NORTH);

        JPanel topBar = new JPanel(new BorderLayout(10, 5));
        historySearchField = new JTextField();
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> filterHistoryTable(historySearchField.getText()));
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        searchPanel.add(historySearchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);
        topBar.add(searchPanel, BorderLayout.CENTER);

        String[] columns = {"Date/Time", "Original File", "Compressed File", "Algorithm",
                "Original Size", "Compressed Size", "Ratio", "Duration (ms)"};
        historyTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        historyTable = new JTable(historyTableModel);
        historyTable.setAutoCreateRowSorter(true);
        JScrollPane tableScroll = new JScrollPane(historyTable);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.addActionListener(e -> deleteSelectedHistory());
        JButton exportBtn = new JButton("Export CSV");
        exportBtn.addActionListener(e -> exportHistoryCsv());
        JButton clearBtn = new JButton("Clear All");
        clearBtn.addActionListener(e -> clearAllHistory());
        bottomBar.add(deleteBtn);
        bottomBar.add(exportBtn);
        bottomBar.add(clearBtn);

        JPanel center = new JPanel(new BorderLayout(5, 5));
        center.add(topBar, BorderLayout.NORTH);
        center.add(tableScroll, BorderLayout.CENTER);
        center.add(bottomBar, BorderLayout.SOUTH);

        panel.add(center, BorderLayout.CENTER);
        refreshHistoryTable();
        return panel;
    }

    private void refreshHistoryTable() {
        historyTableModel.setRowCount(0);
        for (HistoryEntry e : historyList) {
            historyTableModel.addRow(new Object[]{
                    e.dateTime, e.originalFile, e.compressedFile, e.algorithm,
                    formatSize(e.originalSize), formatSize(e.compressedSize),
                    String.format("%.2f%%", e.ratio()), e.durationMillis
            });
        }
    }

    private void filterHistoryTable(String query) {
        if (query == null || query.trim().isEmpty()) {
            refreshHistoryTable();
            return;
        }
        historyTableModel.setRowCount(0);
        String q = query.toLowerCase();
        for (HistoryEntry e : historyList) {
            if (e.originalFile.toLowerCase().contains(q) || e.compressedFile.toLowerCase().contains(q)
                    || e.algorithm.toLowerCase().contains(q)) {
                historyTableModel.addRow(new Object[]{
                        e.dateTime, e.originalFile, e.compressedFile, e.algorithm,
                        formatSize(e.originalSize), formatSize(e.compressedSize),
                        String.format("%.2f%%", e.ratio()), e.durationMillis
                });
            }
        }
    }

    private void deleteSelectedHistory() {
        int row = historyTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = historyTable.convertRowIndexToModel(row);
        String dateTime = (String) historyTableModel.getValueAt(modelRow, 0);
        String compressedFile = (String) historyTableModel.getValueAt(modelRow, 2);
        historyList.removeIf(e -> e.dateTime.equals(dateTime) && e.compressedFile.equals(compressedFile));
        saveHistory();
        refreshHistoryTable();
        setStatus("History entry deleted.");
    }

    private void clearAllHistory() {
        int r = JOptionPane.showConfirmDialog(this, "Clear all history?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            historyList.clear();
            saveHistory();
            refreshHistoryTable();
            setStatus("History cleared.");
        }
    }

    private void exportHistoryCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export History as CSV");
        chooser.setSelectedFile(new File("compression_history.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File out = chooser.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(new FileWriter(out))) {
                pw.println("Date/Time,Original File,Compressed File,Algorithm,Original Size,Compressed Size,Ratio,Duration(ms)");
                for (HistoryEntry e : historyList) {
                    pw.println(csvEscape(e.dateTime) + "," + csvEscape(e.originalFile) + "," + csvEscape(e.compressedFile) + ","
                            + csvEscape(e.algorithm) + "," + e.originalSize + "," + e.compressedSize + ","
                            + String.format("%.2f", e.ratio()) + "," + e.durationMillis);
                }
                JOptionPane.showMessageDialog(this, "History exported successfully.", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addHistoryEntry(HistoryEntry entry) {
        historyList.add(0, entry);
        saveHistory();
        refreshHistoryTable();
    }

    private void loadHistory() {
        if (!historyFile.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(historyFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                List<String> fields = parseCsvLine(line);
                if (fields.size() < 8) continue;
                HistoryEntry e = new HistoryEntry();
                e.dateTime = fields.get(0);
                e.originalFile = fields.get(1);
                e.compressedFile = fields.get(2);
                e.algorithm = fields.get(3);
                e.originalSize = Long.parseLong(fields.get(4));
                e.compressedSize = Long.parseLong(fields.get(5));
                e.durationMillis = Long.parseLong(fields.get(6));
                e.originalHash = fields.get(7);
                historyList.add(e);
            }
        } catch (Exception ex) {
            System.err.println("Failed to load history: " + ex.getMessage());
        }
    }

    private void saveHistory() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(historyFile))) {
            for (HistoryEntry e : historyList) {
                pw.println(csvEscape(e.dateTime) + "," + csvEscape(e.originalFile) + "," + csvEscape(e.compressedFile) + ","
                        + csvEscape(e.algorithm) + "," + e.originalSize + "," + e.compressedSize + "," + e.durationMillis + ","
                        + csvEscape(e.originalHash == null ? "" : e.originalHash));
            }
        } catch (IOException ex) {
            System.err.println("Failed to save history: " + ex.getMessage());
        }
    }

    private static String csvEscape(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    private static List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(c);
                }
            } else {
                if (c == '"') inQuotes = true;
                else if (c == ',') {
                    result.add(cur.toString());
                    cur.setLength(0);
                } else cur.append(c);
            }
        }
        result.add(cur.toString());
        return result;
    }


    private JPanel buildSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.add(buildBackBar("Settings"), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel themePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        themePanel.setBorder(titled("Theme"));
        lightThemeRadio = new JRadioButton("Light", !darkMode);
        darkThemeRadio = new JRadioButton("Dark", darkMode);
        ButtonGroup tg = new ButtonGroup();
        tg.add(lightThemeRadio);
        tg.add(darkThemeRadio);
        lightThemeRadio.addActionListener(e -> applyTheme(false));
        darkThemeRadio.addActionListener(e -> applyTheme(true));
        themePanel.add(lightThemeRadio);
        themePanel.add(darkThemeRadio);
        content.add(themePanel);
        content.add(Box.createVerticalStrut(10));

        JPanel levelPanel = new JPanel(new BorderLayout());
        levelPanel.setBorder(titled("Default Compression Level"));
        defaultLevelSlider = new JSlider(1, 9, defaultCompressionLevel);
        defaultLevelSlider.setMajorTickSpacing(1);
        defaultLevelSlider.setPaintTicks(true);
        defaultLevelSlider.setPaintLabels(true);
        defaultLevelSlider.addChangeListener(e -> {
            defaultCompressionLevel = defaultLevelSlider.getValue();
            if (levelSlider != null) levelSlider.setValue(defaultCompressionLevel);
        });
        levelPanel.add(defaultLevelSlider, BorderLayout.CENTER);
        content.add(levelPanel);
        content.add(Box.createVerticalStrut(10));

        JPanel folderPanel = new JPanel(new BorderLayout(10, 5));
        folderPanel.setBorder(titled("Default Output Folder"));
        defaultFolderField = new JTextField(defaultOutputFolder);
        JButton chooseBtn = new JButton("Choose");
        chooseBtn.addActionListener(e -> chooseOutputFolder(defaultFolderField));
        folderPanel.add(defaultFolderField, BorderLayout.CENTER);
        folderPanel.add(chooseBtn, BorderLayout.EAST);
        content.add(folderPanel);
        content.add(Box.createVerticalStrut(10));

        JButton saveBtn = new JButton("Save Settings");
        saveBtn.addActionListener(e -> {
            defaultOutputFolder = defaultFolderField.getText().trim();
            if (compressOutputFolderField != null) compressOutputFolderField.setText(defaultOutputFolder);
            if (decompressOutputFolderField != null) decompressOutputFolderField.setText(defaultOutputFolder);
            JOptionPane.showMessageDialog(this, "Settings saved.", "Settings", JOptionPane.INFORMATION_MESSAGE);
            setStatus("Settings updated.");
        });
        content.add(saveBtn);

        panel.add(content, BorderLayout.NORTH);
        return panel;
    }


    private JPanel buildHelpPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.add(buildBackBar("Help & About"), BorderLayout.NORTH);

        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane.setText(
                "<html><body style='font-family:Segoe UI; padding:10px;'>"
                        + "<h2>User Guide</h2>"
                        + "<p>1. Go to <b>Compress File</b>, browse or drag a file, choose an algorithm and "
                        + "compression level, then click Compress Now.</p>"
                        + "<p>2. Go to <b>Decompress File</b> to restore a previously compressed file and verify "
                        + "its integrity against history records.</p>"
                        + "<p>3. Use <b>Compare Files</b> to check whether two files are identical.</p>"
                        + "<p>4. Review past activity any time in <b>Compression History</b>.</p>"
                        + "<h2>Compression Algorithms</h2>"
                        + "<p><b>Huffman Coding:</b> A lossless algorithm that assigns shorter binary codes to more "
                        + "frequent bytes, building an optimal prefix-code tree from byte frequencies.</p>"
                        + "<p><b>GZIP:</b> Uses the DEFLATE algorithm (LZ77 + Huffman coding), commonly used for "
                        + "single-file compression.</p>"
                        + "<p><b>ZIP:</b> A container format supporting DEFLATE compression, capable of storing one "
                        + "or more entries.</p>"
                        + "<p><b>LZW (Lempel-Ziv-Welch):</b> A dictionary-based algorithm that replaces repeated "
                        + "sequences with shorter codes, historically used in formats such as GIF.</p>"
                        + "<h2>About</h2>"
                        + "<p>Advanced File Compressor &amp; Decompressor is a desktop utility built with Java "
                        + "Swing that demonstrates multiple lossless compression techniques, file integrity "
                        + "verification, and history tracking — implemented entirely with the standard JDK.</p>"
                        + "</body></html>"
        );
        panel.add(new JScrollPane(editorPane), BorderLayout.CENTER);
        return panel;
    }


    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        bar.setBackground(new Color(230, 230, 230));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bar.add(statusLabel, BorderLayout.WEST);
        return bar;
    }

    private void setStatus(String text) {
        statusLabel.setText(text);
    }

    private JPanel buildBackBar(String title) {
        JPanel bar = new JPanel(new BorderLayout());
        JButton back = new JButton("<- Back to Dashboard");
        back.addActionListener(e -> showCard("dashboard"));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        bar.add(back, BorderLayout.WEST);
        bar.add(titleLabel, BorderLayout.CENTER);
        return bar;
    }

    private void showCard(String name) {
        cardLayout.show(mainContainer, name);
    }

    private Border titled(String text) {
        return BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), text);
    }

    private JLabel boldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        return l;
    }

    private JPanel wrapTitled(Component c, String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(titled(title));
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private void chooseOutputFolder(JTextField field) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        File current = new File(field.getText().trim());
        if (current.exists()) chooser.setCurrentDirectory(current);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            field.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }


    private void applyTheme(boolean dark) {
        darkMode = dark;
        Color bg = dark ? BG_DARK : BG_LIGHT;
        Color cardBg = dark ? CARD_DARK : CARD_LIGHT;
        Color fg = dark ? TEXT_DARK : TEXT_LIGHT;
        getContentPane().setBackground(bg);
        themeContainer(mainContainer, bg, cardBg, fg);
        if (lightThemeRadio != null) {
            lightThemeRadio.setSelected(!dark);
            darkThemeRadio.setSelected(dark);
        }
        mainContainer.revalidate();
        mainContainer.repaint();
    }

    private void themeContainer(Container c, Color bg, Color cardBg, Color fg) {
        for (Component comp : c.getComponents()) {
            if (comp instanceof CardButton || comp instanceof GradientPanel) {
                // Custom-painted components manage their own colors.
            } else if (comp instanceof JTextArea) {
                comp.setBackground(cardBg);
                comp.setForeground(fg);
                ((JTextArea) comp).setCaretColor(fg);
            } else if (comp instanceof JTextField) {
                comp.setBackground(cardBg);
                comp.setForeground(fg);
            } else if (comp instanceof JTable) {
                comp.setBackground(cardBg);
                comp.setForeground(fg);
            } else if (comp instanceof JButton) {
                comp.setBackground(cardBg);
                comp.setForeground(fg);
            } else if (comp instanceof JScrollPane) {
                comp.setBackground(bg);
            } else if (comp instanceof JLabel) {
                comp.setForeground(fg);
            } else if (comp instanceof JPanel) {
                comp.setBackground(bg);
                comp.setForeground(fg);
            }
            if (comp instanceof Container) {
                themeContainer((Container) comp, bg, cardBg, fg);
            }
        }
    }

    static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        String[] units = {"KB", "MB", "GB", "TB"};
        double value = bytes;
        int idx = -1;
        while (value >= 1024 && idx < units.length - 1) {
            value /= 1024;
            idx++;
        }
        return String.format("%.2f %s", value, units[Math.max(idx, 0)]);
    }

    static String getExtension(File f) {
        String name = f.getName();
        int dot = name.lastIndexOf('.');
        if (dot == -1 || dot == name.length() - 1) return "";
        return name.substring(dot + 1);
    }

    static boolean isTextExtension(String ext) {
        return TEXT_EXTENSIONS.contains(ext.toLowerCase());
    }

    static boolean isImageExtension(String ext) {
        return IMAGE_EXTENSIONS.contains(ext.toLowerCase());
    }

    static String readPreviewText(File f, int maxLines) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null && count < maxLines) {
                sb.append(line).append("\n");
                count++;
            }
            if (count == maxLines) sb.append("\n... (preview truncated at ").append(maxLines).append(" lines)");
        }
        return sb.toString();
    }

    static String computeHash(File file, String algorithm) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[65536];
            int n;
            while ((n = is.read(buffer)) != -1) digest.update(buffer, 0, n);
        }
        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

 
    interface ProgressListener {
        void onProgress(int percent);
    }

    
    static class HistoryEntry {
        String dateTime;
        String originalFile;
        String compressedFile;
        String algorithm;
        long originalSize;
        long compressedSize;
        long durationMillis;
        String originalHash;

        double ratio() {
            return originalSize == 0 ? 0 : (compressedSize * 100.0 / originalSize);
        }
    }

    
    static class FileDropHandler extends TransferHandler {
        private final Consumer<File> callback;

        FileDropHandler(Consumer<File> callback) {
            this.callback = callback;
        }

        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean importData(TransferSupport support) {
            try {
                List<File> files = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                if (!files.isEmpty()) {
                    callback.accept(files.get(0));
                    return true;
                }
            } catch (Exception ex) {
                // Ignore malformed drop data.
            }
            return false;
        }
    }


    /** Rounded, hover-highlighted dashboard navigation card. */
    static class CardButton extends JButton {
        private final Color baseColor;
        private Color currentColor;

        CardButton(String title, String subtitle, Color base) {
            super("<html><div style='text-align:center;width:160px;'>"
                    + "<span style='font-size:14px;font-weight:bold;'>" + title + "</span><br>"
                    + "<span style='font-size:11px;'>" + subtitle + "</span></div></html>");
            this.baseColor = base;
            this.currentColor = base;
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setPreferredSize(new Dimension(220, 120));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    currentColor = baseColor.brighter();
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    currentColor = baseColor;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(currentColor);
            g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 22, 22);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Header panel with a horizontal gradient background. */
    static class GradientPanel extends JPanel {
        private final Color c1, c2;

        GradientPanel(Color c1, Color c2) {
            this.c1 = c1;
            this.c2 = c2;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), 0, c2);
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    /** Simple before/after bar chart, drawn with Graphics2D (no external chart library). */
    static class BarChartPanel extends JPanel {
        private long originalSize = 0, compressedSize = 0;

        BarChartPanel() {
            setPreferredSize(new Dimension(280, 200));
            setOpaque(false);
        }

        void setData(long original, long compressed) {
            this.originalSize = original;
            this.compressedSize = compressed;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int padding = 35;
            int maxBarHeight = Math.max(h - padding * 2, 10);
            long max = Math.max(originalSize, Math.max(compressedSize, 1));
            int barWidth = 60;
            int gap = 50;
            int x1 = w / 2 - gap / 2 - barWidth;
            int x2 = w / 2 + gap / 2;
            int origH = (int) (maxBarHeight * (originalSize / (double) max));
            int compH = (int) (maxBarHeight * (compressedSize / (double) max));
            g2.setColor(new Color(66, 133, 244));
            g2.fillRoundRect(x1, h - padding - origH, barWidth, Math.max(origH, 2), 8, 8);
            g2.setColor(new Color(52, 168, 83));
            g2.fillRoundRect(x2, h - padding - compH, barWidth, Math.max(compH, 2), 8, 8);
            Color textColor = getForeground() != null ? getForeground() : Color.BLACK;
            g2.setColor(textColor);
            g2.drawString("Original", x1 - 5, h - padding + 18);
            g2.drawString(formatSize(originalSize), x1 - 5, h - padding - origH - 8);
            g2.drawString("Compressed", x2 - 15, h - padding + 18);
            g2.drawString(formatSize(compressedSize), x2 - 5, h - padding - compH - 8);
        }
    }

    /** Simple pie chart (compressed size vs. space saved), drawn with Graphics2D. */
    static class PieChartPanel extends JPanel {
        private long compressed = 0, saved = 0;

        PieChartPanel() {
            setPreferredSize(new Dimension(280, 200));
            setOpaque(false);
        }

        void setData(long compressed, long saved) {
            this.compressed = compressed;
            this.saved = saved;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(getWidth(), getHeight()) - 50;
            if (size < 20) size = 20;
            int x = (getWidth() - size) / 2;
            int y = 15;
            long total = compressed + saved;
            if (total <= 0) total = 1;
            double compAngle = 360.0 * compressed / total;
            g2.setColor(new Color(52, 168, 83));
            g2.fillArc(x, y, size, size, 90, -(int) Math.round(compAngle));
            g2.setColor(new Color(234, 67, 53));
            g2.fillArc(x, y, size, size, 90 - (int) Math.round(compAngle), -(int) Math.round(360 - compAngle));
            Color textColor = getForeground() != null ? getForeground() : Color.BLACK;
            g2.setColor(textColor);
            g2.drawString("Green = Compressed Size", x, y + size + 18);
            g2.drawString("Red = Space Saved", x, y + size + 34);
        }
    }

    
    static class CompressionService {

        static class CompressionResult {
            File outputFile;
            long originalSize;
            long compressedSize;
            long timeMillis;
            String algorithm;
        }

        static CompressionResult compress(File input, File outDir, String outputName, String algorithm,
                                           int level, ProgressListener listener) throws IOException {
            long start = System.currentTimeMillis();
            long originalSize = input.length();
            String ext;
            switch (algorithm) {
                case "ZIP": ext = ".zip"; break;
                case "GZIP": ext = ".gz"; break;
                case "HUFFMAN": ext = ".huff"; break;
                case "LZW": ext = ".lzw"; break;
                default: ext = ".compressed";
            }
            File output = new File(outDir, outputName + ext);
            switch (algorithm) {
                case "ZIP": compressZip(input, output, level, listener); break;
                case "GZIP": compressGzip(input, output, level, listener); break;
                case "HUFFMAN": HuffmanCodec.compress(input, output, listener); break;
                case "LZW": LZWCodec.compress(input, output, listener); break;
                default: throw new IOException("Unknown algorithm: " + algorithm);
            }
            CompressionResult r = new CompressionResult();
            r.outputFile = output;
            r.originalSize = originalSize;
            r.compressedSize = output.length();
            r.timeMillis = System.currentTimeMillis() - start;
            r.algorithm = algorithm;
            return r;
        }

        private static void compressZip(File input, File output, int level, ProgressListener listener) throws IOException {
            try (FileOutputStream fos = new FileOutputStream(output);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {
                zos.setLevel(level);
                zos.putNextEntry(new ZipEntry(input.getName()));
                copyWithProgress(input, zos, listener);
                zos.closeEntry();
            }
        }

        private static void compressGzip(File input, File output, int level, ProgressListener listener) throws IOException {
            try (FileOutputStream fos = new FileOutputStream(output);
                 LeveledGZIPOutputStream gzos = new LeveledGZIPOutputStream(fos, level)) {
                copyWithProgress(input, gzos, listener);
            }
        }

        private static void copyWithProgress(File input, OutputStream out, ProgressListener listener) throws IOException {
            long total = input.length();
            long copied = 0;
            byte[] buffer = new byte[65536];
            try (InputStream is = new BufferedInputStream(new FileInputStream(input))) {
                int n;
                while ((n = is.read(buffer)) != -1) {
                    out.write(buffer, 0, n);
                    copied += n;
                    if (listener != null && total > 0) listener.onProgress((int) Math.min(100, copied * 100 / total));
                }
            }
        }

        static File decompress(File input, File outDir, ProgressListener listener) throws IOException {
            String ext = getExtension(input).toLowerCase();
            switch (ext) {
                case "zip": return decompressZip(input, outDir, listener);
                case "gz": return decompressGzip(input, outDir, listener);
                case "huff": return HuffmanCodec.decompress(input, outDir, listener);
                case "lzw": return LZWCodec.decompress(input, outDir, listener);
                default: throw new IOException("Unsupported file extension: ." + ext);
            }
        }

        private static File decompressZip(File input, File outDir, ProgressListener listener) throws IOException {
            try (FileInputStream fis = new FileInputStream(input);
                 ZipInputStream zis = new ZipInputStream(fis)) {
                ZipEntry entry = zis.getNextEntry();
                if (entry == null) throw new IOException("ZIP file is empty or corrupted.");
                File output = new File(outDir, new File(entry.getName()).getName());
                try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(output))) {
                    byte[] buffer = new byte[65536];
                    int n;
                    long copied = 0;
                    long total = entry.getSize();
                    while ((n = zis.read(buffer)) != -1) {
                        bos.write(buffer, 0, n);
                        copied += n;
                        if (listener != null && total > 0) listener.onProgress((int) Math.min(100, copied * 100 / total));
                    }
                }
                return output;
            }
        }

        private static File decompressGzip(File input, File outDir, ProgressListener listener) throws IOException {
            String name = input.getName();
            String outName = name.toLowerCase().endsWith(".gz") ? name.substring(0, name.length() - 3) : ("decompressed_" + name);
            File output = new File(outDir, outName);
            long total = input.length();
            try (FileInputStream fis = new FileInputStream(input);
                 GZIPInputStream gzis = new GZIPInputStream(fis);
                 BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(output))) {
                long processed = 0;
                byte[] buffer = new byte[65536];
                int n;
                while ((n = gzis.read(buffer)) != -1) {
                    bos.write(buffer, 0, n);
                    processed += n;
                    if (listener != null && total > 0) listener.onProgress((int) Math.min(99, processed * 100 / total));
                }
            }
            if (listener != null) listener.onProgress(100);
            return output;
        }

        /** GZIPOutputStream subclass that exposes the Deflater compression level setting. */
        static class LeveledGZIPOutputStream extends GZIPOutputStream {
            LeveledGZIPOutputStream(OutputStream out, int level) throws IOException {
                super(out);
                def.setLevel(level);
            }
        }
    }

    static class HuffmanCodec {

        static void compress(File input, File output, ProgressListener listener) throws IOException {
            long totalLen = input.length();
            long[] freq = new long[256];
            if (totalLen > 0) {
                try (InputStream is = new BufferedInputStream(new FileInputStream(input))) {
                    int b;
                    long count = 0;
                    while ((b = is.read()) != -1) {
                        freq[b]++;
                        count++;
                        if (listener != null) listener.onProgress((int) (count * 40 / totalLen));
                    }
                }
            }

            int distinct = 0;
            for (long f : freq) if (f > 0) distinct++;

            PriorityQueue<HuffmanNode> pq = new PriorityQueue<>();
            for (int i = 0; i < 256; i++) if (freq[i] > 0) pq.add(new HuffmanNode((byte) i, freq[i]));

            HuffmanNode root = null;
            if (pq.size() == 1) {
                HuffmanNode only = pq.poll();
                root = new HuffmanNode(only.freq, only, null);
            } else if (!pq.isEmpty()) {
                while (pq.size() > 1) {
                    HuffmanNode a = pq.poll();
                    HuffmanNode b = pq.poll();
                    pq.add(new HuffmanNode(a.freq + b.freq, a, b));
                }
                root = pq.poll();
            }

            Map<Byte, String> codes = new HashMap<>();
            if (root != null) buildCodes(root, "", codes);

            try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(output)))) {
                dos.writeUTF(input.getName());
                dos.writeLong(totalLen);
                dos.writeInt(distinct);
                for (int i = 0; i < 256; i++) {
                    if (freq[i] > 0) {
                        dos.writeByte(i);
                        dos.writeLong(freq[i]);
                    }
                }
                try (BitOutputStream bos = new BitOutputStream(dos)) {
                    if (totalLen > 0) {
                        try (InputStream is = new BufferedInputStream(new FileInputStream(input))) {
                            int b;
                            long count = 0;
                            while ((b = is.read()) != -1) {
                                String code = codes.get((byte) b);
                                for (int i = 0; i < code.length(); i++) bos.writeBit(code.charAt(i) == '1' ? 1 : 0);
                                count++;
                                if (listener != null) listener.onProgress(40 + (int) (count * 60 / totalLen));
                            }
                        }
                    }
                }
            }
            if (listener != null) listener.onProgress(100);
        }

        private static void buildCodes(HuffmanNode node, String prefix, Map<Byte, String> codes) {
            if (node.isLeaf()) {
                codes.put(node.value, prefix.isEmpty() ? "0" : prefix);
                return;
            }
            if (node.left != null) buildCodes(node.left, prefix + "0", codes);
            if (node.right != null) buildCodes(node.right, prefix + "1", codes);
        }

        static File decompress(File input, File outDir, ProgressListener listener) throws IOException {
            try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(input)))) {
                String originalName = dis.readUTF();
                long originalLen = dis.readLong();
                int distinct = dis.readInt();
                long[] freq = new long[256];
                for (int i = 0; i < distinct; i++) {
                    int val = dis.readByte() & 0xFF;
                    long f = dis.readLong();
                    freq[val] = f;
                }

                PriorityQueue<HuffmanNode> pq = new PriorityQueue<>();
                for (int i = 0; i < 256; i++) if (freq[i] > 0) pq.add(new HuffmanNode((byte) i, freq[i]));

                HuffmanNode root = null;
                if (pq.size() == 1) {
                    HuffmanNode only = pq.poll();
                    root = new HuffmanNode(only.freq, only, null);
                } else if (!pq.isEmpty()) {
                    while (pq.size() > 1) {
                        HuffmanNode a = pq.poll();
                        HuffmanNode b = pq.poll();
                        pq.add(new HuffmanNode(a.freq + b.freq, a, b));
                    }
                    root = pq.poll();
                }

                File output = new File(outDir, originalName);
                try (BitInputStream bis = new BitInputStream(dis);
                     BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(output))) {
                    long produced = 0;
                    while (produced < originalLen && root != null) {
                        HuffmanNode node = root;
                        while (!node.isLeaf()) {
                            int bit = bis.readBit();
                            if (bit == -1) {
                                node = null;
                                break;
                            }
                            HuffmanNode next = (bit == 0) ? node.left : node.right;
                            if (next == null) {
                                node = null;
                                break;
                            }
                            node = next;
                        }
                        if (node == null) break;
                        bos.write(node.value & 0xFF);
                        produced++;
                        if (listener != null && originalLen > 0) listener.onProgress((int) (produced * 100 / originalLen));
                    }
                }
                if (listener != null) listener.onProgress(100);
                return output;
            }
        }
    }

    /** Node used to build the Huffman prefix-code tree. */
    static class HuffmanNode implements Comparable<HuffmanNode> {
        byte value;
        long freq;
        HuffmanNode left, right;

        HuffmanNode(byte value, long freq) {
            this.value = value;
            this.freq = freq;
        }

        HuffmanNode(long freq, HuffmanNode left, HuffmanNode right) {
            this.freq = freq;
            this.left = left;
            this.right = right;
        }

        boolean isLeaf() {
            return left == null && right == null;
        }

        @Override
        public int compareTo(HuffmanNode o) {
            return Long.compare(freq, o.freq);
        }
    }

    static class LZWCodec {

        static void compress(File input, File output, ProgressListener listener) throws IOException {
            long totalLen = input.length();
            try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(output)))) {
                dos.writeUTF(input.getName());
                try (BitOutputStream bos = new BitOutputStream(dos);
                     BufferedInputStream is = new BufferedInputStream(new FileInputStream(input))) {
                    Map<String, Integer> dictionary = new HashMap<>();
                    resetDictionary(dictionary);
                    int nextCode = 256;
                    StringBuilder w = new StringBuilder();
                    int b;
                    long processed = 0;
                    while ((b = is.read()) != -1) {
                        char c = (char) (b & 0xFF);
                        String wc = w.toString() + c;
                        if (dictionary.containsKey(wc)) {
                            w.append(c);
                        } else {
                            bos.writeBits(dictionary.get(w.toString()), 16);
                            if (nextCode < 65536) {
                                dictionary.put(wc, nextCode++);
                            } else {
                                resetDictionary(dictionary);
                                nextCode = 256;
                            }
                            w = new StringBuilder();
                            w.append(c);
                        }
                        processed++;
                        if (listener != null && totalLen > 0) listener.onProgress((int) (processed * 100 / totalLen));
                    }
                    if (w.length() > 0) {
                        bos.writeBits(dictionary.get(w.toString()), 16);
                    }
                }
            }
            if (listener != null) listener.onProgress(100);
        }

        private static void resetDictionary(Map<String, Integer> dictionary) {
            dictionary.clear();
            for (int i = 0; i < 256; i++) dictionary.put(String.valueOf((char) i), i);
        }

        static File decompress(File input, File outDir, ProgressListener listener) throws IOException {
            try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(input)))) {
                String originalName = dis.readUTF();
                File output = new File(outDir, originalName);
                try (BitInputStream bis = new BitInputStream(dis);
                     BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(output))) {
                    Map<Integer, String> dictionary = new HashMap<>();
                    int nextCode = resetReverseDictionary(dictionary);
                    String w = null;
                    while (true) {
                        int code = bis.readBits(16);
                        if (code == -1) break;
                        String entry;
                        if (dictionary.containsKey(code)) {
                            entry = dictionary.get(code);
                        } else if (w != null) {
                            entry = w + w.charAt(0);
                        } else {
                            break;
                        }
                        for (int i = 0; i < entry.length(); i++) bos.write(entry.charAt(i) & 0xFF);
                        if (w != null) {
                            if (nextCode < 65536) {
                                dictionary.put(nextCode++, w + entry.charAt(0));
                            } else {
                                nextCode = resetReverseDictionary(dictionary);
                            }
                        }
                        w = entry;
                        if (listener != null) listener.onProgress(50);
                    }
                }
                if (listener != null) listener.onProgress(100);
                return output;
            }
        }

        private static int resetReverseDictionary(Map<Integer, String> dictionary) {
            dictionary.clear();
            for (int i = 0; i < 256; i++) dictionary.put(i, String.valueOf((char) i));
            return 256;
        }
    }


    static class BitOutputStream implements Closeable {
        private final OutputStream out;
        private int currentByte = 0;
        private int numBits = 0;

        BitOutputStream(OutputStream out) {
            this.out = out;
        }

        void writeBit(int bit) throws IOException {
            currentByte = (currentByte << 1) | (bit & 1);
            numBits++;
            if (numBits == 8) {
                out.write(currentByte);
                currentByte = 0;
                numBits = 0;
            }
        }

        void writeBits(int value, int count) throws IOException {
            for (int i = count - 1; i >= 0; i--) writeBit((value >> i) & 1);
        }

        void flush() throws IOException {
            while (numBits != 0) writeBit(0);
        }

        @Override
        public void close() throws IOException {
            flush();
        }
    }

    static class BitInputStream implements Closeable {
        private final InputStream in;
        private int currentByte;
        private int numBitsRemaining = 0;

        BitInputStream(InputStream in) {
            this.in = in;
        }

        int readBit() throws IOException {
            if (numBitsRemaining == 0) {
                currentByte = in.read();
                if (currentByte == -1) return -1;
                numBitsRemaining = 8;
            }
            numBitsRemaining--;
            return (currentByte >> numBitsRemaining) & 1;
        }

        int readBits(int count) throws IOException {
            int result = 0;
            for (int i = 0; i < count; i++) {
                int bit = readBit();
                if (bit == -1) return -1;
                result = (result << 1) | bit;
            }
            return result;
        }

        @Override
        public void close() throws IOException {
            // The underlying stream is closed by the owning try-with-resources block.
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fall back to the default look and feel.
            }
            AdvancedFileCompressor app = new AdvancedFileCompressor();
            app.setVisible(true);
        });
    }
}