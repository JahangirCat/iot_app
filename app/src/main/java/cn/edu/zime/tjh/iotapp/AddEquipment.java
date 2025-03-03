package cn.edu.zime.tjh.iotapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddEquipment extends AppCompatActivity {

    // 请求码定义
    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final int REQUEST_STORAGE_PERMISSION = 1002;

    // 视图组件
    private List<Map<String, Object>> deviceList;
    private SimpleAdapter adapter;
    private ImageView currentDialogImageView;

    private Bitmap selectedImageBitmap;
    private Bitmap qrCodeBitmap;
    private boolean needCheckAfterSettings = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addequipments);

        // 初始化列表视图
        ListView listView = findViewById(R.id.listView);
        deviceList = new ArrayList<>();

        // 配置适配器
        adapter = new SimpleAdapter(this, deviceList, R.layout.item_list_addeq,
                new String[]{"addEq_images", "code", "name", "day", "people", "QRCode_images"},
                new int[]{R.id.addEq_images, R.id.code, R.id.name, R.id.day, R.id.people, R.id.QRCode_images}
        );

        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view instanceof ImageView) {
                    ImageView iv = (ImageView) view;
                    if (data instanceof Bitmap) {
                        iv.setImageBitmap((Bitmap) data);
                    } else if (data instanceof Integer) {
                        iv.setImageResource((Integer) data);
                    }
                    return true;
                }
                return false;
            }
        });

        listView.setAdapter(adapter);
        loadSampleData();

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDeleteConfirmationDialog(position);
                return true;
            }
        });
    }


    private void showDeleteConfirmationDialog(final int position) {
        new AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("确定要删除此设备吗？")
                .setPositiveButton("确定", (d, w) -> deleteDevice(position))
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteDevice(int position) {
        if (position < 0 || position >= deviceList.size()) return;
        deviceList.remove(position);
        adapter.notifyDataSetChanged();
        showToast("设备删除成功");
    }
    // 加载示例数据
    private void loadSampleData() {
        int[] sampleImages = {R.drawable.equipment1, R.drawable.equipment1};
        String[] codes = {"001", "002"};
        String[] names = {"设备A", "设备B"};
        String[] dates = {"2023-01-01", "2023-02-01"};
        String[] staffs = {"张三", "李四"};

        for (int i = 0; i < codes.length; i++) {
            addDevice(
                    codes[i],
                    names[i],
                    dates[i],
                    staffs[i],
                    sampleImages[i],
                    generateQRCode(codes[i])
            );
        }
    }

    // 显示添加设备对话框
    public void showAddDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_equipment, null);

        // 初始化对话框组件
        currentDialogImageView = dialogView.findViewById(R.id.image_view);

        ImageView imgPreview = dialogView.findViewById(R.id.image_view);
        ImageView qrPreview = dialogView.findViewById(R.id.image_code);
        Button btnSelectImg = dialogView.findViewById(R.id.btnSelectImage);
        Button btnGenQR = dialogView.findViewById(R.id.btnGenerateQRCode);
        EditText etCode = dialogView.findViewById(R.id.etCode);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etDate = dialogView.findViewById(R.id.etDay);
        EditText etStaff = dialogView.findViewById(R.id.etPeople);

        // 重置对话框状态
        resetDialog(imgPreview, qrPreview);

        // 图片选择点击事件
        btnSelectImg.setOnClickListener(v -> handleImagePermission()
        );

        // 生成二维码点击事件
        btnGenQR.setOnClickListener(v -> showLinkInputDialog(etCode, qrPreview));

        builder.setView(dialogView)
                .setTitle("添加设备")
                .setPositiveButton("确定", (d, w) -> saveDevice(
                        etCode, etName, etDate, etStaff))
                .setNegativeButton("取消", null)
                .show();
    }

    // 处理图片权限
    private void handleImagePermission() {
        if (checkPermissions()) {
            openImagePicker();
        } else {
            requestPermissions();
        }
    }

    // 检查权限状态
    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return checkSinglePermission(Manifest.permission.READ_MEDIA_IMAGES);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return checkSinglePermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            return checkMultiplePermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            );
        }
    }

    private boolean checkSinglePermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkMultiplePermissions(String... permissions) {
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    // 请求权限
    private void requestPermissions() {
        ArrayList<String> permissionsToRequest = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toArray(new String[0]),
                REQUEST_STORAGE_PERMISSION
        );
    }

    // 处理权限结果
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            handlePermissionResult(grantResults, permissions);
        }
    }

    private void handlePermissionResult(int[] grantResults, String[] permissions) {
        if (isAllPermissionsGranted(grantResults)) {
            openImagePicker();
        } else {
            handleDeniedPermissions(permissions);
        }
    }

    private boolean isAllPermissionsGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void handleDeniedPermissions(String[] permissions) {
        boolean hasPermanentDenial = true;
        for (String perm : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
                hasPermanentDenial = false;
                break;
            }
        }

        if (hasPermanentDenial) {
            showSettingsDialog();
            needCheckAfterSettings = true;
        } else {
            showToast("请授予权限以继续操作");
        }
    }

    // 生命周期处理1
    @Override
    protected void onResume() {
        super.onResume();
        if (needCheckAfterSettings && checkPermissions()) {
            openImagePicker();
            needCheckAfterSettings = false;
        }
    }

    // 打开图片选择器
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK
                && resultCode == RESULT_OK
                && data != null) {
            handleSelectedImage(data.getData());
        }
    }

    // 处理选中图片
    private void handleSelectedImage(Uri imageUri) {
        new Thread(() -> {
            try {
                selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), imageUri);
                runOnUiThread(() ->{
                    if (currentDialogImageView != null) {
                        currentDialogImageView.setImageBitmap(selectedImageBitmap);
                        showToast("图片已选择");
                    }
                });
            } catch (IOException e) {
                runOnUiThread(() ->
                        showToast("图片加载失败"));
            }
        }).start();
    }

    // 新增：链接输入对话框
    private void showLinkInputDialog(EditText etCode, ImageView qrPreview) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_qr_link, null);

        EditText etLink = dialogView.findViewById(R.id.etLink);

        builder.setView(dialogView)
                .setTitle("输入产品链接")
                .setPositiveButton("生成", (dialog, which) -> {
                    String link = etLink.getText().toString().trim();
                    String code = etCode.getText().toString().trim();
                    generateQRCode(!link.isEmpty() ? link : code, qrPreview);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // 生成二维码
    @SuppressLint("MissingInflatedId")
    private void generateQRCode(String content, ImageView qrPreview) {
        new Thread(() -> {
            Bitmap qr = createQRBitmap(content);
            runOnUiThread(() -> {
                qrCodeBitmap = qr;
                qrPreview.setImageBitmap(qr);
            });
        }).start();
    }

    private Bitmap createQRBitmap(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512);
            Bitmap bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565);

            for (int x = 0; x < 512; x++) {
                for (int y = 0; y < 512; y++) {
                    bitmap.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            showToast("二维码生成失败");
            return null;
        }
    }

    // 保存设备
    private void saveDevice(EditText... fields) {
        if (validateFields(fields)) {
            addDevice(
                    fields[0].getText().toString(),
                    fields[1].getText().toString(),
                    fields[2].getText().toString(),
                    fields[3].getText().toString(),
                    (selectedImageBitmap != null) ? selectedImageBitmap : R.drawable.eqimages,
                    qrCodeBitmap
            );
            showToast("设备添加成功");
        }
    }

    private boolean validateFields(EditText... fields) {
        for (EditText field : fields) {
            if (field.getText().toString().trim().isEmpty()) {
                showToast("请填写完整信息");
                return false;
            }
        }
        return true;
    }

    // 添加设备到列表
    private void addDevice(String code, String name, String date,
                           String staff, Object image, Bitmap qrCode) {
        Map<String, Object> device = new HashMap<>();
        device.put("code", code);
        device.put("name", name);
        device.put("day", date);
        device.put("people", staff);
        device.put("addEq_images", image);
        device.put("QRCode_images", (qrCode != null) ? qrCode : generateQRCode(code));
        deviceList.add(device);
        adapter.notifyDataSetChanged();
    }

    // 辅助方法
    private void resetDialog(ImageView... imageViews) {
        selectedImageBitmap = null;
        qrCodeBitmap = null;
        for (ImageView iv : imageViews) {
            iv.setImageResource(R.drawable.eqimages);
        }
    }

    private void showSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("需要权限")
                .setMessage("请在系统设置中启用权限")
                .setPositiveButton("设置", (d, w) -> openAppSettings())
                .setNegativeButton("取消", null)
                .show();
    }

    private void openAppSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName())));
        } catch (Exception e) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // 示例二维码生成
    private Bitmap generateQRCode(String code) {
        return createQRBitmap(code);
    }
    public void EqBack(View v){
        Intent intent = new Intent(AddEquipment.this, MainActivity.class);
        startActivity(intent);

    }
}