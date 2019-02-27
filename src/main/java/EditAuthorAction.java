import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class EditAuthorAction extends AnAction implements AnAction.TransparentUpdate {
    private static VirtualFile getGitDirectory(@NotNull Project project) {
        VirtualFile vfs = project.getProjectFile();
        VirtualFile git = null;
        
        while (git == null && vfs != null) {
            git = vfs.findChild(".git");
            vfs = vfs.getParent();
        }
        
        return git;
    }
    
    @Override
    public void update(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        assert project != null;

        VirtualFile vfs = getGitDirectory(project);
        event.getPresentation().setEnabled(vfs != null);
    }
    
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        assert project != null;

        VirtualFile vfs = getGitDirectory(project);        
        if (vfs == null) {
            Messages.showErrorDialog("The current project is not part of a Git repository.", "Invalid Project / No Repository");
            return;
        }
        
        final VirtualFile configFileVfs = vfs.findChild("config");
        assert configFileVfs != null;
        
        try {
            byte[] contents = configFileVfs.contentsToByteArray();
            String data = new String(contents, StandardCharsets.UTF_8);

            ArrayList<String> lines =
                    new ArrayList<>(
                            Arrays.asList(
                                    data.replaceAll("\r\n", "\n")
                                            .replaceAll("\r", "\n")
                                            .split("\n")));
            
            int userLine = -1;
            int nameLine = -1;
            int emailLine = -1;
            
            String NAME_REGEX = "^\\s+name\\s*=\\s*";
            String EMAIL_REGEX = "^\\s+email\\s*=\\s*";
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith("[user]")) {
                    userLine = i;
                } else if (userLine >= 0) {
                    if (lines.get(i).startsWith("[")) {
                        break;
                    } else if (lines.get(i).matches(NAME_REGEX + ".*")) {
                        nameLine = i;
                    } else if (lines.get(i).matches(EMAIL_REGEX + ".*")) {
                        emailLine = i;
                    }
                }
            }

            String name = nameLine >= 0 ? lines.get(nameLine).replaceFirst(NAME_REGEX, "") : null;
            String email = emailLine >= 0 ? lines.get(emailLine).replaceFirst(EMAIL_REGEX, "") : null;

            GetAuthorNameAndEmailDialogWrapper wrapper = new GetAuthorNameAndEmailDialogWrapper(project, name, email);
            if (wrapper.showAndGet()) {
                name = wrapper.get_name();
                email = wrapper.get_email();
                
                if (userLine < 0) {
                    userLine = lines.size();
                    lines.add("[user]");
                }

                if (emailLine >= 0) {
                    if (nameLine > emailLine) {
                        nameLine--;
                    }
                    lines.remove(emailLine);
                }
                if (!(email == null || email.equals(""))) {
                    lines.add(userLine + 1, "\temail = " + email);
                    if (nameLine >= 0) {
                        nameLine++;
                    }
                }
                
                if (nameLine >= 0) {
                    lines.remove(nameLine);
                }
                if (!(name == null || name.equals(""))) {
                    lines.add(userLine + 1, "\tname = " + name);
                }

                Application application = ApplicationManager.getApplication();
                application.runWriteAction(() -> {
                    try {
                        configFileVfs.setBinaryContent(String.join("\n", lines).getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        Messages.showErrorDialog("The .git/config file could not be written to.", "IO Error");
                    }
                });
                Messages.showMessageDialog(project, "Git configuration updated.", "Success", Messages.getInformationIcon());
            }
        } catch (IOException e) {
            Messages.showErrorDialog("The .git/config file could not be read from.", "IO Error");
        }
    }
}
