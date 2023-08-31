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
            iconName = "/no project.png";
        }
        else if (node.isRoot()){
            iconName = "/root directory.png";
        }
        else if (node.getProjectStructureNodeType().equals(ProjectStructureNodeType.DIRECTORY)){
            iconName = "/directory.png";
        }
        else if (node.getProjectStructureNodeType().equals(ProjectStructureNodeType.FILE)){
            iconName = "/java file.png";
        }
        else if (node.getProjectStructureNodeType().equals(ProjectStructureNodeType.JDK_ROOT)){
            iconName = "/jdk root.png";
        }
        else if (node.getProjectStructureNodeType().equals(ProjectStructureNodeType.MAVEN_ROOT)){
            iconName = "/maven root.png";
        }
        else if (node.getProjectStructureNodeType().equals(ProjectStructureNodeType.CLASS)){
            iconName = "/class file.png";
        }
        else if (node.getProjectStructureNodeType().equals(ProjectStructureNodeType.INTERFACE)){
            iconName = "/interface file.png";
        }
        else if (node.getProjectStructureNodeType().equals(ProjectStructureNodeType.ENUM)){
            iconName = "/enum file.png";
        }
        else if (node.getProjectStructureNodeType().equals(ProjectStructureNodeType.ANNOTATION_TYPE)){
            iconName = "/annotation file.png";
        }
        else if (node.getProjectStructureNodeType().equals(ProjectStructureNodeType.PACKAGE_DECLARATION)){
            iconName = "/package file.png";
        }
        else if (node.getProjectStructureNodeType().equals(ProjectStructureNodeType.MODULE)){
            iconName = "/module file.png";
        }
        else{
            iconName = "/unknown.png";
        }
        setIcon(new ImageIcon(getClass().getResource(iconName)));
        return this;
    }
}
