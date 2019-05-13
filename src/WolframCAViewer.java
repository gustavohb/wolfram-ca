import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.JScrollBar;
import javax.swing.JFrame;
import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Random;

public class WolframCAViewer extends JPanel implements ActionListener {

    private Dimension area;
    private DrawingPane drawingPane;
    private JScrollPane scrollPane;

    JTextField ruleField;
    JTextField stepsField;
    JTextField cellSizeField;
    JRadioButton randomizeButton;
    JButton genButton;

    static final int DEFAULT_WIDTH = 987;
    static final int DEFAULT_HEIGHT = 545;

    public WolframCAViewer() {
        super(new BorderLayout());

        area = new Dimension(0, 0);

        JPanel settingsPanel = new JPanel();
        settingsPanel.setFocusable(true);

        final Integer initialRule = 30;
        final Integer initialSteps = 48;
        final Integer initialCellSize = 10;

        JLabel ruleFieldLabel = new JLabel("Rule (0 - 255): ");
        ruleField = new JTextField(3);
        ruleField.setText(initialRule.toString());

        JLabel stepsFieldLabel = new JLabel("Steps: ");
        stepsField = new JTextField(4);
        stepsField.setText(initialSteps.toString());


        JLabel cellSizeFieldLabel = new JLabel("Cell size: ");
        cellSizeField = new JTextField(2);
        cellSizeField.setText(initialCellSize.toString());


        JRadioButton oneInTheMiddleButton = new JRadioButton("One in the middle");
        oneInTheMiddleButton.setSelected(true);

        randomizeButton = new JRadioButton("Randomize");

        ButtonGroup group = new ButtonGroup();
        group.add(oneInTheMiddleButton);
        group.add(randomizeButton);


        genButton = new JButton("Generate");
        genButton.setActionCommand("generate");
        genButton.addActionListener(this);

        settingsPanel.add(ruleFieldLabel);
        settingsPanel.add(ruleField);
        settingsPanel.add(stepsFieldLabel);
        settingsPanel.add(stepsField);
        settingsPanel.add(cellSizeFieldLabel);
        settingsPanel.add(cellSizeField);
        settingsPanel.add(oneInTheMiddleButton);
        settingsPanel.add(randomizeButton);
        settingsPanel.add(genButton);

        scrollPane = new JScrollPane();

        drawingPane = new DrawingPane(initialRule, initialSteps, initialCellSize);
        scrollPane.setViewportView(drawingPane);

        this.add(settingsPanel, BorderLayout.PAGE_START);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if ("generate".equals(event.getActionCommand())) {
            try {
                int steps = Integer.parseInt(stepsField.getText());
                int cellSize = Integer.parseInt(cellSizeField.getText());
                int rule = Integer.parseInt(ruleField.getText());
                boolean randomize = randomizeButton.isSelected();

                if (steps <= 0) {
                    JOptionPane.showMessageDialog(this, "You should input a valid number of steps", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (cellSize <= 0){
                    JOptionPane.showMessageDialog(this, "You should input a valid number of cell size", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (rule < 0 || rule > 255){
                    JOptionPane.showMessageDialog(this, "The rule number must be between 0 and 255", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                drawingPane.setSteps(steps);
                drawingPane.setRule(rule);
                drawingPane.setCellSize(cellSize);
                drawingPane.setRandomize(randomize);

                int newHeight = steps * cellSize;
                int newWidth = 2 * newHeight;
                area.height = newHeight;
                area.width = newWidth;

                drawingPane.setPreferredSize(area);
                drawingPane.updateCellularAutomaton();
                drawingPane.draw();
                drawingPane.repaint();
                drawingPane.revalidate();

                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        Rectangle bounds = scrollPane.getViewport().getViewRect();

                        JScrollBar horizontal = scrollPane.getHorizontalScrollBar();
                        horizontal.setValue((horizontal.getMaximum() - bounds.width) / 2);
                        JScrollBar vertical = scrollPane.getVerticalScrollBar();
                        vertical.setValue(0);
                    }
                });
            } catch (NumberFormatException exception) {
                JOptionPane.showMessageDialog(this, "You should input a valid number (" + exception + ")", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    public class DrawingPane extends JPanel {
        private byte[] rule;
        private byte[][] cells;
        private int cellSize;
        private int steps;
        private int currentWidth, currentHeight;
        private final Random random = new Random();
        private boolean randomize = false;


        private BufferedImage bufferedImage;
        private Graphics2D graphics2D;

        public DrawingPane(int rule, int steps, int cellSize) {

            this.rule = decimal2rule(rule);
            this.cellSize = cellSize;
            this.steps = steps;

            int newHeight = steps * cellSize;
            int newWidth = 2 * newHeight;
            area.height = newHeight;
            area.width = newWidth;
            setSize(newWidth, newHeight);

            setPreferredSize(area);

            bufferedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
            graphics2D = (Graphics2D) bufferedImage.getGraphics();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            currentWidth = getSize().width;
            currentHeight = getSize().height;

            setBackground(Color.WHITE);

            updateCellularAutomaton();
            draw();
            repaint();
            revalidate();
        }

        public void setSteps(int steps) {
            this.steps = steps;
        }

        public void setCellSize(int cellSize) {
            this.cellSize = cellSize;
        }

        public void setRule(int rule) {
            this.rule = decimal2rule(rule);
        }

        public void setRandomize(boolean randomize) {
            this.randomize = randomize;
        }


        protected void paintComponent(Graphics g) {
            int width = getSize().width;
            int height = getSize().height;

            if (currentWidth != width || currentHeight != height) {
                bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                graphics2D = (Graphics2D) bufferedImage.getGraphics();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (!randomize) {
                    updateCellularAutomaton();
                }
                draw();

                currentWidth = width;
                currentHeight = height;
            }

            g.drawImage(bufferedImage, 0, 0, width, height, null);
        }

        public void draw() {
            drawBackground();
            drawCellularAutomaton();
        }


        public void updateCellularAutomaton() {
            if (randomize) {
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                this.cells = new byte[steps][screenSize.width / cellSize];

                setPreferredSize(new Dimension(screenSize.width, steps * cellSize));
            }
            else {
                this.cells = new byte[steps][getSize().width / cellSize];
            }

            int nLines = cells.length;
            int nColumns = cells[0].length;

            for (int j = 0; j < nColumns; j++) {
                cells[0][j] = 0;
            }

            if (randomize) {
                for (int i = 0; i < nColumns; i++) {
                    if (random.nextBoolean()) {
                        cells[0][i] = 1;
                    } else {
                        cells[0][i] = 0;
                    }
                }
            } else {
                if (nColumns % 2 == 0) {
                    cells[0][nColumns / 2] = 1;
                }
                else {
                    cells[0][(nColumns + 1) / 2] = 1;
                }

            }

            for (int i = 1; i < nLines; i++) {
                int leftNeighborIndex, rightNeighborIndex;

                for (int j = 0; j < nColumns; j++) {
                    if (j == 0) {
                        leftNeighborIndex = nColumns - 1;
                        rightNeighborIndex = 1;
                    }
                    else if (j == nColumns - 1) {
                        leftNeighborIndex = nColumns - 2;
                        rightNeighborIndex = 0;
                    }
                    else {
                        leftNeighborIndex = j - 1;
                        rightNeighborIndex = j + 1;
                    }

                    if (cells[i - 1][leftNeighborIndex] == 1 && cells[i - 1][j] == 1 && cells[i - 1][rightNeighborIndex] == 1) {
                        cells[i][j] = rule[0];
                    }
                    else if (cells[i - 1][leftNeighborIndex] == 1 && cells[i - 1][j] == 1 && cells[i - 1][rightNeighborIndex] == 0) {
                        cells[i][j] = rule[1];
                    }
                    else if (cells[i - 1][leftNeighborIndex] == 1 && cells[i - 1][j] == 0 && cells[i - 1][rightNeighborIndex] == 1) {
                        cells[i][j] = rule[2];
                    }
                    else if (cells[i - 1][leftNeighborIndex] == 1 && cells[i - 1][j] == 0 && cells[i - 1][rightNeighborIndex] == 0) {
                        cells[i][j] = rule[3];
                    }
                    else if (cells[i - 1][leftNeighborIndex] == 0 && cells[i - 1][j] == 1 && cells[i - 1][rightNeighborIndex] == 1) {
                        cells[i][j] = rule[4];
                    }
                    else if (cells[i - 1][leftNeighborIndex] == 0 && cells[i - 1][j] == 1 && cells[i - 1][rightNeighborIndex] == 0) {
                        cells[i][j] = rule[5];
                    }
                    else if (cells[i - 1][leftNeighborIndex] == 0 && cells[i - 1][j] == 0 && cells[i - 1][rightNeighborIndex] == 1) {
                        cells[i][j] = rule[6];
                    }
                    else if (cells[i - 1][leftNeighborIndex] == 0 && cells[i - 1][j] == 0 && cells[i - 1][rightNeighborIndex] == 0) {
                        cells[i][j] = rule[7];
                    }
                }
            }
        }

        public void drawBackground() {

            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();

            graphics2D.setPaint(Color.WHITE);

            graphics2D.fillRect(0, 0, width, height);

            graphics2D.setColor(new Color(240,240,240));

            if (cellSize > 2) {
                int rows = height / cellSize;

                int rowHt = height / (rows);
                for (int i = 0; i < rows; i++) {
                    graphics2D.drawLine(0, i * rowHt, width, i * rowHt);
                }

                int cols = width / cellSize;

                int rowWid = width / cols;
                for (int i = 0; i < cols; i++) {
                    graphics2D.drawLine(i * rowWid, 0, i * rowWid, height);
                }
            }
        }

        public void drawCellularAutomaton() {
            for (int i = 0; i < cells.length; i++) {
                for (int j = 0; j < cells[i].length; j++) {
                    drawCell(i, j, cells[i][j], graphics2D);
                }
            }
        }

        private void drawCell(int i, int j, byte binary, Graphics2D g) {
            g.setColor(Color.black);
            if (binary == 1) {
                g.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
            }
        }

        private byte[] decimal2rule(int decimal) {
            byte[] temp = new byte[8];
            int i = 0;
            while (decimal > 0) {
                int divisor = (int) Math.floor(decimal / 2);
                int remainder = decimal % 2;
                temp[i] = (byte) remainder;
                decimal = divisor;
                i++;
            }

            byte[] binary = new byte[8];
            for (int j = 0; j < 8; j++) {
                binary[j] = temp[7-j];
            }
            return binary;
        }

    }

    private static void centreWindow(Window frame) {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Wolfram's Elementary Cellular Automaton Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JComponent newContentPane = new WolframCAViewer();
        newContentPane.setOpaque(true);
        frame.setContentPane(newContentPane);
        frame.pack();

        frame.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        centreWindow(frame);

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createAndShowGUI();
            }
        });
    }

}


