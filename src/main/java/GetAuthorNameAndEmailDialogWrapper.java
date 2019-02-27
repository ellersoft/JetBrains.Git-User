import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class GetAuthorNameAndEmailDialogWrapper extends DialogWrapper {
    private GetAuthorNameAndEmailDialog _dialog;
    
    private String _name;
    private String _email;

    public String get_name() { return _dialog.get_name(); }
    public String get_email() { return _dialog.get_email(); }

    protected GetAuthorNameAndEmailDialogWrapper(@Nullable Project project, String name, String email) {
        super(project);
        
        _name = name;
        _email = email;
        
        init();
        setTitle("Git Author Name and Email");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        _dialog = new GetAuthorNameAndEmailDialog(_name, _email);
        return _dialog.getRoot();
    }
}
