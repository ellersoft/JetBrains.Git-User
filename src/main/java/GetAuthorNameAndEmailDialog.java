import javax.swing.*;

public class GetAuthorNameAndEmailDialog {
    private JTextField nameTextField;
    private JTextField emailTextField;
    private JPanel rootPanel;

    public String get_name() {
        return nameTextField.getText();
    }
    public String get_email() {
        return emailTextField.getText();
    }
    
    public JComponent getRoot() { return rootPanel; }
    
    public GetAuthorNameAndEmailDialog(String name, String email) {
        nameTextField.setText(name);
        emailTextField.setText(email);
    }
}
