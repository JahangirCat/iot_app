package cn.edu.zime.tjh.iotapp.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cn.edu.zime.tjh.iotapp.R;
import cn.edu.zime.tjh.iotapp.model.DetectionResult;

/**
 * YOLO检测结果适配器
 */
public class DetectionResultAdapter extends RecyclerView.Adapter<DetectionResultAdapter.ResultViewHolder> {
    
    private List<DetectionResult> detectionResults = new ArrayList<>();
    private final int[] COLORS = {
            Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN,
            Color.MAGENTA, Color.DKGRAY, Color.GRAY, Color.LTGRAY, Color.BLACK
    };
    private final Random random = new Random();
    
    public DetectionResultAdapter() {
    }
    
    public void setResults(List<DetectionResult> results) {
        this.detectionResults = results;
        notifyDataSetChanged();
    }
    
    public List<DetectionResult> getResults() {
        return detectionResults;
    }
    
    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_detection_result, parent, false);
        return new ResultViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        DetectionResult result = detectionResults.get(position);
        holder.bind(result);
    }
    
    @Override
    public int getItemCount() {
        return detectionResults.size();
    }
    
    class ResultViewHolder extends RecyclerView.ViewHolder {
        private final View viewObjectColor;
        private final TextView tvObjectName;
        private final TextView tvConfidence;
        private final TextView tvObjectCount;
        
        public ResultViewHolder(@NonNull View itemView) {
            super(itemView);
            viewObjectColor = itemView.findViewById(R.id.viewObjectColor);
            tvObjectName = itemView.findViewById(R.id.tvObjectName);
            tvConfidence = itemView.findViewById(R.id.tvConfidence);
            tvObjectCount = itemView.findViewById(R.id.tvObjectCount);
        }
        
        public void bind(DetectionResult result) {
            // 设置类别颜色(为每个类别分配固定颜色)
            int colorIndex = Math.abs(result.getLabel().hashCode()) % COLORS.length;
            viewObjectColor.setBackgroundColor(COLORS[colorIndex]);
            
            // 设置物体名称
            tvObjectName.setText(result.getLabel());
            
            // 设置置信度
            String confidenceText = String.format("置信度: %.1f%%", result.getConfidence() * 100);
            tvConfidence.setText(confidenceText);
            
            // 设置数量
            tvObjectCount.setText(String.format("%d个", result.getCount()));
        }
    }
} 