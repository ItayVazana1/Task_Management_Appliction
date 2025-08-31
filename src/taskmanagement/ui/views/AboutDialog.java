package taskmanagement.ui.views;

import taskmanagement.ui.UITheme;

import javax.swing.*;
import java.awt.*;

/** Simple About dialog (pure Java, no .form). */
public final class AboutDialog extends JDialog {
    public AboutDialog(Window owner) {
        super(owner, "About", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        p.setBackground(UITheme.BASE_800);

        JLabel title = UITheme.makeTitleLabel("Tasks Management Application");
        JLabel info = new JLabel("<html>MVVM + DAO (Derby) + patterns.<br/>© 2025</html>");
        info.setForeground(UITheme.TEXT_PRIMARY);

        JButton ok = new JButton("OK");
        UITheme.styleFilterButton(ok);
        ok.addActionListener(e -> dispose());

        p.add(title, BorderLayout.NORTH);
        p.add(info, BorderLayout.CENTER);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(ok);
        p.add(south, BorderLayout.SOUTH);

        setContentPane(p);
        pack();
        setLocationRelativeTo(owner);
    }
}
