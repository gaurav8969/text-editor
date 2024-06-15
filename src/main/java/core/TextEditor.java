package core;

import com.formdev.flatlaf.FlatDarculaLaf;

import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.List;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class TextEditor{
    private JFrame frame;
    private JMenuBar menuBar;
    private JMenu menu;
    private JTabbedPane tabbedPane;
    private int tabCount=0;
    private JFileChooser fileChooser;
    private JFileChooser directoryChooser;
    private JTree directoryTree;
    private DefaultTreeModel treeModel;
    private File currentDirectory;

    public TextEditor(){
        FlatDarculaLaf.setup();

        frame = new JFrame();
        frame.setTitle("Text Editor");
        frame.setSize(800,600);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

        tabbedPane = new JTabbedPane();

        currentDirectory = new File(System.getProperty("user.dir"));
        DefaultMutableTreeNode root = createTreeNodes(currentDirectory);
        treeModel = new DefaultTreeModel(root);
        directoryTree = new JTree(treeModel);
        directoryTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        directoryTree.setRootVisible(true);

        directoryTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                if(me.getClickCount() == 2){
                    TreePath tp = directoryTree.getPathForLocation(me.getX(),me.getY());
                    if(tp != null){
                        File selectedFile = new File(currentDirectory, tp.getPathComponent(1).toString());
                        for(int i = 2; i < tp.getPathCount(); i++){
                            selectedFile = new File(selectedFile, tp.getPathComponent(i).toString());
                        }
                        openFile(selectedFile);
                    }
                }
            }
        });

        // Set layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(directoryTree), tabbedPane);
        splitPane.setDividerLocation(200);

        frame.add(splitPane);

        menuBar = new JMenuBar();

        menu = new JMenu("File");

        JMenuItem i1,i2,i3,i4,i5;
        i1 = new JMenuItem("New");
        i2 = new JMenuItem("Open");
        i3 = new JMenuItem("Open Folder");
        i4 = new JMenuItem("Save");
        i5 = new JMenuItem("Close");

        Action action = (event) -> menuItemAction(event);
        i1.addActionListener(new EventAction(action));
        i2.addActionListener(new EventAction(action));
        i3.addActionListener(new EventAction(action));
        i4.addActionListener(new EventAction(action));
        i5.addActionListener(new EventAction(action));

        menu.add(i1);
        menu.add(i2);
        menu.add(i3);
        menu.add(i4);
        menu.add(i5);

        fileChooser = new JFileChooser();
        directoryChooser = new JFileChooser();
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        menuBar.add(menu);

        frame.setJMenuBar(menuBar);

        frame.setVisible(true);
    }

    private DefaultMutableTreeNode createTreeNodes(File dir){
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(dir.getName());
        File[] files = dir.listFiles();
        if(files != null){
            for(File file:files){
                if(file.isDirectory()){
                    node.add(createTreeNodes(file));
                }else{
                    node.add(new DefaultMutableTreeNode(file.getName()));
                }
            }
        }
        return node;
    }

    //allows different kind of action listeners to be registered as long as you define a function for them
    private interface Action{
        void actionPerformed(ActionEvent e);
    }

    private static class EventAction extends AbstractAction{
        Action action;
        public EventAction(Action action){
            this.action = action;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            action.actionPerformed(e);
        }
    }

    private void menuItemAction(ActionEvent e){
        String command = e.getActionCommand();

        switch(command){
            case "New":
                openNewFile();
                break;
            case "Open":
                selectFile();
                break;
            case "Open Folder":
                selectFolder();
                break;
            case "Save":
                saveActiveFile();
                break;
            case "Close":
                System.exit(0);
                break;
        }
    }

    //creates new text pane and a tab for it, returns the pane
    private JTextPane createFileTab(){
        JTextPane textPane = new JTextPane();
        JScrollPane scrollPane = new JScrollPane(textPane);

        //panel for tab title and close button
        JPanel tabComponent = getjPanel();

        tabbedPane.addTab(null, scrollPane);
        tabbedPane.setTabComponentAt(tabCount++, tabComponent);
        return textPane;
    }

    private StyledDocument addDocListener(JTextPane textPane){
        StyledDocument doc = new DefaultStyledDocument(createStyles());
        doc.setCharacterAttributes(0, doc.getLength(), doc.getStyle("Default"), true);
        String text = textPane.getText();
        textPane.setStyledDocument(doc);
        textPane.setText(text);

        doc.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applySyntaxHighlighting(doc);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applySyntaxHighlighting(doc);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Not needed for plain text documents
            }
        });
        return doc;
    }

    private JPanel getjPanel() {
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JButton button = (JButton) e.getSource();
                for(int i = 0; i < tabbedPane.getTabCount(); i++) {
                    if(SwingUtilities.isDescendingFrom(button, tabbedPane.getTabComponentAt(i))) {
                        tabbedPane.remove(i);
                        tabCount--;
                        break;
                    }
                }
            }
        };

        JLabel label = new JLabel("Untitled " + tabCount, JLabel.RIGHT);
        JButton closeButton = new JButton("X");
        closeButton.addActionListener(actionListener);
        JPanel tabComponent = new JPanel(new BorderLayout());
        tabComponent.add(label, BorderLayout.WEST);
        tabComponent.add(closeButton, BorderLayout.EAST);
        Dimension labelSize = label.getPreferredSize();
        Dimension buttonSize = closeButton.getPreferredSize();
        tabComponent.setPreferredSize(new Dimension(labelSize.width + buttonSize.width + 10
                ,Math.max(buttonSize.height,labelSize.height)));
        return tabComponent;
    }

    private void openNewFile(){
        JTextPane textPane = createFileTab();
        addDocListener(textPane);
    }

    private void saveActiveFile(){
        int index = tabbedPane.getSelectedIndex();
        if(index != -1){
            JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(index);
            JTextPane textPane = (JTextPane) scrollPane.getViewport().getView();
            int returnValue = fileChooser.showSaveDialog(frame);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    textPane.write(writer);
                    JPanel panel = (JPanel) tabbedPane.getTabComponentAt(index);
                    Component[] children = panel.getComponents();
                    for(Component c: children){
                        if(c instanceof JLabel){
                            ((JLabel) c).setText(file.getName());
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void selectFile(){
        int returnValue = fileChooser.showOpenDialog(frame);
        if(returnValue == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            openFile(file);
        }
    }

    private void openFile(File file){
        try{
            //take an elementary reader and buffer it for speed
            BufferedReader reader = new BufferedReader(new FileReader(file));
            JTextPane textPane = createFileTab();
            textPane.read(reader,null);
            StyledDocument doc = addDocListener(textPane);
            applySyntaxHighlighting(doc);

            JPanel panel = (JPanel) tabbedPane.getTabComponentAt(tabCount-1); //since indices are 0-indexed

            JButton btn=null;
            JLabel label=null;

            Component[] children = panel.getComponents();
            for(Component c: children){
                if(c instanceof JLabel){
                    label = (JLabel)c;
                    label.setText(file.getName());
                }

                if(c instanceof JButton){
                    btn = (JButton)c;
                }
            }

            Dimension labelSize = label.getPreferredSize();
            Dimension buttonSize = btn.getPreferredSize();
            panel.setPreferredSize(new Dimension(labelSize.width + buttonSize.width+10,
                    Math.max(buttonSize.height,labelSize.height)));


        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    private void selectFolder(){
        int returnValue = directoryChooser.showOpenDialog(frame);
        if(returnValue == JFileChooser.APPROVE_OPTION){
            File folder = directoryChooser.getSelectedFile();
            openFolder(folder);
        }
    }

    private void openFolder(File folder){
        currentDirectory = folder;
        DefaultMutableTreeNode root = createTreeNodes(currentDirectory);
        treeModel.setRoot(root);
        treeModel.reload();
    }

    private StyleContext createStyles() {
        StyleContext styleContext = new StyleContext();

        Style defaultStyle = styleContext.addStyle("Default", null);
        StyleConstants.setForeground(defaultStyle, new Color(0xFF70E2));

        Style keywordStyle = styleContext.addStyle("Keyword", null);
        StyleConstants.setForeground(keywordStyle, new Color(0xF82672));
        StyleConstants.setBold(keywordStyle, true);

        Style numberStyle = styleContext.addStyle("Number", null);
        StyleConstants.setForeground(numberStyle, Color.CYAN);

        Style symbolStyle = styleContext.addStyle("Symbol", null);
        StyleConstants.setForeground(symbolStyle, Color.RED);

        Style identifierStyle = styleContext.addStyle("Identifier", null);
        StyleConstants.setForeground(identifierStyle, new Color(0xFFA500));

        Style operatorStyle = styleContext.addStyle("Operator", null);
        StyleConstants.setForeground(operatorStyle, new Color(0xFFFF00));

        Style functionStyle = styleContext.addStyle("Function", null);
        StyleConstants.setForeground(functionStyle, new Color(0xA8D946));

        return styleContext;
    }

    private void applySyntaxHighlighting(StyledDocument doc) {
        SwingUtilities.invokeLater(() -> {
            String text = null;
            try {
                text = doc.getText(0, doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }

            if (text == null) return;

            Lexer lexer = new Lexer();
            List<Token> tokens = lexer.tokenize(text);

            for (Token token : tokens) {
                Style style = doc.getStyle("Default");

                if (token.getType() == TokenType.FUNCTION) {
                    style = doc.getStyle("Function");
                } else if (token.getType() == TokenType.KEYWORD) {
                    style = doc.getStyle("Keyword");
                } else if (token.getType() == TokenType.NUMBER) {
                    style = doc.getStyle("Number");
                } else if(token.getType() == TokenType.OPERATOR){
                    style = doc.getStyle("Operator");
                } else if (token.getType() == TokenType.SYMBOL) {
                    style = doc.getStyle("Symbol");
                } else if (token.getType() == TokenType.IDENTIFIER) {
                    style = doc.getStyle("Identifier");
                } else if(token.getType() == TokenType.ANNOTATION){
                    style = doc.getStyle("Keyword");
                }

                int length = token.getValue().length();
                doc.setCharacterAttributes(token.getStartingIndex(), length, style, true);
            }
        });
    }
}