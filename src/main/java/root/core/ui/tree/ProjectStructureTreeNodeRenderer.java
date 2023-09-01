package root.core.ui.tree;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class ProjectStructureTreeNodeRenderer extends DefaultTreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        ProjectStructureNode node = (ProjectStructureNode) value;
        String iconName;
        if (node.getProjectStructureNodeType().equals(ProjectStructureNodeType.EMPTY)){
            iconName = "/icons/no project.png";
        }
        else if (node.isRoot()){
            iconName = "/icons/root directory.png";
        }
        else if (node.getProjectStructureNodeType().equals(ProjectStructureNodeType.DIRECTORY)){
            iconName = "/icons/directory.png";
        }
        else if (node.getProjectStructureNodeType().equals(ProjectStructureNodeType.FILE)){
            iconName = "/icons/java file.png";
        }
        else if (node.getProjectStructureNodeType().equals(ProjectStructureNodeType.JDK_ROOT)){
            iconName = "/icons/jdk root.png";
        }
        else if (node.getProjectStructureNodeType().equals(ProjectStructureNodeType.MAVEN_ROOT)){
            iconName = "/icons/maven root.png";
        }
        else if (node.getProjectStructureNodeType().equals(ProjectStructureNodeType.CLASS)){
            iconName = "/icons/class file.png";
        }
        else if (node.getProjectStructureNodeType().equals(ProjectStructureNodeType.INTERFACE)){
            iconName = "/icons/interface file.png";
        }
        else if (node.getProjectStructureNodeType().equals(ProjectStructureNodeType.ENUM)){
            iconName = "/icons/enum file.png";
        }
        else if (node.getProjectStructureNodeType().equals(ProjectStructureNodeType.ANNOTATION_TYPE)){
            iconName = "/icons/annotation file.png";
        }
        else if (node.getProjectStructureNodeType().equals(ProjectStructureNodeType.PACKAGE_DECLARATION)){
            iconName = "/icons/package file.png";
        }
        else if (node.getProjectStructureNodeType().equals(ProjectStructureNodeType.MODULE)){
            iconName = "/icons/module file.png";
        }
        else{
            iconName = "/icons/unknown.png";
        }
        setIcon(new ImageIcon(getClass().getResource(iconName)));
        return this;
    }
}
