// NO IMPORTS AT THE TOP

public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            AppWindow frame = new AppWindow();
            frame.setVisible(true);
        });
    }
}

class AppWindow extends javax.swing.JFrame {
    private java.awt.CardLayout cardLayout = new java.awt.CardLayout();
    private javax.swing.JPanel mainContainer = new javax.swing.JPanel(cardLayout);
    private javax.swing.JTextField weightField, heightField, nameField, ageField;
    private javax.swing.JLabel resultBmiLabel, hiLabel, statusSummaryLabel;
    private javax.swing.JComboBox<String> unitSelector;
    private String selectedGender = "Male";

    public AppWindow() {
        setUndecorated(true);
        setSize(400, 800);
        setLocationRelativeTo(null);
        setShape(new java.awt.geom.RoundRectangle2D.Double(0, 0, 400, 800, 50, 50));

        mainContainer.add(createWelcomePage(), "1");
        mainContainer.add(createInputPage(), "2");
        mainContainer.add(createResultPage(), "3");
        add(mainContainer);
    }

    // --- PAGE 1: WELCOME (SAME AS BEFORE) ---
    private javax.swing.JPanel createWelcomePage() {
        GradientPanel p = new GradientPanel();
        p.setLayout(null);
        javax.swing.JLabel title = new javax.swing.JLabel("Welcome!", 0);
        title.setFont(new java.awt.Font("SansSerif", 1, 28));
        title.setBounds(0, 300, 400, 40);

        WhiteCard card = new WhiteCard();
        card.setBounds(20, 400, 360, 250); card.setLayout(null);
        nameField = new javax.swing.JTextField("Elmira");
        nameField.setBounds(30, 80, 300, 45);
        nameField.setBorder(javax.swing.BorderFactory.createTitledBorder("What's your name?"));

        javax.swing.JButton next = new javax.swing.JButton("Next →");
        next.setBounds(230, 160, 100, 40);
        next.addActionListener(e -> {
            hiLabel.setText("Hi " + nameField.getText() + "!");
            cardLayout.show(mainContainer, "2");
        });

        card.add(nameField); card.add(next);
        p.add(title); p.add(card); p.add(createExitBtn());
        return p;
    }

    // --- PAGE 2: INPUT (MATCHING THE SECOND IMAGE) ---
    private javax.swing.JPanel createInputPage() {
        GradientPanel p = new GradientPanel();
        p.setLayout(null);
        hiLabel = new javax.swing.JLabel("Hi Elmira!");
        hiLabel.setFont(new java.awt.Font("SansSerif", 1, 24));
        hiLabel.setBounds(30, 50, 300, 40);

        WhiteCard card = new WhiteCard();
        card.setBounds(20, 140, 360, 500); card.setLayout(null);

        // Gender Buttons
        javax.swing.JButton maleBtn = new javax.swing.JButton("Male");
        javax.swing.JButton femaleBtn = new javax.swing.JButton("Female");
        maleBtn.setBounds(30, 30, 145, 40);
        femaleBtn.setBounds(185, 30, 145, 40);
        maleBtn.setBackground(new java.awt.Color(224, 247, 250));
        femaleBtn.setBackground(new java.awt.Color(38, 198, 218)); // Default selected

        ageField = new javax.swing.JTextField("22");
        ageField.setBounds(30, 90, 300, 45);
        ageField.setBorder(javax.swing.BorderFactory.createTitledBorder("Age"));

        String[] units = {"Kg / Cm (Metric)", "Lbs / In (English)"};
        unitSelector = new javax.swing.JComboBox<>(units);
        unitSelector.setBounds(30, 150, 300, 45);

        weightField = new javax.swing.JTextField();
        weightField.setBounds(30, 210, 300, 45);
        weightField.setBorder(javax.swing.BorderFactory.createTitledBorder("Weight"));

        heightField = new javax.swing.JTextField();
        heightField.setBounds(30, 270, 300, 45);
        heightField.setBorder(javax.swing.BorderFactory.createTitledBorder("Height"));

        javax.swing.JButton calc = new javax.swing.JButton("Calculate your BMI");
        calc.setBounds(30, 380, 300, 55);
        calc.setBackground(new java.awt.Color(38, 198, 218));
        calc.setForeground(java.awt.Color.WHITE);
        calc.addActionListener(e -> runLogic());

        card.add(maleBtn); card.add(femaleBtn); card.add(ageField);
        card.add(unitSelector); card.add(weightField); card.add(heightField); card.add(calc);
        p.add(hiLabel); p.add(card); p.add(createExitBtn());
        return p;
    }

    // --- PAGE 3: RESULT (DETAILED CHART FROM IMAGE) ---
    private javax.swing.JPanel createResultPage() {
        GradientPanel p = new GradientPanel();
        p.setLayout(null);

        javax.swing.JLabel resTitle = new javax.swing.JLabel("Your BMI result");
        resTitle.setFont(new java.awt.Font("SansSerif", 1, 24));
        resTitle.setBounds(30, 40, 300, 40);

        WhiteCard card = new WhiteCard();
        card.setBounds(20, 100, 360, 650); card.setLayout(null);

        // Circular Score Display
        resultBmiLabel = new javax.swing.JLabel("19.9", 0);
        resultBmiLabel.setFont(new java.awt.Font("SansSerif", 1, 40));
        resultBmiLabel.setBounds(105, 30, 150, 150);
        resultBmiLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 230, 118), 8));

        statusSummaryLabel = new javax.swing.JLabel("You have Normal body weight!", 0);
        statusSummaryLabel.setBounds(0, 200, 360, 30);
        statusSummaryLabel.setFont(new java.awt.Font("SansSerif", 1, 14));

        // BMI Categories Chart (as seen in the 3rd phone screen)
        String[][] categories = {
                {"● Very severely underweight", "< 16"},
                {"● Severely underweight", "16.0 - 16.9"},
                {"● Underweight", "17.0 - 18.4"},
                {"● Normal", "18.5 - 24.9"},
                {"● Overweight", "25.0 - 29.9"},
                {"● Obese Class I", "30.0 - 34.9"},
                {"● Obese Class II", "35.0 - 39.9"},
                {"● Obese Class III", "> 39.9"}
        };

        for (int i = 0; i < categories.length; i++) {
            javax.swing.JLabel name = new javax.swing.JLabel(categories[i][0]);
            javax.swing.JLabel val = new javax.swing.JLabel(categories[i][1], 4);
            name.setBounds(30, 250 + (i * 30), 200, 25);
            val.setBounds(230, 250 + (i * 30), 100, 25);
            card.add(name); card.add(val);
        }

        javax.swing.JButton back = new javax.swing.JButton("← Back");
        back.setBounds(30, 580, 100, 40);
        back.addActionListener(e -> cardLayout.show(mainContainer, "2"));

        card.add(resultBmiLabel); card.add(statusSummaryLabel); card.add(back);
        p.add(resTitle); p.add(card); p.add(createExitBtn());
        return p;
    }

    private void runLogic() {
        try {
            double w = Double.parseDouble(weightField.getText());
            double h = Double.parseDouble(heightField.getText());
            double bmi;

            if (unitSelector.getSelectedIndex() == 0) {
                double hM = h / 100.0;
                bmi = w / (hM * hM);
            } else {
                bmi = (w * 703) / (h * h);
            }

            resultBmiLabel.setText(String.format("%.1f", bmi));

            if (bmi < 18.5) statusSummaryLabel.setText("You are Underweight");
            else if (bmi < 25) statusSummaryLabel.setText("You have Normal body weight!");
            else if (bmi < 30) statusSummaryLabel.setText("You are Overweight");
            else statusSummaryLabel.setText("Obese Category");

            cardLayout.show(mainContainer, "3");
        } catch (Exception ex) { javax.swing.JOptionPane.showMessageDialog(this, "Check numbers!"); }
    }

    private javax.swing.JButton createExitBtn() {
        javax.swing.JButton b = new javax.swing.JButton("X");
        b.setBounds(350, 10, 40, 40); b.setBorderPainted(false); b.setContentAreaFilled(false);
        b.addActionListener(e -> System.exit(0));
        return b;
    }
}

// GRAPHICS CLASSES
class GradientPanel extends javax.swing.JPanel {
    protected void paintComponent(java.awt.Graphics g) {
        java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
        g2d.setPaint(new java.awt.GradientPaint(0, 0, new java.awt.Color(0, 230, 118), 0, 800, new java.awt.Color(38, 198, 218)));
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}

class WhiteCard extends javax.swing.JPanel {
    protected void paintComponent(java.awt.Graphics g) {
        java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(java.awt.Color.WHITE);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
    }
    public WhiteCard() { setOpaque(false); }
}