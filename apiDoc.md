# IoT 平台 API 文档

**基础URL**: `http://localhost:6000/api`

## 用户认证模块

### 1. 用户注册
```http
POST /auth/register
Content-Type: application/json

{
    "username": "test",
    "password": "123456",
    "email": "test@example.com",
    "avatar": "data:image/png;base64,..."  # 可选
}
```

**响应示例**：
```json
{
    "message": "注册成功"
}
```
状态码：
- 201 创建成功
- 400 参数缺失/用户名或邮箱已存在

### 2. 用户登录
```http
POST /auth/login
Content-Type: application/json

{
    "username": "test",
    "password": "123456"
}
```

**响应示例**：
```json
{
    "message": "登录成功",
    "role": "USER"
}
```

### 3. 密码重置
```http
POST /auth/reset-password
Content-Type: application/json

# 第一阶段：获取验证码
{
    "email": "test@example.com"
}

# 第二阶段：验证并重置
{
    "email": "test@example.com",
    "verification_code": "123456",
    "new_password": "newpassword"
}
```

---

## 设备管理模块

### 1. 创建设备
```http
POST /devices
Content-Type: application/json

{
    "device_id": "DEV001",
    "device_name": "温度传感器",
    "image": "data:image/png;base64,..."  # 可选
}
```

**响应字段**：
```json
{
    "id": "DEV001",
    "name": "温度传感器",
    "image": "/static/device_images/xxx.png"
}
```

### 2. 获取设备列表
```http
GET /devices

响应示例：
[
    {
        "id": "DEV001",
        "name": "温度传感器",
        "type": "sensor",
        "status": "active",
        "image": "/static/device_images/xxx.jpg"
    }
]
```

---

## 传感器数据模块

### 1. 提交传感器数据
```http
POST /sensor/data
Content-Type: application/json

{
    "sensor_type": "temperature",
    "sensor_value": 25.5,
    "alarm_level": "Normal"  # 可选
}
```

**错误示例**：
```json
{
    "error": "数值格式错误"
}
```

### 2. 查询历史数据
```http
GET /sensor/history?type=temperature&hours=24
```

**响应结构**：
```json
[
    {
        "id": 1,
        "type": "temperature",
        "value": 25.5,
        "time": "2023-08-20T12:00:00Z"
    }
]
```

---

## 图像识别模块

### 1. 物体检测
```http
POST /detect
Content-Type: multipart/form-data

# 文件上传字段名必须为'image'
curl -F "image=@test.jpg" http://localhost:6000/api/detect
```

**响应示例**：
```json
{
    "detection_id": 42,
    "result_image": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
    "object_count": 3,
    "detection_data": {
        "boxes": [[320,120,480,360], ...],
        "confidences": [0.95, 0.87, 0.78],
        "class_ids": [0, 2, 16],
        "class_names": ["person", "car", "dog"]
    }
}
```

### 2. 查询检测历史详情
```http
GET /detect/history/42

响应示例：
{
    "detection_id": 42,
    "timestamp": "2023-08-20T12:00:00Z",
    "result_image": "data:image/jpeg;base64,...",
    "object_count": 3,
    "detection_data": {
        "class_names": ["person", "car", "dog"],
        "confidences": [0.95, 0.87, 0.78],
        "boxes": [[320,120,480,360], ...]
    }
}
```

---

## 用户管理模块

### 更新用户头像
```http
POST /user/avatar
Content-Type: application/json

{
    "user_id": 1,
    "avatar": "data:image/png;base64,..."
}
```

---

## GPS 数据模块

### 1. 提交GPS数据
```http
POST /gps
Content-Type: application/json

{
    "device_id": "DEV001",
    "latitude": 31.2304,
    "longitude": 121.4737
}
```

### 2. 查询轨迹
```http
GET /gps/history?device_id=DEV001&hours=24
```

**响应示例**：
```json
[
    {
        "device_id": "DEV001",
        "latitude": 31.2304,
        "longitude": 121.4737,
        "accuracy": 1.5,
        "time": "2023-08-20T12:00:00Z"
    }
]
```

---

## 错误处理
通用错误格式：
```json
{
    "error": "错误描述"
}
常见状态码：
- 400 请求参数错误
- 404 资源未找到
- 413 文件大小超过限制(10MB)
- 500 服务器内部错误
```