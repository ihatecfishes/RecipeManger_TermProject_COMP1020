import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.filechooser.FileNameExtensionFilter;


public class MainForm {
    private JPanel panelMain;
    private JList listRecipes;
    private JTabbedPane tabbedPane1;
    private JTextArea textBody;
    private JButton newButton;
    private JButton openButton;
    private JButton saveButton;
    private JButton saveAsButton;
    private JTextPane textNotes;
    private JTextField textTitle;
    private JButton buttonAdd;
    private JButton removeButton;
    private JTextField textField2;
    private JButton searchButton;
    private JButton buttonUpdate;
    private JButton addFolderButton; // New "Add Folder" button
    private JSpinner spinnerServings;
    private JTabbedPane tabbedPane3;
    private JTable tableIngredients;
    private JTable tableNutrition;
    private JEditorPane editorPane2;
    private JTextArea textArea1;
    private JTree recipeTree;
    //private JButton imagesButton;
    private JTree treeRecipes;
    private JButton buttonTAdd;
    private JButton buttonTRemove;
    private JButton buttonTEdit;
    private JComboBox comboMeasurement;

    private Tree<Recipe> recipes = new Tree<>();
    private boolean change = false;
    private boolean enableComboMeasurement = true;
    // TODO: ingredients temporary
    private ArrayList<Unit> ingredients = new ArrayList<>();

    public MainForm() {
        updateTree();
        recipes.addNode("Recipes",null,"");

        updateIngredients();
        updateNutrition();
        updateSpinner();


        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) treeRecipes.getLastSelectedPathComponent();

                String path = getPathFromTree(selectedNode);

                StringBuilder rePath = new StringBuilder();

                // append a string into StringBuilder input1
                rePath.append(path);

                // reverse StringBuilder input1
                rePath.reverse();

                String res = rePath.toString();

                int delimiterIndex = res.indexOf('/');
                if (delimiterIndex != -1) {
                    res = res.substring(delimiterIndex + 1);

                    rePath.delete(0, rePath.length());
                    rePath.append(res);
                    rePath.reverse();
                    res = rePath.toString();
                }
                else res = "";

                System.out.println(recipes.findNode(path).getName() + " " + res);

                recipes.removeNode(recipes.findNode(path).getName(), res);

                updateTree();
                clearFields();
                recipes.display();
            }
        });

        buttonAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (textField2.getText().isEmpty()) {
                    // Adding a recipe
                    // Find unique name
                    int number = 1;
                    Tree.Node<Recipe> rootNode = recipes.root;
                    if (rootNode != null) {
                        int childCount = rootNode.getChildren().size();
                        number = childCount + 1;
                    }

                    // Add recipe
                    Recipe newRecipe = new Recipe("Untitled Recipe " + number);
                    recipes.addNode(newRecipe.getName(), newRecipe, textField2.getText());
                } else {
                    // Adding a folder
                    String folderName = textField2.getText();
                    recipes.addNode(folderName, null, textField2.getText());
                }

                updateTree();
                recipes.display();
            }
        });

        addFolderButton.addActionListener(new ActionListener() { // ActionListener for "Add Folder" button
            @Override
            public void actionPerformed(ActionEvent e) {
                String folderName = JOptionPane.showInputDialog(panelMain, "Enter folder name:");
                if (folderName != null && !folderName.isEmpty()) {
                    recipes.addNode(folderName, null, textField2.getText());
                    updateTree();
                }
            }
        });

        treeRecipes.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) treeRecipes.getLastSelectedPathComponent();
                if (selectedNode == null)
                    return;

                String path = getPathFromTree(selectedNode);

                textField2.setText(path);

                if(path.equals("")) return ;
                Recipe recipe = recipes.findNode(path).getData();
                System.out.println(recipe.getName());



                updateSelection(recipe);


                if (change) {
                    int dialogResult = JOptionPane.showConfirmDialog(panelMain, "Do you want to save the changes?");
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        // Save changes
                        // TODO: Implement save logic
                    }
                }

                change = false;
                buttonUpdate.setEnabled(textArea1.getText().isEmpty());
                buttonUpdate.setEnabled(textTitle.getText().isEmpty());
                buttonUpdate.setEnabled(textNotes.getText().isEmpty());
                buttonUpdate.setEnabled(textBody.getText().isEmpty());


                buttonUpdate.setEnabled(true);
            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveRecipe();
            }
        });


        saveAsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));

                int result = fileChooser.showSaveDialog(panelMain);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    saveRecipeAs(selectedFile);
                }
            }
        });

        buttonUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) treeRecipes.getLastSelectedPathComponent();

                String path = getPathFromTree(selectedNode);




                String newTitle = textTitle.getText();
                String newBody = textBody.getText();
                String newNotes = textNotes.getText();
                String des = textArea1.getText();

                recipes.findNode(path).data.setName(newTitle);
                recipes.findNode(path).data.setContent(newBody);
                recipes.findNode(path).data.setNotes(newNotes);
                recipes.findNode(path).data.setDescription(des);

                System.out.println(1);
                System.out.println(recipes.findNode(path).data.getContent());

                recipes.findNode(path).setKey(newTitle);

                updateTree();

            }
        });

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchText = textField2.getText();
                if (!searchText.isEmpty()) {
                    DefaultMutableTreeNode foundNode = findNodeByKey(searchText);
                    if (foundNode != null) {
                        treeRecipes.setSelectionPath(new TreePath(foundNode.getPath()));
                    } else {
                        JOptionPane.showMessageDialog(panelMain, "Node not found.", "Search", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });

// Helper method to find a node by key in the recipe tree



        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));

                int result = fileChooser.showOpenDialog(panelMain);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    openRecipe(selectedFile);
                }
            }
        });

        newButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });

        /*
        imagesButton.addActionListener(new ActionListener() { // ActionListener for "Images" button
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("JPEG Images", "jpg", "jpeg"));

                int option = fileChooser.showOpenDialog(panelMain);
                if (option == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    displayImage(selectedFile);
                }
            }
        });
         */

        // Add ingredient/nutrition
        buttonTAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IngredientDialog ingredientDialog = new IngredientDialog();
                if (ingredientDialog.displayAdd(panelMain) == 0) {
                    Unit unit = new Unit(
                            ingredientDialog.getIngredientName(),
                            ingredientDialog.getIngredientUnit(),
                            ingredientDialog.getIngredientAmount()
                    );

                    ingredients.add(unit);

                    updateIngredients();
                }
            }
        });

        buttonTRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = tableIngredients.getSelectedRow();
                if (selectedRow == -1) {
                    return;
                }
                ingredients.remove(selectedRow);
                updateIngredients();
            }
        });

        buttonTEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = tableIngredients.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(panelMain,
                            "No ingredient is selected.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                IngredientDialog ingredientDialog = new IngredientDialog();
                if (ingredientDialog.displayEdit(panelMain, ingredients.get(selectedRow)) == 0) {
                    Unit unit = new Unit(
                            ingredientDialog.getIngredientName(),
                            ingredientDialog.getIngredientUnit(),
                            ingredientDialog.getIngredientAmount()
                    );

                    ingredients.set(selectedRow, unit);
                    updateIngredients();
                }
            }
        });

        spinnerServings.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int selectedRow = tableIngredients.getSelectedRow();
                updateIngredients();
                tableIngredients.setRowSelectionInterval(selectedRow, selectedRow);
            }
        });

        tableIngredients.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                enableComboMeasurement = false;

                comboMeasurement.removeAllItems();
                comboMeasurement.setEnabled(false);

                int selectedRow = tableIngredients.getSelectedRow();
                if (selectedRow == -1) {
                    return;
                }

                comboMeasurement.setEnabled(true);

                Measurement measurement = ingredients.get(selectedRow).getMeasurement();
                MeasurementType type = measurement.getType();
                switch (type) {
                    case Mass -> {
                        for (Measurement m : Measurements.mass) {
                            comboMeasurement.addItem(m);
                        }
                    }
                    case Volume -> {
                        for (Measurement m : Measurements.volume) {
                            comboMeasurement.addItem(m);
                        }
                    }
                    case Quantity -> {
                        for (Measurement m : Measurements.quantity) {
                            comboMeasurement.addItem(m);
                        }
                    }
                }

                comboMeasurement.setSelectedItem(measurement);

                enableComboMeasurement = true;
            }
        });

        comboMeasurement.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!enableComboMeasurement) {
                    return;
                }
                if (comboMeasurement.getSelectedItem() == null) {
                    return;
                }


                int selectedRow = tableIngredients.getSelectedRow();
                if (selectedRow == -1) {
                    return;
                }

                Measurement measurement = (Measurement) comboMeasurement.getSelectedItem();
                Unit unit = ingredients.get(selectedRow);
                unit.setValue(measurement.convert(unit.getMeasurement().revert(unit.getValue())));
                unit.setMeasurement(measurement);

                updateIngredients();
                tableIngredients.setRowSelectionInterval(selectedRow, selectedRow);
            }
        });
    }
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("Recipe Manager");
        frame.setContentPane(new MainForm().panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }

        /*
        // Set the color theme
        Color blueColor = new Color(84, 84, 180);
        Color purpleColor = new Color(120, 120, 255);

        UIManager.put("TabbedPane.selected", blueColor);
        UIManager.put("TabbedPane.contentAreaColor", purpleColor);
        UIManager.put("List.selectionBackground", blueColor);
        UIManager.put("List.selectionForeground", Color.WHITE);
        UIManager.put("TextArea.selectionBackground", blueColor);
        UIManager.put("TextArea.selectionForeground", Color.WHITE);
        UIManager.put("TextField.selectionBackground", purpleColor);
        UIManager.put("TextField.selectionForeground", Color.WHITE);
        UIManager.put("TextPane.selectionBackground", blueColor);
        UIManager.put("TextPane.selectionForeground", Color.WHITE);
        UIManager.put("EditorPane.selectionBackground", blueColor);
        UIManager.put("EditorPane.selectionForeground", Color.WHITE);
        UIManager.put("Tree.selectionBackground", blueColor);
        UIManager.put("Tree.selectionForeground", Color.WHITE);
         */

    private <T extends JDialog> void displayDialog(T dialog) {
        dialog.pack();
        dialog.setLocationRelativeTo(panelMain);
        dialog.setVisible(true);
    }

    private void updateSpinner() {
        SpinnerNumberModel model = new SpinnerNumberModel((float) 1, (float) 0, null, (float) 1);
        spinnerServings.setModel(model);
        spinnerServings.setValue((float) 1);
    }

    // Helper method to update the tree view
    private void updateTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Recipes");
        addNodes(root, recipes.root);
        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        treeRecipes.setModel(treeModel);
    }

    private void updateIngredients() {
        String[] columns = new String[] {"Ingredient", "Amount", "Unit"};
        TableModel ingredientsModel = new AbstractTableModel() {
            @Override
            public int getRowCount() {
                // TODO: implement after update button is fixed
                return ingredients.size();
            }

            @Override
            public int getColumnCount() {
                return 3;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                // TODO: implement after update button is fixed
                switch (columnIndex) {
                    case 0 -> {
                        return ingredients.get(rowIndex).getName();
                    }
                    case 1 -> {
                        return ingredients.get(rowIndex).getValue() * (Float) spinnerServings.getValue();
                    }
                    case 2 -> {
                        return ingredients.get(rowIndex).getMeasurement();
                    }
                }
                return "";
            }

            @Override
            public String getColumnName(int column) {
                return columns[column];
            }
        };

        tableIngredients.setModel(ingredientsModel);
        tableIngredients.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableIngredients.setRowSelectionAllowed(true);
        tableIngredients.getTableHeader().setReorderingAllowed(false);
        tableIngredients.getColumnModel().getColumn(0).setMinWidth(120);
        tableIngredients.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);


    }

    private void updateNutrition() {
        String[] columns = new String[] {"Nutrition", "Amount", "Unit"};
        TableModel nutritionModel = new AbstractTableModel() {
            @Override
            public int getRowCount() {
                // TODO: implement after update button is fixed
                return 3;
            }

            @Override
            public int getColumnCount() {
                return 3;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                // TODO: implement after update button is fixed
                return null;
            }

            @Override
            public String getColumnName(int column) {
                return columns[column];
            }
        };

        tableNutrition.setModel(nutritionModel);
        tableNutrition.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableNutrition.setRowSelectionAllowed(true);
        tableNutrition.getTableHeader().setReorderingAllowed(false);
        tableNutrition.getColumnModel().getColumn(0).setMinWidth(120);
        tableNutrition.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

    }

    // Helper method to add nodes recursively to the tree view
    private void addNodes(DefaultMutableTreeNode parent, Tree.Node<Recipe> node) {
        if (node == null)
            return;

        for (Tree.Node<Recipe> child : node.getChildren()) {
            if (child.getData() != null) {
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child.getKey()); // Updated line
                parent.add(childNode);
            } else {
                String folderName = child.getName();
                DefaultMutableTreeNode folderNode = new DefaultMutableTreeNode(folderName);
                parent.add(folderNode);
                addNodes(folderNode, child);
            }
        }
    }

    // Helper method to update the selection fields
    private void updateSelection(Recipe recipe) {
        textTitle.setText(recipe.getName());
        textBody.setText(recipe.getContent());
        textNotes.setText(recipe.getNotes());
        textArea1.setText(recipe.getDescription());
    }

    // Helper method to clear the selection fields
    private void clearFields() {
        textTitle.setText("");
        textBody.setText("");
        textNotes.setText("");
    }

    // Helper method to update the change flag and button
    private void updateChanges() {
        change = true;
        buttonUpdate.setEnabled(true);
    }

    // Helper method to get the path from the selected node
    private String getPathFromTree(DefaultMutableTreeNode node) {
        StringBuilder pathBuilder = new StringBuilder();
        Object[] nodes = node.getPath();
        for (int i = 1; i < nodes.length; i++) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) nodes[i];
            Object userObject = currentNode.getUserObject();
            if (userObject instanceof String) {
                String nodeName = (String) userObject;
                pathBuilder.append(nodeName);
                if (i != nodes.length - 1) {
                    pathBuilder.append("/");
                }
            }
        }
        return pathBuilder.toString();
    }

    // Helper method to save the current recipe
    private void saveRecipe() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) treeRecipes.getLastSelectedPathComponent();
        if (selectedNode == null || !(selectedNode.getUserObject() instanceof Recipe))
            return;

        Recipe recipe = (Recipe) selectedNode.getUserObject();
        String filePath = recipe.getName() + ".txt";

        try {
            FileWriter writer = new FileWriter(filePath);
            writer.write(textBody.getText());
            writer.close();
            JOptionPane.showMessageDialog(panelMain, "Recipe saved successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(panelMain, "Failed to save recipe.");
        }
    }

    // Helper method to save the current recipe as a new file
    private void saveRecipeAs(File selectedFile) {
        if (selectedFile == null)
            return;

        String filePath = selectedFile.getAbsolutePath();

        try {
            FileWriter writer = new FileWriter(filePath);
            writer.write(textBody.getText());
            writer.close();
            JOptionPane.showMessageDialog(panelMain, "Recipe saved successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(panelMain, "Failed to save recipe.");
        }
    }

    // Helper method to open a recipe file
    private void openRecipe(File file) {
        try {
            String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
            textBody.setText(content);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(panelMain, "Failed to open recipe file.");
        }
    }

    /*
    // Helper method to display the selected image
    private void displayImage(File imageFile) {
        try {
            String imagePath = imageFile.getAbsolutePath();
            String imageTag = "<img src='file:" + imagePath + "'>";

            Document doc = editorPane2.getDocument();
            doc.insertString(doc.getLength(), imageTag, null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
     */
    private DefaultMutableTreeNode findNodeByKey(String key) {
        Enumeration<TreeNode> nodes = ((DefaultMutableTreeNode) treeRecipes.getModel().getRoot()).depthFirstEnumeration();

        while (((Enumeration<?>) nodes).hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
            String path = getPathFromTree(node);
                if ( recipes.findNode(path).getKey().equalsIgnoreCase(key)) {
                    return node;
                }
            }

        return null;
    }
}
