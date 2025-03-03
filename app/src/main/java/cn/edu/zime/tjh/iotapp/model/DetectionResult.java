package cn.edu.zime.tjh.iotapp.model;

/**
 * YOLO检测结果数据模型
 */
public class DetectionResult {
    private String label;       // 物体标签/类别
    private float confidence;   // 置信度(0-1)
    private int count;          // 数量
    private float[] box;        // 边界框坐标 [x, y, width, height]
    
    public DetectionResult(String label, float confidence, int count) {
        this.label = label;
        this.confidence = confidence;
        this.count = count;
    }
    
    public DetectionResult(String label, float confidence, int count, float[] box) {
        this.label = label;
        this.confidence = confidence;
        this.count = count;
        this.box = box;
    }
    
    // Getters and Setters
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public float getConfidence() {
        return confidence;
    }
    
    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    public float[] getBox() {
        return box;
    }
    
    public void setBox(float[] box) {
        this.box = box;
    }
    
    @Override
    public String toString() {
        return "DetectionResult{" +
                "label='" + label + '\'' +
                ", confidence=" + confidence +
                ", count=" + count +
                '}';
    }
} 