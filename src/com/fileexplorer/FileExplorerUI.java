package com.fileexplorer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Stack;

public class FileExplorerUI extends JFrame {
    private JPanel homePanel;
    private JTree fileTree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private File currentDirectory;
    private Stack<File> historyStack;

    public FileExplorerUI(String rootPath) {
        setTitle("File Explorer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        historyStack = new Stack<>();
        currentDirectory = new File(rootPath);
        rootNode = new DefaultMutableTreeNode(currentDirectory.getName());
        treeModel = new DefaultTreeModel(rootNode);
        fileTree = new JTree(treeModel);

        // Set custom renderer
        fileTree.setCellRenderer(new CustomTreeCellRenderer());

        // Add mouse listener for double-click events
        fileTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Double click detected
                    int row = fileTree.getClosestRowForLocation(e.getX(), e.getY());
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileTree.getPathForRow(row).getLastPathComponent();
                    openNode(node);
                }
            }
        });

        // Initialize home panel with buttons
        homePanel = new JPanel();
        homePanel.setLayout(new GridLayout(6, 1, 10, 10)); // Increased grid rows for "Previous" button
        homePanel.setBackground(new Color(245, 245, 245)); // Light gray background

        addButton(homePanel, "Previous", e -> navigateBack());
        addButton(homePanel, "Create File", e -> createFileDialog());
        addButton(homePanel, "Create Folder", e -> createFolderDialog());
        addButton(homePanel, "Delete Selected Item", e -> deleteSelectedItem());
        addButton(homePanel, "Refresh", e -> refreshTree());

        add(homePanel, BorderLayout.NORTH);

        // Initialize file tree
        JScrollPane treeScrollPane = new JScrollPane(fileTree);
        add(treeScrollPane, BorderLayout.CENTER);

        // Refresh the tree on startup
        refreshTree();

        setVisible(true);
    }

    private void addButton(JPanel panel, String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setBackground(new Color(0, 122, 204)); // Professional blue background
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.addActionListener(listener);
        panel.add(button);
    }

    private void createFileDialog() {
        String filename = JOptionPane.showInputDialog("Enter the file name:");
        if (filename != null && !filename.trim().isEmpty()) {
            File file = new File(currentDirectory, filename);
            createFile(file);
            refreshTree();
        } else {
            JOptionPane.showMessageDialog(this, "File name cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createFolderDialog() {
        String foldername = JOptionPane.showInputDialog("Enter the folder name:");
        if (foldername != null && !foldername.trim().isEmpty()) {
            File folder = new File(currentDirectory, foldername);
            createFolder(folder);
            refreshTree();
        } else {
            JOptionPane.showMessageDialog(this, "Folder name cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedItem() {
        // Get the selected node
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
        if (selectedNode != null) {
            File file = new File(currentDirectory, selectedNode.getUserObject().toString());
            if (file.isDirectory()) {
                deleteFolder(file);
            } else {
                deleteFile(file);
            }
            refreshTree();
        } else {
            JOptionPane.showMessageDialog(this, "No item selected", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTree() {
        DefaultMutableTreeNode newRoot = new DefaultMutableTreeNode(currentDirectory.getName());
        addNodes(newRoot, currentDirectory);
        treeModel.setRoot(newRoot);
    }

    private void addNodes(DefaultMutableTreeNode node, File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(f.getName());
                node.add(childNode);
                if (f.isDirectory()) {
                    addNodes(childNode, f);
                }
            }
        }
    }

    private void createFile(File file) {
        try {
            if (file.createNewFile()) {
                JOptionPane.showMessageDialog(this, "File created: " + file.getAbsolutePath());
            } else {
                JOptionPane.showMessageDialog(this, "File already exists: " + file.getAbsolutePath(), "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error creating file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createFolder(File folder) {
        if (folder.mkdir()) {
            JOptionPane.showMessageDialog(this, "Folder created: " + folder.getAbsolutePath());
        } else {
            JOptionPane.showMessageDialog(this, "Folder already exists or could not be created: " + folder.getAbsolutePath(), "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteFile(File file) {
        if (file.delete()) {
            JOptionPane.showMessageDialog(this, "File deleted: " + file.getAbsolutePath());
        } else {
            JOptionPane.showMessageDialog(this, "Failed to delete file: " + file.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteFolder(File folder) {
        if (deleteRecursive(folder)) {
            JOptionPane.showMessageDialog(this, "Folder deleted: " + folder.getAbsolutePath());
        } else {
            JOptionPane.showMessageDialog(this, "Failed to delete folder: " + folder.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (!deleteRecursive(f)) {
                        return false;
                    }
                }
            }
        }
        return file.delete();
    }

    // Open file or folder on double-click
    private void openNode(DefaultMutableTreeNode node) {
        File file = new File(currentDirectory, node.getUserObject().toString());

        if (file.isDirectory()) {
            historyStack.push(currentDirectory); // Save current directory to history
            currentDirectory = file;
            refreshTree();
        } else {
            // Open the file (can be customized to open in an editor or viewer)
            JOptionPane.showMessageDialog(this, "File opened: " + file.getAbsolutePath(), "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Navigate back to the previous directory
    private void navigateBack() {
        if (!historyStack.isEmpty()) {
            currentDirectory = historyStack.pop();
            refreshTree();
        } else {
            JOptionPane.showMessageDialog(this, "No previous directory", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Custom Tree Cell Renderer
    private class CustomTreeCellRenderer extends DefaultTreeCellRenderer {
        private final Color folderColor = new Color(0, 122, 204); // Professional blue for folders
        private final Color fileColor = new Color(255, 87, 34); // Orange for files
        private final Color selectedColor = new Color(220, 230, 250); // Light blue for selected items

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            File file = new File(currentDirectory, node.getUserObject().toString());

            if (selected) {
                c.setBackground(selectedColor);
                c.setForeground(Color.BLACK);
            } else if (file.isDirectory()) {
                c.setForeground(folderColor);
            } else {
                c.setForeground(fileColor);
            }

            return c;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FileExplorerUI("C:\\Users\\vikas\\Downloads")); // Change to an appropriate absolute path
    }
}
