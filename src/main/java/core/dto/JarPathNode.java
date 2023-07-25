package core.dto;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class JarPathNode {

    private Map<String, JarPathNode> subNodes = new TreeMap<>();

    private String pathTojar;

    public String getPathTojar() {
        return pathTojar;
    }

    public void setPathTojar(String pathTojar) {
        this.pathTojar = pathTojar;
    }

    public JarPathNode getNode(String name){
        return subNodes.get(name);
    }

    public void addNode(String name, JarPathNode jarPathNode){
        subNodes.put(name, jarPathNode);
    }

    public Set<Map.Entry<String, JarPathNode>> getNodes (){
        return subNodes.entrySet();
    }

}
