package main.java.bean;

import java.util.ArrayList;

public class Node {

    private Node parentNode;
    private ArrayList<Node> childNodes;
    private Integer attributePointer;

    public Node getParentNode() {
        return parentNode;
    }

    public void setParentNode(Node parentNode) {
        this.parentNode = parentNode;
    }

    public ArrayList<Node> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(ArrayList<Node> childNodes) {
        this.childNodes = childNodes;
    }

    public Integer getAttributePointer() {
        return attributePointer;
    }

    public void setAttributePointer(Integer attributePointer) {
        this.attributePointer = attributePointer;
    }
}
