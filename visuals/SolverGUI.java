package visuals;

import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory;
import utilities.Connected;
import utilities.Graph;
import utilities.ProblemInstance;

import javax.swing.*;
import javax.swing.text.html.ObjectView;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by maxgray on 4/16/16.
 */
public class SolverGUI {
    private JList chooseFrom;
    private JList chosen;
    private JButton chooseButton;
    private JButton removeButton;
    private JPanel panel;
    private JLabel chooseFromLabel;
    private JLabel chosenLabel;
    private JButton selectProblemInstanceButton;
    private JTextField filePathField;
    private JButton loadButton;
    private JRadioButton fourConnectedRadioButton;
    private JRadioButton eightConnectedRadioButton;

    public SolverGUI() {
        fourConnectedRadioButton.setSelected(true);
        chooseButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                exchange(chooseFrom, chosen);
            }
        });
        removeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                exchange(chosen, chooseFrom);
            }
        });
        selectProblemInstanceButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                final JFileChooser fc = new JFileChooser();
                fc.showOpenDialog(panel);
                File selectedFile = fc.getSelectedFile();
                System.out.print(selectedFile.getAbsolutePath());
                filePathField.setText(selectedFile.getPath());
            }
        });
        fourConnectedRadioButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (eightConnectedRadioButton.isSelected())
                    eightConnectedRadioButton.setSelected(false);
                else if (fourConnectedRadioButton.isShowing()) {
                    eightConnectedRadioButton.setSelected(true);
                    fourConnectedRadioButton.setSelected(false);
                }
            }
        });
        eightConnectedRadioButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (fourConnectedRadioButton.isSelected())
                    fourConnectedRadioButton.setSelected(false);
                else if (eightConnectedRadioButton.isShowing()) {
                    fourConnectedRadioButton.setSelected(true);
                    eightConnectedRadioButton.setSelected(false);
                }
            }
        });
        loadButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                String path = filePathField.getText().trim();
                Object[] options = new Object[] {
                        "Load instance",
                        "Cancel"
                };
                if (path.length() != 0) {
                    final JFileChooser fc = new JFileChooser();
                    fc.showOpenDialog(panel);
                    File selectedFile = fc.getSelectedFile();
                    Connected c = fourConnectedRadioButton.isSelected() ? Connected.FOUR : Connected.EIGHT;

                    try {
                        ProblemInstance pi = new ProblemInstance(new Graph(c, selectedFile), new File(path));
                        String dialogText = "Agents: " + pi.getAgents().size() + "\n" +
                                "Map: " + pi.getGraph().getMapTitle();
                        boolean toLoad = JOptionPane.showOptionDialog(panel,
                                dialogText,
                                "Loading...",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                options,
                                null) == 0;
                        System.out.println(toLoad);
                    } catch (FileNotFoundException x) {
                        x.printStackTrace();
                    }

                }
            }
        });

    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("SolverGUI");
        frame.setContentPane(new SolverGUI().panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }


    // read the problem instance

    private void exchange(JList removeFrom, JList addTo) {
        List<Object> previousAddTo = new ArrayList<Object>();
        previousAddTo.addAll(getElementsFromList(addTo));

        List<Object> previousRemoveFrom = new ArrayList<Object>();
        previousRemoveFrom.addAll(getElementsFromList(removeFrom));

        for (Object o : removeFrom.getSelectedValuesList()) {
            if (!previousAddTo.contains(o)) {
                previousAddTo.add(o);
                previousRemoveFrom.remove(o);
            }
        }

        DefaultListModel<Object> addToModel = new DefaultListModel<>();
        updateModel(addToModel, previousAddTo);
        DefaultListModel<Object> removeFromModel = new DefaultListModel<>();
        updateModel(removeFromModel, previousRemoveFrom);

        removeFrom.setModel(removeFromModel);
        addTo.setModel(addToModel);
    }

    private List<Object> getElementsFromList(JList list) {
        List<Object> result = new ArrayList<Object>();
        for (int i = 0; i < list.getModel().getSize(); i++)
            result.add(list.getModel().getElementAt(i));
        return result;
    }

    private void updateModel(DefaultListModel<Object> model, List<Object> elements) {
        for (Object o : elements)
            model.addElement(o);
    }
}
